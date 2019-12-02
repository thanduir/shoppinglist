package ch.phwidmer.einkaufsliste.data;

import android.support.annotation.NonNull;

public abstract class ShoppingListItem
{
    public enum Status
    {
        None,
        Taken
    }

    public abstract Status getStatus();
    public abstract void setStatus(@NonNull Status status);

    public abstract String getIngredient();
    public abstract void setIngredient(@NonNull String strIngredient);

    public abstract Amount getAmount();
    public abstract void setAmount(@NonNull Amount amount);

    public abstract String getAdditionalInfo();
    public abstract void setAdditionInfo(@NonNull String additionalInfo);

    public abstract RecipeItem.Size getSize();
    public abstract void setSize(@NonNull RecipeItem.Size size);

    public abstract boolean isOptional();
    public abstract void setIsOptional(boolean optional);

    void invertStatus()
    {
        if(getStatus() == Status.None)
        {
            setStatus(Status.Taken);
        }
        else
        {
            setStatus(Status.None);
        }
    }
}
