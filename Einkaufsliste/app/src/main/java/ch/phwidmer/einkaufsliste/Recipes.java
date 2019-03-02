package ch.phwidmer.einkaufsliste;

import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Vector;

public class Recipes {

    public class Recipe {
        public Integer m_NumberOfPersons = 0;
        public LinkedList<RecipeItem> m_Items = new LinkedList<RecipeItem>();
    }
    private LinkedHashMap<String, Recipe> m_Recipies;

    public Recipes()
    {
        m_Recipies = new LinkedHashMap<String, Recipe>();
    }

    public void addRecipe(String strName, Integer iNrPersons)
    {
        if(m_Recipies.containsKey(strName))
        {
            return;
        }
        Recipe recipe = new Recipe();
        recipe.m_NumberOfPersons = iNrPersons;
        m_Recipies.put(strName, recipe);
    }

    public void addRecipe(String strName, Recipe r)
    {
        if(m_Recipies.containsKey(strName))
        {
            return;
        }
        m_Recipies.put(strName, r);
    }

    public Recipe getRecipe(String strName)
    {
        return m_Recipies.get(strName);
    }

    public void removeRecipe(String strName)
    {
        m_Recipies.remove(strName);
    }

    public Vector<String> getAllRecipes()
    {
        Vector<String> vec = new Vector<String>();
        for(Object obj : m_Recipies.keySet())
        {
            String str = (String)obj;
            vec.add(str);
        }
        Collections.sort(vec, new Helper.SortIgnoreCase());
        return vec;
    }

    public boolean isIngredientInUse(String strIngredient)
    {
        for(Recipe r : m_Recipies.values())
        {
            for(RecipeItem ri : r.m_Items)
            {
                if (ri.m_Ingredient.equals(strIngredient))
                {
                    return true;
                }
            }
        }
        return false;
    }

    // Serializing

    public void saveToJson(JsonWriter writer) throws IOException
    {
        writer.beginObject();
        writer.name("id").value("Recipes");

        for(LinkedHashMap.Entry<String, Recipe> e : m_Recipies.entrySet())
        {
            writer.name(e.getKey());
            writer.beginObject();
            writer.name("NrPersons").value(e.getValue().m_NumberOfPersons);
            for(RecipeItem ri : e.getValue().m_Items)
            {
                writer.name(ri.m_Ingredient);
                writer.beginObject();

                writer.name("amount");
                writer.beginArray();
                writer.value(ri.m_Amount.m_Quantity);
                writer.value(ri.m_Amount.m_Unit.toString());
                writer.endArray();

                writer.name("size").value(ri.m_Size.toString());
                writer.name("optional").value(ri.m_Optional);

                writer.endObject();
            }
            writer.endObject();
        }

        writer.endObject();
    }

    public void readFromJson(JsonReader reader, int iVersion) throws IOException
    {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("id"))
            {
                String id = reader.nextString();
                if (!id.equals("Recipes"))
                {
                    throw new IOException();
                }
            }
            else
            {
                Recipe recipe = new Recipe();

                reader.beginObject();
                while (reader.hasNext())
                {
                    String currentName = reader.nextName();
                    if (currentName.equals("NrPersons"))
                    {
                        recipe.m_NumberOfPersons = reader.nextInt();
                    }
                    else
                    {
                        RecipeItem item = new RecipeItem();
                        item.m_Ingredient = currentName;

                        reader.beginObject();
                        while (reader.hasNext())
                        {
                            String itemName = reader.nextName();
                            if(itemName.equals("amount"))
                            {
                                reader.beginArray();

                                item.m_Amount.m_Quantity = (float)reader.nextDouble();
                                String str = reader.nextString();
                                item.m_Amount.m_Unit = Amount.Unit.valueOf(str);

                                reader.endArray();
                            }
                            else if(itemName.equals("size"))
                            {
                                String size = reader.nextString();
                                item.m_Size = RecipeItem.Size.valueOf(size);
                            }
                            else if(itemName.equals("optional"))
                            {
                                item.m_Optional = reader.nextBoolean();
                            }
                        }
                        reader.endObject();

                        recipe.m_Items.add(item);
                    }
                }

                reader.endObject();

                m_Recipies.put(name, recipe);
            }
        }

        reader.endObject();
    }
}
