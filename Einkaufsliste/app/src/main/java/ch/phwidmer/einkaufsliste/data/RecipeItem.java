package ch.phwidmer.einkaufsliste.data;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import ch.phwidmer.einkaufsliste.R;

public class RecipeItem implements Parcelable
{
    public enum Size
    {
        Small,
        Normal,
        Large
    }

    public String  m_Ingredient;
    public Amount  m_Amount;
    public String  m_AdditionalInfo = "";
    public Size    m_Size = Size.Normal;
    public Boolean m_Optional = false;

    public RecipeItem()
    {
        m_Amount = new Amount();
    }

    RecipeItem(RecipeItem other)
    {
        m_Ingredient = other.m_Ingredient;
        m_Amount = new Amount(other.m_Amount);
        m_AdditionalInfo = other.m_AdditionalInfo;
        m_Size = other.m_Size;
        m_Optional = other.m_Optional;
    }

    public static String toUIString(Context context, Size size)
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
        out.writeString(m_AdditionalInfo);

        // Amount
        out.writeFloat(m_Amount.m_QuantityMin);
        out.writeFloat(m_Amount.m_QuantityMax);
        out.writeInt(m_Amount.m_Unit.ordinal());
    }

    private RecipeItem(Parcel in)
    {
        m_Ingredient = in.readString();
        m_Size = Size.values()[in.readInt()];
        m_Optional = in.readInt() == 1;
        m_AdditionalInfo = in.readString();

        // Amount
        m_Amount = new Amount();
        m_Amount.m_QuantityMin = in.readFloat();
        m_Amount.m_QuantityMax = in.readFloat();
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