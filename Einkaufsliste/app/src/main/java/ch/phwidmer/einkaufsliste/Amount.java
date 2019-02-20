package ch.phwidmer.einkaufsliste;

public class Amount {
    public enum Unit {
        Count,

        Kilogram,
        Gram,

        Liter,
        Deciliter,

        Dessertspoon,
        Teaspoon,

        Unitless;
    }

    public Float m_Quantity = 1.0f;
    public Unit m_Unit = Unit.Count;

    public static Amount addUp(Amount m1, Amount m2)
    {
        if(!canBeAddedUp(m1, m2))
        {
            return null;
        }

        Amount result = new Amount();
        // TODO: Umrechnungsmethode implementieren.
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
                return m2.m_Unit == Unit.Liter || m2.m_Unit == Unit.Deciliter;

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
