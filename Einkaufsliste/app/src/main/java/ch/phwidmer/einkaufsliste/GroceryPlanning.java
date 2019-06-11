package ch.phwidmer.einkaufsliste;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.widget.Toast;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GroceryPlanning
{
    private static int     SERIALIZING_VERSION = 1;

    Categories      m_Categories;
    Ingredients     m_Ingredients;
    Recipes         m_Recipes;
    ShoppingList    m_ShoppingList;

    GroceryPlanning()
    {
        m_Categories = new Categories();
        m_Ingredients = new Ingredients(m_Categories);
        m_Recipes = new Recipes();
        m_ShoppingList = new ShoppingList();
    }

    GroceryPlanning(File file, Context context)
    {
        loadDataFromFile(file, context);
    }

    void saveDataToFile(File file, Context context)
    {
        try
        {
            FileWriter fw = new FileWriter(file, false);
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

            scanFile(context, file);
        }
        catch(IOException e)
        {
            Toast.makeText(context, R.string.text_save_file_failed, Toast.LENGTH_SHORT).show();
        }
    }

    void loadDataFromFile(File file, Context context)
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
                        throw new IOException();
                    }
                    bIDFound = true;
                }
                else if (name.equals("version")) {
                    iVersion = jr.nextInt();
                    if (iVersion > SERIALIZING_VERSION)
                    {
                        throw new IOException();
                    }
                }
            }
            jr.endObject();
            if(!bIDFound || iVersion == -1)
            {
                throw new IOException();
            }

            m_Categories = new Categories();
            m_Categories.readFromJson(jr, iVersion);

            m_Ingredients = new Ingredients(m_Categories);
            m_Ingredients.readFromJson(jr, iVersion);

            m_Recipes = new Recipes();
            m_Recipes.readFromJson(jr, iVersion);

            m_ShoppingList = new ShoppingList();
            m_ShoppingList.readFromJson(jr, iVersion);

            jr.endArray();

            jr.close();
            fw.close();
        }
        catch(IOException e)
        {
            Toast.makeText(context, R.string.text_load_file_failed, Toast.LENGTH_SHORT).show();
            m_Categories = new Categories();
            m_Ingredients = new Ingredients(m_Categories);
            m_Recipes = new Recipes();
            m_ShoppingList = new ShoppingList();
        }
    }

    // Make file known to the MediaScanner so that it apears when the device is mount e.g. on windows.
    void scanFile(Context context, File f)
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
}
