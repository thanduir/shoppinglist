package ch.phwidmer.einkaufsliste.data.fs_based;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Optional;

import ch.phwidmer.einkaufsliste.data.RecipeItem;
import ch.phwidmer.einkaufsliste.data.ShoppingList;
import ch.phwidmer.einkaufsliste.data.ShoppingListItem;

public class ShoppingListFS extends ShoppingList
{
    public class ShoppingRecipeFS extends ShoppingRecipe
    {
        private String m_Name;
        private float m_fScalingFactor = 0.0f;
        private LinkedList<ShoppingListItemFS> m_Items = new LinkedList<>();

        ShoppingRecipeFS(@NonNull String strName)
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
        public Optional<ShoppingListItem> addItem(@NonNull String strIngredient)
        {
            for(ShoppingListItemFS item : m_Items)
            {
                if(item.getIngredient().equals(strIngredient))
                {
                    return Optional.empty();
                }
            }

            ShoppingListItemFS r = new ShoppingListItemFS(strIngredient);
            m_Items.add(r);
            return Optional.of(r);
        }
        @Override
        public void addItem(@NonNull RecipeItem recipeItem)
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
        public void addItem(int position, @NonNull ShoppingListItem item)
        {
            m_Items.add(position, (ShoppingListItemFS)item);
        }
        @Override
        public void removeItem(@NonNull ShoppingListItem item)
        {
            ShoppingListItemFS itemFS = (ShoppingListItemFS)item;
            m_Items.remove(itemFS);
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
    protected Optional<ShoppingRecipe> addRecipe(@NonNull String strName)
    {
        if(getShoppingRecipe(strName).isPresent())
        {
            return Optional.empty();
        }
        ShoppingRecipeFS r = new ShoppingRecipeFS(strName);
        m_ShoppingRecipies.add(r);
        return Optional.of(r);
    }

    @Override
    public void addExistingShoppingRecipe(@NonNull ShoppingRecipe recipe)
    {
        if(getShoppingRecipe(recipe.getName()).isPresent())
        {
            return;
        }

        m_ShoppingRecipies.add((ShoppingRecipeFS)recipe);
    }

    @Override
    public Optional<ShoppingRecipe> getShoppingRecipe(@NonNull String strName)
    {
        for(ShoppingRecipeFS recipe : m_ShoppingRecipies)
        {
            if(recipe.getName().equals(strName))
            {
                return Optional.of(recipe);
            }
        }

        return Optional.empty();
    }

    @Override
    public void renameShoppingRecipe(@NonNull ShoppingRecipe recipe, @NonNull String strNewName)
    {
        if(getShoppingRecipe(strNewName).isPresent())
        {
            return;
        }

        ShoppingRecipeFS r = (ShoppingRecipeFS)recipe;
        r.m_Name = strNewName;
    }

    @Override
    public void removeShoppingRecipe(@NonNull ShoppingRecipe recipe)
    {
        ShoppingRecipeFS recipeFS = (ShoppingRecipeFS)recipe;
        m_ShoppingRecipies.remove(recipeFS);
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
        return vec;
    }

    @Override
    public void clearShoppingList()
    {
        m_ShoppingRecipies.clear();
    }
}
