package ch.phwidmer.einkaufsliste.data.db_based.entities.recipe;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(entity = RecipeRow.class,
                                  parentColumns = "id",
                                  childColumns = "recipeID",
                                  onDelete = CASCADE))
public class RecipeItemGroupRow {
    @ColumnInfo
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(index = true)
    public long recipeID;

    @ColumnInfo
    public String name;
}
