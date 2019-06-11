package ch.phwidmer.einkaufsliste;

class ShoppingListItem {
    public enum Status
    {
        None,
        Taken;
    }

    Status           m_Status = Status.None;

    String           m_Ingredient;
    Amount           m_Amount;
    RecipeItem.Size  m_Size = RecipeItem.Size.Normal;
    Boolean          m_Optional = false;

    ShoppingListItem()
    {
        m_Amount = new Amount();
    }

    void invertStatus()
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
