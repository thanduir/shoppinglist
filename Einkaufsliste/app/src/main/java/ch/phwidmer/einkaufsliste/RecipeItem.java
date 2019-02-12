package ch.phwidmer.einkaufsliste;

public class RecipeItem {
    public enum Size
    {
        Standard,
        Small,
        Large;
    }

    // TODO: Kann das so bleiben, sollte es eine REcipe Klasse geben oder sollte ich das in die REcipies Klasse verschieben?
    public Integer   m_IngredientID;
    public Amount    m_Amount;
    public Size      m_Size;
    public Boolean   m_Optional;
}
