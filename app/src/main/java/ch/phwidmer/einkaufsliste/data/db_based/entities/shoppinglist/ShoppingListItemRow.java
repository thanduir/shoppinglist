package ch.phwidmer.einkaufsliste.data.db_based.entities.shoppinglist;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import ch.phwidmer.einkaufsliste.data.db_based.entities.ingredient.IngredientRow;

import static androidx.room.ForeignKey.CASCADE;

@Entity(foreignKeys = {@ForeignKey(entity = ShoppingRecipeRow.class,
                                   parentColumns = "id",
                                   childColumns = "shoppingRecipeID",
                                   onDelete = CASCADE),
                       @ForeignKey(entity = IngredientRow.class,
                                   parentColumns = "id",
                                   childColumns = "ingredientID",
                                   onDelete = CASCADE)})
public class ShoppingListItemRow
{
    @ColumnInfo
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(index = true)
    public long shoppingRecipeID;

    @ColumnInfo(index = true)
    public long ingredientID;

    @ColumnInfo
    public float amountMin;

    @ColumnInfo
    public float amountMax;

    @ColumnInfo
    public String amountUnit;

    @ColumnInfo
    public String additionalInfo;

    @ColumnInfo
    public String size;

    @ColumnInfo
    public boolean optional;

    @ColumnInfo
    public String status;
}
