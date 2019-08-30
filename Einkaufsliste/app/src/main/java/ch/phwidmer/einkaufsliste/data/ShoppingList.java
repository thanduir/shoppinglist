package ch.phwidmer.einkaufsliste.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class ShoppingList implements Parcelable
{
    public class ShoppingRecipe
    {
        public Float                           m_fScalingFactor; // Current scaling factor used for the items in the list.
        public LinkedList<ShoppingListItem>    m_Items = new LinkedList<>();

        public void changeScalingFactor(float f)
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

    public ShoppingList()
    {
        m_Items = new LinkedHashMap<>();
        m_CurrentSortOrder = "";
    }

    public void setCurrentSortOrder(String strOrder)
    {
        m_CurrentSortOrder = strOrder;
    }

    public String getCurrentSortOrder()
    {
        return m_CurrentSortOrder;
    }

    public void addFromRecipe(String strName, Recipes.Recipe recipe)
    {
        if(m_Items.containsKey(strName))
        {
            return;
        }

        ShoppingRecipe item = new ShoppingRecipe();
        item.m_fScalingFactor = (float)recipe.m_NumberOfPersons;
        for(RecipeItem r : recipe.m_Items)
        {
            ShoppingListItem li = new ShoppingListItem(r);
            item.m_Items.add(li);
        }
        m_Items.put(strName, item);
    }

    public ShoppingRecipe getShoppingRecipe(String strName)
    {
        return m_Items.get(strName);
    }

    public ArrayList<String> getAllShoppingRecipes()
    {
        return new ArrayList<>(m_Items.keySet());
    }

    public void removeShoppingRecipe(String strName)
    {
        m_Items.remove(strName);
    }

    public void renameRecipe(String strRecipe, String strNewName)
    {
        if(!m_Items.containsKey(strRecipe))
        {
            return;
        }

        ShoppingRecipe recipe = m_Items.get(strRecipe);
        m_Items.remove(strRecipe);
        m_Items.put(strNewName, recipe);
    }

    public void addExistingShoppingRecipe(String strName, ShoppingRecipe recipe)
    {
        if(m_Items.containsKey(strName))
        {
            return;
        }

        m_Items.put(strName, recipe);
    }

    public boolean isIngredientInUse(String strIngredient, @NonNull ArrayList<String> shoppingListItemUsingIngredient)
    {
        boolean stillInUse = false;
        for(LinkedHashMap.Entry<String, ShoppingRecipe> e : m_Items.entrySet())
        {
            for(ShoppingListItem sli : e.getValue().m_Items)
            {
                if (sli.m_Ingredient.equals(strIngredient))
                {
                    if(!shoppingListItemUsingIngredient.contains(e.getKey()))
                    {
                        shoppingListItemUsingIngredient.add(e.getKey());
                    }
                    stillInUse = true;
                }
            }
        }
        return stillInUse;
    }

    public void onIngredientRenamed(String strIngredient, String strNewName)
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

    boolean checkDataConsistency(Ingredients ingredients, LinkedList<String> missingIngredients)
    {
        boolean dataConsistent = true;
        for(LinkedHashMap.Entry<String, ShoppingRecipe> e : m_Items.entrySet())
        {
            for(ShoppingListItem li : e.getValue().m_Items)
            {
                String ingredient = li.m_Ingredient;
                if(ingredients.getIngredient(ingredient) == null)
                {
                    if(!missingIngredients.contains(ingredient))
                    {
                        missingIngredients.add(ingredient);
                    }
                    dataConsistent = false;
                }
            }
        }
        return dataConsistent;
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

                writer.name("amountMinMax");
                writer.beginArray();
                writer.value(si.m_Amount.m_QuantityMin);
                writer.value(si.m_Amount.m_QuantityMax);
                writer.value(si.m_Amount.m_Unit.toString());
                writer.endArray();

                writer.name("size").value(si.m_Size.toString());
                writer.name("optional").value(si.m_Optional);

                writer.name("additionalInfo").value(si.m_AdditionalInfo);

                writer.endObject();
            }
            writer.endObject();
        }

        writer.endObject();
    }

    void readFromJson(JsonReader reader) throws IOException
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
                            switch(itemName)
                            {
                                case "status":
                                {
                                    String str = reader.nextString();
                                    item.m_Status = ShoppingListItem.Status.valueOf(str);
                                    break;
                                }

                                case "amount":
                                {
                                    reader.beginArray();

                                    item.m_Amount.m_QuantityMin = (float)reader.nextDouble();
                                    String str = reader.nextString();
                                    item.m_Amount.m_Unit = Amount.Unit.valueOf(str);

                                    reader.endArray();
                                    break;
                                }

                                case "amountMinMax":
                                {
                                    reader.beginArray();

                                    item.m_Amount.m_QuantityMin = (float)reader.nextDouble();
                                    item.m_Amount.m_QuantityMax = (float) reader.nextDouble();
                                    String str = reader.nextString();
                                    item.m_Amount.m_Unit = Amount.Unit.valueOf(str);

                                    reader.endArray();
                                    break;
                                }

                                case "size":
                                {
                                    String size = reader.nextString();
                                    item.m_Size = RecipeItem.Size.valueOf(size);
                                    break;
                                }

                                case "optional":
                                {
                                    item.m_Optional = reader.nextBoolean();
                                    break;
                                }

                                case "additionalInfo":
                                {
                                    item.m_AdditionalInfo = reader.nextString();
                                    break;
                                }
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

    // Parcelable

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeString(m_CurrentSortOrder);

        out.writeInt(m_Items.size());
        for(LinkedHashMap.Entry<String, ShoppingRecipe> e : m_Items.entrySet())
        {
            out.writeString(e.getKey());
            out.writeFloat(e.getValue().m_fScalingFactor);

            out.writeInt(e.getValue().m_Items.size());
            for(ShoppingListItem item : e.getValue().m_Items)
            {
                item.writeToParcel(out, flags);
            }
        }
    }

    private ShoppingList(Parcel in)
    {
        m_CurrentSortOrder = in.readString();

        int size = in.readInt();
        m_Items = new LinkedHashMap<>();
        for(int i = 0; i < size; i++)
        {
            String strName = in.readString();
            ShoppingRecipe recipe = new ShoppingRecipe();
            recipe.m_fScalingFactor = in.readFloat();

            int sizeItem = in.readInt();
            for(int j = 0; j < sizeItem; j++)
            {
                ShoppingListItem item = ShoppingListItem.CREATOR.createFromParcel(in);
                recipe.m_Items.add(item);
            }
            m_Items.put(strName, recipe);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ShoppingList> CREATOR
            = new Parcelable.Creator<ShoppingList>() {

        @Override
        public ShoppingList createFromParcel(Parcel in) {
            return new ShoppingList(in);
        }

        @Override
        public ShoppingList[] newArray(int size) {
            return new ShoppingList[size];
        }
    };
}
