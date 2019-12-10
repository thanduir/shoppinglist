package ch.phwidmer.einkaufsliste.data.db_based.entities.ingredient;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import ch.phwidmer.einkaufsliste.data.db_based.entities.category.CategoryRow;

import static androidx.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(entity = CategoryRow.class,
                                  parentColumns = "id",
                                  childColumns = "category",
                                  onDelete = CASCADE))
public class IngredientRow {
    @ColumnInfo
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo
    public String name;

    @ColumnInfo(index = true)
    public long category;

    @ColumnInfo
    public long provenance;

    @ColumnInfo
    public String defaultUnit;
}
