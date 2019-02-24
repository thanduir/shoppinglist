package ch.phwidmer.einkaufsliste;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.File;

public class ManageRecipes extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private GroceryPlanning m_GroceryPlanning;
    private String          m_SaveFilePath;

    private Spinner     m_SpinnerRecipes;
    private EditText    m_EditTextNrPersons;

    private RecyclerView                m_RecyclerView;
    private RecyclerView.Adapter        m_Adapter;
    private RecyclerView.LayoutManager  m_LayoutManager;

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
                adapter.notifyItemInserted(recipe.m_Items.size()-1);
                adapter.setActiveElement(strIngredient);
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

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        String strRecipe = (String)m_SpinnerRecipes.getSelectedItem();
        Recipes.Recipe recipe = m_GroceryPlanning.m_Recipes.getRecipe(strRecipe);

        m_EditTextNrPersons.setText(recipe.m_NumberOfPersons.toString());

        m_Adapter = new RecipeItemsAdapter(m_RecyclerView, recipe);
        m_RecyclerView.setAdapter(m_Adapter);
        ItemClickSupport.addTo(m_RecyclerView).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v)
                    {
                        RecipeItemsAdapter adapter = (RecipeItemsAdapter) recyclerView.getAdapter();
                        RecipeItemsAdapter.ViewHolder vh = (RecipeItemsAdapter.ViewHolder) recyclerView.getChildViewHolder(v);

                        if(vh.getID() == adapter.getActiveElement())
                        {
                            adapter.setActiveElement("");
                        }
                        else
                        {
                            adapter.setActiveElement((String) vh.getID());
                        }
                    }
                }
        );
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ReactToTouchActionsCallback((RecipeItemsAdapter)m_Adapter,
                                                                                                m_RecyclerView.getContext(),
                                                                                                R.drawable.ic_delete_black_24dp,
                                                                                                false));
        itemTouchHelper.attachToRecyclerView(m_RecyclerView);
    }

    public void onNothingSelected(AdapterView<?> parent)
    {
    }
}
