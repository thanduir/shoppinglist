package ch.phwidmer.einkaufsliste.data.db_based;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ch.phwidmer.einkaufsliste.data.Categories;
import ch.phwidmer.einkaufsliste.data.Ingredients;
import ch.phwidmer.einkaufsliste.data.Unit;
import ch.phwidmer.einkaufsliste.data.db_based.entities.ingredient.IngredientRow;
import ch.phwidmer.einkaufsliste.helper.Helper;

public class IngredientsDB extends Ingredients {
    private AppDatabase database;

    public class IngredientDB extends Ingredients.Ingredient
    {
        private long m_Id;

        IngredientDB(long id)
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
            if(!(other instanceof IngredientDB))
            {
                return false;
            }
            IngredientDB i = (IngredientDB)other;
            return this.m_Id == i.m_Id;
        }

        @Override
        public @NonNull String getName()
        {
            return database.ingredientsDao().getIngredientName(m_Id);
        }

        @Override
        public String getCategory()
        {
            long id = database.ingredientsDao().getIngredientCategory(m_Id);
            return database.categoriesDao().getCategoryName(id);
        }
        @Override
        public void setCategory(@NonNull String category)
        {
            IngredientRow row = database.ingredientsDao().getIngredient(m_Id);
            long[] ids = database.categoriesDao().getCategoryIds(category);
            row.category = ids[0];
            database.ingredientsDao().updateIngredient(row);
        }

        @Override
        public String getProvenance()
        {
            long id = database.ingredientsDao().getIngredientProvenance(m_Id);
            if(id == -1)
            {
                return c_strProvenanceEverywhere;
            }
            return database.categoriesDao().getSortOrderName(id);
        }
        @Override
        public void setProvenance(@NonNull String strProvenance)
        {
            IngredientRow row = database.ingredientsDao().getIngredient(m_Id);
            if(strProvenance.equals(c_strProvenanceEverywhere))
            {
                row.provenance = -1;
            }
            else
            {
                long[] ids = database.categoriesDao().getSortOrderIds(strProvenance);
                row.provenance = ids[0];
            }
            database.ingredientsDao().updateIngredient(row);
        }

        @Override
        public Unit getDefaultUnit()
        {
            String strUnit = database.ingredientsDao().getIngredientDefaultUnit(m_Id);
            return Unit.valueOf(strUnit);
        }
        @Override
        public void setDefaultUnit(@NonNull Unit unit)
        {
            IngredientRow row = database.ingredientsDao().getIngredient(m_Id);
            row.defaultUnit = unit.toString();
            database.ingredientsDao().updateIngredient(row);
        }
    }

    IngredientsDB(AppDatabase database)
    {
        this.database = database;
    }

    @Override
    public Optional<Ingredients.Ingredient> addIngredient(@NonNull String strName, @NonNull Unit defaultUnit, @NonNull Categories.Category category)
    {
        if(database.ingredientsDao().getIngredientIds(strName).length > 0)
        {
            return Optional.empty();
        }

        IngredientRow row = new IngredientRow();

        CategoriesDB.CategoryDB categoryDB = (CategoriesDB.CategoryDB)category;

        row.name = strName;
        row.nameSortable = Helper.stripAccents(strName);
        row.category = categoryDB.getId();
        row.provenance = -1;
        row.defaultUnit = defaultUnit.toString();

        long id = database.ingredientsDao().insertIngredient(row);
        return Optional.of(new IngredientDB(id));
    }

    @Override
    public void removeIngredient(@NonNull Ingredients.Ingredient ingredient)
    {
        IngredientDB ingredientDB = (IngredientDB)ingredient;
        IngredientRow row = database.ingredientsDao().getIngredient(ingredientDB.getId());
        database.ingredientsDao().deleteIngredient(row);
    }

    @Override
    public void renameIngredient(@NonNull Ingredients.Ingredient ingredient, @NonNull String strNewName)
    {
        IngredientDB ingredientDB = (IngredientDB)ingredient;
        IngredientRow row = database.ingredientsDao().getIngredient(ingredientDB.getId());
        row.name = strNewName;
        row.nameSortable = Helper.stripAccents(strNewName);
        database.ingredientsDao().updateIngredient(row);
    }

    @Override
    public Optional<Ingredients.Ingredient> getIngredient(String strName)
    {
        long[] ids = database.ingredientsDao().getIngredientIds(strName);
        if(ids.length == 0)
        {
            return Optional.empty();
        }
        return Optional.of(new IngredientDB(ids[0]));
    }

    @Override
    public int getIngredientsCount()
    {
        return (int)database.ingredientsDao().getIngredientsCount();
    }
    @Override
    public ArrayList<Ingredients.Ingredient> getAllIngredients()
    {
        long[] ids = database.ingredientsDao().getAllIngredientIds();
        ArrayList<Ingredient> vec = new ArrayList<>();
        for(long id : ids)
        {
            vec.add(new IngredientDB(id));
        }
        return vec;
    }
    @Override
    public ArrayList<String> getAllIngredientNames()
    {
        List<String> names = database.ingredientsDao().getAllIngredientNames();
        return new ArrayList<>(names);
    }

    @Override
    public void onCategoryRenamed(@NonNull String oldCategory, @NonNull String newCategory)
    {
        // Nothing to do here as we rely only on category ids anyways
    }

    @Override
    public void onSortOrderRenamed(@NonNull String oldSortOrder, @NonNull String newSortOrder)
    {
        // Nothing to do here as we rely only on sortorder ids anyways
    }

    @Override
    public boolean isCategoryInUse(@NonNull Categories.Category category, @NonNull ArrayList<String> ingredientsUsingCategory)
    {
        CategoriesDB.CategoryDB categoryDB = (CategoriesDB.CategoryDB)category;
        IngredientRow[] rows = database.ingredientsDao().getIngredientsWithCategory(categoryDB.getId());
        if(rows.length == 0)
        {
            return false;
        }

        for(IngredientRow row : rows)
        {
            ingredientsUsingCategory.add(row.name);
        }
        return true;
    }

    @Override
    public boolean isSortOrderInUse(@NonNull Categories.SortOrder sortOrder, @NonNull ArrayList<String> ingredientsUsingSortOrder)
    {
        CategoriesDB.SortOrderDB sortOrderDB = (CategoriesDB.SortOrderDB)sortOrder;
        IngredientRow[] rows = database.ingredientsDao().getIngredientsWithProvenance(sortOrderDB.getId());
        if(rows.length == 0)
        {
            return false;
        }

        for(IngredientRow row : rows)
        {
            ingredientsUsingSortOrder.add(row.name);
        }
        return true;
    }
}
