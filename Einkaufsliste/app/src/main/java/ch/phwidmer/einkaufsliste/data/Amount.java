package ch.phwidmer.einkaufsliste.data;

import android.support.annotation.NonNull;

import java.util.Arrays;
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
        setQuantityMin(getChangedAmount(quanitityMin, true));
    }

    public void decreaseAmountMin()
    {
        float quanitityMin = getQuantityMin();
        float changedAmount = getChangedAmount(quanitityMin, false);
        setQuantityMin(changedAmount);
    }

    public void increaseAmountMax()
    {
        if(!isRange())
        {
            return;
        }
        float quanitityMax = getQuantityMax();
        setQuantityMax(getChangedAmount(quanitityMax, true));
    }

    public void decreaseAmountMax()
    {
        if(!isRange())
        {
            return;
        }

        float quanitityMax = getQuantityMax();
        float changedAmount = getChangedAmount(quanitityMax, false);
        setQuantityMax(changedAmount);
    }

    private static float[] valueSteps =
            {0, 0.1f, 0.2f, 0.25f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f,
             1.0f, 1.25f, 1.5f, 1.75f,
             2.0f, 2.5f, 3.0f, 3.5f, 4.0f, 4.5f, 5.0f, 6.0f, 7.0f, 8.0f, 9.0f,
             10.0f, 12.5f, 15.0f, 16.0f, 17.0f, 18.0f, 19.0f,
             20.0f, 25.0f, 30.0f, 35.0f, 40.0f, 45.0f, 50.0f, 60.0f, 70.0f, 80.0f, 90.0f,
             100.0f, 110.0f, 120.0f, 125.0f, 130.0f, 140.0f, 150.0f, 160.0f, 170.0f, 180.0f, 190.0f,
             200.0f, 225.0f, 250.0f, 275.0f,
             300.0f, 350.0f, 400.0f, 450.0f, 500.0f, 550.0f,
             600.0f, 750.0f, 1000.0f, 1500.0f, 2000.0f, 3000.0f, 4000.0f, 5000.0f, 10000.0f};
    private float getChangedAmount(float quantity, boolean bIncrease)
    {
        if(getUnit() == Unit.Unitless)
        {
            return 0f;
        }

        int position = Arrays.binarySearch(valueSteps, quantity);
        if(position >= 0)
        {
            if(bIncrease)
            {
                if(position == valueSteps.length -1)
                {
                    return quantity;
                }
                return valueSteps[position + 1];
            }
            else
            {
                if(position == 0)
                {
                    return 0f;
                }
                return valueSteps[position - 1];
            }
        }
        else
        {
            int insertionPos = -position - 1;
            if(bIncrease)
            {
                if(insertionPos == valueSteps.length)
                {
                    return quantity;
                }
                return valueSteps[insertionPos];
            }
            else
            {
                if(insertionPos == 0)
                {
                    return 0f;
                }
                return valueSteps[insertionPos - 1];
            }
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
