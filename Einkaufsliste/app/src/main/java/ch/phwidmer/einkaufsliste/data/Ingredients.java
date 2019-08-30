package ch.phwidmer.einkaufsliste.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;

import ch.phwidmer.einkaufsliste.helper.Helper;

public class Ingredients implements Parcelable
{
    public static final String c_strProvenanceEverywhere = "*EVERYWHERE*";

    public class Ingredient
    {
        public String m_Category = "";
        public String m_strProvenance = c_strProvenanceEverywhere;
        public Amount.Unit m_DefaultUnit;
    }
    private TreeMap<String, Ingredient> m_Ingredients;

    Ingredients()
    {
        m_Ingredients = new TreeMap<>(new Helper.SortIgnoreCase());
    }

    public void addIngredient(String strName, Amount.Unit defaultUnit, Categories.Category category)
    {
        if(m_Ingredients.containsKey(strName))
        {
            return;
        }
        Ingredient i = new Ingredient();
        i.m_DefaultUnit = defaultUnit;
        i.m_Category = category.getName();
        m_Ingredients.put(strName, i);
    }

    public Ingredient getIngredient(String strName)
    {
        return m_Ingredients.get(strName);
    }

    public int getIngredientsCount() { return m_Ingredients.size(); }

    public ArrayList<String> getAllIngredients()
    {
        return new ArrayList<>(m_Ingredients.keySet());
    }

    public void removeIngredient(String strName)
    {
        m_Ingredients.remove(strName);
    }

    public void renameIngredient(String strIngredient, String strNewName)
    {
        if(!m_Ingredients.containsKey(strIngredient))
        {
            return;
        }

        Ingredient ingredient = m_Ingredients.get(strIngredient);
        m_Ingredients.remove(strIngredient);
        m_Ingredients.put(strNewName, ingredient);
    }

    public boolean isCategoryInUse(Categories.Category category, @NonNull ArrayList<String> ingredientsUsingCategory)
    {
        boolean stillInUse = false;
        for(TreeMap.Entry<String, Ingredient> e : m_Ingredients.entrySet())
        {
            if(e.getValue().m_Category.equals(category.getName()))
            {
                ingredientsUsingCategory.add(e.getKey());
                stillInUse = true;
            }
        }
        return stillInUse;
    }

    public void onCategoryRenamed(Categories.Category category, Categories.Category newCategory)
    {
        for(Ingredient i : m_Ingredients.values())
        {
            if(i.m_Category.equals(category.getName()))
            {
                i.m_Category = newCategory.getName();
            }
        }
    }

    public boolean isSortOrderInUse(String strSortOrder, @NonNull ArrayList<String> ingredientsUsingSortOrder)
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

    boolean checkDataConsistency(Categories categories, LinkedList<String> missingCategories, LinkedList<String> missingSortOrders)
    {
        boolean dataConsistent = true;

        for(TreeMap.Entry<String, Ingredient> e : m_Ingredients.entrySet())
        {
            String category = e.getValue().m_Category;
            if(categories.getCategory(category) == null)
            {
                if(!missingCategories.contains(category))
                {
                    missingCategories.add(category);
                }
                dataConsistent = false;
            }
            String sortOrder = e.getValue().m_strProvenance;
            if(categories.getSortOrder(sortOrder) == null && !sortOrder.equals(c_strProvenanceEverywhere))
            {
                if(!missingSortOrders.contains(sortOrder))
                {
                    missingSortOrders.add(sortOrder);
                }
                dataConsistent = false;
            }
        }

        return dataConsistent;
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
            writer.name("category").value(e.getValue().m_Category);
            writer.name("provenance").value(e.getValue().m_strProvenance);
            writer.name("default-unit").value(e.getValue().m_DefaultUnit.toString());
            writer.endObject();
        }

        writer.endObject();
    }

    void readFromJson(JsonReader reader) throws IOException
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
                    switch(currentName)
                    {
                        case "category":
                        {
                            in.m_Category = reader.nextString();
                            break;
                        }


                        case "provenance":
                        {
                            in.m_strProvenance = reader.nextString();
                            break;
                        }

                        case "default-unit":
                        {
                            String unit = reader.nextString();
                            in.m_DefaultUnit = Amount.Unit.valueOf(unit);
                            break;
                        }

                        default:
                        {
                            reader.skipValue();
                            break;
                        }
                    }
                }
                reader.endObject();

                m_Ingredients.put(name, in);
            }
        }
        reader.endObject();
    }

    // Parcelable

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeInt(m_Ingredients.size());
        for(TreeMap.Entry<String, Ingredient> e : m_Ingredients.entrySet())
        {
            out.writeString(e.getKey());
            out.writeString(e.getValue().m_Category);
            out.writeString(e.getValue().m_strProvenance);
            out.writeInt(e.getValue().m_DefaultUnit.ordinal());
        }
    }

    private Ingredients(Parcel in)
    {
        int size = in.readInt();
        m_Ingredients = new TreeMap<>(new Helper.SortIgnoreCase());
        for(int i = 0; i < size; i++)
        {
            String strName = in.readString();
            Ingredient order = new Ingredient();
            order.m_Category = in.readString();
            order.m_strProvenance = in.readString();
            order.m_DefaultUnit = Amount.Unit.values()[in.readInt()];
            m_Ingredients.put(strName, order);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Ingredients> CREATOR
            = new Parcelable.Creator<Ingredients>() {

        @Override
        public Ingredients createFromParcel(Parcel in) {
            return new Ingredients(in);
        }

        @Override
        public Ingredients[] newArray(int size) {
            return new Ingredients[size];
        }
    };
}
