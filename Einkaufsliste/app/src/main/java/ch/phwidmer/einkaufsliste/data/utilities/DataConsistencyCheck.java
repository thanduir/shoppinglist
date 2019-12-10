package ch.phwidmer.einkaufsliste.data.utilities;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.LinkedList;

import ch.phwidmer.einkaufsliste.data.GroceryPlanning;
import ch.phwidmer.einkaufsliste.data.Ingredients;
import ch.phwidmer.einkaufsliste.data.RecipeItem;
import ch.phwidmer.einkaufsliste.data.Recipes;
import ch.phwidmer.einkaufsliste.data.ShoppingList;
import ch.phwidmer.einkaufsliste.data.ShoppingListItem;

class DataConsistencyCheck
{
    private GroceryPlanning m_GroceryPlanning;

    DataConsistencyCheck(GroceryPlanning groceryPlanning)
    {
        m_GroceryPlanning = groceryPlanning;
    }

    void checkDataConsistency() throws IOException
    {
        LinkedList<String> missingCategories = new LinkedList<>();
        LinkedList<String> missingSortOrders = new LinkedList<>();
        LinkedList<String> missingIngredients = new LinkedList<>();

        boolean dataConsistent = checkIngredients(missingCategories, missingSortOrders);
        dataConsistent = dataConsistent && checkRecipes(missingIngredients);
        dataConsistent = dataConsistent && checkShoppingList(missingIngredients);

        if(!dataConsistent)
        {
            String strMessage = "Inconsistent data:";
            if(missingCategories.size() > 0)
            {
                strMessage += "\n\nCategories: " + missingCategories.toString();
            }
            if(missingSortOrders.size() > 0)
            {
                strMessage += "\n\nSortOrders: " + missingSortOrders.toString();
            }
            if(missingIngredients.size() > 0)
            {
                strMessage += "\n\nIngredients: " + missingIngredients.toString();
            }
            throw new IOException(strMessage);
        }
    }

    private boolean checkIngredients(@NonNull LinkedList<String> missingCategories, @NonNull LinkedList<String> missingSortOrders)
    {
        boolean dataConsistent = true;

        for(Ingredients.Ingredient ingredient : m_GroceryPlanning.ingredients().getAllIngredients())
        {
            String category = ingredient.getCategory();
            if(!m_GroceryPlanning.categories().getCategory(category).isPresent())
            {
                if(!missingCategories.contains(category))
                {
                    missingCategories.add(category);
                }
                dataConsistent = false;
            }
            String sortOrder = ingredient.getProvenance();
            if(!m_GroceryPlanning.categories().getSortOrder(sortOrder).isPresent() && !sortOrder.equals(Ingredients.c_strProvenanceEverywhere))
            {
                if(!missingSortOrders.contains(sortOrder))
                {
                    missingSortOrders.add(sortOrder);
                }
                dataConsistent = false;
            }
        }

        return dataConsistent;
    }

    private boolean checkRecipes(@NonNull LinkedList<String> missingIngredients)
    {
        boolean dataConsistent = true;
        for(Recipes.Recipe recipe : m_GroceryPlanning.recipes().getAllRecipes())
        {
            for(RecipeItem ri : recipe.getAllRecipeItems())
            {
                String ingredient = ri.getIngredient();
                if(!m_GroceryPlanning.ingredients().getIngredient(ingredient).isPresent())
                {
                    if(!missingIngredients.contains(ingredient))
                    {
                        missingIngredients.add(ingredient);
                    }
                    dataConsistent = false;
                }
            }

            for(String strGroup : recipe.getAllGroupNames())
            {
                for(RecipeItem ri : recipe.getAllRecipeItemsInGroup(strGroup))
                {
                    String ingredient = ri.getIngredient();
                    if(!m_GroceryPlanning.ingredients().getIngredient(ingredient).isPresent())
                    {
                        if(!missingIngredients.contains(ingredient))
                        {
                            missingIngredients.add(ingredient);
                        }
                        dataConsistent = false;
                    }
                }
            }
        }
        return dataConsistent;
    }

    private boolean checkShoppingList(@NonNull LinkedList<String> missingIngredients)
    {
        boolean dataConsistent = true;
        for(ShoppingList.ShoppingRecipe recipe : m_GroceryPlanning.shoppingList().getAllShoppingRecipes())
        {
            for(ShoppingListItem li : recipe.getAllItems())
            {
                String ingredient = li.getIngredient();
                if(!m_GroceryPlanning.ingredients().getIngredient(ingredient).isPresent())
                {
                    if(!missingIngredients.contains(ingredient))
                    {
                        missingIngredients.add(ingredient);
                    }
                    dataConsistent = false;
                }
            }
        }
        return dataConsistent;
    }
}
