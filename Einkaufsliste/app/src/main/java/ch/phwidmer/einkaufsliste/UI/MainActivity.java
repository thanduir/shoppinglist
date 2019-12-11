package ch.phwidmer.einkaufsliste.UI;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import ch.phwidmer.einkaufsliste.R;
import ch.phwidmer.einkaufsliste.data.DataBackend;
import ch.phwidmer.einkaufsliste.data.GroceryPlanning;
import ch.phwidmer.einkaufsliste.data.GroceryPlanningFactory;
import ch.phwidmer.einkaufsliste.data.utilities.JsonSerializer;
import ch.phwidmer.einkaufsliste.helper.stringInput.InputStringFree;
import ch.phwidmer.einkaufsliste.helper.stringInput.InputStringFromList;
import ch.phwidmer.einkaufsliste.helper.stringInput.InputStringResponder;

public class MainActivity extends AppCompatActivity implements InputStringResponder
{
    private File m_AppDataDirectory = null;
    private GroceryPlanning m_GroceryPlanning;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_AppDataDirectory = getExternalFilesDir(null);

        SettingsActivity.setDefaultPreferencesIfNothingSet(this);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String strBackend = preferences.getString(SettingsActivity.KEY_CURRENT_BACKEND, SettingsActivity.defaultBackend.toString());
        GroceryPlanningFactory.setBackend(DataBackend.valueOf(strBackend));

        try {
            m_GroceryPlanning = GroceryPlanningFactory.groceryPlanning(this);
        }
        catch(InvalidParameterException e)
        {
            MainActivity.showErrorDialog(getString(R.string.text_load_file_failed), e.getMessage(), this);
        }
    }

    public void manageCategories(@NonNull View view)
    {
        Intent intent = new Intent(this, CategoriesActivity.class);
         startActivity(intent);
    }

    public void manageIngredients(@NonNull View view)
    {
        Intent intent = new Intent(this, IngredientsActivity.class);
        startActivity(intent);
    }

    public void manageRecipies(@NonNull View view)
    {
        Intent intent = new Intent(this, RecipesActivity.class);
        startActivity(intent);
    }

    public void editShoppingList(@NonNull View view)
    {
        Intent intent = new Intent(this, ShoppingListActivity.class);
        startActivity(intent);
    }

    public void goShopping(@NonNull View view)
    {
        Intent intent = new Intent(this, GoShoppingActivity.class);
        startActivity(intent);
    }

    private ArrayList<String> getListOfExistingFiles(boolean includeVersionWithoutEnding)
    {
        ArrayList<String> inputList = new ArrayList<>();
        File directory = m_AppDataDirectory;
        for(File f : directory.listFiles())
        {
            if(!f.getName().endsWith(".json"))
            {
                continue;
            }

            inputList.add(f.getName());

            if(includeVersionWithoutEnding)
            {
                inputList.add(f.getName().replace(".json", ""));
            }
        }
        return inputList;
    }

    public void onExport()
    {
        InputStringFree newFragment = InputStringFree.newInstance(getResources().getString(R.string.alert_saveas));
        newFragment.setListInputsToConfirm(getListOfExistingFiles(true));
        newFragment.show(getSupportFragmentManager(), "onExport");
    }

    public void onImport()
    {
        InputStringFromList newFragment = InputStringFromList.newInstance(getResources().getString(R.string.alert_load), getListOfExistingFiles(false), "");
        newFragment.show(getSupportFragmentManager(), "onImport");
    }

    @Override
    public void onStringInput(@NonNull String tag, @NonNull String strInput, @NonNull String strAdditonalInformation) {
        if (tag.equals("onExport")) {
            String strFilename = strInput;
            if (!strFilename.endsWith(".json")) {
                strFilename += ".json";
            }

            File file = new File(m_AppDataDirectory, strFilename);
            try {
                JsonSerializer serializer = new JsonSerializer(m_GroceryPlanning);
                serializer.saveDataToFile(file, getBaseContext());
                Toast.makeText(MainActivity.this, getResources().getString(R.string.text_data_saved, strFilename), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                showErrorDialog(getString(R.string.text_save_file_failed), e.getMessage(), this);
            }
        } else if (tag.equals("onImport"))
        {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.text_replace_by_import_header));
            builder.setMessage(getString(R.string.text_action_reset_data));
            builder.setNegativeButton(android.R.string.cancel, (DialogInterface dialog, int which) -> {});
            builder.setPositiveButton(android.R.string.ok, (DialogInterface dialog, int which) ->
            {
                try {
                    File file = new File(m_AppDataDirectory, strInput);
                    JsonSerializer serializer = new JsonSerializer(m_GroceryPlanning);
                    serializer.loadDataFromFile(file);

                    m_GroceryPlanning.flush();

                    Toast.makeText(MainActivity.this, getResources().getString(R.string.text_data_loaded, strInput), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    m_GroceryPlanning.clearAll();
                    showErrorDialog(getString(R.string.text_load_file_failed), e.getMessage(), this);
                }
            });
            builder.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.actionbar_button_settings)
        {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.actionbar_button_import)
        {
            onImport();
        }
        else if (id == R.id.actionbar_button_export)
        {
            onExport();
        }
        return super.onOptionsItemSelected(item);
    }

    private static void showErrorDialog(@NonNull String title, @NonNull String message, @NonNull Context context)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton(android.R.string.ok, (DialogInterface dialog, int which) ->{});
        builder.show();
    }
}
