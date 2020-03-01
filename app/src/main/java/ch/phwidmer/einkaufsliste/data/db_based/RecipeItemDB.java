package ch.phwidmer.einkaufsliste.data.db_based;

import android.support.annotation.NonNull;

import ch.phwidmer.einkaufsliste.data.Amount;
import ch.phwidmer.einkaufsliste.data.RecipeItem;
import ch.phwidmer.einkaufsliste.data.Unit;
import ch.phwidmer.einkaufsliste.data.db_based.entities.recipe.RecipeItemRow;

public class RecipeItemDB implements RecipeItem {
    private AppDatabase database;
    private long m_Id;

    RecipeItemDB(AppDatabase database, long id)
    {
        this.database = database;
        m_Id = id;
    }

    public long getId()
    {
        return m_Id;
    }

    @Override
    public String getIngredient()
    {
        long id = database.recipesDao().getRecipeItemIngredient(m_Id);
        return database.ingredientsDao().getIngredientName(id);
    }
    @Override
    public void setIngredient(@NonNull String strIngredient)
    {
        RecipeItemRow row = database.recipesDao().getRecipeItem(m_Id);
        long[] ids = database.ingredientsDao().getIngredientIds(strIngredient);
        row.ingredientID = ids[0];
        database.recipesDao().updateRecipeItems(row);
    }

    public Amount getAmount()
    {
        Amount amount = new Amount();
        RecipeItemRow row = database.recipesDao().getRecipeItem(m_Id);
        amount.setQuantityMax(row.amountMax);
        amount.setQuantityMin(row.amountMin);
        amount.setUnit(Unit.valueOf(row.amountUnit));
        return amount;
    }
    @Override
    public void setAmount(@NonNull Amount amount)
    {
        RecipeItemRow row = database.recipesDao().getRecipeItem(m_Id);

        row.amountMax = amount.getQuantityMax();
        row.amountMin = amount.getQuantityMin();
        row.amountUnit = amount.getUnit().toString();

        database.recipesDao().updateRecipeItems(row);
    }

    @Override
    public String getAdditionalInfo()
    {
        return database.recipesDao().getRecipeItemAdditionalInfo(m_Id);
    }
    @Override
    public void setAdditionInfo(@NonNull String strAdditionalInfo)
    {
        RecipeItemRow row = database.recipesDao().getRecipeItem(m_Id);
        row.additionalInfo = strAdditionalInfo;
        database.recipesDao().updateRecipeItems(row);
    }

    @Override
    public Size getSize()
    {
        String strSize = database.recipesDao().getRecipeItemSize(m_Id);
        return Size.valueOf(strSize);
    }
    @Override
    public void setSize(@NonNull Size size)
    {
        RecipeItemRow row = database.recipesDao().getRecipeItem(m_Id);
        row.size = size.toString();
        database.recipesDao().updateRecipeItems(row);
    }

    @Override
    public boolean isOptional()
    {
        return database.recipesDao().getRecipeItemOptional(m_Id);
    }

    @Override
    public void setIsOptional(boolean optional)
    {
        RecipeItemRow row = database.recipesDao().getRecipeItem(m_Id);
        row.optional = optional;
        database.recipesDao().updateRecipeItems(row);
    }
}
