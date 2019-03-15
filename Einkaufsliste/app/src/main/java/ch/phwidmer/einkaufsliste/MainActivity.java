package ch.phwidmer.einkaufsliste;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
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
import java.util.Set;

public class MainActivity extends AppCompatActivity
{
    public static final String EXTRA_SAVEFILESPATH = "ch.phwidmer.einkaufsliste.SAVEFILESPATH";

    private GroceryPlanning m_GroceryPlanning;

    private final String c_strStdDataFilename = "default.json";

    public static final String c_strSaveFilename = "einkaufsliste.json";
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

                int read = 0;
                byte[] bytes = new byte[1024];

                while ((read = dataInputStream.read(bytes)) != -1) {
                    output.write(bytes, 0, read);
                }

                output.close();

                m_GroceryPlanning.scanFile(getBaseContext(), testDataFile);
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

    public void onExport()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.alert_saveas);

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strFilename = input.getText().toString();
                if(!strFilename.endsWith(".json"))
                {
                    strFilename += ".json";
                }

                File file = new File(m_AppDataDirectory, strFilename);
                m_GroceryPlanning.saveDataToFile(file, getBaseContext());
                Toast.makeText(MainActivity.this, getResources().getString(R.string.text_data_saved, strFilename), Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog d = builder.create();
        d.setView(input, 50, 0 ,50,0);
        d.show();
    }

    public void onImport()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.alert_load);

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
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strFilename = input.getSelectedItem().toString();

                File file = new File(m_AppDataDirectory, strFilename);
                m_GroceryPlanning.loadDataFromFile(file, MainActivity.this);

                File file2 = new File(m_AppDataDirectory, c_strSaveFilename);
                m_GroceryPlanning.saveDataToFile(file2, getBaseContext());

                Toast.makeText(MainActivity.this, getResources().getString(R.string.text_data_loaded,  strFilename), Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog d = builder.create();
        d.setView(input, 50, 20 ,20,0);
        d.show();
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
            intent.putExtra(EXTRA_SAVEFILESPATH, m_AppDataDirectory.getPath());
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
}
