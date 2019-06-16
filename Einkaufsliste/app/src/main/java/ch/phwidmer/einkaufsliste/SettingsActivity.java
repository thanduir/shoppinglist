package ch.phwidmer.einkaufsliste;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    public static String KEY_DEFAULT_NRPERSONS = "ch.phwidmer.einkaufsliste.DEF_NRPERSONS";
    public static String KEY_DEFAULT_UNIT = "ch.phwidmer.einkaufsliste.DEF_UNIT";
    public static String KEY_DEFAULT_SORORDER = "ch.phwidmer.einkaufsliste.DEF_SORORDER";

    private Spinner         m_SpinnerDefaultUnit;
    private EditText        m_EditTextDefaultNrPersons;
    private Spinner         m_SpinnerDefaultSortOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Intent intent = getIntent();
        GroceryPlanning m_GroceryPlanning = intent.getParcelableExtra(MainActivity.EXTRA_GROCERYPLANNING);

        m_SpinnerDefaultUnit = findViewById(R.id.spinnerDefaultUnit);
        m_EditTextDefaultNrPersons = findViewById(R.id.editTextDefaultNrPersons);
        m_SpinnerDefaultSortOrder = findViewById(R.id.spinnerDefaultSortOrder);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        ArrayAdapter<CharSequence> adapterStdUnit = new ArrayAdapter<CharSequence>(this, R.layout.spinner_item);
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
        m_EditTextDefaultNrPersons.setText(Integer.toString(iNrPersons));
        m_EditTextDefaultNrPersons.addTextChangedListener(new TextWatcher() {

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
        for(String strSortOrder : m_GroceryPlanning.m_Categories.getAllSortOrders())
        {
            adapterDefSortOrder.add(strSortOrder);
        }
        adapterDefSortOrder.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_SpinnerDefaultSortOrder.setAdapter(adapterDefSortOrder);
        m_SpinnerDefaultSortOrder.setOnItemSelectedListener(this);
        String strDefaulSortOrder = preferences.getString(KEY_DEFAULT_SORORDER, "");
        if(m_GroceryPlanning.m_Categories.getAllSortOrders().contains(strDefaulSortOrder))
        {
            m_SpinnerDefaultSortOrder.setSelection(m_GroceryPlanning.m_Categories.getAllSortOrders().indexOf(strDefaulSortOrder));
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

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
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

    public void onNothingSelected(AdapterView<?> parent)
    {
    }
}
