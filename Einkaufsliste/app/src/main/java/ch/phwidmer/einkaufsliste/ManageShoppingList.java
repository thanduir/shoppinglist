package ch.phwidmer.einkaufsliste;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ManageShoppingList extends AppCompatActivity {

    private ShoppingList m_ShoppingList;

    // TODO: Std-Vorgehen (einzige Möglichkeit!) sollte es sein, ein schon definiertes Rezept hinzuzufügen (die Rezepte können aber danach natürlich noch angepasst werden).
    // TODO: Ein Reset-Button (+ eine entsprechende Funktion hier) sollte im Activity auch vorhanden sein

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_shopping_list);

        Intent intent = getIntent();
        m_ShoppingList = (ShoppingList)intent.getParcelableExtra(MainActivity.EXTRA_SHOPPINGLIST);
    }
}
