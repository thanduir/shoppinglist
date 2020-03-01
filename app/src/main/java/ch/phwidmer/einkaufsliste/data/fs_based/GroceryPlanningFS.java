package ch.phwidmer.einkaufsliste.data.fs_based;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.concurrent.locks.ReentrantLock;

import ch.phwidmer.einkaufsliste.data.GroceryPlanning;
import ch.phwidmer.einkaufsliste.data.utilities.JsonSerializer;

public class GroceryPlanningFS extends GroceryPlanning
{
    private static final String c_strSaveFilename = "ch.phwidmer.einkaufsliste.einkaufsliste.json";

    private static GroceryPlanningFS m_Instance = null;

    private File m_AppDataDirectory;

    public static GroceryPlanningFS getInstance(@NonNull Context context)
    {
        if(m_Instance == null)
        {
            m_Instance = new GroceryPlanningFS(context);
        }
        return m_Instance;
    }

    private GroceryPlanningFS(@NonNull Context context)
    {
        clearAll();

        m_AppDataDirectory = context.getFilesDir();

        // Load saved data
        File file = new File(m_AppDataDirectory, c_strSaveFilename);
        JsonSerializer serializer = new JsonSerializer(this);
        if(file.exists())
        {
            try
            {
                serializer.loadDataFromFile(file);
            }
            catch(IOException e)
            {
                throw new InvalidParameterException(e.getMessage());
            }
        }
        else
        {
            try
            {
                serializer.saveDataToFile(file, null);
            }
            catch(IOException e)
            {
                throw new InvalidParameterException(e.getMessage());
            }
        }
    }

    @Override
    public void clearAll()
    {
        m_Categories = new CategoriesFS();
        m_Ingredients = new IngredientsFS();
        m_Recipes = new RecipesFS();
        m_ShoppingList = new ShoppingListFS();
    }

    private class SaveToFileThread implements Runnable
    {
        private final ReentrantLock lock = new ReentrantLock();
        private File m_File;

        SaveToFileThread(File file)
        {
            m_File =  file;
        }

        public void run()
        {
            lock.lock();
            try
            {
                JsonSerializer serializer = new JsonSerializer(GroceryPlanningFS.this);
                serializer.saveDataToFile(m_File, null);
            }
            catch(IOException e)
            {
                //noinspection ResultOfMethodCallIgnored
                m_File.delete();
            }
            finally
            {
                lock.unlock();
            }
        }
    }

    @Override
    public void flush()
    {
        File file = new File(m_AppDataDirectory, c_strSaveFilename);

        Thread thread = new Thread(new SaveToFileThread(file));
        thread.start();
    }
}
