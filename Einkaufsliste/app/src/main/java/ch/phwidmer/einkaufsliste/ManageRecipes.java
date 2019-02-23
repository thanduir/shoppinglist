package ch.phwidmer.einkaufsliste;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class ManageRecipes extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private GroceryPlanning m_GroceryPlanning;
    private String          m_SaveFilePath;

    private Spinner     m_SpinnerRecipes;
    private EditText    m_EditTextNrPersons;

    private CheckBox    m_CheckBoxOptional;
    private TextView    m_TextViewAmount;
    private Spinner     m_SpinnerAmount;
    private EditText    m_EditTextAmount;
    private TextView    m_TextViewSize;
    private Spinner     m_SpinnerSize;

    private RecyclerView                m_RecyclerView;
    private RecyclerView.Adapter        m_Adapter;
    private RecyclerView.LayoutManager  m_LayoutManager;

    // TODO: RecyclerView sollte wenn möglich mehr Infos darstellen!

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_recipes);

        Intent intent = getIntent();
        m_SaveFilePath = intent.getStringExtra(MainActivity.EXTRA_SAVEFILESPATH);
        File file = new File(new File(m_SaveFilePath), MainActivity.c_strSaveFilename);
        m_GroceryPlanning = new GroceryPlanning(file, this);

        m_EditTextNrPersons = (EditText) findViewById(R.id.editText_NrPersons);
        m_SpinnerRecipes = (Spinner) findViewById(R.id.spinnerRecipes);

        m_EditTextNrPersons.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                String strRecipe = (String)m_SpinnerRecipes.getSelectedItem();
                Recipes.Recipe recipe = m_GroceryPlanning.m_Recipes.getRecipe(strRecipe);
                if(recipe == null)
                {
                    return;
                }

                if(s.toString().isEmpty())
                {
                    recipe.m_NumberOfPersons = 0;
                }
                else
                {
                    recipe.m_NumberOfPersons = Integer.valueOf(s.toString());
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        m_CheckBoxOptional = (CheckBox) findViewById(R.id.checkBoxOptional);
        m_TextViewAmount  = (TextView) findViewById(R.id.textViewAmount);
        m_SpinnerAmount = (Spinner) findViewById(R.id.spinnerAmount);
        m_EditTextAmount = (EditText) findViewById(R.id.editText_Amount);
        m_TextViewSize  = (TextView) findViewById(R.id.textViewSize);
        m_SpinnerSize = (Spinner) findViewById(R.id.spinnerSize);

        m_CheckBoxOptional.setVisibility(View.INVISIBLE);
        m_EditTextAmount.setVisibility(View.INVISIBLE);
        m_TextViewAmount.setVisibility(View.INVISIBLE);
        m_TextViewSize.setVisibility(View.INVISIBLE);

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        for(String strName : m_GroceryPlanning.m_Recipes.getAllRecipes())
        {
            adapter.add(strName);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_SpinnerRecipes.setAdapter(adapter);
        m_SpinnerRecipes.setOnItemSelectedListener(this);

        m_RecyclerView = (RecyclerView) findViewById(R.id.recyclerViewRecipeItems);
        m_RecyclerView.setHasFixedSize(true);
        m_LayoutManager = new LinearLayoutManager(this);
        m_RecyclerView.setLayoutManager(m_LayoutManager);

        ArrayAdapter<CharSequence> adapterAmount = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        for(int i = 0; i < Amount.Unit.values().length; ++i)
        {
            adapterAmount.add(Amount.Unit.values()[i].toString());
        }
        adapterAmount.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_SpinnerAmount.setAdapter(adapterAmount);
        m_SpinnerAmount.setOnItemSelectedListener(this);
        m_SpinnerAmount.setVisibility(View.INVISIBLE);

        ArrayAdapter<CharSequence> adapterSize = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        for(int i = 0; i < RecipeItem.Size.values().length; ++i)
        {
            adapterSize.add(RecipeItem.Size.values()[i].toString());
        }
        adapterSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_SpinnerSize.setAdapter(adapterSize);
        m_SpinnerSize.setOnItemSelectedListener(this);
        m_SpinnerSize.setVisibility(View.INVISIBLE);

        m_CheckBoxOptional.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                String strRecipe = (String)m_SpinnerRecipes.getSelectedItem();
                Recipes.Recipe recipe = m_GroceryPlanning.m_Recipes.getRecipe(strRecipe);
                if(recipe == null)
                {
                    return;
                }

                RecipeItem item = getSelectedRecipeItem(recipe);
                if(item == null)
                {
                    return;
                }

                item.m_Optional = isChecked;
            }
        });

        m_EditTextAmount.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                String strRecipe = (String)m_SpinnerRecipes.getSelectedItem();
                Recipes.Recipe recipe = m_GroceryPlanning.m_Recipes.getRecipe(strRecipe);
                if(recipe == null)
                {
                    return;
                }

                RecipeItem item = getSelectedRecipeItem(recipe);
                if(item == null)
                {
                    return;
                }

                if(s.toString().isEmpty())
                {
                    item.m_Amount.m_Quantity = 0.0f;
                }
                else
                {
                    item.m_Amount.m_Quantity = Float.valueOf(s.toString());
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    @Override
    protected void onPause()
    {
        File file = new File(new File(m_SaveFilePath), MainActivity.c_strSaveFilename);
        m_GroceryPlanning.saveDataToFile(file);

        super.onPause();
    }

    public void onAddRecipe(View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add recipe");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_GroceryPlanning.m_Recipes.addRecipe(input.getText().toString());
                ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>)m_SpinnerRecipes.getAdapter();
                adapter.add(input.getText().toString());
                m_SpinnerRecipes.setSelection(adapter.getCount() - 1);
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

    public void onDelRecipe(View v)
    {
        String strName = (String)m_SpinnerRecipes.getSelectedItem();
        m_GroceryPlanning.m_Recipes.removeRecipe(strName);
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>)m_SpinnerRecipes.getAdapter();
        adapter.remove((CharSequence)m_SpinnerRecipes.getSelectedItem());
    }

    public void onAddRecipeItem(View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add ingredient");

        // Set up the input
        final Spinner input = new Spinner(this);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        for(String strName : m_GroceryPlanning.m_Ingredients.getAllIngredients())
        {
            RecipeItemsAdapter adapterItems = (RecipeItemsAdapter)m_RecyclerView.getAdapter();
            if(adapterItems.containsItem(strName))
            {
                continue;
            }
            adapter.add(strName);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        input.setAdapter(adapter);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strRecipe = (String)m_SpinnerRecipes.getSelectedItem();
                Recipes.Recipe recipe = m_GroceryPlanning.m_Recipes.getRecipe(strRecipe);

                String strIngredient = input.getSelectedItem().toString();

                RecipeItem item = new RecipeItem();
                item.m_Ingredient = strIngredient;
                item.m_Amount.m_Unit = m_GroceryPlanning.m_Ingredients.getIngredient(strIngredient).m_DefaultUnit;
                recipe.m_Items.add(item);

                RecipeItemsAdapter adapter = (RecipeItemsAdapter)m_RecyclerView.getAdapter();
                adapter.setActiveElement(strIngredient);
                adapter.notifyDataSetChanged();
                handleIngredientSelected(strIngredient);
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

    public void onDelRecipeItem(View v)
    {
        RecipeItemsAdapter adapter = (RecipeItemsAdapter)m_RecyclerView.getAdapter();
        if(adapter.getActiveElement() == "")
        {
            // TODO: Eigentlich sollte stattdessen der Delete-Button deaktiviert sein, wenn kein Element ausgewählt ist.
            Toast.makeText(v.getContext(), "No element selected", Toast.LENGTH_SHORT).show();
            return;
        }

        View activeItem = m_RecyclerView.getChildAt(adapter.getActiveElementIndex());
        RecipeItemsAdapter.ViewHolder holder = (RecipeItemsAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(activeItem);

        Toast.makeText(v.getContext(), "Deleteing " + holder.m_TextView.getText(), Toast.LENGTH_SHORT).show();

        String strRecipe = (String)m_SpinnerRecipes.getSelectedItem();
        Recipes.Recipe recipe = m_GroceryPlanning.m_Recipes.getRecipe(strRecipe);
        RecipeItem item = getSelectedRecipeItem(recipe);
        recipe.m_Items.remove(item);

        adapter.notifyItemRemoved(adapter.getActiveElementIndex());
        adapter.setActiveElement("");
        if(m_RecyclerView.getAdapter() != null)
        {
            m_RecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        String strRecipe = (String)m_SpinnerRecipes.getSelectedItem();
        Recipes.Recipe recipe = m_GroceryPlanning.m_Recipes.getRecipe(strRecipe);

        if(parent == m_SpinnerRecipes)
        {
            m_EditTextNrPersons.setText(recipe.m_NumberOfPersons.toString());

            handleIngredientSelected("");

            m_Adapter = new RecipeItemsAdapter(recipe);
            m_RecyclerView.setAdapter(m_Adapter);
            ItemClickSupport.addTo(m_RecyclerView).setOnItemClickListener(
                    new ItemClickSupport.OnItemClickListener() {
                        @Override
                        public void onItemClicked(RecyclerView recyclerView, int position, View v)
                        {
                            RecipeItemsAdapter adapter = (RecipeItemsAdapter)recyclerView.getAdapter();
                            if(adapter.getActiveElement() != "")
                            {
                                View prevItem = recyclerView.getChildAt(adapter.getActiveElementIndex());
                                prevItem.setBackgroundColor(Color.TRANSPARENT);
                                prevItem.setActivated(false);
                            }

                            RecipeItemsAdapter.ViewHolder vh = (RecipeItemsAdapter.ViewHolder) recyclerView.getChildViewHolder(v);
                            adapter.setActiveElement((String)vh.m_TextView.getText());
                            v.setBackgroundColor(Color.GRAY);
                            v.setActivated(true);

                            handleIngredientSelected(adapter.getActiveElement());
                        }
                    }
            );
            return;
        }
        else if(parent == m_SpinnerAmount)
        {
            RecipeItem item = getSelectedRecipeItem(recipe);
            if(item == null)
            {
                return;
            }

            item.m_Amount.m_Unit = Amount.Unit.values()[m_SpinnerAmount.getSelectedItemPosition()];

            adjustEditTextAmount(item);
        }
        else if(parent == m_SpinnerSize)
        {
            RecipeItem item = getSelectedRecipeItem(recipe);
            if(item == null)
            {
                return;
            }

            item.m_Size = RecipeItem.Size.values()[m_SpinnerSize.getSelectedItemPosition()];
        }

    }

    public void onNothingSelected(AdapterView<?> parent)
    {
    }

    private void handleIngredientSelected(String strActiveElement)
    {
        if(strActiveElement == "")
        {
            m_CheckBoxOptional.setVisibility(View.INVISIBLE);
            m_SpinnerSize.setVisibility(View.INVISIBLE);
            m_SpinnerAmount.setVisibility(View.INVISIBLE);
            m_EditTextAmount.setVisibility(View.INVISIBLE);
            m_TextViewAmount.setVisibility(View.INVISIBLE);
            m_TextViewSize.setVisibility(View.INVISIBLE);
            return;
        }

        m_CheckBoxOptional.setVisibility(View.VISIBLE);
        m_TextViewSize.setVisibility(View.VISIBLE);
        m_SpinnerSize.setVisibility(View.VISIBLE);
        m_TextViewAmount.setVisibility(View.VISIBLE);
        m_SpinnerAmount.setVisibility(View.VISIBLE);
        m_EditTextAmount.setVisibility(View.VISIBLE);

        String strRecipe = (String)m_SpinnerRecipes.getSelectedItem();
        Recipes.Recipe recipe = m_GroceryPlanning.m_Recipes.getRecipe(strRecipe);

        RecipeItem item = getSelectedRecipeItem(recipe);
        if(item == null)
        {
            return;
        }

        m_CheckBoxOptional.setChecked(item.m_Optional);

        m_SpinnerAmount.setSelection(item.m_Amount.m_Unit.ordinal());
        adjustEditTextAmount(item);

        m_SpinnerSize.setSelection(item.m_Size.ordinal());
    }

    private RecipeItem getSelectedRecipeItem(Recipes.Recipe recipe)
    {
        RecipeItemsAdapter adapter = (RecipeItemsAdapter)m_RecyclerView.getAdapter();
        if(adapter == null)
        {
            return null;
        }

        RecipeItem item = null;
        for(RecipeItem r : recipe.m_Items)
        {
            if(r.m_Ingredient == adapter.getActiveElement())
            {
                return r;
            }
        }

        return null;
    }

    private void adjustEditTextAmount(RecipeItem item)
    {
        if(item.m_Amount.m_Unit == Amount.Unit.Unitless)
        {
            m_EditTextAmount.setText("");
            m_EditTextAmount.setVisibility(View.INVISIBLE);
        }
        else
        {
            m_EditTextAmount.setText(item.m_Amount.m_Quantity.toString());
            m_EditTextAmount.setVisibility(View.VISIBLE);
        }
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
