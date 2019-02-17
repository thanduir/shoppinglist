package ch.phwidmer.einkaufsliste;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.StateSet;

public class ShoppingListItem implements Parcelable {
    public enum Status
    {
        None,
        Taken;
    }

    public Status           m_Status = Status.None;

    public String           m_Ingredient;
    public Amount           m_Amount;
    public RecipeItem.Size  m_Size = RecipeItem.Size.Normal;
    public Boolean          m_Optional = false;

    public ShoppingListItem()
    {
        m_Amount = new Amount();
    }

    public void invertStatus()
    {
        if(m_Status == Status.None)
        {
            m_Status = Status.Taken;
        }
        else
        {
            m_Status = Status.None;
        }
    }

    // Parceling

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeInt(m_Status.ordinal());

        out.writeString(m_Ingredient);

        out.writeFloat(m_Amount.m_Quantity);
        out.writeInt(m_Amount.m_Unit.ordinal());

        out.writeInt(m_Size.ordinal());
        out.writeInt(m_Optional ? 1 : 0);
    }

    private ShoppingListItem(Parcel in)
    {
        m_Status = Status.values()[in.readInt()];

        m_Ingredient = in.readString();

        m_Amount = new Amount();
        m_Amount.m_Quantity = in.readFloat();
        m_Amount.m_Unit = Amount.Unit.values()[in.readInt()];

        m_Size = RecipeItem.Size.values()[in.readInt()];
        m_Optional = in.readInt() == 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ShoppingListItem> CREATOR
            = new Parcelable.Creator<ShoppingListItem>()
    {
        @Override
        public ShoppingListItem createFromParcel(Parcel in) {
            return new ShoppingListItem(in);
        }

        @Override
        public ShoppingListItem[] newArray(int size) {
            return new ShoppingListItem[size];
        }
    };
}
