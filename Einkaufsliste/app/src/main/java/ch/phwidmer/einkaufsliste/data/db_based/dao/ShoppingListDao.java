package ch.phwidmer.einkaufsliste.data.db_based.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ch.phwidmer.einkaufsliste.data.db_based.entities.shoppinglist.ShoppingListItemRow;
import ch.phwidmer.einkaufsliste.data.db_based.entities.shoppinglist.ShoppingRecipeRow;

@Dao
public interface ShoppingListDao {

    // ShoppingRecipeRow

    @Query("DELETE FROM ShoppingRecipeRow")
    void clearShoppingRecipes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertShoppingRecipe(ShoppingRecipeRow recipeRow);

    @Query("SELECT ShoppingRecipeRow.id FROM ShoppingRecipeRow ORDER BY nameSortable COLLATE NOCASE ASC")
    long[] getAllShoppingRecipeIds();

    @Query("SELECT ShoppingRecipeRow.name FROM ShoppingRecipeRow ORDER BY nameSortable COLLATE NOCASE ASC")
    List<String> getAllShoppingRecipeNames();

    @Query("SELECT ShoppingRecipeRow.name FROM ShoppingRecipeRow WHERE id = :id")
    String getShoppingRecipeName(long id);

    @Query("SELECT ShoppingRecipeRow.scalingFactor FROM ShoppingRecipeRow WHERE id = :id")
    int getShoppingRecipeScalingFactor(long id);

    @Query("SELECT * FROM ShoppingRecipeRow WHERE id = :id")
    ShoppingRecipeRow getShoppingRecipe(long id);

    @Query("SELECT ShoppingRecipeRow.id FROM ShoppingRecipeRow WHERE name LIKE :name")
    long[] getShoppingRecipeIds(String name);

    @Update
    void updateShoppingRecipes(ShoppingRecipeRow... recipes);

    @Delete
    void deleteShoppingRecipes(ShoppingRecipeRow... recipeItems);

    // ShoppingListItemRow

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertShoppingListItem(ShoppingListItemRow user);

    @Query("SELECT * FROM ShoppingListItemRow WHERE id = :id")
    ShoppingListItemRow getShoppingListItem(long id);

    @Query("SELECT ShoppingListItemRow.status FROM ShoppingListItemRow WHERE id = :id")
    String getShoppingListItemStatus(long id);

    @Query("SELECT ShoppingListItemRow.ingredientID FROM ShoppingListItemRow WHERE id = :id")
    long getShoppingListItemIngredient(long id);

    @Query("SELECT ShoppingListItemRow.additionalInfo FROM ShoppingListItemRow WHERE id = :id")
    String getShoppingListItemAdditionalInfo(long id);

    @Query("SELECT ShoppingListItemRow.size FROM ShoppingListItemRow WHERE id = :id")
    String getShoppingListItemSize(long id);

    @Query("SELECT ShoppingListItemRow.optional FROM ShoppingListItemRow WHERE id = :id")
    boolean getShoppingListItemOptional(long id);

    @Query("SELECT * FROM ShoppingListItemRow WHERE ingredientID = :ingredient")
    ShoppingListItemRow[] getShoppingListItemsWithIngredient(long ingredient);

    @Query("SELECT ShoppingListItemRow.id FROM ShoppingListItemRow WHERE shoppingRecipeID = :recipeID")
    long[] getShoppingListItemIdsFromRecipe(long recipeID);

    @Query("SELECT ShoppingListItemRow.id FROM ShoppingListItemRow WHERE shoppingRecipeID = :recipeID AND ingredientID = :ingredientID")
    long[] getShoppingListItemIds(long recipeID, long ingredientID);

    @Update
    void updateShoppingListItems(ShoppingListItemRow... recipeItems);

    @Delete
    void deleteShoppingListItems(ShoppingListItemRow... recipeItems);
}
