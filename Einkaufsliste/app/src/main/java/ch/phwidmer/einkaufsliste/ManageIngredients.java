package ch.phwidmer.einkaufsliste;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class ManageIngredients extends AppCompatActivity  implements AdapterView.OnItemSelectedListener
{
    private GroceryPlanning m_GroceryPlanning;
    private String          m_SaveFilePath;

    private RecyclerView               m_RecyclerView;
    private RecyclerView.Adapter       m_Adapter;
    private RecyclerView.LayoutManager m_LayoutManager;

    private TextView    m_TextViewIngredient;
    private Spinner     m_SpinnerCategory;
    private Spinner     m_SpinnerProvenance;
    private Spinner     m_SpinnerStdUnit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_ingredients);

        Intent intent = getIntent();
        m_SaveFilePath = intent.getStringExtra(MainActivity.EXTRA_SAVEFILESPATH);
        File file = new File(new File(m_SaveFilePath), MainActivity.c_strSaveFilename);
        m_GroceryPlanning = new GroceryPlanning(file, this);

        m_TextViewIngredient = (TextView) findViewById(R.id.textViewIntegrdient);
        m_TextViewIngredient.setVisibility(View.INVISIBLE);

        m_SpinnerCategory = (Spinner) findViewById(R.id.spinnerCategory);
        ArrayAdapter<CharSequence> adapterCategory = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        for(String strCategory : m_GroceryPlanning.m_Categories.getAllCategories())
        {
            adapterCategory.add(strCategory);
        }
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_SpinnerCategory.setAdapter(adapterCategory);
        m_SpinnerCategory.setOnItemSelectedListener(this);
        m_SpinnerCategory.setVisibility(View.INVISIBLE);

        m_SpinnerProvenance = (Spinner) findViewById(R.id.spinnerProvenance);
        ArrayAdapter<CharSequence> adapterProvenance = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        for(int i = 0; i < Ingredients.Provenance.values().length; ++i)
        {
            adapterProvenance.add(Ingredients.Provenance.values()[i].toString());
        }
        adapterProvenance.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_SpinnerProvenance.setAdapter(adapterProvenance);
        m_SpinnerProvenance.setOnItemSelectedListener(this);
        m_SpinnerProvenance.setVisibility(View.INVISIBLE);

        m_SpinnerStdUnit = (Spinner) findViewById(R.id.spinnerStdUnit);
        ArrayAdapter<CharSequence> adapterStdUnit = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        for(Amount.Unit u : Amount.Unit.values())
        {
            adapterStdUnit.add(u.toString());
        }
        adapterStdUnit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_SpinnerStdUnit.setAdapter(adapterStdUnit);
        m_SpinnerStdUnit.setOnItemSelectedListener(this);
        m_SpinnerStdUnit.setVisibility(View.INVISIBLE);

        m_RecyclerView = (RecyclerView) findViewById(R.id.recyclerViewIngredients);
        m_RecyclerView.setHasFixedSize(true);
        m_LayoutManager = new LinearLayoutManager(this);
        m_RecyclerView.setLayoutManager(m_LayoutManager);
        m_Adapter = new IngredientsAdapter(m_GroceryPlanning.m_Ingredients);
        m_RecyclerView.setAdapter(m_Adapter);
        ItemClickSupport.addTo(m_RecyclerView).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v)
                    {
                        IngredientsAdapter adapter = (IngredientsAdapter)recyclerView.getAdapter();
                        if(adapter.getActiveElement() != "")
                        {
                            View prevItem = recyclerView.getChildAt(adapter.getActiveElementIndex());
                            prevItem.setBackgroundColor(Color.TRANSPARENT);
                            prevItem.setActivated(false);
                        }

                        IngredientsAdapter.ViewHolder vh = (IngredientsAdapter.ViewHolder) recyclerView.getChildViewHolder(v);
                        adapter.setActiveElement((String)vh.m_TextView.getText());
                        v.setBackgroundColor(Color.GRAY);
                        v.setActivated(true);

                        handleIngredientSelected(adapter.getActiveElement());
                    }
                }
        );
    }

    @Override
    protected void onPause()
    {
        File file = new File(new File(m_SaveFilePath), MainActivity.c_strSaveFilename);
        m_GroceryPlanning.saveDataToFile(file);

        super.onPause();
    }

    public void onAddIngredient(View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add category");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_GroceryPlanning.m_Ingredients.addIngredient(input.getText().toString());
                IngredientsAdapter adapter = (IngredientsAdapter)m_RecyclerView.getAdapter();
                adapter.setActiveElement(input.getText().toString());
                adapter.notifyDataSetChanged();
                handleIngredientSelected(input.getText().toString());
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

    public void onDelIngredient(View v)
    {
        IngredientsAdapter adapter = (IngredientsAdapter)m_RecyclerView.getAdapter();
        if(adapter.getActiveElement() == "")
        {
            // TODO: Eigentlich sollte stattdessen der Delete-Button deaktiviert sein, wenn kein Element ausgewählt ist.
            Toast.makeText(v.getContext(), "No Element selected", Toast.LENGTH_SHORT).show();
            return;
        }

        View activeItem = m_RecyclerView.getChildAt(adapter.getActiveElementIndex());
        IngredientsAdapter.ViewHolder holder = (IngredientsAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(activeItem);

        Toast.makeText(v.getContext(), "Deleteing " + holder.m_TextView.getText(), Toast.LENGTH_SHORT).show();
        m_GroceryPlanning.m_Ingredients.removeIngredient((String)holder.m_TextView.getText());
        adapter.notifyItemRemoved(adapter.getActiveElementIndex());
        adapter.setActiveElement("");

        if(m_RecyclerView.getAdapter() != null)
        {
            m_RecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    private void handleIngredientSelected(String strActiveElement)
    {
        if(strActiveElement == "")
        {
            m_TextViewIngredient.setVisibility(View.INVISIBLE);
            m_SpinnerCategory.setVisibility(View.INVISIBLE);
            m_SpinnerProvenance.setVisibility(View.INVISIBLE);
            m_SpinnerStdUnit.setVisibility(View.INVISIBLE);
            return;
        }

        m_TextViewIngredient.setVisibility(View.VISIBLE);
        m_SpinnerCategory.setVisibility(View.VISIBLE);
        m_SpinnerProvenance.setVisibility(View.VISIBLE);
        m_SpinnerStdUnit.setVisibility(View.VISIBLE);

        Ingredients.Ingredient ingredient = m_GroceryPlanning.m_Ingredients.getIngredient(strActiveElement);
        m_TextViewIngredient.setText(strActiveElement);

        ArrayAdapter<CharSequence> adapterCategory = (ArrayAdapter<CharSequence>)m_SpinnerCategory.getAdapter();
        m_SpinnerCategory.setSelection(adapterCategory.getPosition(ingredient.m_Category.getName()));

        m_SpinnerProvenance.setSelection(ingredient.m_Provenance.ordinal());

        m_SpinnerStdUnit.setSelection(ingredient.m_DefaultUnit.ordinal());
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        IngredientsAdapter adapter = (IngredientsAdapter)m_RecyclerView.getAdapter();
        if(adapter.getActiveElement() == "")
        {
            return;
        }

        Ingredients.Ingredient ingredient = m_GroceryPlanning.m_Ingredients.getIngredient(adapter.getActiveElement());

        if(parent == m_SpinnerCategory)
        {
            String category = (String)m_SpinnerCategory.getSelectedItem();
            ingredient.m_Category = m_GroceryPlanning.m_Categories.getCategory(category);
            return;
        }
        else if(parent == m_SpinnerProvenance)
        {
            String provenance = (String)m_SpinnerProvenance.getSelectedItem();
            ingredient.m_Provenance = Ingredients.Provenance.valueOf(provenance);
            return;
        }
        else if(parent == m_SpinnerStdUnit)
        {
            String provenance = (String)m_SpinnerStdUnit.getSelectedItem();
            ingredient.m_DefaultUnit = Amount.Unit.valueOf(provenance);
            return;
        }
    }

    public void onNothingSelected(AdapterView<?> parent)
    {
    }

    public void onConfirm(View v)
    {
        finish();
    }

    public void onCancel(View v)
    {
        finish();
    }
}
