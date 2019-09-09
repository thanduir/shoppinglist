package ch.phwidmer.einkaufsliste.data;

import android.content.Context;

import java.io.File;
import java.io.IOException;

import ch.phwidmer.einkaufsliste.data.fs_based.GroceryPlanningFS;

public class GroceryPlanningFactory
{
    public enum Backend {
        fs_based,
        db_based
    }

    private static Backend m_Backend = null;
    private static File m_AppDataDirectory = null;

    public static void setBackend(Backend backend)
    {
        m_Backend = backend;
    }

    public static void setAppDataDirectory(File appDataDirectory)
    {
        m_AppDataDirectory = appDataDirectory;
    }

    public static GroceryPlanning groceryPlanning(Context context)
    {
        switch(m_Backend)
        {
            case fs_based:
            {
                return GroceryPlanningFS.getInstance(m_AppDataDirectory, context);
            }

            case db_based:
            {
                // Not implemented yet
                return null;
            }
        }

        return null;
    }
}
