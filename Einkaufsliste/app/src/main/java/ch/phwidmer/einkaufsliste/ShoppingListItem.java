package ch.phwidmer.einkaufsliste;

public class ShoppingListItem {
    // TODO: Ggfs. anpassen!
    public enum Status
    {
        None,
        Taken;
    }

    private RecipeItem m_Item;
    private Status m_Status;
}
