package ch.phwidmer.einkaufsliste.data;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Optional;

import ch.phwidmer.einkaufsliste.R;

public abstract class Amount {

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

    protected abstract Amount createNewInstance();

    public abstract float getQuantityMin();
    public abstract void setQuantityMin(float quantity);

    public abstract float getQuantityMax();
    public abstract void setQuantityMax(float quantity);

    public abstract Amount.Unit getUnit();
    public abstract void setUnit(@NonNull Amount.Unit unit);

    public abstract boolean isRange();
    public abstract void setIsRange(boolean bIsRange);

    void scaleAmount(float fFactor)
    {
        if(getUnit() == Unit.Unitless || fFactor < 0.0f)
        {
            return;
        }

        setQuantityMin(getQuantityMin() * fFactor);
        if(isRange())
        {
            setQuantityMax(getQuantityMax() * fFactor);
        }
    }

    public void increaseAmountMin()
    {
        float quanitityMin = getQuantityMin();
        setQuantityMin(quanitityMin + getChangeAmount(quanitityMin));
    }

    public void decreaseAmountMin()
    {
        float quanitityMin = getQuantityMin();
        float changeAmount = getChangeAmount(quanitityMin);
        if(quanitityMin > changeAmount)
        {
            setQuantityMin(quanitityMin - changeAmount);
        }
        else
        {
            setQuantityMin(0);
        }
    }

    public void increaseAmountMax()
    {
        if(!isRange())
        {
            return;
        }
        float quanitityMax = getQuantityMax();
        setQuantityMax(quanitityMax + getChangeAmount(quanitityMax));
    }

    public void decreaseAmountMax()
    {
        if(!isRange())
        {
            return;
        }

        float quanitityMax = getQuantityMax();
        float changeAmount = getChangeAmount(quanitityMax);
        if(quanitityMax > changeAmount)
        {
            setQuantityMax(quanitityMax - changeAmount);
        }
        else
        {
            setQuantityMax(0);
        }
    }

    private float getChangeAmount(float quantity)
    {
        if(getUnit() == Unit.Unitless)
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

    static Optional<Amount> addUp(@NonNull Amount m1, @NonNull Amount m2)
    {
        if(!canBeAddedUp(m1, m2))
        {
            return Optional.empty();
        }

        Amount result = m1.createNewInstance();

        switch(m1.getUnit())
        {
            case Count:
            case Dessertspoon:
            case Teaspoon:
            case Unitless:
            {
                result.setQuantityMin(m1.getQuantityMin() + m2.getQuantityMin());
                result.setUnit(m1.getUnit());
                break;
            }

            case Kilogram:
            case Gram:
            {
                float value1 = m1.getUnit() == Unit.Kilogram ? m1.getQuantityMin() * 1000.0f : m1.getQuantityMin();
                float value2 = m2.getUnit() == Unit.Kilogram ? m2.getQuantityMin() * 1000.0f : m2.getQuantityMin();
                result.setQuantityMin(value1 + value2);

                if(result.getQuantityMin() >= 1000.0f)
                {
                    result.setQuantityMin(result.getQuantityMin() / 1000.0f);
                    result.setUnit(Unit.Kilogram);
                }
                else
                {
                    result.setUnit(Unit.Gram);
                }
                break;
            }

            case Liter:
            case Deciliter:
            case Milliliter:
            {
                float value1 = m1.getQuantityMin();
                if(m1.getUnit() == Unit.Liter)
                {
                    value1 *= 1000.0f;
                }
                else if(m1.getUnit() == Unit.Deciliter)
                {
                    value1 *= 100.0f;
                }

                float value2 = m2.getQuantityMin();
                if(m2.getUnit() == Unit.Liter)
                {
                    value2 *= 1000.0f;
                }
                else if(m2.getUnit() == Unit.Deciliter)
                {
                    value2 *= 100.0f;
                }

                result.setQuantityMin(value1 + value2);
                if(result.getQuantityMin() >= 1000.0f)
                {
                    result.setQuantityMin(result.getQuantityMin() / 1000.0f);
                    result.setUnit(Unit.Liter);
                }
                else if(result.getQuantityMin() >= 10.0f)
                {
                    result.setQuantityMin(result.getQuantityMin() / 100.0f);
                    result.setUnit(Unit.Deciliter);
                }
                else
                {
                    result.setUnit(Unit.Milliliter);
                }
                break;
            }

            default:
                throw new IllegalArgumentException();
        }
        return Optional.of(result);
    }

    static boolean canBeAddedUp(@NonNull Amount m1, @NonNull Amount m2)
    {
        if(m1.isRange() || m2.isRange())
        {
            return false;
        }

        switch(m1.getUnit())
        {
            case Count:
                return m2.getUnit() == Unit.Count;

            case Kilogram:
            case Gram:
                return m2.getUnit() == Unit.Kilogram || m2.getUnit() == Unit.Gram;

            case Liter:
            case Deciliter:
            case Milliliter:
                return m2.getUnit() == Unit.Liter || m2.getUnit() == Unit.Deciliter || m2.getUnit() == Unit.Milliliter;

            case Dessertspoon:
                return m2.getUnit() == Unit.Dessertspoon;

            case Teaspoon:
                return m2.getUnit() == Unit.Teaspoon;

            case Unitless:
                return m2.getUnit() == Unit.Unitless;

            default:
                throw new IllegalArgumentException();
        }
    }
}
