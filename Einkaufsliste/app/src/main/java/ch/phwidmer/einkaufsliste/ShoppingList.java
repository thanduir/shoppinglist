package ch.phwidmer.einkaufsliste;

import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Vector;

class ShoppingList
{
    class ShoppingRecipe
    {
        Float                           m_fScalingFactor; // Current scaling factor used for the items in the list.
        LinkedList<ShoppingListItem>    m_Items = new LinkedList<>();

        void changeScalingFactor(float f)
        {
            float fFactor = f / m_fScalingFactor;
            m_fScalingFactor = f;

            for(ShoppingListItem sli : m_Items)
            {
                sli.m_Amount.scaleAmount(fFactor);
            }
        }
    }
    private LinkedHashMap<String, ShoppingRecipe> m_Items;
    private String                                m_CurrentSortOrder;

    ShoppingList()
    {
        m_Items = new LinkedHashMap<>();
        m_CurrentSortOrder = "";
    }

    void setCurrentSortOrder(String strOrder)
    {
        m_CurrentSortOrder = strOrder;
    }

    String getCurrentSortOrder()
    {
        return m_CurrentSortOrder;
    }

    void addFromRecipe(String strName, Recipes.Recipe recipe)
    {
        if(m_Items.containsKey(strName))
        {
            return;
        }

        ShoppingRecipe item = new ShoppingRecipe();
        item.m_fScalingFactor = (float)recipe.m_NumberOfPersons;
        for(RecipeItem r : recipe.m_Items)
        {
            ShoppingListItem li = new ShoppingListItem();
            li.m_Amount = r.m_Amount;
            li.m_Ingredient = r.m_Ingredient;
            li.m_Optional = r.m_Optional;
            li.m_Size = r.m_Size;
            item.m_Items.add(li);
        }
        m_Items.put(strName, item);
    }

    ShoppingRecipe getShoppingRecipe(String strName)
    {
        return m_Items.get(strName);
    }

    Vector<String> getAllShoppingRecipes()
    {
        Vector<String> vec = new Vector<>();
        for(Object obj : m_Items.keySet())
        {
            String str = (String)obj;
            vec.add(str);
        }
        return vec;
    }

    void removeShoppingRecipe(String strName)
    {
        m_Items.remove(strName);
    }

    void addExistingShoppingRecipe(String strName, ShoppingRecipe recipe)
    {
        if(m_Items.containsKey(strName))
        {
            return;
        }

        m_Items.put(strName, recipe);
    }

    boolean isIngredientInUse(String strIngredient)
    {
        for(ShoppingRecipe sr : m_Items.values())
        {
            for(ShoppingListItem sli : sr.m_Items)
            {
                if (sli.m_Ingredient.equals(strIngredient))
                {
                    return true;
                }
            }
        }
        return false;
    }

    void onIngredientRenamed(String strIngredient, String strNewName)
    {
        for(ShoppingRecipe sr : m_Items.values())
        {
            for(ShoppingListItem sli : sr.m_Items)
            {
                if (sli.m_Ingredient.equals(strIngredient))
                {
                    sli.m_Ingredient = strNewName;
                }
            }
        }
    }

    // Serializing

    void saveToJson(JsonWriter writer) throws IOException
    {
        writer.beginObject();
        writer.name("id").value("Shoppinglist");
        writer.name("currentSortOrder").value(m_CurrentSortOrder);

        for(LinkedHashMap.Entry<String, ShoppingRecipe> e : m_Items.entrySet())
        {
            writer.name(e.getKey());
            writer.beginObject();
            writer.name("ScalingFactor").value(e.getValue().m_fScalingFactor);
            for(ShoppingListItem si : e.getValue().m_Items)
            {
                writer.name(si.m_Ingredient);
                writer.beginObject();

                writer.name("status").value(si.m_Status.toString());

                writer.name("amount");
                writer.beginArray();
                writer.value(si.m_Amount.m_Quantity);
                writer.value(si.m_Amount.m_Unit.toString());
                writer.endArray();

                writer.name("size").value(si.m_Size.toString());
                writer.name("optional").value(si.m_Optional);

                writer.endObject();
            }
            writer.endObject();
        }

        writer.endObject();
    }

    void readFromJson(JsonReader reader, int iVersion) throws IOException
    {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("id"))
            {
                String id = reader.nextString();
                if (!id.equals("Shoppinglist"))
                {
                    throw new IOException();
                }
            }
            else if(name.equals("currentSortOrder"))
            {
                m_CurrentSortOrder = reader.nextString();
            }
            else
            {
                ShoppingRecipe recipe = new ShoppingRecipe();

                reader.beginObject();
                while (reader.hasNext())
                {
                    String currentName = reader.nextName();
                    if (currentName.equals("ScalingFactor"))
                    {
                        recipe.m_fScalingFactor = (float)reader.nextDouble();
                    }
                    else
                    {
                        ShoppingListItem item = new ShoppingListItem();
                        item.m_Ingredient = currentName;

                        reader.beginObject();
                        while (reader.hasNext())
                        {
                            String itemName = reader.nextName();
                            if(itemName.equals("status"))
                            {
                                String str = reader.nextString();
                                item.m_Status = ShoppingListItem.Status.valueOf(str);
                            }
                            else if(itemName.equals("amount"))
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

                m_Items.put(name, recipe);
            }
        }

        reader.endObject();
    }
}
