package ch.phwidmer.einkaufsliste.UI;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;

import ch.phwidmer.einkaufsliste.helper.InputStringDialogFragment;
import ch.phwidmer.einkaufsliste.R;

public class DataSynchronizationActivity extends AppCompatActivity implements InputStringDialogFragment.InputStringResponder
{
    // Type: P2P_STAR or P2P_POINT_TO_POINT
    private static final Strategy m_Strategy = Strategy.P2P_POINT_TO_POINT;
    private static final String m_ServiceID = "ch.phwidmer.einkaufsliste.Service";

    private static final String m_TagEKList = "EK-LIST";
    private static final String m_TagEKRequestFile = "EK-REQUESTFILE";
    private static final String m_TagEKFileInfo = "EK-FILEID";

    private String m_strSaveFilePath;
    private String m_DeviceName;

    private ConnectionsClient m_Connection;

    private EndpointDiscoveryCallback m_EndpointDiscoveryCallback;
    private ConnectionLifecycleCallback m_ConnectionLifecycleCallback;

    private boolean m_Discovering = false;
    private boolean m_Advertising = false;

    private LinkedHashMap<String, String>   m_Devices;

    private Button      m_ButtonDiscover;
    private ListView    m_ListViewNearyDevices;
    private TextView    m_TextViewConnected;
    private ListView    m_ListViewAvailableFiles;

    private ArrayAdapter<CharSequence> m_AdapterListViewDevices;
    private ArrayAdapter<CharSequence> m_AdapterAvailableFiles;

    private String m_ConnectedEndpointId;

    private class PayloadListener extends PayloadCallback {

        private final SimpleArrayMap<Long, Payload> incomingFilePayloads = new SimpleArrayMap<>();
        private final SimpleArrayMap<Long, Payload> completedFilePayloads = new SimpleArrayMap<>();
        private final SimpleArrayMap<Long, String> filePayloadFilenames = new SimpleArrayMap<>();

