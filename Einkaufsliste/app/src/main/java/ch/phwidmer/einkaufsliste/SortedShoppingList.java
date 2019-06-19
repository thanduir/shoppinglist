package ch.phwidmer.einkaufsliste;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

// Generates the list of ingredients sorted by categories according to a sort order from the ShoppingList.
class SortedShoppingList
{
    private Ingredients m_Ingredients;

    private LinkedHashMap<String, LinkedList<CategoryShoppingItem>>  m_UnorderdList; // Key: Categories; Value: items
    private LinkedList<CategoryItem>                                 m_SortedList;  // Ingredientslist sorted by category. Same ingredients have been combined.

    class CategoryShoppingItem
    {
        private String                       m_Ingredient;
        private Amount                       m_Amount = new Amount();
        private LinkedList<ShoppingListItem> m_ShoppingItems = new LinkedList<>();

        ShoppingListItem.Status getStatus()
        {
            return m_ShoppingItems.getFirst().m_Status;
        }
        void invertStatus()
        {
            for(ShoppingListItem item : m_ShoppingItems)
            {
                item.invertStatus();
            }
        }

        Boolean isOptional()
        {
            return m_ShoppingItems.getFirst().m_Optional;
        }

        Amount getAmount()
        {
            return m_Amount;
        }

        RecipeItem.Size getSize()
        {
            return m_ShoppingItems.getFirst().m_Size;
        }

        String getAdditionalInfo()
        {
            return m_ShoppingItems.getFirst().m_AdditionalInfo;
        }
    }

    private class CategoryItem
    {
        String m_Name;
        boolean m_IncompatibleItemsList = false;
        LinkedList<CategoryShoppingItem> m_Items;
    }

    SortedShoppingList(ShoppingList shoppingList, Ingredients ingredients)
    {
        m_Ingredients = ingredients;

        m_UnorderdList = new LinkedHashMap<>();

        ArrayList<String> recipes = shoppingList.getAllShoppingRecipes();
        for(String strName : recipes)
        {
            ShoppingList.ShoppingRecipe recipe = shoppingList.getShoppingRecipe(strName);
            for(ShoppingListItem item : recipe.m_Items)
            {
                String strCategory = ingredients.getIngredient(item.m_Ingredient).m_Category.getName();

                if(!m_UnorderdList.containsKey(strCategory))
                {
                    m_UnorderdList.put(strCategory, new LinkedList<>());
                }

                addToCategoryItem(m_UnorderdList.get(strCategory), item);
            }
        }
    }

    private void addToCategoryItem(LinkedList<CategoryShoppingItem> categoryItems, ShoppingListItem item)
    {
        if(categoryItems == null)
        {
            return;
        }

        for(CategoryShoppingItem csi : categoryItems)
        {
            if(!csi.m_Ingredient.equals(item.m_Ingredient))
            {
                continue;
            }

            ShoppingListItem firstItem = csi.m_ShoppingItems.getFirst();
            if(item.m_Optional != firstItem.m_Optional
               || item.m_Size != firstItem.m_Size
               || item.m_Status != firstItem.m_Status
               || !item.m_AdditionalInfo.equals(firstItem.m_AdditionalInfo))
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

    void setSortOrder(String strSortOrderName, Categories.SortOrder sortOrder)
    {
        LinkedList<CategoryItem> incompatibleItems = new LinkedList<>();

        m_SortedList = new LinkedList<>();
        for(Categories.Category category : sortOrder.m_CategoriesOrder)
        {
            String strCategory = category.getName();

            if(m_UnorderdList.containsKey(strCategory))
            {
                CategoryItem item = new CategoryItem();
                item.m_Name = strCategory;
                item.m_Items = new LinkedList<>(m_UnorderdList.get(strCategory));

                LinkedList<CategoryShoppingItem> removeList = new LinkedList<>();
                for(CategoryShoppingItem current : item.m_Items)
                {
                    // Move items with an incompatible provenance to the corresponding list
                    String strProvenance = m_Ingredients.getIngredient(current.m_Ingredient).m_strProvenance;
                    if(!strProvenance.equals(Ingredients.c_strProvenanceEverywhere) && !strProvenance.equals(strSortOrderName))
                    {
                        addToIncompatibleItemsList(incompatibleItems, strProvenance, current);
                        removeList.add(current);
                    }
                }
                m_SortedList.add(item);

                for(CategoryShoppingItem delItem : removeList)
                {
                    item.m_Items.remove(delItem);
                }
            }
        }

        // Add incompatible items at the end
        for(CategoryItem item : incompatibleItems)
        {
            if(item.m_Items.size() > 0)
            {
                m_SortedList.add(item);
            }
        }
    }

    private void addToIncompatibleItemsList(LinkedList<CategoryItem> incompatibleItems, String strProvenance, CategoryShoppingItem current)
    {
        for(CategoryItem item : incompatibleItems)
        {
            if(item.m_Name.equals(strProvenance))
            {
                item.m_Items.add(current);
                return;
            }
        }

        CategoryItem otherItems = new CategoryItem();
        otherItems.m_Name = strProvenance;
        otherItems.m_IncompatibleItemsList = true;
        otherItems.m_Items = new LinkedList<>();
        otherItems.m_Items.add(current);

        incompatibleItems.add(otherItems);
    }

    Integer itemsCount()
    {
        int size = 0;
        for(CategoryItem e : m_SortedList)
        {
            size += 1 + e.m_Items.size();
        }
        return size;
    }

    String getName(int index)
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
        return "";
    }

    Boolean isCategory(int index)
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

    Boolean isIncompatibleItemsList(int index)
    {
        int i = 0;
        for(CategoryItem e : m_SortedList)
        {
            if(i == index)
            {
                return e.m_IncompatibleItemsList;
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

    CategoryShoppingItem getListItem(int index)
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
