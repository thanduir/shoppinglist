package ch.phwidmer.einkaufsliste;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.LinkedHashMap;
import java.util.LinkedList;

public class Recipes implements Parcelable {

    private class Recipe {
        private Integer m_NumberOfPersons;
        private LinkedList<RecipeItem> m_Items;
    }
    private LinkedHashMap<String, Recipe> m_Recipies;

    public Recipes()
    {
        m_Recipies = new LinkedHashMap<String, Recipe>();

        // TODO: Ein paar Beispiele hinzufügen!
    }

    // Parceling

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        // TODO
        out.writeInt(m_Recipies.size());
        for(LinkedHashMap.Entry<String, Recipe> e : m_Recipies.entrySet())
        {
            out.writeString(e.getKey());
            out.writeInt(e.getValue().m_NumberOfPersons);

            out.writeInt(e.getValue().m_Items.size());
            for(Object obj : e.getValue().m_Items.toArray())
            {
                RecipeItem recipe = (RecipeItem)obj;
                out.writeParcelable(recipe, flags);
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
