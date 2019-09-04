package ch.phwidmer.einkaufsliste.data;

public abstract class ShoppingListItem
{
    public enum Status
    {
        None,
        Taken
    }

    public abstract Status getStatus();
    public abstract void setStatus(Status status);

    public abstract String getIngredient();
    public abstract void setIngredient(String strIngredient);

    public abstract Amount getAmount();
    public abstract void setAmount(Amount amount);

    public abstract String getAdditionalInfo();
    public abstract void setAdditionInfo(String additionalInfo);

    public abstract RecipeItem.Size getSize();
    public abstract void setSize(RecipeItem.Size size);

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
