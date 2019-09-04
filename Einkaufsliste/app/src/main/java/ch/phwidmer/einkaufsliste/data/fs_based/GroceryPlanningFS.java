package ch.phwidmer.einkaufsliste.data.fs_based;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import ch.phwidmer.einkaufsliste.UI.MainActivity;
import ch.phwidmer.einkaufsliste.data.GroceryPlanning;

public class GroceryPlanningFS extends GroceryPlanning
{
    private static GroceryPlanningFS m_Instance = null;

    private File m_AppDataDirectory;

    public static GroceryPlanningFS getInstance(File appDataDirectory, Context context) throws IOException
    {
        if(m_Instance == null)
        {
            m_Instance = new GroceryPlanningFS(appDataDirectory, context);
        }
        return m_Instance;
    }

    private GroceryPlanningFS(File appDataDirectory, Context context) throws IOException
    {
        clearAll();

        m_AppDataDirectory = appDataDirectory;

        // Load saved data
        File file = new File(m_AppDataDirectory, MainActivity.c_strSaveFilename);
        if(file.exists())
        {
            loadDataFromFile(file);
        }
        else
        {
            saveDataToFile(file, context);
        }
    }

    @Override
    protected void clearAll()
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
                saveDataToFile(m_File, null);
            }
            catch(IOException e)
            {
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
        File file = new File(m_AppDataDirectory, MainActivity.c_strSaveFilename);

        Thread thread = new Thread(new SaveToFileThread(file));
        thread.start();
    }
}
