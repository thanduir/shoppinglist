package ch.phwidmer.einkaufsliste;

import android.os.Parcel;
import android.os.Parcelable;

class ShoppingListItem implements Parcelable
{
    public enum Status
    {
        None,
        Taken
    }

    Status           m_Status = Status.None;

    String           m_Ingredient;
    Amount           m_Amount;
    String           m_AdditionalInfo = "";
    RecipeItem.Size  m_Size = RecipeItem.Size.Normal;
    Boolean          m_Optional = false;

    ShoppingListItem()
    {
        m_Amount = new Amount();
    }

    ShoppingListItem(RecipeItem recipeItem)
    {
        m_Amount = new Amount(recipeItem.m_Amount);
        m_Ingredient = recipeItem.m_Ingredient;
        m_Optional = recipeItem.m_Optional;
        m_AdditionalInfo = recipeItem.m_AdditionalInfo;
        m_Size = recipeItem.m_Size;
    }

    void invertStatus()
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

    // Parcelable

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeInt(m_Status.ordinal());

        out.writeString(m_Ingredient);

        out.writeFloat(m_Amount.m_QuantityMin);
        out.writeFloat(m_Amount.m_QuantityMax);
        out.writeInt(m_Amount.m_Unit.ordinal());

        out.writeInt(m_Size.ordinal());
        out.writeInt(m_Optional ? 1 : 0);

        out.writeString(m_AdditionalInfo);
    }

    private ShoppingListItem(Parcel in)
    {
        m_Status = Status.values()[in.readInt()];

        m_Ingredient = in.readString();

        m_Amount = new Amount();
        m_Amount.m_QuantityMin = in.readFloat();
        m_Amount.m_QuantityMax = in.readFloat();
        m_Amount.m_Unit = Amount.Unit.values()[in.readInt()];

        m_Size = RecipeItem.Size.values()[in.readInt()];
        m_Optional = in.readInt() == 1;

        m_AdditionalInfo = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<ShoppingListItem> CREATOR
            = new Parcelable.Creator<ShoppingListItem>() {

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
