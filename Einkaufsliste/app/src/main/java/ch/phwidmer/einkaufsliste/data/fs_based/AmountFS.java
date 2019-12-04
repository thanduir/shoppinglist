package ch.phwidmer.einkaufsliste.data.fs_based;

import android.support.annotation.NonNull;

import ch.phwidmer.einkaufsliste.data.Amount;
import ch.phwidmer.einkaufsliste.data.Unit;

public class AmountFS extends Amount
{
    private static float QUANTITY_UNUSED = -1;

    private float m_QuantityMin;
    private float m_QuantityMax;
    private Unit m_Unit;

    AmountFS()
    {
        m_QuantityMin = 1.0f;
        m_QuantityMax = QUANTITY_UNUSED;
        m_Unit = Unit.Count;
    }

    AmountFS(@NonNull AmountFS other)
    {
        m_QuantityMin = other.m_QuantityMin;
        m_QuantityMax = other.m_QuantityMax;
        m_Unit = other.m_Unit;
    }

    @Override
    protected Amount createNewInstance()
    {
        return new AmountFS();
    }

    @Override
    public float getQuantityMin()
    {
        return m_QuantityMin;
    }
    @Override
    public void setQuantityMin(float quantity)
    {
        m_QuantityMin = quantity;
    }

    @Override
    public float getQuantityMax()
    {
        return m_QuantityMax;
    }
    @Override
    public void setQuantityMax(float quantity)
    {
        m_QuantityMax = quantity;
    }

    @Override
    public Unit getUnit()
    {
        return m_Unit;
    }
    @Override
    public void setUnit(@NonNull Unit unit)
    {
        m_Unit = unit;
    }

    @Override
    public boolean isRange()
    {
        return m_QuantityMax != QUANTITY_UNUSED;
    }

    @Override
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
}
