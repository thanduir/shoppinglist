package ch.phwidmer.einkaufsliste.data.db_based.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ch.phwidmer.einkaufsliste.data.db_based.entities.recipe.RecipeItemGroupRow;
import ch.phwidmer.einkaufsliste.data.db_based.entities.recipe.RecipeItemRow;
import ch.phwidmer.einkaufsliste.data.db_based.entities.recipe.RecipeRow;

@Dao
public interface RecipesDao {

    // Recipe

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertRecipe(RecipeRow user);

    @Query("SELECT RecipeRow.id FROM RecipeRow ORDER BY nameSortable COLLATE NOCASE ASC")
    long[] getAllRecipeIds();

    @Query("SELECT RecipeRow.name FROM RecipeRow ORDER BY nameSortable COLLATE NOCASE ASC")
    List<String> getAllRecipeNames();

    @Query("SELECT RecipeRow.name FROM RecipeRow WHERE id = :id")
    String getRecipeName(long id);

    @Query("SELECT RecipeRow.numberOfPersons FROM RecipeRow WHERE id = :id")
    int getNumberOfPersons(long id);

    @Query("SELECT * FROM RecipeRow WHERE id = :id")
    RecipeRow getRecipe(long id);

    @Query("SELECT RecipeRow.id FROM RecipeRow WHERE name LIKE :name")
    long[] getRecipeIds(String name);

    @Update
    void updateRecipes(RecipeRow... recipes);

    @Delete
    void deleteRecipes(RecipeRow... recipeItems);

    // RecipeItemRow

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertRecipeItem(RecipeItemRow user);

    @Query("SELECT * FROM RecipeItemRow WHERE ingredientID = :ingredient")
    RecipeItemRow[] getRecipeItemsWithIngredient(long ingredient);

    @Query("SELECT RecipeItemRow.ingredientID FROM RecipeItemRow WHERE id = :id")
    long getRecipeItemIngredient(long id);

    @Query("SELECT RecipeItemRow.additionalInfo FROM RecipeItemRow WHERE id = :id")
    String getRecipeItemAdditionalInfo(long id);

    @Query("SELECT RecipeItemRow.size FROM RecipeItemRow WHERE id = :id")
    String getRecipeItemSize(long id);

    @Query("SELECT RecipeItemRow.optional FROM RecipeItemRow WHERE id = :id")
    boolean getRecipeItemOptional(long id);

    @Query("SELECT * FROM RecipeItemRow WHERE id = :id")
    RecipeItemRow getRecipeItem(long id);

    @Query("SELECT RecipeItemRow.id FROM RecipeItemRow WHERE recipeID = :recipeID AND ingredientID = :ingredientID")
    long[] getRecipeItemIds(long recipeID, long ingredientID);

    @Query("SELECT RecipeItemRow.id FROM RecipeItemRow WHERE recipeID = :recipeID AND groupID = -1")
    long[] getRecipeItemIdsWithoutGroup(long recipeID);

    @Query("SELECT RecipeItemRow.id FROM RecipeItemRow WHERE groupID = :groupID")
    long[] getRecipeItemIdsFromGroup(long groupID);

    @Query("SELECT RecipeItemRow.* FROM RecipeItemRow WHERE groupID = :groupID")
    RecipeItemRow[] getRecipeItemsOfGroup(long groupID);

    @Update
    void updateRecipeItems(RecipeItemRow... recipeItems);

    @Delete
    void deleteRecipeItems(RecipeItemRow... recipeItems);

    // RecipeItemGroupRow

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRecipeItemGroup(RecipeItemGroupRow user);

    @Query("SELECT RecipeItemGroupRow.id FROM RecipeItemGroupRow WHERE recipeID = :recipeID AND name LIKE :name")
    long[] getRecipeItemGroupIds(long recipeID, String name);

    @Query("SELECT * FROM RecipeItemGroupRow WHERE recipeId = :recipeId AND name LIKE :name ORDER BY nameSortable COLLATE NOCASE ASC")
    RecipeItemGroupRow getRecipeItemGroup(long recipeId, String name);

    @Query("SELECT RecipeItemGroupRow.name FROM RecipeItemGroupRow WHERE recipeId = :recipeId ORDER BY nameSortable COLLATE NOCASE ASC")
    List<String> getAllRecipeItemGroupNames(long recipeId);

    @Update
    void updateRecipeItemGroups(RecipeItemGroupRow... recipeItemGroups);

    @Delete
    void deleteRecipeItemGroup(RecipeItemGroupRow user);
}
