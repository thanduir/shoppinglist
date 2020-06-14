package ch.phwidmer.einkaufsliste.helper.sortedshoppinglist;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Optional;

import ch.phwidmer.einkaufsliste.R;
import ch.phwidmer.einkaufsliste.data.Amount;
import ch.phwidmer.einkaufsliste.data.Categories;
import ch.phwidmer.einkaufsliste.data.Ingredients;
import ch.phwidmer.einkaufsliste.data.ShoppingList;
import ch.phwidmer.einkaufsliste.data.ShoppingListItem;

public class SortedShoppingList
{
    private Ingredients m_Ingredients;
    private LinkedHashMap<String, LinkedList<CategoryShoppingItem>> m_UnorderdList; // Key: Categories; Value: items
    private ArrayList<SortedListItem> m_SortedList;

    public enum ListOrder
    {
        STANDARD,
        SEPARATE_CHECKED
    }

    public SortedShoppingList(@NonNull ShoppingList shoppingList, @NonNull Ingredients ingredients)
    {
        m_Ingredients = ingredients;
        m_SortedList = new ArrayList<>();

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
            if(!csi.getIngredient().equals(item.getIngredient()))
            {
                continue;
            }

            Optional<ShoppingListItem.Status> otherStatus = csi.getStatus();
            if(!otherStatus.isPresent())
            {
                continue;
            }
            if(item.isOptional() != csi.isOptional()
                    || item.getSize() != csi.getSize()
                    || item.getStatus() != otherStatus.get()
                    || !item.getAdditionalInfo().equals(csi.getAdditionalInfo()))
            {
                continue;
            }

            if(Amount.canBeAddedUp(item.getAmount(), csi.getAmount()))
            {
                Optional<Amount> addedAmounts = Amount.addUp(item.getAmount(), csi.getAmount());
                if(!addedAmounts.isPresent())
                {
                    continue;
                }
                csi.setAmount(addedAmounts.get());
                csi.addShoppingListItem(item);
                return;
            }
        }

        CategoryShoppingItem csi = new CategoryShoppingItem(item.getIngredient(), item.getAmount());
        csi.addShoppingListItem(item);
        categoryItems.add(csi);

