package ch.phwidmer.einkaufsliste.data;

import android.content.Context;
import android.support.annotation.NonNull;

import ch.phwidmer.einkaufsliste.R;

public interface RecipeItem
{
    enum Size
    {
        Small,
        Normal,
        Large;

        public static String toUIString(@NonNull Context context, @NonNull Size size)
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

    String getIngredient();
    void setIngredient(@NonNull String strIngredient);

    Amount getAmount();
    void setAmount(@NonNull Amount amount);

    String getAdditionalInfo();
    void setAdditionInfo(@NonNull String additionalInfo);

    Size getSize();
    void setSize(@NonNull Size size);

    boolean isOptional();
    void setIsOptional(boolean optional);
}
