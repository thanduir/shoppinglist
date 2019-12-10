package ch.phwidmer.einkaufsliste.data;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Optional;

import ch.phwidmer.einkaufsliste.helper.Helper;

public abstract class Ingredients
{
    public static final String c_strProvenanceEverywhere = "*EVERYWHERE*";

    public abstract class Ingredient implements Helper.NamedObject
    {
        public abstract String getCategory();
        public abstract void setCategory(@NonNull String cateogry);

        public abstract String getProvenance();
        public abstract void setProvenance(@NonNull String strProvenance);

        public abstract Unit getDefaultUnit();
        public abstract void setDefaultUnit(@NonNull Unit unit);
    }

    public abstract Optional<Ingredient> addIngredient(@NonNull String strName, @NonNull Unit defaultUnit, @NonNull Categories.Category category);
    public abstract void removeIngredient(@NonNull Ingredient ingredient);
    public abstract void renameIngredient(@NonNull Ingredient ingredient, @NonNull String strNewName);
    public abstract Optional<Ingredient> getIngredient(String strName);

    public abstract int getIngredientsCount();
    public abstract ArrayList<Ingredient> getAllIngredients();
    public abstract ArrayList<String> getAllIngredientNames();

    public abstract void onCategoryRenamed(@NonNull String oldCategory, @NonNull String newCategory);
    public abstract void onSortOrderRenamed(@NonNull String oldSortOrder, @NonNull String newSortOrder);

    public abstract boolean isCategoryInUse(@NonNull Categories.Category category, @NonNull ArrayList<String> ingredientsUsingCategory);
    public abstract boolean isSortOrderInUse(@NonNull Categories.SortOrder sortOrder, @NonNull ArrayList<String> ingredientsUsingSortOrder);
}
