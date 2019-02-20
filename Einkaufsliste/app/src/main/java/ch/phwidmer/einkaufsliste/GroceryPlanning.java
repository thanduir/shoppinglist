package ch.phwidmer.einkaufsliste;

import android.content.Context;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.widget.Toast;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GroceryPlanning
{
    static int             SERIALIZING_VERSION = 0;

    public Categories      m_Categories;
    public Ingredients     m_Ingredients;
    public Recipes         m_Recipes;
    public ShoppingList    m_ShoppingList;

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

    public void saveDataToFile(File file)
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
        }
        catch(IOException e)
        {
            return;
        }
    }

    public void loadDataFromFile(File file, Context context)
    {
        try
        {
            FileReader fw = new FileReader(file);
            JsonReader jr = new JsonReader(fw);

            jr.beginArray();

            jr.beginObject();
            String id = jr.nextName();
            String name = jr.nextString();
            if(!id.equals("id") || !name.equals("ch.phwidmer.einkaufsliste"))
            {
                throw new IOException();
            }
            String strVersion = jr.nextName();
            int iVersion = jr.nextInt();
            if(!strVersion.equals("version") || iVersion > SERIALIZING_VERSION)
            {
                throw new IOException();
            }
            jr.endObject();

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
            Toast.makeText(context, "Couldn't load data file.", Toast.LENGTH_SHORT).show();
            m_Categories = new Categories();
            m_Ingredients = new Ingredients(m_Categories);
            m_Recipes = new Recipes();
            m_ShoppingList = new ShoppingList();
            return;
        }
    }

}
