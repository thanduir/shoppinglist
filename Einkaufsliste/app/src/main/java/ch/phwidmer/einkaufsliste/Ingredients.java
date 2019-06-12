package ch.phwidmer.einkaufsliste;

import android.support.annotation.NonNull;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Vector;

class Ingredients
{
    private Categories m_Categories;

    static String c_strProvenanceEverywhere = "*EVERYWHERE*";

    class Ingredient
    {
        Categories.Category m_Category;
        String m_strProvenance = c_strProvenanceEverywhere;
        Amount.Unit m_DefaultUnit;
    }
    private TreeMap<String, Ingredient> m_Ingredients;

    Ingredients(Categories categories)
    {
        m_Categories = categories;
        m_Ingredients = new TreeMap<>(new Helper.SortIgnoreCase());
    }

    void updateCategories(Categories categories)
    {
        m_Categories = categories;
    }

    void addIngredient(String strName, Amount.Unit defaultUnit)
    {
        if(m_Ingredients.containsKey(strName))
        {
            return;
        }
        Ingredient i = new Ingredient();
        i.m_DefaultUnit = defaultUnit;
        i.m_Category = m_Categories.getCategory(m_Categories.getAllCategories().firstElement());
        m_Ingredients.put(strName, i);
    }

    Ingredient getIngredient(String strName)
    {
        return m_Ingredients.get(strName);
    }

    Vector<String> getAllIngredients()
    {
        Vector<String> vec = new Vector<>();
        for(Object obj : m_Ingredients.keySet())
        {
            String str = (String)obj;
            vec.add(str);
        }
        return vec;
    }

    void removeIngredient(String strName)
    {
        m_Ingredients.remove(strName);
    }

    void renameIngredient(String strIngredient, String strNewName)
    {
        if(!m_Ingredients.containsKey(strIngredient))
        {
            return;
        }

        Ingredient ingredient = m_Ingredients.get(strIngredient);
        m_Ingredients.remove(strIngredient);
        m_Ingredients.put(strNewName, ingredient);
    }

    boolean isCategoryInUse(Categories.Category category, @NonNull ArrayList<String> ingredientsUsingCategory)
    {
        boolean stillInUse = false;
        for(TreeMap.Entry<String, Ingredient> e : m_Ingredients.entrySet())
        {
            if(e.getValue().m_Category.equals(category))
            {
                ingredientsUsingCategory.add(e.getKey());
                stillInUse = true;
            }
        }
        return stillInUse;
    }

    void onCategoryRenamed(Categories.Category category, Categories.Category newCategory)
    {
        for(Ingredient i : m_Ingredients.values())
        {
            if(i.m_Category.equals(category))
            {
                i.m_Category = newCategory;
            }
        }
    }

    boolean isSortOrderInUse(String strSortOrder, @NonNull ArrayList<String> ingredientsUsingSortOrder)
    {
        boolean stillInUse = false;
        for(TreeMap.Entry<String, Ingredient> e : m_Ingredients.entrySet())
        {
            if(e.getValue().m_strProvenance.equals(strSortOrder))
            {
                ingredientsUsingSortOrder.add(e.getKey());
                stillInUse = true;
            }
        }
        return stillInUse;
    }

    // Serializing

    void saveToJson(JsonWriter writer) throws IOException
    {
        writer.beginObject();
        writer.name("id").value("Ingredients");

        for(TreeMap.Entry<String, Ingredient> e : m_Ingredients.entrySet())
        {
            writer.name(e.getKey());

            writer.beginObject();
            writer.name("category").value(e.getValue().m_Category.getName());
            writer.name("provenance").value(e.getValue().m_strProvenance);
            writer.name("default-unit").value(e.getValue().m_DefaultUnit.toString());
            writer.endObject();
        }

        writer.endObject();
    }

    void readFromJson(JsonReader reader, int iVersion) throws IOException
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
                        in.m_strProvenance = reader.nextString();
                        if(m_Categories.getSortOrder(in.m_strProvenance) == null && !in.m_strProvenance.equals(c_strProvenanceEverywhere))
                        {
                            // Provenance doesn't exist as a SortOrder -> set default provenance.
                            in.m_strProvenance = c_strProvenanceEverywhere;
                        }
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
