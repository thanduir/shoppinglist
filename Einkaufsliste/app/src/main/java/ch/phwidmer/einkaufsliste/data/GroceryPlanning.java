package ch.phwidmer.einkaufsliste.data;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.support.annotation.NonNull;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import ch.phwidmer.einkaufsliste.R;

public abstract class GroceryPlanning
{
    private static int     SERIALIZING_VERSION = 1;

    protected Categories      m_Categories;
    protected Ingredients     m_Ingredients;
    protected Recipes         m_Recipes;
    protected ShoppingList    m_ShoppingList;

    protected abstract void clearAll();
    public abstract void flush();

    public Categories categories()
    {
        return m_Categories;
    }

    public Ingredients ingredients()
    {
        return m_Ingredients;
    }

    public Recipes recipes()
    {
        return m_Recipes;
    }

    public ShoppingList shoppingList()
    {
        return m_ShoppingList;
    }

    public void saveDataToFile(@NonNull File fileToBeCreated, Context context) throws IOException
    {
        boolean moveFilesFirst = fileToBeCreated.exists();

        // Concept if the file already exists:
        //      * write to filename.new
        //      * move existing to filename.old
        //      * move filename.new to filename
        //      * delete filename.old

        File fileNew;
        if(moveFilesFirst)
        {
            fileNew = new File(fileToBeCreated.getAbsolutePath() + ".new");
        }
        else
        {
            fileNew = fileToBeCreated;
        }

        try
        {
            FileWriter fw = new FileWriter(fileNew, false);
            JsonWriter jw = new JsonWriter(fw);
            jw.setIndent("  ");

            jw.beginArray();

            jw.beginObject();
            jw.name("id").value("ch.phwidmer.einkaufsliste");
            jw.name("version").value(SERIALIZING_VERSION);
            jw.endObject();

            m_Categories.saveToJson(jw);
            m_Ingredients.saveToJson(jw);
            m_Recipes.saveToJson(jw);
            m_ShoppingList.saveToJson(jw);

            jw.endArray();

            jw.close();
            fw.close();
        }
        catch(IOException e)
        {
            throw new IOException(e.getMessage());
        }

        if(moveFilesFirst)
        {
            File fileOld = new File(fileToBeCreated.getAbsolutePath() + ".old");
            if(!fileToBeCreated.renameTo(fileOld))
            {
                //noinspection ResultOfMethodCallIgnored
                fileNew.delete();
                //noinspection ResultOfMethodCallIgnored
                fileOld.delete();
                throw new IOException(context.getString(R.string.text_save_file_failed));
            }
            if(!fileNew.renameTo(fileToBeCreated))
            {
                //noinspection ResultOfMethodCallIgnored
                fileOld.renameTo(fileToBeCreated);
                //noinspection ResultOfMethodCallIgnored
                fileOld.delete();
                throw new IOException(context.getString(R.string.text_save_file_failed));
            }

            //noinspection ResultOfMethodCallIgnored
            fileOld.delete();
        }

        scanFile(context, fileToBeCreated);
    }

    public void loadDataFromFile(@NonNull File file) throws IOException
    {
        try
        {
            FileReader fw = new FileReader(file);
            JsonReader jr = new JsonReader(fw);

            jr.beginArray();

            jr.beginObject();
            boolean bIDFound = false;
            int iVersion = -1;
            while (jr.hasNext()) {
                String name = jr.nextName();
                if (name.equals("id")) {
                    String id = jr.nextString();
                    if (!id.equals("ch.phwidmer.einkaufsliste"))
                    {
                        throw new IOException("Invalid ID string");
                    }
                    bIDFound = true;
                }
                else if (name.equals("version")) {
                    iVersion = jr.nextInt();
                    if (iVersion > SERIALIZING_VERSION)
                    {
                        throw new IOException("Invalid version");
                    }
                }
            }
            jr.endObject();
            if(!bIDFound || iVersion == -1)
            {
                throw new IOException("Invalid ID");
            }

            clearAll();

            m_Categories.readFromJson(jr);
            m_Ingredients.readFromJson(jr);
            m_Recipes.readFromJson(jr);
            m_ShoppingList.readFromJson(jr);

            jr.endArray();

            jr.close();
            fw.close();
        }
        catch(IOException e)
        {
            clearAll();
            throw new IOException(e.getMessage());
        }

        checkDataConsistency();
    }

    // Make file known to the MediaScanner so that it apears when the device is mount e.g. on windows.
    public void scanFile(Context context, @NonNull File f)
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

    private void checkDataConsistency() throws IOException
    {
        LinkedList<String> missingCategories = new LinkedList<>();
        LinkedList<String> missingSortOrders = new LinkedList<>();
        LinkedList<String> missingIngredients = new LinkedList<>();

        boolean dataConsistent = m_Ingredients.checkDataConsistency(m_Categories, missingCategories, missingSortOrders);
        dataConsistent = dataConsistent && m_Recipes.checkDataConsistency(m_Ingredients, missingIngredients);
        dataConsistent = dataConsistent && m_ShoppingList.checkDataConsistency(m_Ingredients, missingIngredients);

        if(!dataConsistent)
        {
            String strMessage = "Inconsistent data:";
            if(missingCategories.size() > 0)
            {
                strMessage += "\n\nCategories: " + missingCategories.toString();
            }
            if(missingSortOrders.size() > 0)
            {
                strMessage += "\n\nSortOrders: " + missingSortOrders.toString();
            }
            if(missingIngredients.size() > 0)
            {
                strMessage += "\n\nIngredients: " + missingIngredients.toString();
            }
            throw new IOException(strMessage);
        }
    }
}
