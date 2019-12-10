package ch.phwidmer.einkaufsliste.data.db_based.entities.category;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(foreignKeys = {@ForeignKey(entity = SortOrderRow.class,
                                   parentColumns = "id",
                                   childColumns = "sortOrderId",
                                   onDelete = CASCADE),
                       @ForeignKey(entity = CategoryRow.class,
                                   parentColumns = "id",
                                   childColumns = "categoryId",
                                   onDelete = CASCADE)})
public class CategorySortOrderRow {
    @ColumnInfo
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(index = true)
    public long sortOrderId;

    @ColumnInfo(index = true)
    public long categoryId;

    @ColumnInfo
    public long position;
}
