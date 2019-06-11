package ch.phwidmer.einkaufsliste;

import android.content.Context;

public class RecipeItem {
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
}
