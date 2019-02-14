package ch.phwidmer.einkaufsliste;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Vector;

public class Recipes implements Parcelable {

    public class Recipe {
        // TODO: Evtl. sollte dieser Default-Wert (+ evtl. vorhandene andere) in eine Configuration gespeichert werden?
        public Integer m_NumberOfPersons = 4;
        public LinkedList<RecipeItem> m_Items = new LinkedList<RecipeItem>();
    }
    private LinkedHashMap<String, Recipe> m_Recipies;

    public Recipes()
    {
        m_Recipies = new LinkedHashMap<String, Recipe>();

        // TODO: Test-Code hier löschen, nachdem alles soweit fertig ist (und v.a. gespeichert wird!)

        Recipe recipe0 = new Recipe();
        recipe0.m_NumberOfPersons = 2;
        RecipeItem j0 = new RecipeItem();
        j0.m_Ingredient = "Pepper";
        j0.m_Amount.m_Unit = Amount.Unit.Count;
        j0.m_Amount.m_Quantity = 1f;
        j0.m_Optional = true;
        j0.m_Size = RecipeItem.Size.Large;
        recipe0.m_Items.add(j0);
        RecipeItem j1 = new RecipeItem();
        j1.m_Ingredient = "Apple";
        j1.m_Amount.m_Unit = Amount.Unit.Count;
        j1.m_Amount.m_Quantity = 2f;
        j1.m_Optional = false;
        j1.m_Size = RecipeItem.Size.Small;
        recipe0.m_Items.add(j1);
        m_Recipies.put("Diverses", recipe0);

        Recipe recipe1 = new Recipe();
        recipe1.m_NumberOfPersons = 3;
        RecipeItem i0 = new RecipeItem();
        i0.m_Ingredient = "Zopf";
        i0.m_Amount.m_Unit = Amount.Unit.Kilogram;
        i0.m_Amount.m_Quantity = 0.5f;
        i0.m_Optional = true;
        i0.m_Size = RecipeItem.Size.Normal;
        recipe1.m_Items.add(i0);
        RecipeItem i1 = new RecipeItem();
        i1.m_Ingredient = "Milk";
        i1.m_Amount.m_Unit = Amount.Unit.Liter;
        i1.m_Amount.m_Quantity = 1.0f;
        i1.m_Optional = false;
        i1.m_Size = RecipeItem.Size.Normal;
        recipe1.m_Items.add(i1);
        m_Recipies.put("Zopf mit Milch", recipe1);
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
