package ch.phwidmer.einkaufsliste.helper.sortedshoppinglist;

import android.support.annotation.NonNull;

import java.util.LinkedList;
import java.util.Optional;

import ch.phwidmer.einkaufsliste.data.Amount;
import ch.phwidmer.einkaufsliste.data.RecipeItem;
import ch.phwidmer.einkaufsliste.data.ShoppingListItem;

public class CategoryShoppingItem {
    private String                       m_Ingredient;
    private Amount                       m_Amount;
    private LinkedList<ShoppingListItem> m_ShoppingItems = new LinkedList<>();

    CategoryShoppingItem(@NonNull String ingredient, @NonNull Amount amount)
    {
        m_Ingredient = ingredient;
        m_Amount = amount;
    }

    public String getIngredient()
    {
        return m_Ingredient;
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

    void setAmount(Amount amount)
    {
        m_Amount = amount;
    }

    void addShoppingListItem(ShoppingListItem item)
    {
        m_ShoppingItems.add(item);
    }
}
