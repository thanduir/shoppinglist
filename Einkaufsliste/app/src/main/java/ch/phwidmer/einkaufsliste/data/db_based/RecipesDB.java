package ch.phwidmer.einkaufsliste.data.db_based;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ch.phwidmer.einkaufsliste.data.Amount;
import ch.phwidmer.einkaufsliste.data.Ingredients;
import ch.phwidmer.einkaufsliste.data.RecipeItem;
import ch.phwidmer.einkaufsliste.data.Recipes;
import ch.phwidmer.einkaufsliste.data.db_based.entities.recipe.RecipeItemGroupRow;
import ch.phwidmer.einkaufsliste.data.db_based.entities.recipe.RecipeItemRow;
import ch.phwidmer.einkaufsliste.data.db_based.entities.recipe.RecipeRow;

public class RecipesDB extends Recipes {
    private AppDatabase database;

    public class RecipeDB extends Recipes.Recipe
    {
        private long m_Id;

        RecipeDB(long id)
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
            if(!(other instanceof RecipeDB))
            {
                return false;
            }
            RecipeDB i = (RecipeDB)other;
            return this.m_Id == i.m_Id;
        }

        @Override
        public @NonNull String getName()
        {
            return database.recipesDao().getRecipeName(m_Id);
        }

        @Override
        public int getNumberOfPersons()
        {
            return database.recipesDao().getNumberOfPersons(m_Id);
        }
        @Override
        public void setNumberOfPersons(int number)
        {
            RecipeRow row = database.recipesDao().getRecipe(m_Id);
            row.numberOfPersons = number;
            database.recipesDao().updateRecipes(row);
        }

        // RecipeItems

        @Override
        public Optional<RecipeItem> addRecipeItem(@NonNull String strIngredient)
        {
            return addRecipeItem(strIngredient, -1);
        }
        @Override
        public void removeRecipeItem(@NonNull RecipeItem r)
        {
            RecipeItemDB recipeItemDB = (RecipeItemDB)r;
            RecipeItemRow row = database.recipesDao().getRecipeItem(recipeItemDB.getId());
            database.recipesDao().deleteRecipeItems(row);
        }
        @Override
        public ArrayList<RecipeItem> getAllRecipeItems()
        {
            long[] ids = database.recipesDao().getRecipeItemIdsWithoutGroup(m_Id);
            ArrayList<RecipeItem> vec = new ArrayList<>();
            for(long id : ids)
            {
                vec.add(new RecipeItemDB(database, id));
            }
            return vec;
        }

        // Groups

        @Override
        public void addGroup(@NonNull String strName)
        {
            if(database.recipesDao().getRecipeItemGroupIds(m_Id, strName).length > 0)
            {
                return;
            }

            RecipeItemGroupRow row = new RecipeItemGroupRow();

            row.name = strName;
            row.recipeID = m_Id;

            database.recipesDao().insertRecipeItemGroup(row);
        }
        @Override
        public void removeGroup(@NonNull String strName)
        {
            RecipeItemGroupRow row = database.recipesDao().getRecipeItemGroup(m_Id, strName);
            long groupId = row.id;
            database.recipesDao().deleteRecipeItemGroup(row);

            RecipeItemRow[] recipeItemRows = database.recipesDao().getRecipeItemsOfGroup(groupId);
            database.recipesDao().deleteRecipeItems(recipeItemRows);
        }
        @Override
        public void renameGroup(@NonNull String strOldName, @NonNull String strNewName)
        {
            RecipeItemGroupRow row = database.recipesDao().getRecipeItemGroup(m_Id, strOldName);
            row.name = strNewName;
            database.recipesDao().updateRecipeItemGroups(row);
        }
        @Override
        public ArrayList<String> getAllGroupNames()
        {
            List<String> names = database.recipesDao().getAllRecipeItemGroupNames(m_Id);
            return new ArrayList<>(names);
        }

        @Override
        public Optional<RecipeItem> addRecipeItemToGroup(@NonNull String strGroup, @NonNull String strIngredient)
        {
            long[] ids = database.recipesDao().getRecipeItemGroupIds(m_Id, strGroup);
            if(ids.length == 0)
            {
                return Optional.empty();
            }
            return addRecipeItem(strIngredient, ids[0]);
        }
        @Override
        public void removeRecipeItemFromGroup(@NonNull String strGroup, @NonNull RecipeItem r)
        {
            RecipeItemDB recipeItemDB = (RecipeItemDB)r;
            RecipeItemRow row = database.recipesDao().getRecipeItem(recipeItemDB.getId());
            database.recipesDao().deleteRecipeItems(row);
        }
        @Override
        public ArrayList<RecipeItem> getAllRecipeItemsInGroup(@NonNull String strGroup)
        {
            long[] groupIDs = database.recipesDao().getRecipeItemGroupIds(m_Id, strGroup);
            if(groupIDs.length == 0)
            {
                return new ArrayList<>();
            }

            long[] ids = database.recipesDao().getRecipeItemIdsFromGroup(groupIDs[0]);

            ArrayList<RecipeItem> vec = new ArrayList<>();
            for(long id : ids)
            {
                vec.add(new RecipeItemDB(database, id));
            }
            return vec;
        }

