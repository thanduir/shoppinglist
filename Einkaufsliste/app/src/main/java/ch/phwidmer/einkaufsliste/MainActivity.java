package ch.phwidmer.einkaufsliste;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity
{
    // TODO: Überprüfen, dass alle Activities auch mit leeren Daten zurechtkommen! (nach dem serialisierung etc. eingebaut ist. Evtl. brauche ich dann auch einen Debug-Button zum Resetten der Daten (u/o "mit irgendwas füllen")
    // TODO: Suchen, wieviele standard-werte notwendig sind und ob ich dafür einen Configuration anlegen sollte!
    // TODO: Code-Doku!
    // TODO: DesignDokuemnt.txt aktualisieren!
    // TODO: Macht es Sinn, in jedem Dialog OK und CANCEL zu haben oder sollten z.T. die Änderungen *immer* übernommen werden? ZUMINDEST SOLLTE ICH BEI CANCEL ODER BACKBUTTON EINE BENUTZERABFRAGE EINBAUEN, ODER NICHT? (back-button handling funktioniert atm sowieso nicht und muss geändert werden!!)
    // TODO: Braucht es in (einzelnen) Activites noch Reset-Methoden? Oder eine "alles resetten" Methode in der Config?

    public static final String EXTRA_CATEGORIES = "ch.phwidmer.einkaufsliste.CATEGORIES";
    public static final String EXTRA_INGREDIENTS = "ch.phwidmer.einkaufsliste.INGREDIENTS";
    public static final String EXTRA_RECIPES = "ch.phwidmer.einkaufsliste.RECIPES";
    public static final String EXTRA_SHOPPINGLIST = "ch.phwidmer.einkaufsliste.SHOPPINGLIST";
    private final int REQUEST_CODE_ManageCategories = 1;
    private final int REQUEST_CODE_ManageIngredients = 2;
    private final int REQUEST_CODE_ManageRecipes = 3;
    private final int REQUEST_CODE_ManageShoppingList = 4;

    private GroceryPlanning m_GroceryPlanning;

    // TODO: Serialization etc.

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_GroceryPlanning = new GroceryPlanning();
    }

    public void manageCategories(View view)
    {
        Intent intent = new Intent(this, ManageCategories.class);
        intent.putExtra(EXTRA_CATEGORIES, m_GroceryPlanning.m_Categories);
        startActivityForResult(intent, REQUEST_CODE_ManageCategories);
    }

    public void manageIngredients(View view)
    {
        Intent intent = new Intent(this, ManageIngredients.class);
        intent.putExtra(EXTRA_CATEGORIES, m_GroceryPlanning.m_Categories);
        intent.putExtra(EXTRA_INGREDIENTS, m_GroceryPlanning.m_Ingredients);
        startActivityForResult(intent, REQUEST_CODE_ManageIngredients);
    }

    public void manageRecipies(View view)
    {
        Intent intent = new Intent(this, ManageRecipes.class);
        intent.putExtra(EXTRA_INGREDIENTS, m_GroceryPlanning.m_Ingredients);
        intent.putExtra(EXTRA_RECIPES, m_GroceryPlanning.m_Recipes);
        startActivityForResult(intent, REQUEST_CODE_ManageRecipes);
    }

    public void editShoppingList(View view)
    {
        Intent intent = new Intent(this, ManageShoppingList.class);
        intent.putExtra(EXTRA_SHOPPINGLIST, m_GroceryPlanning.m_ShoppingList);
        intent.putExtra(EXTRA_RECIPES, m_GroceryPlanning.m_Recipes);
        intent.putExtra(EXTRA_INGREDIENTS, m_GroceryPlanning.m_Ingredients);
        startActivityForResult(intent, REQUEST_CODE_ManageShoppingList);
    }

    public void goShopping(View view)
    {
        // TODO
        /*Intent intent = new Intent(this, ManageRecipes.class);
        intent.putExtra(EXTRA_INGREDIENTS, m_GroceryPlanning.m_Ingredients);
        intent.putExtra(EXTRA_RECIPES, m_GroceryPlanning.m_Recipes);
        startActivityForResult(intent, REQUEST_CODE_ManageRecipes);*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode != RESULT_OK)
        {
            return;
        }

        if(requestCode == REQUEST_CODE_ManageCategories)
        {
            m_GroceryPlanning.m_Categories = data.getParcelableExtra(EXTRA_CATEGORIES);
        }
        else if(requestCode == REQUEST_CODE_ManageIngredients)
        {
            m_GroceryPlanning.m_Ingredients = data.getParcelableExtra(EXTRA_INGREDIENTS);
        }
        else if(requestCode == REQUEST_CODE_ManageRecipes)
        {
            m_GroceryPlanning.m_Recipes = data.getParcelableExtra(EXTRA_RECIPES);
        }
        else if(requestCode == REQUEST_CODE_ManageShoppingList)
        {
            m_GroceryPlanning.m_ShoppingList = data.getParcelableExtra(EXTRA_SHOPPINGLIST);
        }
    }
}
