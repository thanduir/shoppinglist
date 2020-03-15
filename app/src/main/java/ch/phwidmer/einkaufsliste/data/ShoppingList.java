package ch.phwidmer.einkaufsliste.data;

import android.support.annotation.NonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

public abstract class ShoppingList
{
    public static abstract class ShoppingRecipe
    {
        public abstract String getName();

        public abstract LocalDate getDueDate();
        public abstract void setDueDate(LocalDate date);

        // Current scaling factor used for the items in the list.
        public abstract float getScalingFactor();
        public abstract void setScalingFactor(float factor);

        public abstract Optional<ShoppingListItem> addItem(@NonNull String strIngredient);
        public abstract void addItem(@NonNull RecipeItem recipeItem);
        public abstract void removeItem(@NonNull ShoppingListItem r);
        public abstract ArrayList<ShoppingListItem> getAllItems();

        public abstract void changeScalingFactor(float f);
    }

    public abstract Optional<ShoppingRecipe> addNewRecipe(@NonNull String strName);
    public abstract void addFromRecipe(@NonNull String strName, @NonNull Recipes.Recipe recipe);

    public abstract Optional<ShoppingRecipe> getShoppingRecipe(@NonNull String strName);
    public abstract void renameShoppingRecipe(@NonNull ShoppingRecipe recipe, @NonNull String strNewName);
    public abstract void removeShoppingRecipe(@NonNull ShoppingRecipe recipe);

    public abstract ArrayList<ShoppingRecipe> getAllShoppingRecipes();
    public abstract ArrayList<String> getAllShoppingRecipeNames();

    public abstract void clearShoppingList();

    public abstract boolean isIngredientInUse(@NonNull Ingredients.Ingredient ingredient, @NonNull ArrayList<String> shoppingListItemUsingIngredient);
    public abstract void onIngredientRenamed(@NonNull String strIngredient, @NonNull String strNewName);
}
