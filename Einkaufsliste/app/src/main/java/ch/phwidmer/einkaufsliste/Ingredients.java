package ch.phwidmer.einkaufsliste;

import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Vector;

public class Ingredients
{
    private Categories m_Categories;

    public enum Provenance
    {
        Everywhere,
        Migros,
        Coop,
        Denner,
        Aldi,
        Lidl;
    }

    public class Ingredient
    {
        public Categories.Category m_Category;
        public Provenance m_Provenance = Provenance.Everywhere;
        public Amount.Unit m_DefaultUnit = Amount.Unit.Count;
    }
    private LinkedHashMap<String, Ingredient>  m_Ingredients;

    public Ingredients(Categories categories)
    {
        m_Categories = categories;
        m_Ingredients = new LinkedHashMap<String, Ingredient>();
    }

    public void updateCategories(Categories categories)
    {
        m_Categories = categories;
    }

    public void addIngredient(String strName)
    {
        if(m_Ingredients.containsKey(strName))
        {
            return;
        }
        Ingredient i = new Ingredient();
        i.m_Category = m_Categories.getCategory(m_Categories.getAllCategories().firstElement());
        m_Ingredients.put(strName, i);
    }

    public Ingredient getIngredient(String strName)
    {
        return m_Ingredients.get(strName);
    }

    public Vector<String> getAllIngredients()
    {
        Vector<String> vec = new Vector<String>();
        for(Object obj : m_Ingredients.keySet())
        {
            String str = (String)obj;
            vec.add(str);
        }
        Collections.sort(vec, new Helper.SortIgnoreCase());
        return vec;
    }

    public void removeIngredient(String strName)
    {
        m_Ingredients.remove(strName);
    }

    public boolean isCategoryInUse(Categories.Category category)
    {
        for(Ingredient i : m_Ingredients.values())
        {
            if(i.m_Category.equals(category))
            {
                return true;
            }
        }
        return false;
    }

    // Serializing

    public void saveToJson(JsonWriter writer) throws IOException
    {
        writer.beginObject();
        writer.name("id").value("Ingredients");

        for(LinkedHashMap.Entry<String, Ingredient> e : m_Ingredients.entrySet())
        {
            writer.name(e.getKey());
            writer.beginObject();
            writer.name("category").value(e.getValue().m_Category.getName());
            writer.name("provenance").value(e.getValue().m_Provenance.toString());
            writer.name("default-unit").value(e.getValue().m_DefaultUnit.toString());

            writer.endObject();
        }

        writer.endObject();
    }

    public void readFromJson(JsonReader reader, int iVersion) throws IOException
    {
        reader.beginObject();
        while (reader.hasNext())
        {
            String name = reader.nextName();
            if (name.equals("id"))
            {
                String id = reader.nextString();
                if(!id.equals("Ingredients"))
                {
                    throw new IOException();
                }
            }
            else
            {
                Ingredient in = new Ingredient();

                reader.beginObject();
                while (reader.hasNext())
                {
                    String currentName = reader.nextName();
                    if (currentName.equals("category"))
                    {
                        in.m_Category = m_Categories.getCategory(reader.nextString());
                    }
                    else if(currentName.equals("provenance"))
                    {
                        String provenance = reader.nextString();
                        in.m_Provenance = Provenance.valueOf(provenance);
                    }
                    else if(currentName.equals("default-unit"))
                    {
                        String unit = reader.nextString();
                        in.m_DefaultUnit = Amount.Unit.valueOf(unit);
                    }
                    else
                    {
                        reader.skipValue();
                    }
                }
                reader.endObject();

                m_Ingredients.put(name, in);
            }
        }
        reader.endObject();
    }
}