        Optional<RecipeItem> addRecipeItem(@NonNull String strIngredient, long groupID)
        {
            long[] ingredientIds = database.ingredientsDao().getIngredientIds(strIngredient);
            if(ingredientIds.length == 0)
            {
                return Optional.empty();
            }

            if(database.recipesDao().getRecipeItemIds(m_Id, ingredientIds[0]).length > 0)
            {
                return Optional.empty();
            }

            RecipeItemRow row = new RecipeItemRow();

            row.recipeID = m_Id;
            row.groupID = groupID;
            row.ingredientID = ingredientIds[0];
            Amount amount = new Amount();
            row.amountMin = amount.getQuantityMin();
            row.amountMax = amount.getQuantityMax();
            row.amountUnit = amount.getUnit().toString();
            row.additionalInfo = "";
            row.size = RecipeItem.Size.Normal.toString();
            row.optional = false;

            long id = database.recipesDao().insertRecipeItem(row);
            return Optional.of(new RecipeItemDB(database, id));
        }

        void addRecipeItem(RecipeItem item, long groupID)
        {
            long[] ingredientIds = database.ingredientsDao().getIngredientIds(item.getIngredient());
            if(ingredientIds.length == 0)
            {
                return;
            }

            if(database.recipesDao().getRecipeItemIds(m_Id, ingredientIds[0]).length > 0)
            {
                return;
            }

            RecipeItemRow row = new RecipeItemRow();

            row.recipeID = m_Id;
            row.groupID = groupID;
            row.ingredientID = ingredientIds[0];
            Amount amount = item.getAmount();
            row.amountMin = amount.getQuantityMin();
            row.amountMax = amount.getQuantityMax();
            row.amountUnit = amount.getUnit().toString();
            row.additionalInfo = item.getAdditionalInfo();
            row.size = item.getSize().toString();
            row.optional = item.isOptional();

            database.recipesDao().insertRecipeItem(row);
        }
    }

    RecipesDB(AppDatabase database)
    {
        this.database = database;
    }

    @Override
    public Optional<Recipes.Recipe> addRecipe(@NonNull String strName, int iNrPersons)
    {
        if(database.recipesDao().getRecipeIds(strName).length > 0)
        {
            return Optional.empty();
        }

        RecipeRow row = new RecipeRow();

        row.name = strName;
        row.numberOfPersons = iNrPersons;

        long id = database.recipesDao().insertRecipe(row);
        return Optional.of(new RecipeDB(id));
    }
    @Override
    public void removeRecipe(@NonNull Recipes.Recipe r)
    {
        RecipeDB recipeDB = (RecipeDB)r;
        RecipeRow row = database.recipesDao().getRecipe(recipeDB.getId());
        database.recipesDao().deleteRecipes(row);
    }
    @Override
    public void renameRecipe(@NonNull Recipes.Recipe recipe, @NonNull String strNewName)
    {
        RecipeDB recipeDB = (RecipeDB)recipe;
        RecipeRow row = database.recipesDao().getRecipe(recipeDB.getId());
        row.name = strNewName;
        database.recipesDao().updateRecipes(row);
    }
    @Override
    public void copyRecipe(@NonNull Recipes.Recipe recipe, @NonNull String strNewName)
    {
        if(database.recipesDao().getRecipeIds(strNewName).length > 0)
        {
            return;
        }

        RecipeRow row = new RecipeRow();
        row.name = strNewName;
        row.numberOfPersons = recipe.getNumberOfPersons();
        long id = database.recipesDao().insertRecipe(row);

        RecipeDB newRecipeDB = new RecipeDB(id);

        for(RecipeItem item : recipe.getAllRecipeItems())
        {
            newRecipeDB.addRecipeItem(item, -1);
        }

        for(String strGroup : recipe.getAllGroupNames())
        {
            newRecipeDB.addGroup(strGroup);
            long[] groupIds = database.recipesDao().getRecipeItemGroupIds(id, strGroup);
            if(groupIds.length == 0)
            {
                continue;
            }

            for(RecipeItem groupItem : recipe.getAllRecipeItemsInGroup(strGroup))
            {
                newRecipeDB.addRecipeItem(groupItem, groupIds[0]);
            }
        }
    }
    @Override
    public Optional<Recipes.Recipe> getRecipe(@NonNull String strName)
    {
        long[] ids = database.recipesDao().getRecipeIds(strName);
        if(ids.length == 0)
        {
            return Optional.empty();
        }
        return Optional.of(new RecipeDB(ids[0]));
    }

    @Override
    public ArrayList<Recipes.Recipe> getAllRecipes()
    {
        long[] ids = database.recipesDao().getAllRecipeIds();
        ArrayList<Recipes.Recipe> vec = new ArrayList<>();
        for(long id : ids)
        {
            vec.add(new RecipeDB(id));
        }
        return vec;
    }
    @Override
    public ArrayList<String> getAllRecipeNames()
    {
        List<String> names = database.recipesDao().getAllRecipeNames();
        return new ArrayList<>(names);
    }

    @Override
    public boolean isIngredientInUse(@NonNull Ingredients.Ingredient ingredient, @NonNull ArrayList<String> recipesUsingIngredient)
    {
        IngredientsDB.IngredientDB ingredientDB = (IngredientsDB.IngredientDB)ingredient;
        RecipeItemRow[] rows = database.recipesDao().getRecipeItemsWithIngredient(ingredientDB.getId());
        if(rows.length == 0)
        {
            return false;
        }

        for(RecipeItemRow row : rows)
        {
            String recipe = database.recipesDao().getRecipeName(row.recipeID);
            recipesUsingIngredient.add(recipe);
        }
        return true;
    }

    @Override
    public void onIngredientRenamed(@NonNull String strIngredient, @NonNull String strNewName)
    {
        // Nothing to do here as we rely only on ingredient ids anyways
    }
}
