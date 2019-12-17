package ch.phwidmer.einkaufsliste.data;

import android.support.annotation.NonNull;

import java.util.Optional;

public class Amount
{
    private static float QUANTITY_UNUSED = -1;

    private float m_QuantityMin;
    private float m_QuantityMax;
    private Unit m_Unit;

    public Amount()
    {
        m_QuantityMin = 1.0f;
        m_QuantityMax = QUANTITY_UNUSED;
        m_Unit = Unit.Count;
    }

    public Amount(@NonNull Amount other)
    {
        m_QuantityMin = other.m_QuantityMin;
        m_QuantityMax = other.m_QuantityMax;
        m_Unit = other.m_Unit;
    }

    public float getQuantityMin()
    {
        return m_QuantityMin;
    }
    public void setQuantityMin(float quantity)
    {
        m_QuantityMin = quantity;
    }

    public float getQuantityMax()
    {
        return m_QuantityMax;
    }
    public void setQuantityMax(float quantity)
    {
        m_QuantityMax = quantity;
    }

    public Unit getUnit()
    {
        return m_Unit;
    }
    public void setUnit(@NonNull Unit unit)
    {
        m_Unit = unit;
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

    public void scaleAmount(float fFactor)
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

    public static Optional<Amount> addUp(@NonNull Amount m1, @NonNull Amount m2)
    {
        if(!canBeAddedUp(m1, m2))
        {
            return Optional.empty();
        }

        Amount result = new Amount();

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

    public static boolean canBeAddedUp(@NonNull Amount m1, @NonNull Amount m2)
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
