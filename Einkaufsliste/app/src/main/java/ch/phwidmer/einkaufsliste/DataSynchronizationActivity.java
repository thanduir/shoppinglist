package ch.phwidmer.einkaufsliste;

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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class DataSynchronizationActivity extends AppCompatActivity
{
    // Type: P2P_STAR or P2P_POINT_TO_POINT ?
    private final Strategy m_Strategy = Strategy.P2P_POINT_TO_POINT;
    private final String m_ServiceID = "ch.phwidmer.einkaufsliste.Service";

    private String m_strSaveFilePath;
    private String m_DeviceName;

    private ConnectionsClient m_Connection;

    private EndpointDiscoveryCallback m_EndpointDiscoveryCallback;
    private ConnectionLifecycleCallback m_ConnectionLifecycleCallback;

    private boolean m_Discovering = false;
    private boolean m_Advertising = false;

    private LinkedHashMap<String, String>   m_Devices;

    private ListView m_ListViewDevices;
    private ListView m_ListViewAvailableFiles;
    private Button m_ButtonDiscover;
    private Button m_ButtonAdvertise;

    static class PayloadListener extends PayloadCallback {

        @Override
        public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
            // TODO
            // This always gets the full data of the payload. Will be null if it's not a BYTES
            // payload. You can check the payload type with payload.getType().
            //byte[] receivedBytes = payload.asBytes();
        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String endpointId, @NonNull PayloadTransferUpdate update) {
            // TODO
            // Bytes payloads are sent as a single chunk, so you'll receive a SUCCESS update immediately
            // after the call to onPayloadReceived().
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_synchronization);

        Intent intent = getIntent();
        String strSaveFilePath = intent.getStringExtra(MainActivity.EXTRA_SAVEFILESPATH);

        m_ListViewDevices = findViewById(R.id.listviewOtherDevices);
        m_ListViewAvailableFiles = findViewById(R.id.listviewAvailableFiles);
        m_ButtonAdvertise = findViewById(R.id.buttonAdvertise);
        m_ButtonDiscover = findViewById(R.id.buttonDiscover);

        m_DeviceName = Settings.Secure.getString(getContentResolver(), "bluetooth_name");

        m_Devices = new LinkedHashMap<>();

        m_Connection = Nearby.getConnectionsClient(this);

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        m_ListViewDevices.setAdapter(adapter);

        m_EndpointDiscoveryCallback =
                new EndpointDiscoveryCallback() {
                    @Override
                    public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo info) {
                        // An endpoint was found. Add it to the list of endpoints.
                        Toast.makeText(DataSynchronizationActivity.this, "Endpoint found: " + info.getEndpointName(), Toast.LENGTH_SHORT).show();
                        m_Devices.put(endpointId, info.getEndpointName());

                        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>)m_ListViewDevices.getAdapter();
                        adapter.add(info.getEndpointName());
                        m_ListViewDevices.setAdapter(adapter);
                    }

                    @Override
                    public void onEndpointLost(@NonNull String endpointId) {
                        // A previously discovered endpoint has gone away.
                        m_Devices.remove(endpointId);

                        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>)m_ListViewDevices.getAdapter();
                        adapter.remove(m_Devices.get(endpointId));
                        m_ListViewDevices.setAdapter(adapter);
                    }
                };

        m_ConnectionLifecycleCallback =
                new ConnectionLifecycleCallback() {
                    @Override
                    public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(DataSynchronizationActivity.this);
                        builder.setTitle("Accept connection to " + connectionInfo.getEndpointName());
                        builder.setMessage("Confirm the code matches on both devices: " + connectionInfo.getAuthenticationToken());
                        builder.setIcon(android.R.drawable.ic_dialog_alert);
                        builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // The user confirmed, so we can accept the connection.
                                m_Connection.acceptConnection(endpointId, new PayloadListener());
                            }
                        });
                        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // The user canceled, so we should reject the connection.
                                m_Connection.rejectConnection(endpointId);
                            }
                        });
                        builder.show();
                    }

                    @Override
                    public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution result) {
                        switch (result.getStatus().getStatusCode()) {
                            case ConnectionsStatusCodes.STATUS_OK:
                                // We're connected! Can now start sending and receiving data.
                                Toast.makeText(DataSynchronizationActivity.this, "Connection successfully established", Toast.LENGTH_SHORT).show();

                                if(m_Advertising)
                                {
                                    onAdvertise(null);
                                }
                                if(m_Discovering)
                                {
                                    onDiscover(null);
                                }

                                sendFilesList();

                                break;
                            case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                                // The connection was rejected by one or both sides.
                                Toast.makeText(DataSynchronizationActivity.this, "Connection rejected", Toast.LENGTH_SHORT).show();
                                // TODO: How to proceed from here?
                                break;
                            case ConnectionsStatusCodes.STATUS_ERROR:
                                // The connection broke before it was able to be accepted.
                                Toast.makeText(DataSynchronizationActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
                                // TODO: How to proceed from here?
                                break;
                            default:
                                // Unknown status code
                        }
                    }

                    @Override
                    public void onDisconnected(@NonNull String endpointId) {
                        // We've been disconnected from this endpoint. No more data can be
                        // sent or received.
                    }
                };

        m_ListViewDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Connect to other device
                Toast.makeText(DataSynchronizationActivity.this, "Connecting to device " + m_ListViewDevices.getAdapter().getItem(position).toString(), Toast.LENGTH_SHORT).show();

                String endpointId = (String)m_Devices.keySet().toArray()[position];

                // TODO: Das sollte ich evtl. besser machen als nur per itemclicked?

                m_Connection
                        .requestConnection(m_DeviceName, endpointId, m_ConnectionLifecycleCallback)
                        .addOnSuccessListener(
                                (Void unused) -> {
                                    // We successfully requested a connection. Now both sides
                                    // must accept before the connection is established.
                                })
                        .addOnFailureListener(
                                (Exception e) -> {
                                    // Nearby Connections failed to request the connection.
                                    Toast.makeText(DataSynchronizationActivity.this, "Connection failed. Reason: " + e.getCause(), Toast.LENGTH_SHORT).show();
                                });
            }
        });
    }


    public void onAdvertise(View view)
    {
        if(m_Advertising)
        {
            m_Connection.stopAdvertising();
            m_Advertising = false;
            m_ButtonAdvertise.setTextColor(Color.BLACK);
            return;
        }

        AdvertisingOptions advertisingOptions =
                new AdvertisingOptions.Builder().setStrategy(m_Strategy).build();
        m_Connection
                .startAdvertising(
                        m_DeviceName, m_ServiceID, m_ConnectionLifecycleCallback, advertisingOptions)
                .addOnSuccessListener(
                        (Void unused) -> {
                            // We're advertising!
                            m_Advertising = true;
                            m_ButtonAdvertise.setTextColor(Color.RED);
                        })
                .addOnFailureListener(
                        (Exception e) -> {
                            // We were unable to start advertising.
                            m_Advertising = false;
                            m_ButtonAdvertise.setTextColor(Color.BLACK);
                        });
    }

    public void onDiscover(View view)
    {
        if(m_Discovering)
        {
            m_Connection.stopDiscovery();
            m_Discovering = false;
            m_ButtonDiscover.setTextColor(Color.BLACK);
            return;
        }

        DiscoveryOptions discoveryOptions =
                new DiscoveryOptions.Builder().setStrategy(m_Strategy).build();
        m_Connection
                .startDiscovery(m_ServiceID, m_EndpointDiscoveryCallback, discoveryOptions)
                .addOnSuccessListener(
                        (Void unused) -> {
                            // We're discovering!
                            m_Discovering = true;
                            m_ButtonDiscover.setTextColor(Color.RED);
                        })
                .addOnFailureListener(
                        (Exception e) -> {
                            // We're unable to start discovering.
                            m_Discovering = false;
                            m_ButtonDiscover.setTextColor(Color.BLACK);
                        });
    }

    private void sendFilesList()
    {
        ArrayList<String> inputList = new ArrayList<String>();
        File directory = new File(m_strSaveFilePath);
        for(File f : directory.listFiles())
        {
            if(!f.getName().endsWith(".json"))
            {
                continue;
            }

            inputList.add(f.getName());
        }

        // TODO: inputList senden als Payload!

    }

    // Permissions

    private static final String[] REQUIRED_PERMISSIONS =
            new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
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
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
        recreate();
    }
}
