package ch.phwidmer.einkaufsliste;

import android.content.DialogInterface;
import android.content.Intent;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity
{
    // TODO: TODOs in anderen Dateien!
    // TODO: TODO.txt erstellen und alle TODOs aus den java-Dateien entfernen! (Oder wo sammle ich das am Besten? Im Projektview rechts sehe ich die Datei dann nicht mehr...)
    // TODO: Speicherort der Dateien? (MainActivity.m_AppDataDirectory: Ich sollte das am Besten durch getExternalFilesDir ersetzen, dann habe ich aber keinen Zugriff mehr im Device Explorer! -> Korrektur suchen!)
    // TODO: Data synchronisation between different devices (in the same network?) possible?
    // TODO: Überprüfen, dass alle Activities auch mit leeren Daten zurechtkommen! (nach dem serialisierung etc. eingebaut ist. Evtl. brauche ich dann auch einen Debug-Button zum Resetten der Daten (u/o "mit irgendwas füllen")
    /* TODO: Nötige Werte in Configuration (ALLE SUCHEN!): -> sollte ich evtl. einfach (mit Tabs) das jetzige ManageCategories in eine Art Configuration umbauen?
            * Ingredients.Provenance -> Dynamische Liste, Daten definiert in Einstellungen
            * Std.-Wert für Ingredient.Ingredient.m_DefaultUnit
            * Std-Wert für Recipes.Recipe.m_NumberOfPersons
            * Default-SortOrder aus ShoppingList?
     */
    // TODO: Activities verbessern: ManageCategories, ManageIngredients, Manage Recipes, Manage ShoppingList, Edit ShoppingList Recipe, GoShopping
    // TODO: Macht es Sinn, in jedem Dialog OK und CANCEL zu haben oder sollten z.T. die Änderungen *immer* übernommen werden? ZUMINDEST SOLLTE ICH BEI CANCEL ODER BACKBUTTON EINE BENUTZERABFRAGE EINBAUEN, ODER NICHT? (back-button handling funktioniert atm sowieso nicht und muss geändert werden!!)
    // TODO: Braucht es in (einzelnen) Activites noch Reset-Methoden? Oder eine "alles resetten" Methode in der Config?
    // TODO: Handling von gelöschten Daten in späteren Activites (i.e. wenn eine ID (String) nicht mehr existiert)!
    // TODO: StateLoad/Save (cf. Links).
    // TODO: Schauen, dass es für verschiedene FormFactors funktioniert! (mein natel, nicoles natel, tablet)
    // TODO(?) Liste von nicht-abgehakten Ingredients der Einkaufsliste
    // TODO: Provenance und optional-Flag in GoShopping beachten...
    // TODO: Make sure all UI strings are in strings.xml (esp. those used directly from code instead of xml)!
    // TODO: Code-Doku, wo nötig / sinnvoll!
    // TODO: DesignDokuemnt.txt aktualisieren!

    public static final String EXTRA_SAVEFILESPATH = "ch.phwidmer.einkaufsliste.SAVEFILESPATH";

    private GroceryPlanning m_GroceryPlanning;

    private final String c_strTestDataFilename = "testData.json";

    public static final String c_strSaveFilename = "einkaufsliste.json";
    private File m_AppDataDirectory = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_AppDataDirectory = getFilesDir();

        writeTestDataFileIfNotPresent();

        File file = new File(m_AppDataDirectory, c_strSaveFilename);
        if(file.exists())
        {
            m_GroceryPlanning = new GroceryPlanning(file, this);
        }
        else
        {
            m_GroceryPlanning = new GroceryPlanning();
            m_GroceryPlanning.saveDataToFile(file);
        }

    }

    private void writeTestDataFileIfNotPresent()
    {
        File testDataFile = new File(m_AppDataDirectory, c_strTestDataFilename);
        if(!testDataFile.exists())
        {
            try {
                InputStream dataInputStream = getResources().openRawResource(R.raw.testdata);
                OutputStream output = new FileOutputStream(testDataFile);

                int read = 0;
                byte[] bytes = new byte[1024];

                while ((read = dataInputStream.read(bytes)) != -1) {
                    output.write(bytes, 0, read);
                }

                output.close();
            }
            catch(IOException exception)
            {
            }
        }
    }

    public void manageCategories(View view)
    {
        Intent intent = new Intent(this, ManageCategories.class);
        intent.putExtra(EXTRA_SAVEFILESPATH, m_AppDataDirectory.getPath());
        startActivity(intent);
    }

    public void manageIngredients(View view)
    {
        Intent intent = new Intent(this, ManageIngredients.class);
        intent.putExtra(EXTRA_SAVEFILESPATH, m_AppDataDirectory.getPath());
        startActivity(intent);
    }

    public void manageRecipies(View view)
    {
        Intent intent = new Intent(this, ManageRecipes.class);
        intent.putExtra(EXTRA_SAVEFILESPATH, m_AppDataDirectory.getPath());
        startActivity(intent);
    }

    public void editShoppingList(View view)
    {
        Intent intent = new Intent(this, ManageShoppingList.class);
        intent.putExtra(EXTRA_SAVEFILESPATH, m_AppDataDirectory.getPath());
        startActivity(intent);
    }

    public void goShopping(View view)
    {
        Intent intent = new Intent(this, GoShoppingActivity.class);
        intent.putExtra(EXTRA_SAVEFILESPATH, m_AppDataDirectory.getPath());
        startActivity(intent);
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

                File file = new File(m_AppDataDirectory, strFilename);
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

        File directory = m_AppDataDirectory;
        for(File f : directory.listFiles())
        {
            if(!f.getName().endsWith(".json") || f.getName().equals(c_strSaveFilename))
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

                File file = new File(m_AppDataDirectory, strFilename);
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
