package ch.phwidmer.einkaufsliste.data.fs_based;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
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

        IngredientFS(String name)
        {
            m_Name = name;
        }

        @Override
        public String getName()
        {
            return m_Name;
        }

        @Override
        public String getCategory()
        {
            return m_Category;
        }
        @Override
        public void setCategory(String category)
        {
            m_Category = category;
        }

        @Override
        public String getProvenance()
        {
            return m_Provenance;
        }
        @Override
        public void setProvenance(String strProvenance)
        {
            m_Provenance = strProvenance;
        }

        @Override
        public Amount.Unit getDefaultUnit()
        {
            return m_DefaultUnit;
        }
        @Override
        public void setDefaultUnit(Amount.Unit unit)
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
    public Ingredient addIngredient(String strName, Amount.Unit defaultUnit, Categories.Category category)
    {
        if(getIngredient(strName) != null)
        {
            return null;
        }
        IngredientFS i = new IngredientFS(strName);
        i.m_DefaultUnit = defaultUnit;
        i.m_Category = category.getName();
        m_Ingredients.add(i);
        return i;
    }

    @Override
    protected Ingredient addIngredient(String strName, Amount.Unit defaultUnit, String strCategory)
    {
        if(getIngredient(strName) != null)
        {
            return null;
        }

        IngredientFS i = new IngredientFS(strName);
        i.m_DefaultUnit = defaultUnit;
        i.m_Category = strCategory;
        m_Ingredients.add(i);
        return i;
    }

    @Override
    public void removeIngredient(Ingredient ingredient)
    {
        m_Ingredients.remove((IngredientFS)ingredient);
    }

    @Override
    public void renameIngredient(Ingredient ingredient, String strNewName)
    {
        if(getIngredient(strNewName) != null)
        {
            return;
        }

        IngredientFS i = (IngredientFS)ingredient;
        i.m_Name = strNewName;
    }

    @Override
    public Ingredient getIngredient(String strName)
    {
        for(IngredientFS ingredient : m_Ingredients)
        {
            if(ingredient.getName().equals(strName))
            {
                return ingredient;
            }
        }

        return null;
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
