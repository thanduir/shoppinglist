package ch.phwidmer.einkaufsliste.data;

public abstract class GroceryPlanning
{
    protected Categories      m_Categories;
    protected Ingredients     m_Ingredients;
    protected Recipes         m_Recipes;
    protected ShoppingList    m_ShoppingList;

    public abstract void clearAll();
    public abstract void flush();

    public Categories categories()
    {
        return m_Categories;
    }

    public Ingredients ingredients()
    {
        return m_Ingredients;
    }

    public Recipes recipes()
    {
        return m_Recipes;
    }

    public ShoppingList shoppingList()
    {
        return m_ShoppingList;
    }
}
