package ch.phwidmer.einkaufsliste.data;

import android.content.Context;
import android.support.annotation.NonNull;

import ch.phwidmer.einkaufsliste.R;

public enum Unit {
    Count,

    Kilogram,
    Gram,

    Liter,
    Deciliter,
    Milliliter,

    Dessertspoon,
    Teaspoon,

    Unitless;

    public static String toUIString(@NonNull Context context, @NonNull Unit unit)
    {
        switch(unit)
        {
            case Count:
                return context.getResources().getString(R.string.unit_count_long);

            case Kilogram:
                return context.getResources().getString(R.string.unit_kilogram_long);
            case Gram:
                return context.getResources().getString(R.string.unit_gram_long);

            case Liter:
                return context.getResources().getString(R.string.unit_liter_long);
            case Deciliter:
                return context.getResources().getString(R.string.unit_deciliter_long);
            case Milliliter:
                return context.getResources().getString(R.string.unit_mililiter_long);

            case Dessertspoon:
                return context.getResources().getString(R.string.unit_dessertspoon_long);
            case Teaspoon:
                return context.getResources().getString(R.string.unit_teaspoon_long);

            case Unitless:
                return context.getResources().getString(R.string.unit_unitless_long);

            default:
                return unit.toString();
        }
    }

    public static String shortForm(@NonNull Context context, @NonNull Unit unit)
    {
        switch(unit)
        {
            case Count:
                return context.getResources().getString(R.string.unit_count_short);

            case Kilogram:
                return context.getResources().getString(R.string.unit_kilogram_short);
            case Gram:
                return context.getResources().getString(R.string.unit_gram_short);

            case Liter:
                return context.getResources().getString(R.string.unit_liter_short);
            case Deciliter:
                return context.getResources().getString(R.string.unit_deciliter_short);
            case Milliliter:
                return context.getResources().getString(R.string.unit_mililiter_short);

            case Dessertspoon:
                return context.getResources().getString(R.string.unit_dessertspoon_short);
            case Teaspoon:
                return context.getResources().getString(R.string.unit_teaspoon_short);

            case Unitless:
                return context.getResources().getString(R.string.unit_unitless_short);

            default:
                return unit.toString();
        }
    }

    // asPrefix: Is it used in front of the corresponding item? Then we don't need to write "1 Piece Apple", but can use another form.
    public static String shortFormAsPrefix(@NonNull Context context, @NonNull Unit unit)
    {
        switch(unit)
        {
            case Count:
                return context.getResources().getString(R.string.unit_count_short_prefix);

            case Kilogram:
                return context.getResources().getString(R.string.unit_kilogram_short_prefix);
            case Gram:
                return context.getResources().getString(R.string.unit_gram_short_prefix);

            case Liter:
                return context.getResources().getString(R.string.unit_liter_short_prefix);
            case Deciliter:
                return context.getResources().getString(R.string.unit_deciliter_short_prefix);
            case Milliliter:
                return context.getResources().getString(R.string.unit_mililiter_short_prefix);

            case Dessertspoon:
                return context.getResources().getString(R.string.unit_dessertspoon_short_prefix);
            case Teaspoon:
                return context.getResources().getString(R.string.unit_teaspoon_short_prefix);

            case Unitless:
                return context.getResources().getString(R.string.unit_unitless_short_prefix);

            default:
                return unit.toString();
        }
    }
}
