package ch.phwidmer.einkaufsliste;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Vector;

public class Recipes implements Parcelable {

    public class Recipe {
        public Integer m_NumberOfPersons = 4;
        public LinkedList<RecipeItem> m_Items = new LinkedList<RecipeItem>();
    }
    private LinkedHashMap<String, Recipe> m_Recipies;

    public Recipes()
    {
        m_Recipies = new LinkedHashMap<String, Recipe>();
    }

    public void addRecipe(String strName)
    {
        if(m_Recipies.containsKey(strName))
        {
            return;
        }
        Recipe recipe = new Recipe();
        m_Recipies.put(strName, recipe);
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
        return vec;
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

    // Parceling

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeInt(m_Recipies.size());
        for(LinkedHashMap.Entry<String, Recipe> e : m_Recipies.entrySet())
        {
            out.writeString(e.getKey());
            out.writeInt(e.getValue().m_NumberOfPersons);

            out.writeInt(e.getValue().m_Items.size());
            for(Object obj : e.getValue().m_Items.toArray())
            {
                RecipeItem recipe = (RecipeItem)obj;
                recipe.writeToParcel(out, flags);
            }
        }
    }

    private Recipes(Parcel in)
    {
        int size = in.readInt();
        m_Recipies = new LinkedHashMap<String, Recipe>(size);
        for(int i = 0; i < size; i++)
        {
            String strName = in.readString();
            Recipe recipe = new Recipe();
            recipe.m_NumberOfPersons = in.readInt();

            int sizeItem = in.readInt();
            for(int j = 0; j < sizeItem; j++)
            {
                RecipeItem item = RecipeItem.CREATOR.createFromParcel(in);
                recipe.m_Items.add(item);
            }
            m_Recipies.put(strName, recipe);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Recipes> CREATOR
            = new Parcelable.Creator<Recipes>()
    {
        @Override
        public Recipes createFromParcel(Parcel in) {
            return new Recipes(in);
        }

        @Override
        public Recipes[] newArray(int size) {
            return new Recipes[size];
        }
    };
}
