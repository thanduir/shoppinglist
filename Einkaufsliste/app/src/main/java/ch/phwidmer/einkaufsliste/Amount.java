package ch.phwidmer.einkaufsliste;

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

    public static String shortForm(Unit unit)
    {
        switch(unit)
        {
            case Count:
                return "Piece";

            case Kilogram:
                return "kg";
            case Gram:
                return "g";

            case Liter:
                return "L";
            case Deciliter:
                return "dl";
            case Milliliter:
                return "ml";

            case Dessertspoon:
                return "ds";
            case Teaspoon:
                return "ts";

            case Unitless:
                return "";

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
