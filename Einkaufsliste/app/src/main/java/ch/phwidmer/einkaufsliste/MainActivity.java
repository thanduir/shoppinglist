package ch.phwidmer.einkaufsliste;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements InputStringDialogFragment.InputStringResponder
{
    public static final String EXTRA_GROCERYPLANNING = "ch.phwidmer.einkaufsliste.GROCERYPLANNING";
    public static final String EXTRA_SAVEDFILESPATH = "ch.phwidmer.einkaufsliste.SAVEDFILESPATH";

    private final int REQUEST_CODE_ManageCategories = 1;
    private final int REQUEST_CODE_ManageIngredients = 2;
    private final int REQUEST_CODE_ManageRecipes = 3;
    private final int REQUEST_CODE_ManageShoppingList = 4;
    private final int REQUEST_CODE_GoShopping = 5;

    private GroceryPlanning m_GroceryPlanning;

    private static final String c_strStdDataFilename = "ch.phwidmer.einkaufsliste.default.json";
    private static final String c_strSaveFilename = "ch.phwidmer.einkaufsliste.einkaufsliste.json";

    private File m_AppDataDirectory = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_AppDataDirectory = getExternalFilesDir(null);

        File file = new File(m_AppDataDirectory, c_strSaveFilename);
        if(file.exists())
        {
            m_GroceryPlanning = new GroceryPlanning(file, this);
        }
        else
        {
            m_GroceryPlanning = new GroceryPlanning();
            m_GroceryPlanning.saveDataToFile(file, this);
        }

        writeStdDataFileIfNotPresent();
    }

    private void writeStdDataFileIfNotPresent()
    {
        File testDataFile = new File(m_AppDataDirectory, c_strStdDataFilename);
        if(!testDataFile.exists())
        {
            try {
                InputStream dataInputStream = getResources().openRawResource(R.raw.default_data);
                OutputStream output = new FileOutputStream(testDataFile);

                int read;
                byte[] bytes = new byte[1024];

                while ((read = dataInputStream.read(bytes)) != -1) {
                    output.write(bytes, 0, read);
                }

                output.close();

                m_GroceryPlanning.scanFile(getBaseContext(), testDataFile);
            }
            catch(IOException ignore)
            {
            }
        }
    }

    public void manageCategories(View view)
    {
        Intent intent = new Intent(this, ManageCategories.class);
        intent.putExtra(EXTRA_GROCERYPLANNING, m_GroceryPlanning);
        startActivityForResult(intent, REQUEST_CODE_ManageCategories);
    }

    public void manageIngredients(View view)
    {
        Intent intent = new Intent(this, ManageIngredients.class);
        intent.putExtra(EXTRA_GROCERYPLANNING, m_GroceryPlanning);
        startActivityForResult(intent, REQUEST_CODE_ManageIngredients);
    }

    public void manageRecipies(View view)
    {
        Intent intent = new Intent(this, ManageRecipes.class);
        intent.putExtra(EXTRA_GROCERYPLANNING, m_GroceryPlanning);
        startActivityForResult(intent, REQUEST_CODE_ManageRecipes);
    }

    public void editShoppingList(View view)
    {
        Intent intent = new Intent(this, ManageShoppingList.class);
        intent.putExtra(EXTRA_GROCERYPLANNING, m_GroceryPlanning);
        startActivityForResult(intent, REQUEST_CODE_ManageShoppingList);
    }

    public void goShopping(View view)
    {
        Intent intent = new Intent(this, GoShoppingActivity.class);
        intent.putExtra(EXTRA_GROCERYPLANNING, m_GroceryPlanning);
        startActivityForResult(intent, REQUEST_CODE_GoShopping);
    }

    private ArrayList<String> getListOfExistingFiles(boolean includeVersionWithoutEnding)
    {
        ArrayList<String> inputList = new ArrayList<>();
        File directory = m_AppDataDirectory;
        for(File f : directory.listFiles())
        {
            if(!f.getName().endsWith(".json") || f.getName().equals(c_strSaveFilename) || f.getName().equals(c_strStdDataFilename))
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
        InputStringDialogFragment newFragment = InputStringDialogFragment.newInstance(getResources().getString(R.string.alert_saveas));
        newFragment.setListInputsToConfirm(getListOfExistingFiles(true));
        ArrayList<String> excludedList = new ArrayList<>();
        excludedList.add(c_strStdDataFilename);
        excludedList.add(c_strStdDataFilename.replace(".json", ""));
        excludedList.add(c_strSaveFilename);
        excludedList.add(c_strSaveFilename.replace(".json", ""));
        newFragment.setListExcludedInputs(excludedList);
        newFragment.show(getSupportFragmentManager(), "onExport");
    }

    public void onImport()
    {
        InputStringDialogFragment newFragment = InputStringDialogFragment.newInstance(getResources().getString(R.string.alert_load));
        newFragment.setListOnlyAllowed(getListOfExistingFiles(false));
        newFragment.show(getSupportFragmentManager(), "onImport");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode != RESULT_OK)
        {
            return;
        }

        if(requestCode == REQUEST_CODE_ManageCategories
           || requestCode == REQUEST_CODE_ManageIngredients
           || requestCode == REQUEST_CODE_ManageRecipes
           || requestCode == REQUEST_CODE_ManageShoppingList
           || requestCode == REQUEST_CODE_GoShopping)
        {
            m_GroceryPlanning = data.getParcelableExtra(EXTRA_GROCERYPLANNING);

            File file = new File(m_AppDataDirectory, c_strSaveFilename);
            m_GroceryPlanning.saveDataToFile(file, null);
        }
    }

    @Override
    public void onStringInput(String tag, String strInput, String strAdditonalInformation)
    {
        if(tag.equals("onExport"))
        {
            String strFilename = strInput;
            if(!strFilename.endsWith(".json"))
            {
                strFilename += ".json";
            }

            File file = new File(m_AppDataDirectory, strFilename);
            m_GroceryPlanning.saveDataToFile(file, getBaseContext());
            Toast.makeText(MainActivity.this, getResources().getString(R.string.text_data_saved, strFilename), Toast.LENGTH_SHORT).show();
        }
        else if(tag.equals("onImport"))
        {
            File file = new File(m_AppDataDirectory, strInput);
            m_GroceryPlanning.loadDataFromFile(file, MainActivity.this);

            File file2 = new File(m_AppDataDirectory, c_strSaveFilename);
            m_GroceryPlanning.saveDataToFile(file2, getBaseContext());

            Toast.makeText(MainActivity.this, getResources().getString(R.string.text_data_loaded,  strInput), Toast.LENGTH_SHORT).show();
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
            intent.putExtra(EXTRA_GROCERYPLANNING, m_GroceryPlanning);
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
        else if (id == R.id.actionbar_button_datasynchronization)
        {
            Intent intent = new Intent(this, DataSynchronizationActivity.class);
            intent.putExtra(EXTRA_SAVEDFILESPATH, m_AppDataDirectory.getPath());
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
