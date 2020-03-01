package ch.phwidmer.einkaufsliste.data.db_based;

import android.support.annotation.NonNull;

import ch.phwidmer.einkaufsliste.data.Amount;
import ch.phwidmer.einkaufsliste.data.RecipeItem;
import ch.phwidmer.einkaufsliste.data.ShoppingListItem;
import ch.phwidmer.einkaufsliste.data.Unit;
import ch.phwidmer.einkaufsliste.data.db_based.entities.shoppinglist.ShoppingListItemRow;

public class ShoppingListItemDB extends ShoppingListItem
{
    private AppDatabase database;
    private long m_Id;

    ShoppingListItemDB(AppDatabase database, long id)
    {
        this.database = database;
        m_Id = id;
    }

    public long getId()
    {
        return m_Id;
    }

    @Override
    public Status getStatus()
    {
        String strStatus = database.shoppingListDao().getShoppingListItemStatus(m_Id);
        return Status.valueOf(strStatus);
    }
    @Override
    public void setStatus(@NonNull Status status)
    {
        ShoppingListItemRow row = database.shoppingListDao().getShoppingListItem(m_Id);
        row.status = status.toString();
        database.shoppingListDao().updateShoppingListItems(row);
    }
    @Override
    public void invertStatus()
    {
        ShoppingListItemRow row = database.shoppingListDao().getShoppingListItem(m_Id);
        row.status = (Status.valueOf(row.status) == Status.None ? Status.Taken.toString() : Status.None.toString());
        database.shoppingListDao().updateShoppingListItems(row);
    }

    @Override
    public String getIngredient()
    {
        long id = database.shoppingListDao().getShoppingListItemIngredient(m_Id);
        return database.ingredientsDao().getIngredientName(id);
    }
    @Override
    public void setIngredient(@NonNull String strIngredient)
    {
        ShoppingListItemRow row = database.shoppingListDao().getShoppingListItem(m_Id);
        long[] ids = database.ingredientsDao().getIngredientIds(strIngredient);
        row.ingredientID = ids[0];
        database.shoppingListDao().updateShoppingListItems(row);
    }

    @Override
    public Amount getAmount()
    {
        ShoppingListItemRow row = database.shoppingListDao().getShoppingListItem(m_Id);

        Amount amount = new Amount();
        amount.setQuantityMax(row.amountMax);
        amount.setQuantityMin(row.amountMin);
        amount.setUnit(Unit.valueOf(row.amountUnit));

        return amount;
    }
    @Override
    public void setAmount(@NonNull Amount amount)
    {
        ShoppingListItemRow row = database.shoppingListDao().getShoppingListItem(m_Id);

        row.amountMax = amount.getQuantityMax();
        row.amountMin = amount.getQuantityMin();
        row.amountUnit = amount.getUnit().toString();

        database.shoppingListDao().updateShoppingListItems(row);
    }

    @Override
    public String getAdditionalInfo()
    {
        return database.shoppingListDao().getShoppingListItemAdditionalInfo(m_Id);
    }
    @Override
    public void setAdditionInfo(@NonNull String additionalInfo)
    {
        ShoppingListItemRow row = database.shoppingListDao().getShoppingListItem(m_Id);
        row.additionalInfo = additionalInfo;
        database.shoppingListDao().updateShoppingListItems(row);
    }

    @Override
    public RecipeItem.Size getSize()
    {
        String strSize = database.shoppingListDao().getShoppingListItemSize(m_Id);
        return RecipeItem.Size.valueOf(strSize);
    }
    @Override
    public void setSize(@NonNull RecipeItem.Size size)
    {
        ShoppingListItemRow row = database.shoppingListDao().getShoppingListItem(m_Id);
        row.size = size.toString();
        database.shoppingListDao().updateShoppingListItems(row);
    }

    @Override
    public boolean isOptional()
    {
        return database.shoppingListDao().getShoppingListItemOptional(m_Id);
    }
    @Override
    public void setIsOptional(boolean optional)
    {
        ShoppingListItemRow row = database.shoppingListDao().getShoppingListItem(m_Id);
        row.optional = optional;
        database.shoppingListDao().updateShoppingListItems(row);
    }
}
