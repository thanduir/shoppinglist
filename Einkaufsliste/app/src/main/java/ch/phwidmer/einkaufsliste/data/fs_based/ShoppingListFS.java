package ch.phwidmer.einkaufsliste.data.fs_based;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import ch.phwidmer.einkaufsliste.data.RecipeItem;
import ch.phwidmer.einkaufsliste.data.ShoppingList;
import ch.phwidmer.einkaufsliste.data.ShoppingListItem;
import ch.phwidmer.einkaufsliste.helper.Helper;

public class ShoppingListFS extends ShoppingList
{
    public class ShoppingRecipeFS extends ShoppingRecipe
    {
        private String m_Name;
        private float m_fScalingFactor = 0.0f;
        private LinkedList<ShoppingListItemFS> m_Items = new LinkedList<>();

        ShoppingRecipeFS(String strName)
        {
            m_Name = strName;
        }

        @Override
        public String getName()
        {
            return m_Name;
        }

        // Current scaling factor used for the items in the list.
        @Override
        public float getScalingFactor()
        {
            return m_fScalingFactor;
        }
        @Override
        public void setScalingFactor(float factor)
        {
            m_fScalingFactor = factor;
        }

        @Override
        public ShoppingListItem addItem(String strIngredient)
        {
            for(ShoppingListItemFS item : m_Items)
            {
                if(item.getIngredient().equals(strIngredient))
                {
                    return null;
                }
            }

            ShoppingListItemFS r = new ShoppingListItemFS(strIngredient);
            m_Items.add(r);
            return r;
        }
        @Override
        public void addItem(RecipeItem recipeItem)
        {
            for(ShoppingListItemFS item : m_Items)
            {
                if(item.getIngredient().equals(recipeItem.getIngredient()))
                {
                    return;
                }
            }

            ShoppingListItemFS r = new ShoppingListItemFS(recipeItem);
            m_Items.add(r);
        }
        @Override
        public void addItem(int position, ShoppingListItem item)
        {
            m_Items.add(position, (ShoppingListItemFS)item);
        }
        @Override
        public void removeItem(ShoppingListItem item)
        {
            m_Items.remove((ShoppingListItemFS)item);
        }
        @Override
        public ArrayList<ShoppingListItem> getAllItems()
        {
            return new ArrayList<>(m_Items);
        }

    }

    private LinkedHashSet<ShoppingRecipeFS> m_ShoppingRecipies;

    ShoppingListFS()
    {
        m_ShoppingRecipies = new LinkedHashSet<>();
    }

    @Override
    protected ShoppingRecipe addRecipe(String strName)
    {
        if(getShoppingRecipe(strName) != null)
        {
            return null;
        }
        ShoppingRecipeFS r = new ShoppingRecipeFS(strName);
        m_ShoppingRecipies.add(r);
        return r;
    }

    @Override
    public void addExistingShoppingRecipe(ShoppingRecipe recipe)
    {
        if(getShoppingRecipe(recipe.getName()) != null)
        {
            return;
        }

        m_ShoppingRecipies.add((ShoppingRecipeFS)recipe);
    }

    @Override
    public ShoppingRecipe getShoppingRecipe(String strName)
    {
        for(ShoppingRecipeFS recipe : m_ShoppingRecipies)
        {
            if(recipe.getName().equals(strName))
            {
                return recipe;
            }
        }

        return null;
    }

    @Override
    public void renameShoppingRecipe(ShoppingRecipe recipe, String strNewName)
    {
        if(getShoppingRecipe(strNewName) != null)
        {
            return;
        }

        ShoppingRecipeFS r = (ShoppingRecipeFS)recipe;
        r.m_Name = strNewName;
    }

    @Override
    public void removeShoppingRecipe(ShoppingRecipe recipe)
    {
        m_ShoppingRecipies.remove((ShoppingRecipeFS) recipe);
    }

    @Override
    public ArrayList<ShoppingRecipe> getAllShoppingRecipes()
    {
        return new ArrayList<>(m_ShoppingRecipies);
    }
    @Override
    public ArrayList<String> getAllShoppingRecipeNames()
    {
        ArrayList<String> vec = new ArrayList<>();
        for(ShoppingRecipeFS recipe : m_ShoppingRecipies)
        {
            vec.add(recipe.getName());
        }
        Collections.sort(vec, new Helper.SortIgnoreCase());
        return vec;
    }

    @Override
    public void clearShoppingList()
    {
        m_ShoppingRecipies.clear();
    }
}
