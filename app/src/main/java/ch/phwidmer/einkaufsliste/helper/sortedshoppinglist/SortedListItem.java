package ch.phwidmer.einkaufsliste.helper.sortedshoppinglist;

import java.util.Optional;

public class SortedListItem {
    private String                  m_Name;
    private ShoppingItemType        m_Type;
    private CategoryShoppingItem    m_Item;

    public enum ShoppingItemType
    {
        TOPLEVEL_HEADER,
        CATEGORY_HEADER,
        INCOMPATIBLE_ITEMS_HEADER,
        INGREDIENT
    }

    SortedListItem(ShoppingItemType type, String name)
    {
        m_Name = name;
        m_Type = type;
        m_Item = null;
    }

    SortedListItem(String name, CategoryShoppingItem item)
    {
        m_Name = name;
        m_Type = ShoppingItemType.INGREDIENT;
        m_Item = item;
    }

    public String getName()
    {
        return m_Name;
    }

    public ShoppingItemType getType()
    {
        return m_Type;
    }

    public Optional<CategoryShoppingItem> getShoppingItem()
    {
        if(m_Type != ShoppingItemType.INGREDIENT)
        {
            return Optional.empty();
        }

        return Optional.of(m_Item);
    }
}
