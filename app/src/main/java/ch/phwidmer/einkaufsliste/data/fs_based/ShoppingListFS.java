package ch.phwidmer.einkaufsliste.data.fs_based;

import android.support.annotation.NonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Optional;

import ch.phwidmer.einkaufsliste.data.Amount;
import ch.phwidmer.einkaufsliste.data.Ingredients;
import ch.phwidmer.einkaufsliste.data.RecipeItem;
import ch.phwidmer.einkaufsliste.data.Recipes;
import ch.phwidmer.einkaufsliste.data.ShoppingList;
import ch.phwidmer.einkaufsliste.data.ShoppingListItem;

public class ShoppingListFS extends ShoppingList
{
    public static class ShoppingRecipeFS extends ShoppingRecipe
    {
        private String m_Name;
        private float m_fScalingFactor = 0.0f;
        private LocalDate m_LocalDate = LocalDate.now();
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

        @Override
        public LocalDate getDueDate()
        {
            return m_LocalDate;
        }
        @Override
        public void setDueDate(LocalDate date)
        {
            m_LocalDate = date;
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

        @Override
        public void changeScalingFactor(float f)
        {
            float fFactor = f / getScalingFactor();
            setScalingFactor(f);

            for(ShoppingListItem sli : m_Items)
            {
                Amount amount = sli.getAmount();
                amount.scaleAmount(fFactor);
                sli.setAmount(amount);
            }
        }

    }

    private LinkedHashSet<ShoppingRecipeFS> m_ShoppingRecipies;

    ShoppingListFS()
    {
        m_ShoppingRecipies = new LinkedHashSet<>();
    }

    @Override
    public Optional<ShoppingRecipe> addNewRecipe(@NonNull String strName)
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
    public void addFromRecipe(@NonNull String strName, @NonNull Recipes.Recipe recipe)
    {
        if(getShoppingRecipe(strName).isPresent())
        {
            return;
        }

        Optional<ShoppingRecipe> item = addNewRecipe(strName);
        if(!item.isPresent())
        {
            return;
        }
        item.get().setScalingFactor((float)recipe.getNumberOfPersons());
        for(RecipeItem r : recipe.getAllRecipeItems())
        {
            item.get().addItem(r);
        }
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

    @Override
    public boolean isIngredientInUse(@NonNull Ingredients.Ingredient ingredient, @NonNull ArrayList<String> shoppingListItemUsingIngredient)
    {
        boolean stillInUse = false;
        for(ShoppingRecipe recipe : getAllShoppingRecipes())
        {
            for(ShoppingListItem sli : recipe.getAllItems())
            {
                if (sli.getIngredient().equals(ingredient.getName()))
                {
                    if(!shoppingListItemUsingIngredient.contains(recipe.getName()))
                    {
                        shoppingListItemUsingIngredient.add(recipe.getName());
                    }
                    stillInUse = true;
                }
            }
        }
        return stillInUse;
    }

    @Override
    public void onIngredientRenamed(@NonNull String strIngredient, @NonNull String strNewName)
    {
        for(ShoppingRecipeFS sr : m_ShoppingRecipies)
        {
            for(ShoppingListItemFS sli : sr.m_Items)
            {
                if (sli.getIngredient().equals(strIngredient))
                {
                    sli.setIngredient(strNewName);
                }
            }
        }
    }
}
