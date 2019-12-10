package ch.phwidmer.einkaufsliste.data.db_based.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ch.phwidmer.einkaufsliste.data.db_based.entities.category.CategoryRow;
import ch.phwidmer.einkaufsliste.data.db_based.entities.category.CategorySortOrderRow;
import ch.phwidmer.einkaufsliste.data.db_based.entities.category.SortOrderRow;

@Dao
public interface CategoriesDao {

    // Categories

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertCategory(CategoryRow user);

    @Query("SELECT Count(CategoryRow.id) FROM CategoryRow")
    long getCategoriesCount();

    @Query("SELECT CategoryRow.id FROM CategoryRow")
    long[] getAllCategoryIds();

    @Query("SELECT CategoryRow.name FROM CategoryRow ORDER BY name COLLATE NOCASE ASC")
    List<String> getAllCategoryNames();

    @Query("SELECT CategoryRow.id FROM CategoryRow WHERE name LIKE :name")
    long[] getCategoryIds(String name);

    @Query("SELECT CategoryRow.name FROM CategoryRow WHERE id = :id")
    String getCategoryName(long id);

    @Query("SELECT * FROM CategoryRow WHERE id = :id")
    CategoryRow getCategory(long id);

    @Update
    void updateCategories(CategoryRow... categories);

    @Delete
    void deleteCategory(CategoryRow user);

    // SortOrders

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSortOrder(SortOrderRow user);

    @Query("SELECT SortOrderRow.name FROM SortOrderRow WHERE id = :id")
    String getSortOrderName(long id);

    @Query("SELECT SortOrderRow.id FROM SortOrderRow ORDER BY name COLLATE NOCASE ASC")
    long[] getAllSortOrderIds();

    @Query("SELECT SortOrderRow.name FROM SortOrderRow ORDER BY name COLLATE NOCASE ASC")
    List<String> getAllSortOrderNames();

    @Query("SELECT SortOrderRow.id FROM SortOrderRow WHERE name LIKE :name")
    long[] getSortOrderIds(String name);

    @Query("SELECT * FROM SortOrderRow WHERE id = :id")
    SortOrderRow getSortOrder(long id);

    @Update
    void updateSortOrders(SortOrderRow... sortOrders);

    @Delete
    void deleteSortOrder(SortOrderRow user);

    // CategorySortOrder

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategorySortOrders(List<CategorySortOrderRow> categorySortOrders);

    @Query("SELECT * FROM CategorySortOrderRow WHERE sortOrderId = :sortOrderId ORDER BY position ASC")
    CategorySortOrderRow[] getCategoriesFromSortOrderIdSortedByPosition(long sortOrderId);

    @Query("SELECT * FROM CategorySortOrderRow WHERE sortOrderId = :sortOrderId ORDER BY categoryId ASC")
    CategorySortOrderRow[] getCategoriesFromSortOrderIdSortedByCategoryId(long sortOrderId);

    @Update
    void updateCategorySortOrderss(CategorySortOrderRow... sortOrders);
}
