package ch.phwidmer.einkaufsliste;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.LinkedHashMap;
import java.util.LinkedList;

public class ShoppingList implements Parcelable
{
    public class ShoppingRecipe
    {
        private Float                           m_fScalingFactor; // Current scaling factor used for the items in the list.
        private LinkedList<ShoppingListItem>    m_Items = new LinkedList<ShoppingListItem>();
    }
    private LinkedHashMap<String, ShoppingRecipe> m_Items;

    // TODO: addFromRecipe-Methode (und keine andere "add" Methode, nur change-Methoden
    // TODO: Methode zum Generieren von map<Categories, Ingredient-List>? Oder wie mache ich das, damit es nicht ständig neu generiert wird?

    public ShoppingList()
    {
        m_Items = new LinkedHashMap<String, ShoppingRecipe>();

        // TODO: Will ich hier noch ein oder zwei Test-Daten haben?
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
