package ch.phwidmer.einkaufsliste;

import java.util.LinkedList;

public class GroceryPlanning
{
    // TODO: Serialisierung

    public Categories      m_Categories;
    public Ingredients     m_Ingredients;
    public Recipes         m_Recipes;
    public ShoppingList    m_ShoppingList;

    GroceryPlanning()
    {
        m_Categories = new Categories();
        m_Ingredients = new Ingredients(m_Categories);
        m_Recipes = new Recipes();
        m_ShoppingList = new ShoppingList();
    }

    // TODO(?) Liste von nicht-abgehakten Ingredients der Einkaufsliste
}
