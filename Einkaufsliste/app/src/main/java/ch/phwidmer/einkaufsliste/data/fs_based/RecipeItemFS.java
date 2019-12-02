package ch.phwidmer.einkaufsliste.data.fs_based;

import android.support.annotation.NonNull;

import ch.phwidmer.einkaufsliste.data.Amount;
import ch.phwidmer.einkaufsliste.data.RecipeItem;

public class RecipeItemFS implements RecipeItem {
    private String m_Ingredient;
    private AmountFS m_Amount;
    private String m_AdditionalInfo = "";
    private Size m_Size = Size.Normal;
    private Boolean m_Optional = false;

    RecipeItemFS(@NonNull String strIngredient) {
        m_Ingredient = strIngredient;
        m_Amount = new AmountFS();
    }

    RecipeItemFS(@NonNull RecipeItemFS other) {
        m_Ingredient = other.m_Ingredient;
        m_Amount = new AmountFS(other.m_Amount);
        m_AdditionalInfo = other.m_AdditionalInfo;
        m_Size = other.m_Size;
        m_Optional = other.m_Optional;
    }

    @Override
    public String getIngredient() {
        return m_Ingredient;
    }

    @Override
    public void setIngredient(@NonNull String strIngredient) {
        m_Ingredient = strIngredient;
    }

    @Override
    public Amount getAmount() {
        return m_Amount;
    }

    @Override
    public void setAmount(@NonNull Amount amount) {
        m_Amount = (AmountFS) amount;
    }

    @Override
    public String getAdditionalInfo() {
        return m_AdditionalInfo;
    }

    @Override
    public void setAdditionInfo(@NonNull String additionalInfo) {
        m_AdditionalInfo = additionalInfo;
    }

    @Override
    public Size getSize() {
        return m_Size;
    }

    @Override
    public void setSize(@NonNull Size size) {
        m_Size = size;
    }

    @Override
    public boolean isOptional() {
        return m_Optional;
    }

    @Override
    public void setIsOptional(boolean optional)
    {
        m_Optional = optional;
    }
}
