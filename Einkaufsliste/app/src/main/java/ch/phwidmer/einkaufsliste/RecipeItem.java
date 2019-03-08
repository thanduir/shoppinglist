package ch.phwidmer.einkaufsliste;

import android.content.Context;

public class RecipeItem {
    public enum Size
    {
        Small,
        Normal,
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
}