        @Override
        public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload)
        {
            if(payload.getType() == Payload.Type.BYTES)
            {
                byte[] receivedBytes = payload.asBytes();
                String strResult = new String(receivedBytes);

                if (strResult.startsWith(m_TagEKList))
                {
                    // Remove tag-element and split remainder
                    String[] splitResult = strResult.replace(m_TagEKList + ";", "").split(";");
                    m_AdapterAvailableFiles.clear();
                    m_AdapterAvailableFiles.addAll(splitResult);
                }
                else if(strResult.startsWith(m_TagEKRequestFile))
                {
                    String strFilename = strResult.replace(m_TagEKRequestFile + ";", "");
                    File fileToSend = new File(m_strSaveFilePath, strFilename);

                    Payload filePayload;
                    try
                    {
                        filePayload = Payload.fromFile(fileToSend);
                    }
                    catch (FileNotFoundException e)
                    {
                        Toast.makeText(DataSynchronizationActivity.this, "ERROR: File not found \"" + strFilename + "\"", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String filenameMessage = m_TagEKFileInfo + ";" + filePayload.getId() + ";" + strFilename;
                    Payload filenameBytesPayload = Payload.fromBytes(filenameMessage.getBytes());
                    m_Connection.sendPayload(endpointId, filenameBytesPayload);

                    m_Connection.sendPayload(m_ConnectedEndpointId, filePayload);
                }
                else if(strResult.startsWith(m_TagEKFileInfo))
                {
                    String payloadFilenameMessage = new String(payload.asBytes());

                    String[] parts = payloadFilenameMessage.split(";");
                    long payloadId = Long.parseLong(parts[1]);
                    String filename = parts[2];
                    filePayloadFilenames.put(payloadId, filename);

                    processFilePayload(payloadId);
                }
                else
                {
                    Toast.makeText(DataSynchronizationActivity.this, "ERROR: Unknown request \"" + strResult + "\"", Toast.LENGTH_SHORT).show();
                }
            }
            else if(payload.getType() == Payload.Type.FILE)
            {
                incomingFilePayloads.put(payload.getId(), payload);
            }
        }

        private void processFilePayload(long payloadId)
        {
            // BYTES and FILE could be received in any order, so we call when either the BYTES or the FILE
            // payload is completely received. The file payload is considered complete only when both have
            // been received.
            Payload filePayload = completedFilePayloads.get(payloadId);
            String filename = filePayloadFilenames.get(payloadId);
            if (filePayload != null && filename != null)
            {
                completedFilePayloads.remove(payloadId);
                filePayloadFilenames.remove(payloadId);

                // Get the received file (which will be in the Downloads folder)
                Payload.File file = filePayload.asFile();
                if(file == null)
                {
                    return;
                }
                File payloadFile = file.asJavaFile();
                if(payloadFile == null)
                {
                    return;
                }

                if(!existsFileAlready(filename))
                {
                    File newFile = new File(m_strSaveFilePath, filename);
                    if(payloadFile.renameTo(newFile))
                    {
                        Toast.makeText(DataSynchronizationActivity.this, getResources().getString(R.string.file_received, newFile.getAbsolutePath()), Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(DataSynchronizationActivity.this, getResources().getString(R.string.file_copy_error, filename), Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    InputStringDialogFragment newFragment = InputStringDialogFragment.newInstance(getResources().getString(R.string.file_exists_already));
                    newFragment.setAdditionalInformation(payloadFile.getAbsolutePath());
                    newFragment.setDefaultValue(filename);
                    newFragment.setListExcludedInputs(getListOfExistingFiles());
                    newFragment.show(getSupportFragmentManager(), "onCopiedFileAlreayExists");
                }
            }
        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String endpointId, @NonNull PayloadTransferUpdate update)
        {
            if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS)
            {
                long payloadId = update.getPayloadId();
                if(!incomingFilePayloads.containsKey(payloadId))
                {
                    return;
                }
                Payload payload = incomingFilePayloads.remove(payloadId);
                if(payload == null)
                {
                    return;
                }
                completedFilePayloads.put(payloadId, payload);
                if (payload.getType() == Payload.Type.FILE) {
                    processFilePayload(payloadId);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_synchronization);

        Intent intent = getIntent();
        m_strSaveFilePath = intent.getStringExtra(MainActivity.EXTRA_SAVEDFILESPATH);

        m_ListViewNearyDevices = findViewById(R.id.listviewOtherDevices);
        m_TextViewConnected = findViewById(R.id.textViewConnected);
        m_ListViewAvailableFiles = findViewById(R.id.listviewAvailableFiles);
        m_ButtonDiscover = findViewById(R.id.buttonDiscover);

        m_DeviceName = Settings.Secure.getString(getContentResolver(), "bluetooth_name");

        m_Devices = new LinkedHashMap<>();

        m_ConnectedEndpointId = "";

        m_Connection = Nearby.getConnectionsClient(this);

        m_AdapterListViewDevices = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        m_ListViewNearyDevices.setAdapter(m_AdapterListViewDevices);

        m_AdapterAvailableFiles = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        m_ListViewAvailableFiles.setAdapter(m_AdapterAvailableFiles);
        m_ListViewAvailableFiles.setVisibility(View.INVISIBLE);
        m_TextViewConnected.setVisibility(View.INVISIBLE);

        m_ListViewAvailableFiles.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
        {
            // Request file

            String strFilename = m_ListViewAvailableFiles.getAdapter().getItem(position).toString();
            Toast.makeText(DataSynchronizationActivity.this, getResources().getString(R.string.requesting_file, strFilename), Toast.LENGTH_SHORT).show();

            String strRequest = m_TagEKRequestFile + ";" + strFilename;
            byte[] bytesPayload = strRequest.getBytes();

            Payload payload = Payload.fromBytes(bytesPayload);
            m_Connection.sendPayload(m_ConnectedEndpointId, payload);
        });

        m_EndpointDiscoveryCallback = new EndpointDiscoveryCallback()
        {
            @Override
            public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo info)
            {
                // An endpoint was found. Add it to the list of endpoints.
                m_Devices.put(endpointId, info.getEndpointName());
                m_AdapterListViewDevices.add(info.getEndpointName());
            }

            @Override
            public void onEndpointLost(@NonNull String endpointId)
            {
                // A previously discovered endpoint has gone away.
                m_Devices.remove(endpointId);
                m_AdapterListViewDevices.remove(m_Devices.get(endpointId));
            }
        };

        m_ConnectionLifecycleCallback = new ConnectionLifecycleCallback()
        {
            @Override
            public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(DataSynchronizationActivity.this);
                builder.setTitle(getResources().getString(R.string.accept_connection_header, connectionInfo.getEndpointName()));
                builder.setMessage(getResources().getString(R.string.accept_connection_confirm_code, connectionInfo.getAuthenticationToken()));
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                builder.setPositiveButton(R.string.accept, (DialogInterface dialog, int which) ->
                {
                    // The user confirmed, so we can accept the connection.
                    m_Connection.acceptConnection(endpointId, new PayloadListener());
                });
                builder.setNegativeButton(android.R.string.cancel, (DialogInterface dialog, int which) ->
                {
                    // The user canceled, so we should reject the connection.
                    m_Connection.rejectConnection(endpointId);
                });
                builder.show();
            }

            @Override
            public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution result)
            {
                switch (result.getStatus().getStatusCode()) {
                    case ConnectionsStatusCodes.STATUS_OK:
                        // We're connected! Can now start sending and receiving data.
                        m_ConnectedEndpointId = endpointId;

                        changeAdvertisingStatus(false);
                        changeDiscoveringStatus(false);

                        m_ButtonDiscover.setVisibility(View.GONE);
                        m_ListViewNearyDevices.setVisibility(View.GONE);

                        m_AdapterAvailableFiles.clear();
                        m_ListViewAvailableFiles.setVisibility(View.VISIBLE);
                        m_TextViewConnected.setVisibility(View.VISIBLE);
                        m_TextViewConnected.setText( getResources().getString(R.string.connection_established, m_Devices.get(endpointId)));

                        sendFilesList();

                        break;
                    case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                        // The connection was rejected by one or both sides.
                        Toast.makeText(DataSynchronizationActivity.this, getResources().getString(R.string.connection_rejected), Toast.LENGTH_SHORT).show();
                        break;
                    case ConnectionsStatusCodes.STATUS_ERROR:
                        // The connection broke before it was able to be accepted.
                        Toast.makeText(DataSynchronizationActivity.this, "ERROR: Connection failed", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        // Unknown status code
                }
            }

            @Override
            public void onDisconnected(@NonNull String endpointId)
            {
                // We've been disconnected from this endpoint. No more data can be
                // sent or received.

                m_TextViewConnected.setVisibility(View.VISIBLE);
                m_ListViewNearyDevices.setVisibility(View.VISIBLE);
                m_ButtonDiscover.setEnabled(true);
                m_ListViewNearyDevices.setEnabled(true);

                m_AdapterAvailableFiles.clear();
                m_ListViewAvailableFiles.setVisibility(View.INVISIBLE);
                m_TextViewConnected.setVisibility(View.INVISIBLE);
            }
        };

        m_ListViewNearyDevices.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->
        {
            if(m_Devices.size() < position)
            {
                return;
            }
            String endpointId = (String) Objects.requireNonNull(m_Devices.keySet().toArray())[position];
            if(endpointId == null)
            {
                return;
            }
            m_ListViewNearyDevices.setEnabled(false);
            m_ButtonDiscover.setEnabled(false);

            m_Connection.requestConnection(m_DeviceName, endpointId, m_ConnectionLifecycleCallback)
                .addOnSuccessListener((Void unused) ->
                {
                    // We successfully requested a connection. Now both sides
                    // must accept before the connection is established.
                })
                .addOnFailureListener((Exception e) ->
                {
                    // Nearby Connections failed to request the connection.
                    Toast.makeText(DataSynchronizationActivity.this, "ERROR: Connection failed. Reason: " + e.getCause(), Toast.LENGTH_SHORT).show();
                    m_ListViewNearyDevices.setEnabled(true);
                    m_ButtonDiscover.setEnabled(true);
                });
        });

        // Start advertising on load up
        changeAdvertisingStatus(true);
    }

    @Override
    public void onPause()
    {
        changeDiscoveringStatus(false);
        changeAdvertisingStatus(false);
        super.onPause();
    }

    private void changeAdvertisingStatus(boolean bOn)
    {
        if(m_Advertising == bOn)
        {
            return;
        }

        m_Advertising = bOn;

        if(!m_Advertising)
        {
            m_Connection.stopAdvertising();
            m_Advertising = false;
        }
        else
        {
            AdvertisingOptions advertisingOptions = new AdvertisingOptions.Builder().setStrategy(m_Strategy).build();
            m_Connection.startAdvertising(m_DeviceName, m_ServiceID, m_ConnectionLifecycleCallback, advertisingOptions)
                    .addOnSuccessListener((Void unused) ->
                    {
                        // We're advertising!
                        m_Advertising = true;
                    })
                    .addOnFailureListener((Exception e) ->
                    {
                        // We were unable to start advertising.
                        m_Advertising = false;
                    });
        }
    }

    private void changeDiscoveringStatus(boolean bOn)
    {
        if (m_Discovering == bOn)
        {
            return;
        }

        m_Discovering = bOn;

        if(!m_Discovering)
        {
            m_Connection.stopDiscovery();
            m_Discovering = false;

            m_AdapterListViewDevices.clear();
            m_ButtonDiscover.setBackgroundColor(Color.LTGRAY);
        }
        else
        {
            DiscoveryOptions discoveryOptions = new DiscoveryOptions.Builder().setStrategy(m_Strategy).build();
            m_Connection.startDiscovery(m_ServiceID, m_EndpointDiscoveryCallback, discoveryOptions)
                    .addOnSuccessListener((Void unused) ->
                    {
                        // We're discovering!
                        m_Discovering = true;
                        m_ButtonDiscover.setBackgroundColor(Color.GRAY);
                    })
                    .addOnFailureListener((Exception e) ->
                    {
                        // We're unable to start discovering.
                        m_Discovering = false;
                        m_ButtonDiscover.setBackgroundColor(Color.LTGRAY);
                    });
        }
    }

    public void onDiscover(View view)
    {
        changeDiscoveringStatus(!m_Discovering);
    }

    @Override
    public void onStringInput(String tag, String strInput, String strAdditonalInformation)
    {
        if (tag.equals("onCopiedFileAlreayExists"))
        {
            File currentFile = new File(strAdditonalInformation);
            File newFile = new File(m_strSaveFilePath, strInput);
            if(currentFile.renameTo(newFile))
            {
                Toast.makeText(DataSynchronizationActivity.this, getResources().getString(R.string.file_received, newFile.getAbsolutePath()), Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(DataSynchronizationActivity.this, getResources().getString(R.string.file_copy_error, strInput), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendFilesList()
    {
        File directory = new File(m_strSaveFilePath);
        StringBuilder builder = new StringBuilder();
        builder.append(m_TagEKList);
        for(File f : directory.listFiles())
        {
            if(!f.getName().endsWith(".json") || f.getName().equals(MainActivity.c_strSaveFilename) || f.getName().equals(MainActivity.c_strStdDataFilename))
            {
                continue;
            }

            builder.append(";");
            builder.append(f.getName());
        }

        byte[] bytesPayload = builder.toString().getBytes();

        Payload payload = Payload.fromBytes(bytesPayload);
        m_Connection.sendPayload(m_ConnectedEndpointId, payload);
    }

    private boolean existsFileAlready(String strFilename)
    {
        File directory = new File(m_strSaveFilePath);
        for(File f : directory.listFiles())
        {
            if(!f.getName().endsWith(".json"))
            {
                continue;
            }

            if(f.getName().equals(strFilename))
            {
                return true;
            }
        }
        return false;
    }

    private ArrayList<String> getListOfExistingFiles()
    {
        ArrayList<String> fileList = new ArrayList<>();
        File directory = new File(m_strSaveFilePath);
        for(File f : directory.listFiles())
        {
            if(!f.getName().endsWith(".json"))
            {
                continue;
            }

            fileList.add(f.getName());
        }
        return fileList;
    }

    // Permissions

    private static final String[] REQUIRED_PERMISSIONS =
            new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
            };
    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;

    @Override
    protected void onStart() {
        super.onStart();

        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        }
    }

    @Override
    protected void onStop() {
        m_Connection.stopAllEndpoints();
        super.onStop();
    }

    /** Returns true if the app was granted all the permissions. Otherwise, returns false. */
    private static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /** Handles user acceptance (or denial) of our permission request. */
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_CODE_REQUIRED_PERMISSIONS) {
            return;
        }

        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, getResources().getString(R.string.permission_not_granted), Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
        recreate();
    }
}
