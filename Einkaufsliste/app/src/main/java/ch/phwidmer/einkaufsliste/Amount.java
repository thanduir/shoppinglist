package ch.phwidmer.einkaufsliste;

public class Amount {
    public enum Unit {
        Count,
        Kilogramm,
        Liter;
    }

    public Float m_Quantity;
    public Unit m_Unit;
}
