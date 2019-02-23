package ch.phwidmer.einkaufsliste;

public class ShoppingListItem {
    public enum Status
    {
        None,
        Taken;
    }

    public Status           m_Status = Status.None;

    public String           m_Ingredient;
    public Amount           m_Amount;
    public RecipeItem.Size  m_Size = RecipeItem.Size.Normal;
    public Boolean          m_Optional = false;

    public ShoppingListItem()
    {
        m_Amount = new Amount();
    }

    public void invertStatus()
    {
        if(m_Status == Status.None)
        {
            m_Status = Status.Taken;
        }
        else
        {
            m_Status = Status.None;
        }
    }
}
