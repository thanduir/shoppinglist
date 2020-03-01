package ch.phwidmer.einkaufsliste.data;

import android.content.Context;
import android.support.annotation.NonNull;

import java.security.InvalidParameterException;

import ch.phwidmer.einkaufsliste.data.db_based.GroceryPlanningDB;
import ch.phwidmer.einkaufsliste.data.fs_based.GroceryPlanningFS;

public class GroceryPlanningFactory
{
    private static DataBackend m_Backend = null;

    public static void setBackend(DataBackend backend)
    {
        m_Backend = backend;
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
                return GroceryPlanningFS.getInstance(context);
            }

            case db_based:
            {
                return GroceryPlanningDB.getInstance(context);
            }
        }

        throw new InvalidParameterException("Unknown backend");
    }
}
