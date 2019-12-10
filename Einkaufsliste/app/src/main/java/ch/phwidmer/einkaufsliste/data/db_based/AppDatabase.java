package ch.phwidmer.einkaufsliste.data.db_based;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import ch.phwidmer.einkaufsliste.data.db_based.dao.CategoriesDao;
import ch.phwidmer.einkaufsliste.data.db_based.dao.IngredientsDao;
import ch.phwidmer.einkaufsliste.data.db_based.dao.RecipesDao;
import ch.phwidmer.einkaufsliste.data.db_based.dao.ShoppingListDao;
import ch.phwidmer.einkaufsliste.data.db_based.entities.category.CategoryRow;
import ch.phwidmer.einkaufsliste.data.db_based.entities.category.CategorySortOrderRow;
import ch.phwidmer.einkaufsliste.data.db_based.entities.category.SortOrderRow;
import ch.phwidmer.einkaufsliste.data.db_based.entities.ingredient.IngredientRow;
import ch.phwidmer.einkaufsliste.data.db_based.entities.recipe.RecipeItemGroupRow;
import ch.phwidmer.einkaufsliste.data.db_based.entities.recipe.RecipeItemRow;
import ch.phwidmer.einkaufsliste.data.db_based.entities.recipe.RecipeRow;
import ch.phwidmer.einkaufsliste.data.db_based.entities.shoppinglist.ShoppingListItemRow;
import ch.phwidmer.einkaufsliste.data.db_based.entities.shoppinglist.ShoppingRecipeRow;

@Database(entities={CategoryRow.class, SortOrderRow.class, CategorySortOrderRow.class,
                    IngredientRow.class,
                    RecipeRow.class, RecipeItemRow.class, RecipeItemGroupRow.class,
                    ShoppingRecipeRow.class, ShoppingListItemRow.class},
          version=1)
abstract class AppDatabase extends RoomDatabase {
    static final String DB_NAME = "EinkaufslisteDB";

    abstract CategoriesDao categoriesDao();
    abstract IngredientsDao ingredientsDao();
    abstract RecipesDao recipesDao();
    abstract ShoppingListDao shoppingListDao();
}
