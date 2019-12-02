package ch.phwidmer.einkaufsliste.data.fs_based;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import ch.phwidmer.einkaufsliste.R;
import ch.phwidmer.einkaufsliste.UI.MainActivity;
import ch.phwidmer.einkaufsliste.data.GroceryPlanning;

public class GroceryPlanningFS extends GroceryPlanning
{
    private static GroceryPlanningFS m_Instance = null;

    private File m_AppDataDirectory;
    private String m_strSaveFilename;

    public static GroceryPlanningFS getInstance(@NonNull File appDataDirectory, @NonNull String strSaveFilename, @NonNull Context context)
    {
        if(m_Instance == null)
        {
            m_Instance = new GroceryPlanningFS(appDataDirectory, strSaveFilename, context);
        }
        return m_Instance;
    }

    private GroceryPlanningFS(@NonNull File appDataDirectory, @NonNull String strSaveFilename, @NonNull Context context)
    {
        clearAll();

        m_AppDataDirectory = appDataDirectory;
        m_strSaveFilename = strSaveFilename;

        // Load saved data
        File file = new File(m_AppDataDirectory, strSaveFilename);
        if(file.exists())
        {
            try
            {
                loadDataFromFile(file);
            }
            catch(IOException e)
            {
                MainActivity.showErrorDialog(context.getString(R.string.text_load_file_failed), e.getMessage(), context);
            }
        }
        else
        {
            try
            {
                saveDataToFile(file, context);
            }
            catch(IOException e)
            {
                MainActivity.showErrorDialog(context.getString(R.string.text_save_file_failed), e.getMessage(), context);
            }
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
        File file = new File(m_AppDataDirectory, m_strSaveFilename);

        Thread thread = new Thread(new SaveToFileThread(file));
        thread.start();
    }
}
