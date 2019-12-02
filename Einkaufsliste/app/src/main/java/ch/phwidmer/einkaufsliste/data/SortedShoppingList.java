package ch.phwidmer.einkaufsliste.data;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Optional;

// Generates the list of ingredients sorted by categories according to a sort order from the ShoppingList.
public class SortedShoppingList
{
    private Ingredients m_Ingredients;

    private LinkedHashMap<String, LinkedList<CategoryShoppingItem>>  m_UnorderdList; // Key: Categories; Value: items
    private LinkedList<CategoryItem>                                 m_SortedList;  // Ingredientslist sorted by category. Same ingredients have been combined.

    public class CategoryShoppingItem
    {
        private String                       m_Ingredient;
        private Amount                       m_Amount;
        private LinkedList<ShoppingListItem> m_ShoppingItems = new LinkedList<>();

        CategoryShoppingItem(@NonNull String ingredient, @NonNull Amount amount)
        {
            m_Ingredient = ingredient;
            m_Amount = amount;
        }

        public Optional<ShoppingListItem.Status> getStatus()
        {
            if(m_ShoppingItems.isEmpty())
            {
                return Optional.empty();
            }
            return Optional.of(m_ShoppingItems.getFirst().getStatus());
        }
        public void invertStatus()
        {
            for(ShoppingListItem item : m_ShoppingItems)
            {
                item.invertStatus();
            }
        }

        public boolean isOptional()
        {
            return m_ShoppingItems.getFirst().isOptional();
        }

        public Amount getAmount()
        {
            return m_Amount;
        }

        public RecipeItem.Size getSize()
        {
            return m_ShoppingItems.getFirst().getSize();
        }

        public String getAdditionalInfo()
        {
            return m_ShoppingItems.getFirst().getAdditionalInfo();
        }
    }

    private class CategoryItem
    {
        String m_Name;
        boolean m_IncompatibleItemsList = false;
        LinkedList<CategoryShoppingItem> m_Items;
    }

    public SortedShoppingList(@NonNull ShoppingList shoppingList, @NonNull Ingredients ingredients)
    {
        m_Ingredients = ingredients;

        m_UnorderdList = new LinkedHashMap<>();

        ArrayList<ShoppingList.ShoppingRecipe> recipes = shoppingList.getAllShoppingRecipes();
        for(ShoppingList.ShoppingRecipe recipe : recipes)
        {
            for(ShoppingListItem item : recipe.getAllItems())
            {
                Optional<Ingredients.Ingredient> ingredient = ingredients.getIngredient(item.getIngredient());
                if(!ingredient.isPresent())
                {
                    continue;
                }
                String strCategory = ingredient.get().getCategory();

                if(!m_UnorderdList.containsKey(strCategory))
                {
                    m_UnorderdList.put(strCategory, new LinkedList<>());
                }

                addToCategoryItem(m_UnorderdList.get(strCategory), item);
            }
        }
    }

    private void addToCategoryItem(LinkedList<CategoryShoppingItem> categoryItems, @NonNull ShoppingListItem item)
    {
        if(categoryItems == null)
        {
            return;
        }

        for(CategoryShoppingItem csi : categoryItems)
        {
            if(!csi.m_Ingredient.equals(item.getIngredient()))
            {
                continue;
            }

            ShoppingListItem firstItem = csi.m_ShoppingItems.getFirst();
            if(item.isOptional() != firstItem.isOptional()
               || item.getSize() != firstItem.getSize()
               || item.getStatus() != firstItem.getStatus()
               || !item.getAdditionalInfo().equals(firstItem.getAdditionalInfo()))
            {
                continue;
            }

            if(Amount.canBeAddedUp(item.getAmount(), csi.m_Amount))
            {
                Optional<Amount> addedAmounts = Amount.addUp(item.getAmount(), csi.m_Amount);
                if(!addedAmounts.isPresent())
                {
                    continue;
                }
                csi.m_Amount = addedAmounts.get();
                csi.m_ShoppingItems.add(item);
                return;
            }
        }

        CategoryShoppingItem csi = new CategoryShoppingItem(item.getIngredient(), item.getAmount());
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

    public void setSortOrder(@NonNull String strSortOrderName, @NonNull Categories.SortOrder sortOrder)
    {
        LinkedList<CategoryItem> incompatibleItems = new LinkedList<>();

        m_SortedList = new LinkedList<>();
        for(Categories.Category category : sortOrder.getOrder())
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
                    Optional<Ingredients.Ingredient> ingredient = m_Ingredients.getIngredient(current.m_Ingredient);
                    if(!ingredient.isPresent())
                    {
                        continue;
                    }

                    // Move items with an incompatible provenance to the corresponding list
                    String strProvenance = ingredient.get().getProvenance();
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

    private void addToIncompatibleItemsList(@NonNull LinkedList<CategoryItem> incompatibleItems, @NonNull String strProvenance, @NonNull CategoryShoppingItem current)
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
        return "";
    }

    public boolean isCategory(int index)
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

    public boolean isIncompatibleItemsList(int index)
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

    public Optional<CategoryShoppingItem> getListItem(int index)
    {
        int i = 0;
        for(CategoryItem e : m_SortedList)
        {
            if(i == index)
            {
                return Optional.empty();
            }
            else if(index < i + 1 + e.m_Items.size())
            {
                int delta = index - i;
                int j = 1;
                for(CategoryShoppingItem l : e.m_Items)
                {
                    if(delta == j)
                    {
                        return Optional.of(l);
                    }
                    ++j;
                }
            }
            else
            {
                i += 1 + e.m_Items.size();
            }
        }
        return Optional.empty();
    }
}
