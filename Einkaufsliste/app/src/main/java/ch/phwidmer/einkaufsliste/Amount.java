package ch.phwidmer.einkaufsliste;

import android.content.Context;

public class Amount {
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
    }

    public Float m_Quantity = 1.0f;
    public Unit m_Unit = Unit.Count;

    public void scaleAmount(float fFactor)
    {
        switch(m_Unit)
        {
            case Unitless:
            {
                break;
            }

            default:
            {
                m_Quantity *= fFactor;
            }
        }
    }

    public static String toUIString(Context context, Unit unit)
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

    public static String shortForm(Context context, Unit unit)
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
    public static String shortFormAsPrefix(Context context, Unit unit)
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

    public static Amount addUp(Amount m1, Amount m2)
    {
        if(!canBeAddedUp(m1, m2))
        {
            return null;
        }

        Amount result = new Amount();

        switch(m1.m_Unit)
        {
            case Count:
            case Dessertspoon:
            case Teaspoon:
            case Unitless:
            {
                result.m_Quantity = m1.m_Quantity + m2.m_Quantity;
                result.m_Unit = m1.m_Unit;
                break;
            }

            case Kilogram:
            case Gram:
            {
                result.m_Quantity = m1.m_Unit == Unit.Kilogram ? m1.m_Quantity * 1000.0f : m1.m_Quantity;
                result.m_Quantity += m2.m_Unit == Unit.Kilogram ? m2.m_Quantity * 1000.0f : m2.m_Quantity;

                if(result.m_Quantity >= 1000.0f)
                {
                    result.m_Quantity /= 1000.0f;
                    result.m_Unit = Unit.Kilogram;
                }
                else
                {
                    result.m_Unit = Unit.Gram;
                }
                break;
            }

            case Liter:
            case Deciliter:
            case Milliliter:
            {
                float value1 = m1.m_Quantity;
                if(m1.m_Unit == Unit.Liter)
                {
                    value1 *= 1000.0f;
                }
                else if(m1.m_Unit == Unit.Deciliter)
                {
                    value1 *= 100.0f;
                }

                float value2 = m2.m_Quantity;
                if(m2.m_Unit == Unit.Liter)
                {
                    value2 *= 1000.0f;
                }
                else if(m2.m_Unit == Unit.Deciliter)
                {
                    value2 *= 100.0f;
                }

                result.m_Quantity = value1 + value2;
                if(result.m_Quantity >= 1000.0f)
                {
                    result.m_Quantity /= 1000.0f;
                    result.m_Unit = Unit.Liter;
                }
                else if(result.m_Quantity >= 10.0f)
                {
                    result.m_Quantity /= 100.0f;
                    result.m_Unit = Unit.Deciliter;
                }
                else
                {
                    result.m_Unit = Unit.Milliliter;
                }
                break;
            }

            default:
                throw new IllegalArgumentException();
        }
        return result;
    }

    public static boolean canBeAddedUp(Amount m1, Amount m2)
    {
        switch(m1.m_Unit)
        {
            case Count:
                return m2.m_Unit == Unit.Count;

            case Kilogram:
            case Gram:
                return m2.m_Unit == Unit.Kilogram || m2.m_Unit == Unit.Gram;

            case Liter:
            case Deciliter:
            case Milliliter:
                return m2.m_Unit == Unit.Liter || m2.m_Unit == Unit.Deciliter || m2.m_Unit == Unit.Milliliter;

            case Dessertspoon:
                return m2.m_Unit == Unit.Dessertspoon;

            case Teaspoon:
                return m2.m_Unit == Unit.Teaspoon;

            case Unitless:
                return m2.m_Unit == Unit.Unitless;

            default:
                throw new IllegalArgumentException();
        }
    }
}
