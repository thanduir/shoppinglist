package ch.phwidmer.einkaufsliste;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

class Ingredients implements Parcelable
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
        i.m_Category = m_Categories.getCategory(m_Categories.getAllCategories().get(0));
        m_Ingredients.put(strName, i);
    }

    Ingredient getIngredient(String strName)
    {
        return m_Ingredients.get(strName);
    }

    int getIngredientsCount() { return m_Ingredients.size(); }

    ArrayList<String> getAllIngredients()
    {
        return new ArrayList<>(m_Ingredients.keySet());
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
                            in.m_Category = m_Categories.getCategory(reader.nextString());
                            break;
                        }


                        case "provenance":
                        {
                            in.m_strProvenance = reader.nextString();
                            if(m_Categories.getSortOrder(in.m_strProvenance) == null && !in.m_strProvenance.equals(c_strProvenanceEverywhere))
                            {
                                // Provenance doesn't exist as a SortOrder -> set default provenance.
                                in.m_strProvenance = c_strProvenanceEverywhere;
                            }
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
            out.writeString(e.getValue().m_Category.getName());
            out.writeString(e.getValue().m_strProvenance);
            out.writeInt(e.getValue().m_DefaultUnit.ordinal());
        }
    }

    Ingredients(Parcel in, Categories categories)
    {
        m_Categories = categories;

        int size = in.readInt();
        m_Ingredients = new TreeMap<>(new Helper.SortIgnoreCase());
        for(int i = 0; i < size; i++)
        {
            String strName = in.readString();
            Ingredient order = new Ingredient();
            order.m_Category = m_Categories.getCategory(in.readString());
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
            return new Ingredients(in, new Categories());
        }

        @Override
        public Ingredients[] newArray(int size) {
            return new Ingredients[size];
        }
    };
}
