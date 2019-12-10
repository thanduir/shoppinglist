package ch.phwidmer.einkaufsliste.data.db_based.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ch.phwidmer.einkaufsliste.data.db_based.entities.ingredient.IngredientRow;

@Dao
public interface IngredientsDao {

    @Query("SELECT Count(IngredientRow.id) FROM IngredientRow")
    long getIngredientsCount();

    @Query("SELECT IngredientRow.id FROM IngredientRow ORDER BY name COLLATE NOCASE ASC")
    long[] getAllIngredientIds();

    @Query("SELECT IngredientRow.name FROM IngredientRow ORDER BY name COLLATE NOCASE ASC")
    List<String> getAllIngredientNames();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertIngredient(IngredientRow ingredient);

    @Update
    void updateIngredient(IngredientRow ingredient);

    @Delete
    void deleteIngredient(IngredientRow user);

    @Query("SELECT * FROM IngredientRow WHERE id = :id")
    IngredientRow getIngredient(long id);

    @Query("SELECT IngredientRow.id FROM IngredientRow WHERE name LIKE :name")
    long[] getIngredientIds(String name);

    @Query("SELECT IngredientRow.name FROM IngredientRow WHERE id = :id")
    String getIngredientName(long id);

    @Query("SELECT IngredientRow.category FROM IngredientRow WHERE id = :id")
    long getIngredientCategory(long id);

    @Query("SELECT IngredientRow.provenance FROM IngredientRow WHERE id = :id")
    long getIngredientProvenance(long id);

    @Query("SELECT IngredientRow.defaultUnit FROM IngredientRow WHERE id = :id")
    String getIngredientDefaultUnit(long id);

    @Query("SELECT * FROM IngredientRow WHERE category = :category")
    IngredientRow[] getIngredientsWithCategory(long category);

    @Query("SELECT * FROM IngredientRow WHERE provenance = :sortOrder")
    IngredientRow[] getIngredientsWithProvenance(long sortOrder);
}
