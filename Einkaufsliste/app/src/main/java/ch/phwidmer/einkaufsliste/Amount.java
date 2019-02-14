package ch.phwidmer.einkaufsliste;

public class Amount {
    public enum Unit {
        Count,
        Kilogram,
        Liter,
        Unitless;
    }

    public Float m_Quantity = 1.0f;
    public Unit m_Unit;
}
