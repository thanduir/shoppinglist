package ch.phwidmer.einkaufsliste;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;

class Recipes implements Parcelable
{
    private String m_ActiveRecipe;

    class Recipe {
        Integer m_NumberOfPersons = 0;
        LinkedList<RecipeItem> m_Items = new LinkedList<>();
    }
    private TreeMap<String, Recipe> m_Recipies;

    Recipes()
    {
        m_Recipies = new TreeMap<>(new Helper.SortIgnoreCase());
        m_ActiveRecipe = "";
    }

    void setActiveRecipe(String strRecipe)
    {
        m_ActiveRecipe = strRecipe;
    }

    String getActiveRecipe()
    {
        return m_ActiveRecipe;
    }

    void addRecipe(String strName, Integer iNrPersons)
    {
        if(m_Recipies.containsKey(strName))
        {
            return;
        }
        Recipe recipe = new Recipe();
        recipe.m_NumberOfPersons = iNrPersons;
        m_Recipies.put(strName, recipe);
    }

    void addRecipe(String strName, Recipe r)
    {
        if(m_Recipies.containsKey(strName))
        {
            return;
        }
        m_Recipies.put(strName, r);
    }

    Recipe getRecipe(String strName)
    {
        return m_Recipies.get(strName);
    }

    void removeRecipe(String strName)
    {
        m_Recipies.remove(strName);
    }

    ArrayList<String> getAllRecipes()
    {
        return new ArrayList<>(m_Recipies.keySet());
    }

    void renameRecipe(String strRecipe, String strNewName)
    {
        if(!m_Recipies.containsKey(strRecipe))
        {
            return;
        }

        Recipe recipe = m_Recipies.get(strRecipe);
        m_Recipies.remove(strRecipe);
        m_Recipies.put(strNewName, recipe);
    }

    void copyRecipe(String strRecipe, String strNewName)
    {
        Recipe oldRecipe = m_Recipies.get(strRecipe);
        if(oldRecipe == null)
        {
            return;
        }

        Recipe recipe = new Recipe();
        recipe.m_NumberOfPersons = oldRecipe.m_NumberOfPersons;
        for(RecipeItem item : oldRecipe.m_Items)
        {
            recipe.m_Items.add(new RecipeItem(item));
        }
        m_Recipies.put(strNewName, recipe);
    }

    boolean isIngredientInUse(String strIngredient, @NonNull ArrayList<String> recipesUsingIngredient)
    {
        boolean stillInUse = false;
        for(TreeMap.Entry<String, Recipe> e : m_Recipies.entrySet())
        {
            for(RecipeItem ri : e.getValue().m_Items)
            {
                if(ri.m_Ingredient.equals(strIngredient))
                {
                    if(!recipesUsingIngredient.contains(e.getKey()))
                    {
                        recipesUsingIngredient.add(e.getKey());
                    }
                    stillInUse = true;
                }
            }
        }
        return stillInUse;
    }

    void onIngredientRenamed(String strIngredient, String strNewName)
    {
        for(Recipe r : m_Recipies.values())
        {
            for(RecipeItem ri : r.m_Items)
            {
                if (ri.m_Ingredient.equals(strIngredient))
                {
                    ri.m_Ingredient = strNewName;
                }
            }
        }
    }

    // Serializing

    void saveToJson(JsonWriter writer) throws IOException
    {
        writer.beginObject();
        writer.name("id").value("Recipes");
        writer.name("activeRecipe").value(m_ActiveRecipe);

        for(TreeMap.Entry<String, Recipe> e : m_Recipies.entrySet())
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
                writer.name("additionalInfo").value(ri.m_AdditionalInfo);

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
                if (!id.equals("Recipes"))
                {
                    throw new IOException();
                }
            }
            else if(name.equals("activeRecipe"))
            {
                m_ActiveRecipe = reader.nextString();
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
                            switch(itemName)
                            {
                                case "amount":
                                {
                                    reader.beginArray();

                                    item.m_Amount.m_Quantity = (float)reader.nextDouble();
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

                m_Recipies.put(name, recipe);
            }
        }

        reader.endObject();
    }

    // Parcelable

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeInt(m_Recipies.size());
        for(TreeMap.Entry<String, Recipe> e : m_Recipies.entrySet())
        {
            out.writeString(e.getKey());
            out.writeInt(e.getValue().m_NumberOfPersons);

            out.writeInt(e.getValue().m_Items.size());
            for(RecipeItem recipe : e.getValue().m_Items)
            {
                recipe.writeToParcel(out, flags);
            }
        }

        out.writeString(m_ActiveRecipe);
    }

    private Recipes(Parcel in)
    {
        int size = in.readInt();
        m_Recipies = new TreeMap<>(new Helper.SortIgnoreCase());
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

        m_ActiveRecipe = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Recipes> CREATOR
            = new Parcelable.Creator<Recipes>() {

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
