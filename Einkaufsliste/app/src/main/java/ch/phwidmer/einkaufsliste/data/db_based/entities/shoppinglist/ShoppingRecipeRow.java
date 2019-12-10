package ch.phwidmer.einkaufsliste.data.db_based.entities.shoppinglist;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index("id")})
public class ShoppingRecipeRow
{
    @ColumnInfo
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo
    public String name;

    @ColumnInfo
    public float scalingFactor;
}
