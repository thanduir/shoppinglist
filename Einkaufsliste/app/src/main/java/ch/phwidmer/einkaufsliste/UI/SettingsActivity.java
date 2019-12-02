package ch.phwidmer.einkaufsliste.UI;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Locale;
import java.util.Optional;

import ch.phwidmer.einkaufsliste.R;
import ch.phwidmer.einkaufsliste.data.Amount;
import ch.phwidmer.einkaufsliste.data.Categories;
import ch.phwidmer.einkaufsliste.data.GroceryPlanning;
import ch.phwidmer.einkaufsliste.data.GroceryPlanningFactory;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public static String KEY_DEFAULT_NRPERSONS = "ch.phwidmer.einkaufsliste.DEF_NRPERSONS";
    public static String KEY_DEFAULT_UNIT = "ch.phwidmer.einkaufsliste.DEF_UNIT";
    public static String KEY_DEFAULT_SORORDER = "ch.phwidmer.einkaufsliste.DEF_SORORDER";

    public static String KEY_ACTIVE_SORTORDER_CATEGORIES = "ch.phwidmer.einkaufsliste.categories.ACTIVE_SORORDER";
    public static String KEY_ACTIVE_RECIPE = "ch.phwidmer.einkaufsliste.ACTIVE_RECIPE";
    public static String KEY_ACTIVE_SORTORDER_GOSHOPPING = "ch.phwidmer.einkaufsliste.goshopping.ACTIVE_SORORDER";

    private Spinner         m_SpinnerDefaultUnit;
    private Spinner         m_SpinnerDefaultSortOrder;

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

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        ArrayAdapter<CharSequence> adapterStdUnit = new ArrayAdapter<>(this, R.layout.spinner_item);
        for(Amount.Unit u : Amount.Unit.values())
        {
            adapterStdUnit.add(Amount.toUIString(this, u));
        }
        adapterStdUnit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_SpinnerDefaultUnit.setAdapter(adapterStdUnit);
        m_SpinnerDefaultUnit.setOnItemSelectedListener(this);
        String strDefaultUnit = preferences.getString(KEY_DEFAULT_UNIT, Amount.Unit.Count.toString());
        m_SpinnerDefaultUnit.setSelection(Amount.Unit.valueOf(strDefaultUnit).ordinal());

        int iNrPersons = preferences.getInt(KEY_DEFAULT_NRPERSONS, 4);
        editTextDefaultNrPersons.setText(String.format(Locale.getDefault(), "%d", iNrPersons));
        editTextDefaultNrPersons.addTextChangedListener(new TextWatcher() {

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
        String strDefaulSortOrder = preferences.getString(KEY_DEFAULT_SORORDER, "");
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
        SharedPreferences.Editor editor = preferences.edit();

        if(parent == m_SpinnerDefaultUnit)
        {
            editor.putString(KEY_DEFAULT_UNIT, Amount.Unit.values()[m_SpinnerDefaultUnit.getSelectedItemPosition()].toString());
        }
        else if(parent == m_SpinnerDefaultSortOrder)
        {
            editor.putString(KEY_DEFAULT_SORORDER, m_SpinnerDefaultSortOrder.getSelectedItem().toString());
        }

        editor.apply();
    }

    public void onNothingSelected(@NonNull AdapterView<?> parent)
    {
    }
}
