package ch.phwidmer.einkaufsliste;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Vector;

public class SortedShoppingList
{
    private LinkedHashMap<String, LinkedList<CategoryShoppingItem>>  m_UnorderdList;
    private LinkedList<CategoryItem>                                 m_SortedList;

    public class CategoryShoppingItem
    {
        private String                       m_Ingredient;
        private Amount                       m_Amount = new Amount();
        private LinkedList<ShoppingListItem> m_ShoppingItems = new LinkedList<ShoppingListItem>();

        public ShoppingListItem.Status getStatus()
        {
            return m_ShoppingItems.getFirst().m_Status;
        }
        public void invertStatus()
        {
            for(ShoppingListItem item : m_ShoppingItems)
            {
                item.invertStatus();
            }
        }

        public Boolean isOptional()
        {
            return m_ShoppingItems.getFirst().m_Optional;
        }

        public Amount getAmount()
        {
            return m_Amount;
        }

        public RecipeItem.Size getSize()
        {
            return m_ShoppingItems.getFirst().m_Size;
        }
    }

    private class CategoryItem
    {
        public String m_Name;
        LinkedList<CategoryShoppingItem> m_Items;
    }

    public SortedShoppingList(ShoppingList shoppingList, Ingredients ingredients)
    {
        m_UnorderdList = new LinkedHashMap<String, LinkedList<CategoryShoppingItem>>();

        Vector<String> recipes = shoppingList.getAllShoppingRecipes();
        for(String strName : recipes)
        {
            ShoppingList.ShoppingRecipe recipe = shoppingList.getShoppingRecipe(strName);
            for(ShoppingListItem item : recipe.m_Items)
            {
                String strCategory = ingredients.getIngredient(item.m_Ingredient).m_Category.getName();

                if(!m_UnorderdList.containsKey(strCategory))
                {
                    m_UnorderdList.put(strCategory, new LinkedList<CategoryShoppingItem>());
                }

                addToCategoryItem(m_UnorderdList.get(strCategory), item);
            }
        }
    }

    private void addToCategoryItem(LinkedList<CategoryShoppingItem> categoryItems, ShoppingListItem item)
    {
        for(CategoryShoppingItem csi : categoryItems)
        {
            if(!csi.m_Ingredient.equals(item.m_Ingredient))
            {
                continue;
            }

            ShoppingListItem firstItem = csi.m_ShoppingItems.getFirst();
            if(item.m_Optional != firstItem.m_Optional || item.m_Size != firstItem.m_Size || item.m_Status != firstItem.m_Status)
            {
                continue;
            }

            if(Amount.canBeAddedUp(item.m_Amount, csi.m_Amount))
            {
                csi.m_Amount = Amount.addUp(item.m_Amount, csi.m_Amount);
                csi.m_ShoppingItems.add(item);
                return;
            }
        }

        CategoryShoppingItem csi = new CategoryShoppingItem();
        csi.m_Ingredient = item.m_Ingredient;
        csi.m_Amount = item.m_Amount;
        csi.m_ShoppingItems.add(item);
        categoryItems.add(csi);

        Collections.sort(categoryItems, new SortByIngredients());
    }

    private class SortByIngredients implements Comparator<Object> {
        public int compare(Object o1, Object o2) {
            CategoryShoppingItem s1 = (CategoryShoppingItem) o1;
            CategoryShoppingItem s2 = (CategoryShoppingItem) o2;
            return s1.m_Ingredient.compareTo(s2.m_Ingredient);
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
                for(CategoryShoppingItem l : e.m_Items)
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

    public CategoryShoppingItem getListItem(int index)
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
                for(CategoryShoppingItem l : e.m_Items)
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
