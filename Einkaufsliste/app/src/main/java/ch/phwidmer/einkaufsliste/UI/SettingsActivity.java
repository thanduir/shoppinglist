package ch.phwidmer.einkaufsliste.UI;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Optional;

import ch.phwidmer.einkaufsliste.R;
import ch.phwidmer.einkaufsliste.data.Categories;
import ch.phwidmer.einkaufsliste.data.DataBackend;
import ch.phwidmer.einkaufsliste.data.GroceryPlanning;
import ch.phwidmer.einkaufsliste.data.GroceryPlanningFactory;
import ch.phwidmer.einkaufsliste.data.Unit;
import ch.phwidmer.einkaufsliste.data.utilities.JsonSerializer;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public static final String KEY_DEFAULT_NRPERSONS  = "ch.phwidmer.einkaufsliste.DEF_NRPERSONS";
    public static final String KEY_DEFAULT_UNIT       = "ch.phwidmer.einkaufsliste.DEF_UNIT";
    public static final String KEY_DEFAULT_SORORDER   = "ch.phwidmer.einkaufsliste.DEF_SORORDER";
    public static final String KEY_CURRENT_BACKEND    = "ch.phwidmer.einkaufsliste.goshopping.CURRENT_BACKEND";

    public static final String KEY_ACTIVE_SORTORDER_CATEGORIES    = "ch.phwidmer.einkaufsliste.categories.ACTIVE_SORORDER";
    public static final String KEY_ACTIVE_RECIPE                  = "ch.phwidmer.einkaufsliste.ACTIVE_RECIPE";
    public static final String KEY_ACTIVE_SORTORDER_GOSHOPPING    = "ch.phwidmer.einkaufsliste.goshopping.ACTIVE_SORORDER";

    public static final int defaultNrPersons        = 4;
    public static final Unit defaultUnit            = Unit.Count;
    public static final String defaultSortOrder     = "";
    public static final DataBackend defaultBackend  = DataBackend.fs_based;

    private Spinner m_SpinnerDefaultUnit;
    private Spinner m_SpinnerDefaultSortOrder;
    private Spinner m_SpinnerBackend;

    public static void setDefaultPreferencesIfNothingSet(Context context)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        if(!preferences.contains(KEY_DEFAULT_NRPERSONS))
        {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(KEY_DEFAULT_NRPERSONS, defaultNrPersons);
            editor.apply();
        }

        if(!preferences.contains(KEY_DEFAULT_UNIT))
        {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(KEY_DEFAULT_UNIT, defaultUnit.toString());
            editor.apply();
        }

        if(!preferences.contains(KEY_DEFAULT_SORORDER))
        {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(KEY_DEFAULT_SORORDER, defaultSortOrder);
            editor.apply();
        }

        if(!preferences.contains(KEY_CURRENT_BACKEND))
        {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(KEY_CURRENT_BACKEND, defaultBackend.toString());
            editor.apply();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        GroceryPlanning groceryPlanning = GroceryPlanningFactory.groceryPlanning(this);
        if(groceryPlanning == null)
        {
            return;
        }

        m_SpinnerDefaultUnit = findViewById(R.id.spinnerDefaultUnit);
        EditText editTextDefaultNrPersons = findViewById(R.id.editTextDefaultNrPersons);
        m_SpinnerDefaultSortOrder = findViewById(R.id.spinnerDefaultSortOrder);
        m_SpinnerBackend = findViewById(R.id.spinnerBackend);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        ArrayAdapter<CharSequence> adapterStdUnit = new ArrayAdapter<>(this, R.layout.spinner_item);
        for(Unit u : Unit.values())
        {
            adapterStdUnit.add(Unit.toUIString(this, u));
        }
        adapterStdUnit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_SpinnerDefaultUnit.setAdapter(adapterStdUnit);
        m_SpinnerDefaultUnit.setOnItemSelectedListener(this);
        String strDefaultUnit = preferences.getString(KEY_DEFAULT_UNIT, defaultUnit.toString());
        m_SpinnerDefaultUnit.setSelection(Unit.valueOf(strDefaultUnit).ordinal());

        int iNrPersons = preferences.getInt(KEY_DEFAULT_NRPERSONS, defaultNrPersons);
        editTextDefaultNrPersons.setText(String.format(Locale.getDefault(), "%d", iNrPersons));
        editTextDefaultNrPersons.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable s) {
                if(s.length() == 0)
                {
                    return;
                }
                onDefaultNrPersonsChanged(Integer.valueOf(s.toString()));
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        ArrayAdapter<CharSequence> adapterDefSortOrder = new ArrayAdapter<>(this, R.layout.spinner_item);
        for(Categories.SortOrder sortOrder : groceryPlanning.categories().getAllSortOrders())
        {
            adapterDefSortOrder.add(sortOrder.getName());
        }
        adapterDefSortOrder.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_SpinnerDefaultSortOrder.setAdapter(adapterDefSortOrder);
        m_SpinnerDefaultSortOrder.setOnItemSelectedListener(this);
        String strDefaulSortOrder = preferences.getString(KEY_DEFAULT_SORORDER, defaultSortOrder);
        if(strDefaulSortOrder == null)
        {
            strDefaulSortOrder = "";
        }
        Optional<Categories.SortOrder> sortOrder = groceryPlanning.categories().getSortOrder(strDefaulSortOrder);
        if(sortOrder.isPresent())
        {
            m_SpinnerDefaultSortOrder.setSelection(groceryPlanning.categories().getAllSortOrders().indexOf(sortOrder.get()));
        }
        else
        {
            m_SpinnerDefaultSortOrder.setSelection(0);
        }

        ArrayAdapter<CharSequence> adapterBackend = new ArrayAdapter<>(this, R.layout.spinner_item);
        for(DataBackend backend : DataBackend.values())
        {
            adapterBackend.add(backend.toUIString(this));
        }
        adapterBackend.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_SpinnerBackend.setAdapter(adapterBackend);
        m_SpinnerBackend.setOnItemSelectedListener(this);
        String strBackend = preferences.getString(KEY_CURRENT_BACKEND, defaultBackend.toString());
        m_SpinnerBackend.setSelection(DataBackend.valueOf(strBackend).ordinal());
    }

    void onDefaultNrPersonsChanged(int iNewValue)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_DEFAULT_NRPERSONS, iNewValue);
        editor.apply();
    }

    public void onItemSelected(@NonNull AdapterView<?> parent, @NonNull View view, int pos, long id)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if(parent == m_SpinnerDefaultUnit)
        {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(KEY_DEFAULT_UNIT, Unit.values()[m_SpinnerDefaultUnit.getSelectedItemPosition()].toString());
            editor.apply();
        }
        else if(parent == m_SpinnerDefaultSortOrder)
        {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(KEY_DEFAULT_SORORDER, m_SpinnerDefaultSortOrder.getSelectedItem().toString());
            editor.apply();
        }
        else if(parent == m_SpinnerBackend)
        {
            String strBackend = DataBackend.values()[m_SpinnerBackend.getSelectedItemPosition()].toString();
            String currentBackend = preferences.getString(KEY_CURRENT_BACKEND, "no backend");
            if(strBackend.equals(currentBackend))
            {
                // Nothing changed -> ignore request
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.text_switch_backend_header));
            builder.setMessage(getString(R.string.text_switch_backend));
            builder.setNegativeButton(android.R.string.cancel, (DialogInterface dialog, int which) -> m_SpinnerBackend.setSelection(DataBackend.valueOf(currentBackend).ordinal()));
            builder.setPositiveButton(android.R.string.ok, (DialogInterface dialog, int which) ->
            {
                if(changeBackend(strBackend))
                {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(KEY_CURRENT_BACKEND, strBackend);
                    editor.apply();
                    GroceryPlanningFactory.setBackend(DataBackend.valueOf(strBackend));
                }
            });
            builder.show();
        }
    }

    public void onNothingSelected(@NonNull AdapterView<?> parent)
    {
    }

    private boolean changeBackend(String strNewBackend)
    {
        File currentDataFile = getTemporaryFile();

        try {
            JsonSerializer serializer = new JsonSerializer(GroceryPlanningFactory.groceryPlanning(this));
            serializer.saveDataToFile(currentDataFile, this);
            GroceryPlanningFactory.setBackend(DataBackend.valueOf(strNewBackend));
            serializer.loadDataFromFile(currentDataFile);

            Toast.makeText(this, getResources().getString(R.string.text_backend_changed_successfully), Toast.LENGTH_SHORT).show();
        }
        catch(IOException e) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.text_change_backend_failed));
            builder.setMessage(e.getMessage());
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setPositiveButton(android.R.string.ok, (DialogInterface dialog, int which) ->{});
            builder.show();
            return false;
        }
        finally
        {
            //noinspection ResultOfMethodCallIgnored
            currentDataFile.delete();
        }

        return true;
    }

    public void onClearAllData(View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.text_clear_all_data_header));
        builder.setMessage(getString(R.string.text_action_reset_data));
        builder.setNegativeButton(android.R.string.cancel, (DialogInterface dialog, int which) -> {});
        builder.setPositiveButton(android.R.string.ok, (DialogInterface dialog, int which) ->
        {
            GroceryPlanningFactory.groceryPlanning(this).clearAll();
            Toast.makeText(this, getResources().getString(R.string.text_all_data_cleared), Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    public void onResetDataToDefault(View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.text_reset_to_default_header));
        builder.setMessage(getString(R.string.text_action_reset_data));
        builder.setNegativeButton(android.R.string.cancel, (DialogInterface dialog, int which) -> {});
        builder.setPositiveButton(android.R.string.ok, (DialogInterface dialog, int which) -> replaceDataWithDefault());
        builder.show();
    }

    private void replaceDataWithDefault()
    {
        File testDataFile = getTemporaryFile();

        try {
            // Copy stream to actual file
            InputStream dataInputStream = getResources().openRawResource(R.raw.default_data);
            OutputStream output = new FileOutputStream(testDataFile);
            int read;
            byte[] bytes = new byte[1024];
            while ((read = dataInputStream.read(bytes)) != -1) {
                output.write(bytes, 0, read);
            }
            output.close();

            // Load file into GroceryPlanning
            GroceryPlanning groceryPlanning = GroceryPlanningFactory.groceryPlanning(this);
            JsonSerializer serializer = new JsonSerializer(groceryPlanning);
            serializer.loadDataFromFile(testDataFile);
            groceryPlanning.flush();

            Toast.makeText(this, getResources().getString(R.string.text_default_data_loaded), Toast.LENGTH_SHORT).show();
        }
        catch(IOException e)
        {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.text_save_file_failed));
            builder.setMessage(e.getMessage());
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setPositiveButton(android.R.string.ok, (DialogInterface dialog, int which) ->{});
            builder.show();
        }
        finally
        {
            //noinspection ResultOfMethodCallIgnored
            testDataFile.delete();
        }
    }

    private File getTemporaryFile()
    {
        final String c_strStdDataFilename = "ch.phwidmer.einkaufsliste.temp_file";

        // Get unused filename
        File tempFile;
        for(int i = 0; ; ++i)
        {
            tempFile = new File(getCacheDir(), c_strStdDataFilename + i + ".json");
            if(!tempFile.exists())
            {
                break;
            }
        }
        return tempFile;
    }
}
