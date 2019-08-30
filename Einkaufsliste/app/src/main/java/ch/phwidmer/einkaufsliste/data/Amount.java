package ch.phwidmer.einkaufsliste.data;

import android.content.Context;

import ch.phwidmer.einkaufsliste.R;

public class Amount {
    private static float QUANTITY_UNUSED = -1;

    public enum Unit {
        Count,

        Kilogram,
        Gram,

        Liter,
        Deciliter,
        Milliliter,

        Dessertspoon,
        Teaspoon,

        Unitless
    }

    public float m_QuantityMin;
    public float m_QuantityMax;
    public Unit m_Unit;

    public Amount()
    {
        m_QuantityMin = 1.0f;
        m_QuantityMax = QUANTITY_UNUSED;
        m_Unit = Unit.Count;
    }

    public Amount(Amount other)
    {
        m_QuantityMin = other.m_QuantityMin;
        m_QuantityMax = other.m_QuantityMax;
        m_Unit = other.m_Unit;
    }

    public boolean isRange()
    {
        return m_QuantityMax != QUANTITY_UNUSED;
    }

    public void setIsRange(boolean bIsRange)
    {
        if(bIsRange == isRange())
        {
            return;
        }

        if(bIsRange)
        {
            m_QuantityMax = m_QuantityMin;
        }
        else
        {
            m_QuantityMax = QUANTITY_UNUSED;
        }
    }

    void scaleAmount(float fFactor)
    {
        if(m_Unit == Unit.Unitless || fFactor < 0.0f)
        {
            return;
        }
        m_QuantityMin *= fFactor;
        if(m_QuantityMax != QUANTITY_UNUSED)
        {
            m_QuantityMax *= fFactor;
        }
    }

    public void increaseAmountMin()
    {
        m_QuantityMin += getChangeAmount(m_QuantityMin);
    }

    public void decreaseAmountMin()
    {
        float changeAmount = getChangeAmount(m_QuantityMin);
        if(m_QuantityMin > changeAmount)
        {
            m_QuantityMin -= changeAmount;
        }
        else
        {
            m_QuantityMin = 0;
        }
    }

    public void increaseAmountMax()
    {
        if(m_QuantityMax == QUANTITY_UNUSED)
        {
            return;
        }
        m_QuantityMax += getChangeAmount(m_QuantityMax);
    }

    public void decreaseAmountMax()
    {
        if(m_QuantityMax == QUANTITY_UNUSED)
        {
            return;
        }

        float changeAmount = getChangeAmount(m_QuantityMax);
        if(m_QuantityMax > changeAmount)
        {
            m_QuantityMax -= changeAmount;
        }
        else
        {
            m_QuantityMax = 0;
        }
    }

    private float getChangeAmount(float quantity)
    {
        if(m_Unit == Unit.Unitless)
        {
            return 0f;
        }

        if(quantity == 0.0f)
        {
            return 1.0f;
        }
        else if(quantity < 0.01f)
        {
            return 0.001f;
        }
        else if(quantity < 0.1f)
        {
            return 0.01f;
        }
        else if(quantity < 1.0f)
        {
            return 0.1f;
        }
        else if(quantity < 10.0f)
        {
            return 1.0f;
        }
        else if(quantity < 100.0f)
        {
            return 10.0f;
        }
        else if(quantity < 1000.0f)
        {
            return 50.0f;
        }
        else
        {
            return 100.0f;
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

    static Amount addUp(Amount m1, Amount m2)
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
                result.m_QuantityMin = m1.m_QuantityMin + m2.m_QuantityMin;
                result.m_Unit = m1.m_Unit;
                break;
            }

            case Kilogram:
            case Gram:
            {
                result.m_QuantityMin = m1.m_Unit == Unit.Kilogram ? m1.m_QuantityMin * 1000.0f : m1.m_QuantityMin;
                result.m_QuantityMin += m2.m_Unit == Unit.Kilogram ? m2.m_QuantityMin * 1000.0f : m2.m_QuantityMin;

                if(result.m_QuantityMin >= 1000.0f)
                {
                    result.m_QuantityMin /= 1000.0f;
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
                float value1 = m1.m_QuantityMin;
                if(m1.m_Unit == Unit.Liter)
                {
                    value1 *= 1000.0f;
                }
                else if(m1.m_Unit == Unit.Deciliter)
                {
                    value1 *= 100.0f;
                }

                float value2 = m2.m_QuantityMin;
                if(m2.m_Unit == Unit.Liter)
                {
                    value2 *= 1000.0f;
                }
                else if(m2.m_Unit == Unit.Deciliter)
                {
                    value2 *= 100.0f;
                }

                result.m_QuantityMin = value1 + value2;
                if(result.m_QuantityMin >= 1000.0f)
                {
                    result.m_QuantityMin /= 1000.0f;
                    result.m_Unit = Unit.Liter;
                }
                else if(result.m_QuantityMin >= 10.0f)
                {
                    result.m_QuantityMin /= 100.0f;
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

    static boolean canBeAddedUp(Amount m1, Amount m2)
    {
        if(m1.isRange() || m2.isRange())
        {
            return false;
        }

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
