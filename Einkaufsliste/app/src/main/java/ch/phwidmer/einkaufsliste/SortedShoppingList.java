package ch.phwidmer.einkaufsliste;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Vector;

public class SortedShoppingList
{
    private LinkedHashMap<String, LinkedList<ShoppingListItem>> m_UnorderdList;
    private LinkedList<CategoryItem>                            m_SortedList;

    private class CategoryItem
    {
        public String m_Name;
        LinkedList<ShoppingListItem> m_Items;
    }

    public SortedShoppingList(ShoppingList shoppingList, Ingredients ingredients)
    {
        m_UnorderdList = new LinkedHashMap<String, LinkedList<ShoppingListItem>>();

        Vector<String> recipes = shoppingList.getAllShoppingRecipes();
        for(String strName : recipes)
        {
            ShoppingList.ShoppingRecipe recipe = shoppingList.getShoppingRecipe(strName);
            for(ShoppingListItem item : recipe.m_Items)
            {
                String strCategory = ingredients.getIngredient(item.m_Ingredient).m_Category.getName();

                if(!m_UnorderdList.containsKey(strCategory))
                {
                    m_UnorderdList.put(strCategory, new LinkedList<ShoppingListItem>());
                }

                // TODO: Ggfs. zusammenfassen! (oder mache ich das erst am Schluss ausserhalb dieser Schleife?)
                m_UnorderdList.get(strCategory).add(item);
            }
        }
    }

    public void setSortOrder(Categories.SortOrder sortOrder)
    {
        m_SortedList = new LinkedList<CategoryItem>();
        for(Categories.Category category : sortOrder.m_CategoriesOrder)
        {
            String strCategory = category.getName();

            if(m_UnorderdList.containsKey(strCategory))
            {
                CategoryItem item = new CategoryItem();
                item.m_Name = strCategory;
                item.m_Items = m_UnorderdList.get(strCategory);
                m_SortedList.add(item);
            }
        }
    }

    public Integer itemsCount()
    {
        int size = 0;
        for(CategoryItem e : m_SortedList)
        {
            size += 1 + e.m_Items.size();
        }
        return size;
    }

    public String getName(int index)
    {
        int i = 0;
        for(CategoryItem e : m_SortedList)
        {
            if(i == index)
            {
                return e.m_Name;
            }
            else if(index < i + 1 + e.m_Items.size())
            {
                int delta = index - i;
                int j = 1;
                for(ShoppingListItem l : e.m_Items)
                {
                    if(j == delta)
                    {
                        return l.m_Ingredient;
                    }
                    ++j;
                }
            }
            else
            {
                i += 1 + e.m_Items.size();
            }
        }
        return new String();
    }

    public Boolean isCategory(int index)
    {
        int i = 0;
        for(CategoryItem e : m_SortedList)
        {
            if(i == index)
            {
                return true;
            }
            else if(index < i + 1 + e.m_Items.size())
            {
                return false;
            }
            else
            {
                i += 1 + e.m_Items.size();
            }
        }
        return false;
    }

    public ShoppingListItem getListItem(int index)
    {
        int i = 0;
        for(CategoryItem e : m_SortedList)
        {
            if(i == index)
            {
                return null;
            }
            else if(index < i + 1 + e.m_Items.size())
            {
                int delta = index - i;
                int j = 1;
                for(ShoppingListItem l : e.m_Items)
                {
                    if(delta == j)
                    {
                        return l;
                    }
                    ++j;
                }
            }
            else
            {
                i += 1 + e.m_Items.size();
            }
        }
        return null;
    }
}
