package ch.phwidmer.einkaufsliste.data;

import android.content.Context;

import ch.phwidmer.einkaufsliste.R;

public interface RecipeItem
{
    enum Size
    {
        Small,
        Normal,
        Large
    }

    String getIngredient();
    void setIngredient(String strIngredient);

    Amount getAmount();
    void setAmount(Amount amount);

    String getAdditionalInfo();
    void setAdditionInfo(String additionalInfo);

    Size getSize();
    void setSize(Size size);

    boolean isOptional();
    void setIsOptional(boolean optional);

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
