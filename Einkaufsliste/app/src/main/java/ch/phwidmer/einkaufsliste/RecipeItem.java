package ch.phwidmer.einkaufsliste;

import android.os.Parcel;
import android.os.Parcelable;

public class RecipeItem implements Parcelable {
    public enum Size
    {
        Normal,
        Small,
        Large;
    }

    public String   m_Ingredient;
    public Amount   m_Amount;
    public Size     m_Size = Size.Normal;
    public Boolean  m_Optional = false;

    public RecipeItem()
    {
        m_Amount = new Amount();
    }

    // Parceling

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeString(m_Ingredient);
        out.writeInt(m_Size.ordinal());
        out.writeInt(m_Optional ? 1 : 0);

        // Amount
        out.writeFloat(m_Amount.m_Quantity);
        out.writeInt(m_Amount.m_Unit.ordinal());
    }

    private RecipeItem(Parcel in)
    {
        m_Ingredient = in.readString();
        m_Size = Size.values()[in.readInt()];
        m_Optional = in.readInt() == 1;

        // Amount
        m_Amount = new Amount();
        m_Amount.m_Quantity = in.readFloat();
        m_Amount.m_Unit = Amount.Unit.values()[in.readInt()];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<RecipeItem> CREATOR
            = new Parcelable.Creator<RecipeItem>()
    {
        @Override
        public RecipeItem createFromParcel(Parcel in) {
            return new RecipeItem(in);
        }

        @Override
        public RecipeItem[] newArray(int size) {
            return new RecipeItem[size];
        }
    };
}
