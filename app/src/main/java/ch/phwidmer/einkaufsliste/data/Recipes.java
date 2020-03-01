package ch.phwidmer.einkaufsliste.data;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Optional;

import ch.phwidmer.einkaufsliste.helper.Helper;

public abstract class Recipes
{
    public abstract class Recipe implements Helper.NamedObject
    {
        public abstract int getNumberOfPersons();
        public abstract void setNumberOfPersons(int number);

        public abstract Optional<RecipeItem> addRecipeItem(@NonNull String strIngredient);
        public abstract void removeRecipeItem(@NonNull RecipeItem r);
        public abstract ArrayList<RecipeItem> getAllRecipeItems();

        // Groups

        public abstract void addGroup(@NonNull String strName);
        public abstract void removeGroup(@NonNull String strName);
        public abstract void renameGroup(@NonNull String strOldName, @NonNull String strNewName);
        public abstract ArrayList<String> getAllGroupNames();

        public abstract Optional<RecipeItem> addRecipeItemToGroup(@NonNull String strGroup, @NonNull String strIngredient);
        public abstract void removeRecipeItemFromGroup(@NonNull String strGroup, @NonNull RecipeItem r);
        public abstract ArrayList<RecipeItem> getAllRecipeItemsInGroup(@NonNull String strGroup);
    }

    public abstract Optional<Recipe> addRecipe(@NonNull String strName, int iNrPersons);
    public abstract void removeRecipe(@NonNull Recipe r);
    public abstract void renameRecipe(@NonNull Recipe recipe, @NonNull String strNewName);
    public abstract void copyRecipe(@NonNull Recipe recipe, @NonNull String strNewName);
    public abstract Optional<Recipe> getRecipe(@NonNull String strName);

    public abstract ArrayList<Recipe> getAllRecipes();
    public abstract ArrayList<String> getAllRecipeNames();

    public abstract boolean isIngredientInUse(@NonNull Ingredients.Ingredient ingredient, @NonNull ArrayList<String> recipesUsingIngredient);
    public abstract void onIngredientRenamed(@NonNull String strIngredient, @NonNull String strNewName);
}
