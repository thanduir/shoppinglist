package ch.phwidmer.einkaufsliste;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class ManageRecipes extends AppCompatActivity implements AdapterView.OnItemSelectedListener, InputStringDialogFragment.InputStringResponder {

    private GroceryPlanning m_GroceryPlanning;

    private Spinner     m_SpinnerRecipes;
    private EditText    m_EditTextNrPersons;
    private TextView    m_textViewNrPersons;

    private ArrayAdapter<CharSequence>  m_SpinnerRecipesAdapter;

    private Button                      m_ButtonDelRecipe;

    private String                      m_strRecentlyDeletedRecipe;
    private Recipes.Recipe              m_RecentlyDeletedRecipe;

    private RecyclerView                m_RecyclerView;
    private RecipeItemsAdapter          m_Adapter;

    private String                      m_SavedActiveRecipe;
    private String                      m_SavedActiveElement;

    private FloatingActionButton m_FAB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_recipes);

        Intent intent = getIntent();
        m_GroceryPlanning = intent.getParcelableExtra(MainActivity.EXTRA_GROCERYPLANNING);

        m_FAB = findViewById(R.id.fab);

        m_EditTextNrPersons = findViewById(R.id.editText_NrPersons);
        m_textViewNrPersons = findViewById(R.id.textViewNrPersons);
        m_SpinnerRecipes = findViewById(R.id.spinnerRecipes);

        m_ButtonDelRecipe = findViewById(R.id.buttonDelRecipe);

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

        m_SpinnerRecipesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        for(String strName : m_GroceryPlanning.m_Recipes.getAllRecipes())
        {
            m_SpinnerRecipesAdapter.add(strName);
        }
        m_SpinnerRecipesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_SpinnerRecipes.setAdapter(m_SpinnerRecipesAdapter);
        m_SpinnerRecipes.setOnItemSelectedListener(this);
        String strActiveRecipe = m_GroceryPlanning.m_Recipes.getActiveRecipe();
        if(!strActiveRecipe.isEmpty() && m_GroceryPlanning.m_Recipes.getAllRecipes().contains(strActiveRecipe))
        {
            m_SpinnerRecipes.setSelection(m_SpinnerRecipesAdapter.getPosition(strActiveRecipe));
        }

        m_RecyclerView = findViewById(R.id.recyclerViewRecipeItems);
        m_RecyclerView.setHasFixedSize(true);
        m_RecyclerView.setLayoutManager(new LinearLayoutManager(this));
        m_RecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && m_FAB.getVisibility() == View.VISIBLE) {
                    m_FAB.hide();
                } else if (dy < 0 && m_FAB.getVisibility() != View.VISIBLE) {
                    m_FAB.show();
                }
            }
        });

        if(savedInstanceState != null)
        {
            m_SavedActiveRecipe = savedInstanceState.getString("AdapterActiveRecipe");
            m_SavedActiveElement = savedInstanceState.getString("AdapterActiveElement");
        }

        updateVisibility();
    }

    private void updateVisibility()
    {
        if(m_SpinnerRecipes.getAdapter().getCount() > 0)
        {
            m_EditTextNrPersons.setVisibility(View.VISIBLE);
            m_textViewNrPersons.setVisibility(View.VISIBLE);

            m_ButtonDelRecipe.setEnabled(true);
        }
        else
        {
            m_EditTextNrPersons.setVisibility(View.INVISIBLE);
            m_textViewNrPersons.setVisibility(View.INVISIBLE);

            m_ButtonDelRecipe.setEnabled(false);

            m_Adapter = null;
            m_RecyclerView.setAdapter(null);
        }

        m_FAB.setFocusable(true);
        m_FAB.setFocusableInTouchMode(true);
        m_FAB.requestFocus();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == android.R.id.home)
        {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish()
    {
        Intent data = new Intent();
        data.putExtra(MainActivity.EXTRA_GROCERYPLANNING, m_GroceryPlanning);
        setResult(RESULT_OK, data);

        super.finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState)
    {
        if(m_Adapter != null)
        {
            savedInstanceState.putString("AdapterActiveRecipe", (String)m_SpinnerRecipes.getSelectedItem());
            savedInstanceState.putString("AdapterActiveElement", m_Adapter.getActiveElement());
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    public void onAddRecipe(View v)
    {
        DialogFragment newFragment = InputStringDialogFragment.newInstance(getResources().getString(R.string.text_add_recipe), "");
        newFragment.show(getSupportFragmentManager(), "addRecipe");
    }

    public void onDelRecipe(View v)
    {
        if(m_SpinnerRecipes.getSelectedItem() == null)
        {
            return;
        }

        String strName = (String)m_SpinnerRecipes.getSelectedItem();

        m_strRecentlyDeletedRecipe = strName;
        m_RecentlyDeletedRecipe = m_GroceryPlanning.m_Recipes.getRecipe(strName);

        m_GroceryPlanning.m_Recipes.removeRecipe(strName);
        m_SpinnerRecipesAdapter.remove((CharSequence)m_SpinnerRecipes.getSelectedItem());
        updateVisibility();

        // Allow undo

        Snackbar snackbar = Snackbar.make(m_RecyclerView, R.string.text_recipe_deleted, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.text_undo, (View view) ->
        {
            m_GroceryPlanning.m_Recipes.addRecipe(m_strRecentlyDeletedRecipe, m_RecentlyDeletedRecipe);
            m_SpinnerRecipesAdapter.add(m_strRecentlyDeletedRecipe);
            m_SpinnerRecipes.setSelection(m_SpinnerRecipesAdapter.getCount() - 1);
            updateVisibility();

            m_strRecentlyDeletedRecipe = "";
            m_RecentlyDeletedRecipe = null;

            Snackbar snackbar1 = Snackbar.make(m_RecyclerView, R.string.text_recipe_restored, Snackbar.LENGTH_SHORT);
            snackbar1.show();
        });
        snackbar.show();
    }

    public void onRenameRecipe(View v)
    {
        final String strCurrentRecipe = (String)m_SpinnerRecipes.getSelectedItem();

        DialogFragment newFragment = InputStringDialogFragment.newInstance(getResources().getString(R.string.text_rename_recipe, strCurrentRecipe), strCurrentRecipe);
        newFragment.show(getSupportFragmentManager(), "renameRecipe");
    }

    public void onAddRecipeItem(View v)
    {
        ArrayList<String> inputList = new ArrayList<>();
        for(String strName : m_GroceryPlanning.m_Ingredients.getAllIngredients())
        {
            RecipeItemsAdapter adapterItems = (RecipeItemsAdapter)m_RecyclerView.getAdapter();
            if(adapterItems == null || adapterItems.containsItem(strName))
            {
                continue;
            }
            inputList.add(strName);
        }

        DialogFragment newFragment = InputStringDialogFragment.newInstance(getResources().getString(R.string.text_add_ingredient), "", inputList);
        newFragment.show(getSupportFragmentManager(), "addRecipeItem");
    }

    public void onStringInput(String tag, String strInput, String strAdditonalInformation)
    {
        switch(tag) {
            case "addRecipe":
            {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                final Integer iNrPersons = preferences.getInt(SettingsActivity.KEY_DEFAULT_NRPERSONS, 4);

                m_GroceryPlanning.m_Recipes.addRecipe(strInput, iNrPersons);
                m_SpinnerRecipesAdapter.add(strInput);
                m_SpinnerRecipes.setSelection(m_SpinnerRecipesAdapter.getCount() - 1);
                updateVisibility();
                break;
            }

            case "renameRecipe":
            {
                final String strCurrentRecipe = (String) m_SpinnerRecipes.getSelectedItem();

                m_GroceryPlanning.m_Recipes.renameRecipe(strCurrentRecipe, strInput);

                int index = m_SpinnerRecipes.getSelectedItemPosition();
                m_SpinnerRecipesAdapter.remove(strCurrentRecipe);
                m_SpinnerRecipesAdapter.insert(strInput, index);
                m_SpinnerRecipes.setSelection(index);

                Toast.makeText(ManageRecipes.this, getResources().getString(R.string.text_recipe_renamed, strCurrentRecipe, strInput), Toast.LENGTH_SHORT).show();
                break;
            }

            case "addRecipeItem":
            {
                String strRecipe = (String) m_SpinnerRecipes.getSelectedItem();
                Recipes.Recipe recipe = m_GroceryPlanning.m_Recipes.getRecipe(strRecipe);

                RecipeItem item = new RecipeItem();
                item.m_Ingredient = strInput;
                item.m_Amount.m_Unit = m_GroceryPlanning.m_Ingredients.getIngredient(strInput).m_DefaultUnit;
                recipe.m_Items.add(item);

                RecipeItemsAdapter adapter = (RecipeItemsAdapter) m_RecyclerView.getAdapter();
                if (adapter == null) {
                    return;
                }
                adapter.notifyItemInserted(recipe.m_Items.size() - 1);
                adapter.setActiveElement(strInput);
                break;
            }
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        String strRecipe = (String)m_SpinnerRecipes.getSelectedItem();
        Recipes.Recipe recipe = m_GroceryPlanning.m_Recipes.getRecipe(strRecipe);
        m_GroceryPlanning.m_Recipes.setActiveRecipe(strRecipe);

        m_EditTextNrPersons.setText(String.format(Locale.getDefault(), "%d", recipe.m_NumberOfPersons));

        m_Adapter = new RecipeItemsAdapter(m_RecyclerView, recipe);
        m_RecyclerView.setAdapter(m_Adapter);
        ItemClickSupport.addTo(m_RecyclerView).setOnItemClickListener(
            (RecyclerView recyclerView, int position, View v) ->
            {
                RecipeItemsAdapter adapter = (RecipeItemsAdapter) recyclerView.getAdapter();
                RecipeItemsAdapter.ViewHolder vh = (RecipeItemsAdapter.ViewHolder) recyclerView.getChildViewHolder(v);

                if(adapter == null)
                {
                    return;
                }

                if(vh.getID().equals(adapter.getActiveElement()))
                {
                    adapter.setActiveElement("");
                }
                else
                {
                    adapter.setActiveElement(vh.getID());
                }
            }
        );
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ReactToTouchActionsCallback((ReactToTouchActionsInterface)m_RecyclerView.getAdapter(),
                                                                                              this,
                                                                                              R.drawable.ic_delete_black_24dp,
                                                                                              false));
        itemTouchHelper.attachToRecyclerView(m_RecyclerView);

        if(m_SavedActiveElement != null && m_SavedActiveRecipe != null)
        {
            if(m_SavedActiveRecipe.equals(strRecipe))
            {
                m_Adapter.setActiveElement(m_SavedActiveElement);
            }
            else
            {
                m_SavedActiveRecipe = null;
                m_SavedActiveElement = null;
            }
        }
    }

    public void onNothingSelected(AdapterView<?> parent)
    {
    }
}
