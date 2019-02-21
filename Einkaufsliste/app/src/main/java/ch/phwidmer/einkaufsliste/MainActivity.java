package ch.phwidmer.einkaufsliste;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity
{
    // TODO: Überprüfen, dass alle Activities auch mit leeren Daten zurechtkommen! (nach dem serialisierung etc. eingebaut ist. Evtl. brauche ich dann auch einen Debug-Button zum Resetten der Daten (u/o "mit irgendwas füllen")
    /* TODO: Nötige Werte in Configuration (ALLE SUCHEN!):
            * Ingredients.Provenance
            * Std.-Wert für Ingredient.Ingredient.m_DefaultUnit
            * Std-Wert für Recipes.Recipe.m_NumberOfPersons
     */
    // TODO: Macht es Sinn, in jedem Dialog OK und CANCEL zu haben oder sollten z.T. die Änderungen *immer* übernommen werden? ZUMINDEST SOLLTE ICH BEI CANCEL ODER BACKBUTTON EINE BENUTZERABFRAGE EINBAUEN, ODER NICHT? (back-button handling funktioniert atm sowieso nicht und muss geändert werden!!)
    // TODO: Braucht es in (einzelnen) Activites noch Reset-Methoden? Oder eine "alles resetten" Methode in der Config?
    // TODO: Handling von gelöschten Daten in späteren Activites (i.e. wenn eine ID (String) nicht mehr existiert)!
    // TODO: StateLoad/Save (cf. Links).
    // TODO(?) Liste von nicht-abgehakten Ingredients der Einkaufsliste
    // TODO: Provenance und optional-Flag in GoShopping beachten...
    // TODO: Make sure all UI strings are in strings.xml (esp. those used directly from code instead of xml)!
    // TODO: TODOs in anderen Dateien!
    // TODO: TODO.txt erstellen und alle TODOs aus den java-Dateien entfernen!
    // TODO: Code-Doku, wo nötig / sinnvoll!
    // TODO: DesignDokuemnt.txt aktualisieren!

    public static final String EXTRA_CATEGORIES = "ch.phwidmer.einkaufsliste.CATEGORIES";
    public static final String EXTRA_INGREDIENTS = "ch.phwidmer.einkaufsliste.INGREDIENTS";
    public static final String EXTRA_RECIPES = "ch.phwidmer.einkaufsliste.RECIPES";
    public static final String EXTRA_SHOPPINGLIST = "ch.phwidmer.einkaufsliste.SHOPPINGLIST";
    private final int REQUEST_CODE_ManageCategories = 1;
    private final int REQUEST_CODE_ManageIngredients = 2;
    private final int REQUEST_CODE_ManageRecipes = 3;
    private final int REQUEST_CODE_ManageShoppingList = 4;
    private final int REQUEST_CODE_GoShopping = 5;

    private final String c_strFilename = "einkaufsliste.json";

    private GroceryPlanning m_GroceryPlanning;

    private File getAppDataDirectory()
    {
        // TODO: Ich sollte das am Besten durch getExternalFilesDir ersetzen, dann habe ich aber keinen Zugriff mehr im Device Explorer! -> Korrektur suchen!
        return getFilesDir();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File file = new File(getAppDataDirectory(), c_strFilename);
        if(file.exists())
        {
            m_GroceryPlanning = new GroceryPlanning(file, this);
        }
        else
        {
            m_GroceryPlanning = new GroceryPlanning();
        }

    }

    private void saveStateToFile()
    {
        File file = new File(getAppDataDirectory(), c_strFilename);
        m_GroceryPlanning.saveDataToFile(file);
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
        Intent intent = new Intent(this, GoShoppingActivity.class);
        intent.putExtra(EXTRA_SHOPPINGLIST, m_GroceryPlanning.m_ShoppingList);
        intent.putExtra(EXTRA_CATEGORIES, m_GroceryPlanning.m_Categories);
        intent.putExtra(EXTRA_INGREDIENTS, m_GroceryPlanning.m_Ingredients);
        startActivityForResult(intent, REQUEST_CODE_GoShopping);
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
        else if(requestCode == REQUEST_CODE_GoShopping)
        {
            m_GroceryPlanning.m_ShoppingList = data.getParcelableExtra(EXTRA_SHOPPINGLIST);
        }

        saveStateToFile();
    }

    protected void onPause()
    {
        saveStateToFile();
        super.onPause();
    }

    public void onExport(View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save as");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strFilename = input.getText().toString();
                if(!strFilename.endsWith(".json"))
                {
                    strFilename += ".json";
                }

                File file = new File(getAppDataDirectory(), strFilename);
                m_GroceryPlanning.saveDataToFile(file);
                Toast.makeText(MainActivity.this, "Data saved to " + strFilename, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void onImport(View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Import file");

        // Set up the input
        final Spinner input = new Spinner(this);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);

        File directory = getAppDataDirectory();
        for(File f : directory.listFiles())
        {
            if(!f.getName().endsWith(".json") || f.getName().equals(c_strFilename))
            {
                continue;
            }

            adapter.add(f.getName());
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        input.setAdapter(adapter);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strFilename = input.getSelectedItem().toString();

                File file = new File(getAppDataDirectory(), strFilename);
                m_GroceryPlanning.loadDataFromFile(file, MainActivity.this);
                Toast.makeText(MainActivity.this, "Data loaded from " + strFilename, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}
