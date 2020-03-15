package ch.phwidmer.einkaufsliste.data.db_based;

import android.support.annotation.NonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ch.phwidmer.einkaufsliste.data.Amount;
import ch.phwidmer.einkaufsliste.data.Ingredients;
import ch.phwidmer.einkaufsliste.data.RecipeItem;
import ch.phwidmer.einkaufsliste.data.Recipes;
import ch.phwidmer.einkaufsliste.data.ShoppingList;
import ch.phwidmer.einkaufsliste.data.ShoppingListItem;
import ch.phwidmer.einkaufsliste.data.db_based.entities.recipe.RecipeItemRow;
import ch.phwidmer.einkaufsliste.data.db_based.entities.shoppinglist.ShoppingListItemRow;
import ch.phwidmer.einkaufsliste.data.db_based.entities.shoppinglist.ShoppingRecipeRow;
import ch.phwidmer.einkaufsliste.helper.Helper;

public class ShoppingListDB extends ShoppingList
{
    private AppDatabase database;

    public class ShoppingRecipeDB extends ShoppingList.ShoppingRecipe
    {
        private long m_Id;

        ShoppingRecipeDB(long id)
        {
            m_Id = id;
        }

        public long getId()
        {
            return m_Id;
        }

        @Override
        public boolean equals(Object other)
        {
            if(!(other instanceof ShoppingRecipeDB))
            {
                return false;
            }
            ShoppingRecipeDB i = (ShoppingRecipeDB)other;
            return this.m_Id == i.m_Id;
        }

        @Override
        public String getName()
        {
            return database.shoppingListDao().getShoppingRecipeName(m_Id);
        }

        // TODO: Implement due date correctly!
        @Override
        public LocalDate getDueDate()
        {
            return LocalDate.now();
        }
        @Override
        public void setDueDate(LocalDate date)
        {
        }

        // Current scaling factor used for the items in the list.
        @Override
        public float getScalingFactor()
        {
            return database.shoppingListDao().getShoppingRecipeScalingFactor(m_Id);
        }
        @Override
        public void setScalingFactor(float factor)
        {
            ShoppingRecipeRow row = database.shoppingListDao().getShoppingRecipe(m_Id);
            row.scalingFactor = factor;
            database.shoppingListDao().updateShoppingRecipes(row);
        }

        @Override
        public Optional<ShoppingListItem> addItem(@NonNull String strIngredient)
        {
            long[] ingredientIds = database.ingredientsDao().getIngredientIds(strIngredient);
            if(ingredientIds.length == 0)
            {
                return Optional.empty();
            }

            if(database.shoppingListDao().getShoppingListItemIds(m_Id, ingredientIds[0]).length > 0)
            {
                return Optional.empty();
            }

            ShoppingListItemRow row = new ShoppingListItemRow();

            row.shoppingRecipeID = m_Id;
            row.ingredientID = ingredientIds[0];
            Amount amount = new Amount();
            row.amountMin = amount.getQuantityMin();
            row.amountMax = amount.getQuantityMax();
            row.amountUnit = amount.getUnit().toString();
            row.additionalInfo = "";
            row.size = RecipeItem.Size.Normal.toString();
            row.optional = false;
            row.status = ShoppingListItem.Status.None.toString();

            long id = database.shoppingListDao().insertShoppingListItem(row);
            return Optional.of(new ShoppingListItemDB(database, id));
        }
        @Override
        public void addItem(@NonNull RecipeItem recipeItem)
        {
            RecipeItemDB recipeItemDB = (RecipeItemDB)recipeItem;
            RecipeItemRow recipeItemRow = database.recipesDao().getRecipeItem(recipeItemDB.getId());

            if(database.shoppingListDao().getShoppingListItemIds(m_Id, recipeItemRow.ingredientID).length > 0)
            {
                return;
            }

            ShoppingListItemRow row = new ShoppingListItemRow();

            row.shoppingRecipeID = m_Id;
            row.ingredientID = recipeItemRow.ingredientID;
            row.amountMin = recipeItemRow.amountMin;
            row.amountMax = recipeItemRow.amountMax;
            row.amountUnit = recipeItemRow.amountUnit;
            row.additionalInfo = recipeItemRow.additionalInfo;
            row.size = recipeItemRow.size;
            row.optional = recipeItemRow.optional;
            row.status = ShoppingListItem.Status.None.toString();

            database.shoppingListDao().insertShoppingListItem(row);
        }
        @Override
        public void removeItem(@NonNull ShoppingListItem r)
        {
            ShoppingListItemDB recipeItemDB = (ShoppingListItemDB)r;
            ShoppingListItemRow row = database.shoppingListDao().getShoppingListItem(recipeItemDB.getId());
            database.shoppingListDao().deleteShoppingListItems(row);
        }
        @Override
        public ArrayList<ShoppingListItem> getAllItems()
        {
            long[] ids = database.shoppingListDao().getShoppingListItemIdsFromRecipe(m_Id);
            ArrayList<ShoppingListItem> vec = new ArrayList<>();
            for(long id : ids)
            {
                vec.add(new ShoppingListItemDB(database, id));
            }
            return vec;
        }

