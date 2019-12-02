package ch.phwidmer.einkaufsliste.data;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;
import java.security.InvalidParameterException;

import ch.phwidmer.einkaufsliste.data.fs_based.GroceryPlanningFS;

public class GroceryPlanningFactory
{
    public enum Backend {
        fs_based,
        db_based
    }

    private static Backend m_Backend = null;
    private static File m_AppDataDirectory = null;
    private static String m_strAppSaveFilename = null;

    public static void setBackend(Backend backend)
    {
        m_Backend = backend;
    }

    public static void setAppDataDirectory(File appDataDirectory)
    {
        m_AppDataDirectory = appDataDirectory;
    }

    public static void setAppSaveFilename(String appSaveFilename)
    {
        m_strAppSaveFilename = appSaveFilename;
    }

    public static GroceryPlanning groceryPlanning(@NonNull Context context)
    {
        if(m_Backend == null)
        {
            throw new InvalidParameterException("Backend not set");
        }

        switch(m_Backend)
        {
            case fs_based:
            {
                return GroceryPlanningFS.getInstance(m_AppDataDirectory, m_strAppSaveFilename, context);
            }

            case db_based:
            {
                // Not implemented yet
                throw new InvalidParameterException("Unimplemented backend");
            }
        }

        throw new InvalidParameterException("Unknown backend");
    }
}
