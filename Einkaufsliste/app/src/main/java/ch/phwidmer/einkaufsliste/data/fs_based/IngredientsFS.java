package ch.phwidmer.einkaufsliste.data.fs_based;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Optional;
import java.util.TreeSet;

import ch.phwidmer.einkaufsliste.data.Amount;
import ch.phwidmer.einkaufsliste.data.Categories;
import ch.phwidmer.einkaufsliste.data.Ingredients;
import ch.phwidmer.einkaufsliste.helper.Helper;

public class IngredientsFS extends Ingredients
{
    public class IngredientFS extends Ingredients.Ingredient
    {
        String m_Name;
        private String m_Category = "";
        private String m_Provenance = c_strProvenanceEverywhere;
        private AmountFS.Unit m_DefaultUnit;

        IngredientFS(@NonNull String name)
        {
            m_Name = name;
        }

        @Override
        public @NonNull String getName()
        {
            return m_Name;
        }

        @Override
        public String getCategory()
        {
            return m_Category;
        }
        @Override
        public void setCategory(@NonNull String category)
        {
            m_Category = category;
        }

        @Override
        public String getProvenance()
        {
            return m_Provenance;
        }
        @Override
        public void setProvenance(@NonNull String strProvenance)
        {
            m_Provenance = strProvenance;
        }

        @Override
        public Amount.Unit getDefaultUnit()
        {
            return m_DefaultUnit;
        }
        @Override
        public void setDefaultUnit(@NonNull Amount.Unit unit)
        {
            m_DefaultUnit = unit;
        }
    }
    private TreeSet<IngredientFS> m_Ingredients;

    IngredientsFS()
    {
        m_Ingredients = new TreeSet<>(new Helper.SortNamedIgnoreCase());
    }

    @Override
    public Optional<Ingredient> addIngredient(@NonNull String strName, @NonNull Amount.Unit defaultUnit, @NonNull Categories.Category category)
    {
        if(getIngredient(strName).isPresent())
        {
            return Optional.empty();
        }
        IngredientFS i = new IngredientFS(strName);
        i.m_DefaultUnit = defaultUnit;
        i.m_Category = category.getName();
        m_Ingredients.add(i);
        return Optional.of(i);
    }

    @Override
    protected Optional<Ingredient> addIngredient(@NonNull String strName, @NonNull Amount.Unit defaultUnit, @NonNull String strCategory)
    {
        if(getIngredient(strName).isPresent())
        {
            return Optional.empty();
        }

        IngredientFS i = new IngredientFS(strName);
        i.m_DefaultUnit = defaultUnit;
        i.m_Category = strCategory;
        m_Ingredients.add(i);
        return Optional.of(i);
    }

    @Override
    public void removeIngredient(@NonNull Ingredient ingredient)
    {
        IngredientFS ingredientFS = (IngredientFS)ingredient;
        m_Ingredients.remove(ingredientFS);
    }

    @Override
    public void renameIngredient(@NonNull Ingredient ingredient, @NonNull String strNewName)
    {
        if(getIngredient(strNewName).isPresent())
        {
            return;
        }

        IngredientFS i = (IngredientFS)ingredient;
        i.m_Name = strNewName;
    }

    @Override
    public Optional<Ingredient> getIngredient(@NonNull String strName)
    {
        for(IngredientFS ingredient : m_Ingredients)
        {
            if(ingredient.getName().equals(strName))
            {
                return Optional.of(ingredient);
            }
        }

        return Optional.empty();
    }

    @Override
    public int getIngredientsCount()
    {
        return m_Ingredients.size();
    }

    @Override
    public ArrayList<Ingredient> getAllIngredients()
    {
        return new ArrayList<>(m_Ingredients);
    }

    @Override
    public ArrayList<String> getAllIngredientNames()
    {
        ArrayList<String> vec = new ArrayList<>();
        for(IngredientFS ingredient : m_Ingredients)
        {
            vec.add(ingredient.getName());
        }
        return vec;
    }
}