        @Override
        public void changeScalingFactor(float f)
        {
            ShoppingRecipeRow row = database.shoppingListDao().getShoppingRecipe(m_Id);
            row.scalingFactor = f / row.scalingFactor;
            database.shoppingListDao().updateShoppingRecipes(row);

            for(ShoppingListItem sli : getAllItems())
            {
                Amount amount = sli.getAmount();
                amount.scaleAmount(row.scalingFactor);
                sli.setAmount(amount);
            }
        }
    }

    ShoppingListDB(AppDatabase database)
    {
        this.database = database;
    }

    @Override
    public Optional<ShoppingList.ShoppingRecipe> addNewRecipe(@NonNull String strName)
    {
        if(database.shoppingListDao().getShoppingRecipeIds(strName).length > 0)
        {
            return Optional.empty();
        }

        ShoppingRecipeRow row = new ShoppingRecipeRow();

        row.name = strName;
        row.nameSortable = Helper.stripAccents(strName);
        row.scalingFactor = 0.0f;

        long id = database.shoppingListDao().insertShoppingRecipe(row);
        return Optional.of(new ShoppingRecipeDB(id));
    }

    @Override
    public void addFromRecipe(@NonNull String strName, @NonNull Recipes.Recipe recipe)
    {
        if(database.shoppingListDao().getShoppingRecipeIds(strName).length > 0)
        {
            return;
        }

        ShoppingRecipeRow row = new ShoppingRecipeRow();
        row.name = strName;
        row.nameSortable = Helper.stripAccents(strName);
        row.scalingFactor = recipe.getNumberOfPersons();
        long id = database.shoppingListDao().insertShoppingRecipe(row);

        ShoppingRecipeDB newRecipe = new ShoppingRecipeDB(id);
        for(RecipeItem r : recipe.getAllRecipeItems())
        {
            newRecipe.addItem(r);
        }
    }

    @Override
    public Optional<ShoppingList.ShoppingRecipe> getShoppingRecipe(@NonNull String strName)
    {
        long[] ids = database.shoppingListDao().getShoppingRecipeIds(strName);
        if(ids.length == 0)
        {
            return Optional.empty();
        }
        return Optional.of(new ShoppingRecipeDB(ids[0]));
    }
    @Override
    public void renameShoppingRecipe(@NonNull ShoppingList.ShoppingRecipe recipe, @NonNull String strNewName)
    {
        ShoppingRecipeDB recipeDB = (ShoppingRecipeDB)recipe;
        ShoppingRecipeRow row = database.shoppingListDao().getShoppingRecipe(recipeDB.getId());
        row.name = strNewName;
        row.nameSortable = Helper.stripAccents(strNewName);
        database.shoppingListDao().updateShoppingRecipes(row);
    }
    @Override
    public void removeShoppingRecipe(@NonNull ShoppingList.ShoppingRecipe recipe)
    {
        ShoppingRecipeDB recipeDB = (ShoppingRecipeDB)recipe;
        ShoppingRecipeRow row = database.shoppingListDao().getShoppingRecipe(recipeDB.getId());
        database.shoppingListDao().deleteShoppingRecipes(row);
    }

    @Override
    public ArrayList<ShoppingList.ShoppingRecipe> getAllShoppingRecipes()
    {
        long[] ids = database.shoppingListDao().getAllShoppingRecipeIds();
        ArrayList<ShoppingList.ShoppingRecipe> vec = new ArrayList<>();
        for(long id : ids)
        {
            vec.add(new ShoppingRecipeDB(id));
        }
        return vec;
    }
    @Override
    public ArrayList<String> getAllShoppingRecipeNames()
    {
        List<String> names = database.shoppingListDao().getAllShoppingRecipeNames();
        return new ArrayList<>(names);
    }

    @Override
    public void clearShoppingList()
    {
        database.shoppingListDao().clearShoppingRecipes();
    }

    @Override
    public boolean isIngredientInUse(@NonNull Ingredients.Ingredient ingredient, @NonNull ArrayList<String> shoppingListItemUsingIngredient)
    {
        IngredientsDB.IngredientDB ingredientDB = (IngredientsDB.IngredientDB)ingredient;
        ShoppingListItemRow[] rows = database.shoppingListDao().getShoppingListItemsWithIngredient(ingredientDB.getId());
        if(rows.length == 0)
        {
            return false;
        }

        for(ShoppingListItemRow row : rows)
        {
            String recipe = database.shoppingListDao().getShoppingRecipeName(row.shoppingRecipeID);
            shoppingListItemUsingIngredient.add(recipe);
        }
        return true;
    }

    @Override
    public void onIngredientRenamed(@NonNull String strIngredient, @NonNull String strNewName)
    {
        // Nothing to do here as we rely only on ingredient ids anyways
    }
}
