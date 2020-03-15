package ch.phwidmer.einkaufsliste.data.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import ch.phwidmer.einkaufsliste.data.GroceryPlanning;

public class JsonSerializer {
    static final int SERIALIZING_VERSION = 1;

    private static final String KEY_INSTANCE_UID    = "ch.phwidmer.einkaufsliste.INSTANCE_UID";

    private GroceryPlanning m_GroceryPlanning;
    private boolean         m_CheckDataConsistency = true;

    public JsonSerializer(GroceryPlanning groceryPlanning)
    {
        m_GroceryPlanning = groceryPlanning;
    }

    @SuppressWarnings("unused")
    public void disableDataConsistencyCheck()
    {
        m_CheckDataConsistency = false;
    }

    public void saveDataToFile(@NonNull File fileToBeCreated, Context context) throws IOException
    {
        GroceryPlanningJsonWriter writer = new GroceryPlanningJsonWriter(m_GroceryPlanning);
        writer.write(fileToBeCreated, getUID(context));

        scanFile(context, fileToBeCreated);
    }

    public void loadDataFromFile(@NonNull File file) throws IOException
    {
        GroceryPlanningJsonReader reader = new GroceryPlanningJsonReader(m_GroceryPlanning);
        reader.read(file);

        if(m_CheckDataConsistency)
        {
            DataConsistencyCheck check = new DataConsistencyCheck(m_GroceryPlanning);
            check.checkDataConsistency();
        }
    }

    // Make file known to the MediaScanner so that it apears when the device is mount e.g. on windows.
    private void scanFile(Context context, @NonNull File f)
    {
        if(context == null)
        {
            return;
        }

        MediaScannerConnection.scanFile(context,
                new String[] {f.getAbsolutePath()},
                new String[] {"application/json"},
                null);
    }

    private String getUID(Context context)
    {
        if(context == null)
        {
            return "";
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        if(!preferences.contains(KEY_INSTANCE_UID))
        {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(KEY_INSTANCE_UID, UUID.randomUUID().toString());
            editor.apply();
        }

        return preferences.getString(KEY_INSTANCE_UID, "");
    }
}