        Collections.sort(categoryItems, new SortByIngredients());
    }

    private static class SortByIngredients implements Comparator<Object> {
        public int compare(Object o1, Object o2) {
            CategoryShoppingItem s1 = (CategoryShoppingItem) o1;
            CategoryShoppingItem s2 = (CategoryShoppingItem) o2;
            return s1.getIngredient().compareTo(s2.getIngredient());
        }
    }

    public void generateSortedList(@NonNull Context context, @NonNull ListOrder listOrder, @NonNull Categories.SortOrder sortOrder)
    {
        LinkedList<SortedListItem> checkedItems = new LinkedList<>();
        LinkedList<SortedListItem> checkedIncompatibleItems = new LinkedList<>();
        LinkedList<SortedListItem> uncheckedItems = new LinkedList<>();
        LinkedList<SortedListItem> uncheckedIncompatibleItems = new LinkedList<>();

        if(listOrder == ListOrder.SEPARATE_CHECKED)
        {
            SortedListItem checkedHeader = new SortedListItem(SortedListItem.ShoppingItemType.TOPLEVEL_HEADER, context.getString(R.string.text_header_checked_items));
            checkedItems.add(checkedHeader);

            SortedListItem uncheckedHeader = new SortedListItem(SortedListItem.ShoppingItemType.TOPLEVEL_HEADER, context.getString(R.string.text_header_unchecked_items));
            uncheckedItems.add(uncheckedHeader);
        }

        for(Categories.Category category : sortOrder.getOrder())
        {
            String strCategory = category.getName();

            if(m_UnorderdList.containsKey(strCategory))
            {
                boolean addedCheckedHeader = false;
                boolean addedCheckedIncompatibleHeader = false;
                boolean addedUncheckedHeader = false;
                boolean addedUncheckedIncompatibleHeader = false;

                LinkedList<CategoryShoppingItem> categoryList = m_UnorderdList.get(strCategory);
                if(categoryList == null)
                {
                    continue;
                }
                for(CategoryShoppingItem current : categoryList)
                {
                    Optional<Ingredients.Ingredient> ingredient = m_Ingredients.getIngredient(current.getIngredient());
                    if(!ingredient.isPresent() || !current.getStatus().isPresent())
                    {
                        continue;
                    }

                    String strProvenance = ingredient.get().getProvenance();
                    boolean incompatible = (!strProvenance.equals(Ingredients.c_strProvenanceEverywhere) && !strProvenance.equals(sortOrder.getName()));
                    boolean taken = current.getStatus().get() == ShoppingListItem.Status.Taken;

                    SortedListItem item = new SortedListItem(current.getIngredient(), current);

                    if(taken || listOrder != ListOrder.SEPARATE_CHECKED)
                    {
                        // checked
                        if(!incompatible)
                        {
                            if(!addedCheckedHeader)
                            {
                                addedCheckedHeader = true;
                                SortedListItem itemHeader = new SortedListItem(SortedListItem.ShoppingItemType.CATEGORY_HEADER, strCategory);
                                checkedItems.add(itemHeader);
                            }
                            checkedItems.add(item);
                        }
                        else
                        {
                            if(!addedCheckedIncompatibleHeader)
                            {
                                addedCheckedIncompatibleHeader = true;
                                SortedListItem itemHeader = new SortedListItem(SortedListItem.ShoppingItemType.INCOMPATIBLE_ITEMS_HEADER, strCategory);
                                checkedIncompatibleItems.add(itemHeader);
                            }
                            checkedIncompatibleItems.add(item);
                        }
                    }
                    else
                    {
                        // unchecked
                        if(!incompatible)
                        {
                            if(!addedUncheckedHeader)
                            {
                                addedUncheckedHeader = true;
                                SortedListItem itemHeader = new SortedListItem(SortedListItem.ShoppingItemType.CATEGORY_HEADER, strCategory);
                                uncheckedItems.add(itemHeader);
                            }
                            uncheckedItems.add(item);
                        }
                        else
                        {
                            if(!addedUncheckedIncompatibleHeader)
                            {
                                addedUncheckedIncompatibleHeader = true;
                                SortedListItem itemHeader = new SortedListItem(SortedListItem.ShoppingItemType.INCOMPATIBLE_ITEMS_HEADER, strCategory);
                                uncheckedIncompatibleItems.add(itemHeader);
                            }
                            uncheckedIncompatibleItems.add(item);
                        }
                    }
                }
            }
        }

        m_SortedList = new ArrayList<>(checkedItems.size() + checkedIncompatibleItems.size() + uncheckedItems.size() + uncheckedIncompatibleItems.size());
        m_SortedList.addAll(checkedItems);
        m_SortedList.addAll(checkedIncompatibleItems);
        m_SortedList.addAll(uncheckedItems);
        m_SortedList.addAll(uncheckedIncompatibleItems);
    }

    public int updateListOnItemChanged(@NonNull Context context, int posChangedItem,
                                       @NonNull ListOrder listOrder, @NonNull Categories.SortOrder sortOrder,
                                       @NonNull LinkedList<Integer> positionsRemoved, @NonNull LinkedList<Integer> positionsInserted)
    {
        if(listOrder != ListOrder.SEPARATE_CHECKED)
        {
            return posChangedItem;
        }
        SortedListItem listItem = m_SortedList.get(posChangedItem);
        if(listItem.getType() != SortedListItem.ShoppingItemType.INGREDIENT)
        {
            return posChangedItem;
        }

        Optional<CategoryShoppingItem> item = listItem.getShoppingItem();
        if(!item.isPresent())
        {
            return posChangedItem;
        }

        // Will the previous item (header) be removed?
        SortedListItem prevItem = m_SortedList.get(posChangedItem-1);
        if(prevItem.getType() != SortedListItem.ShoppingItemType.INGREDIENT)
        {
            boolean removePrev = true;
            if(posChangedItem + 1 < m_SortedList.size())
            {
                SortedListItem nextItem = m_SortedList.get(posChangedItem + 1);
                removePrev = nextItem.getType() != SortedListItem.ShoppingItemType.INGREDIENT;
            }
            if(removePrev)
            {
                positionsRemoved.add(posChangedItem-1);
            }
        }

        // Find new position
        generateSortedList(context, listOrder, sortOrder);
        int newPosition = indexOf(item.get());

        // New header needed at this position?
        SortedListItem newPrevItem = m_SortedList.get(newPosition-1);
        if(newPrevItem.getType() != SortedListItem.ShoppingItemType.INGREDIENT)
        {
            boolean addHeader = true;
            if(newPosition + 1 < m_SortedList.size())
            {
                SortedListItem nextItem = m_SortedList.get(newPosition + 1);
                addHeader = nextItem.getType() != SortedListItem.ShoppingItemType.INGREDIENT;
            }
            if(addHeader)
            {
                positionsInserted.add(newPosition-1);
            }
        }

        return newPosition;
    }

    private int indexOf(CategoryShoppingItem item)
    {
        for(SortedListItem listItem : m_SortedList)
        {
            Optional<CategoryShoppingItem> testItem = listItem.getShoppingItem();
            if(!testItem.isPresent())
            {
                continue;
            }

            if(testItem.get().equals(item))
            {
                return m_SortedList.indexOf(listItem);
            }
        }
        return -1;
    }

    public int itemsCount()
    {
        return m_SortedList.size();
    }

    public Optional<SortedListItem> getItem(int index)
    {
        if(index < 0 || index >= m_SortedList.size())
        {
            return Optional.empty();
        }

        return Optional.of(m_SortedList.get(index));
    }
}
