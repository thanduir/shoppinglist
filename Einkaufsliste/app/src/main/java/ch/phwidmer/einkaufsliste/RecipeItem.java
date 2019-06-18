package ch.phwidmer.einkaufsliste;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

public class RecipeItem implements Parcelable
{
    public enum Size
    {
        Small,
        Normal,
        Large
    }

    String   m_Ingredient;
    Amount   m_Amount;
    Size     m_Size = Size.Normal;
    Boolean  m_Optional = false;

    RecipeItem()
    {
        m_Amount = new Amount();
    }

    RecipeItem(RecipeItem other)
    {
        m_Ingredient = other.m_Ingredient;
        m_Amount = new Amount(other.m_Amount);
        m_Size = other.m_Size;
        m_Optional = other.m_Optional;
    }

    static String toUIString(Context context, Size size)
    {
        switch(size)
        {
            case Normal:
                return context.getResources().getString(R.string.size_normal);

            case Small:
                return context.getResources().getString(R.string.size_small);

            case Large:
                return context.getResources().getString(R.string.size_large);

            default:
                return size.toString();
        }
    }

    // Parcelable

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
            = new Parcelable.Creator<RecipeItem>() {

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
