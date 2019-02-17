package ch.phwidmer.einkaufsliste;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Vector;

public class ShoppingList implements Parcelable
{
    public class ShoppingRecipe
    {
        public Float                           m_fScalingFactor; // Current scaling factor used for the items in the list.
        public LinkedList<ShoppingListItem>    m_Items = new LinkedList<ShoppingListItem>();
    }
    private LinkedHashMap<String, ShoppingRecipe> m_Items;

    public ShoppingList()
    {
        m_Items = new LinkedHashMap<String, ShoppingRecipe>();
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
            ShoppingListItem li = new ShoppingListItem();
            li.m_Amount = r.m_Amount;
            li.m_Ingredient = r.m_Ingredient;
            li.m_Optional = r.m_Optional;
            li.m_Size = r.m_Size;
            item.m_Items.add(li);
        }
        m_Items.put(strName, item);
    }

    public ShoppingRecipe getShoppingRecipe(String strName)
    {
        return m_Items.get(strName);
    }

    public Vector<String> getAllShoppingRecipes()
    {
        Vector<String> vec = new Vector<String>();
        for(Object obj : m_Items.keySet())
        {
            String str = (String)obj;
            vec.add(str);
        }
        return vec;
    }

    public void removeShoppingRecipe(String strName)
    {
        m_Items.remove(strName);
    }

    // Parceling

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeInt(m_Items.size());
        for(LinkedHashMap.Entry<String, ShoppingRecipe> e : m_Items.entrySet())
        {
            out.writeString(e.getKey());
            out.writeFloat(e.getValue().m_fScalingFactor);

            out.writeInt(e.getValue().m_Items.size());
            for(Object obj : e.getValue().m_Items.toArray())
            {
                ShoppingListItem item = (ShoppingListItem)obj;
                item.writeToParcel(out, flags);
            }
        }
    }

    private ShoppingList(Parcel in)
    {
        int size = in.readInt();
        m_Items = new LinkedHashMap<String, ShoppingRecipe>(size);
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
            = new Parcelable.Creator<ShoppingList>()
    {
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
