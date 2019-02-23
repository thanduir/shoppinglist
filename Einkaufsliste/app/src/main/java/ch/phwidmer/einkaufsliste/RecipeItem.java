package ch.phwidmer.einkaufsliste;

public class RecipeItem {
    public enum Size
    {
        Normal,
        Small,
        Large;
    }

    public String   m_Ingredient;
    public Amount   m_Amount;
    public Size     m_Size = Size.Normal;
    public Boolean  m_Optional = false;

    public RecipeItem()
    {
        m_Amount = new Amount();
    }
}
