package ch.phwidmer.einkaufsliste.data.fs_based;

import android.support.annotation.NonNull;

import ch.phwidmer.einkaufsliste.data.Amount;
import ch.phwidmer.einkaufsliste.data.RecipeItem;
import ch.phwidmer.einkaufsliste.data.ShoppingListItem;

public class ShoppingListItemFS extends ShoppingListItem
{
    private Status              m_Status = Status.None;

    private String              m_Ingredient;
    private AmountFS            m_Amount;
    private String              m_AdditionalInfo;
    private RecipeItemFS.Size   m_Size;
    private Boolean             m_Optional;

    ShoppingListItemFS(@NonNull String strIngredient)
    {
        m_Ingredient = strIngredient;
        m_Amount = new AmountFS();
        m_AdditionalInfo = "";
        m_Size = RecipeItem.Size.Normal;
        m_Optional = false;
    }

    ShoppingListItemFS(@NonNull RecipeItem recipeItem)
    {
        m_Amount = new AmountFS((AmountFS)recipeItem.getAmount());
        m_Ingredient = recipeItem.getIngredient();
        m_Optional = recipeItem.isOptional();
        m_AdditionalInfo = recipeItem.getAdditionalInfo();
        m_Size = recipeItem.getSize();
    }

    @Override
    public Status getStatus()
    {
        return m_Status;
    }
    @Override
    public void setStatus(@NonNull Status status)
    {
        m_Status = status;
    }

    @Override
    public String getIngredient()
    {
        return m_Ingredient;
    }
    @Override
    public void setIngredient(@NonNull String strIngredient)
    {
        m_Ingredient = strIngredient;
    }

    @Override
    public Amount getAmount()
    {
        return m_Amount;
    }
    @Override
    public void setAmount(@NonNull Amount amount)
    {
        m_Amount = (AmountFS)amount;
    }

    @Override
    public String getAdditionalInfo()
    {
        return m_AdditionalInfo;
    }
    @Override
    public void setAdditionInfo(@NonNull String additionalInfo)
    {
        m_AdditionalInfo = additionalInfo;
    }

    @Override
    public RecipeItem.Size getSize()
    {
        return m_Size;
    }
    @Override
    public void setSize(@NonNull RecipeItem.Size size)
    {
        m_Size = size;
    }

    @Override
    public boolean isOptional()
    {
        return m_Optional;
    }
    @Override
    public void setIsOptional(boolean optional)
    {
        m_Optional = optional;
    }
}
