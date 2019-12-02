package ch.phwidmer.einkaufsliste.UI;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;

import ch.phwidmer.einkaufsliste.data.GroceryPlanningFactory;
import ch.phwidmer.einkaufsliste.data.Ingredients;
import ch.phwidmer.einkaufsliste.helper.InputStringDialogFragment;
import ch.phwidmer.einkaufsliste.helper.ItemClickSupport;
import ch.phwidmer.einkaufsliste.R;
import ch.phwidmer.einkaufsliste.data.GroceryPlanning;
import ch.phwidmer.einkaufsliste.data.RecipeItem;
import ch.phwidmer.einkaufsliste.data.Recipes;
import ch.phwidmer.einkaufsliste.helper.ReactToTouchActionsCallback;
import ch.phwidmer.einkaufsliste.helper.ReactToTouchActionsInterface;

public class RecipesActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, InputStringDialogFragment.InputStringResponder {

    private GroceryPlanning m_GroceryPlanning;

    private Spinner                     m_SpinnerRecipes;
    private EditText                    m_EditTextNrPersons;
    private TextView                    m_textViewNrPersons;

    private ArrayAdapter<CharSequence>  m_SpinnerRecipesAdapter;

    private ItemTouchHelper             m_ItemTouchHelper;

    private String                      m_strRecentlyDeletedRecipe;
    private Recipes.Recipe              m_RecentlyDeletedRecipe;

    private RecyclerView                m_RecyclerView;
    private RecipeItemsAdapter          m_Adapter;

    private String                      m_SavedActiveRecipe;
    private String                      m_SavedActiveElement;

    private FloatingActionButton        m_FAB;
    private FloatingActionButton        m_FABGroup;

    private boolean                     m_IgnoreNextSpinnerRecipesClick = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_recipes);

        m_GroceryPlanning = GroceryPlanningFactory.groceryPlanning(this);

        m_FAB = findViewById(R.id.fab);
        m_FABGroup = findViewById(R.id.fabGroup);

        m_EditTextNrPersons = findViewById(R.id.editText_NrPersons);
        m_textViewNrPersons = findViewById(R.id.textViewNrPersons);
        m_SpinnerRecipes = findViewById(R.id.spinnerRecipes);

        m_EditTextNrPersons.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                String strRecipe = (String)m_SpinnerRecipes.getSelectedItem();
                Optional<Recipes.Recipe> recipe = m_GroceryPlanning.recipes().getRecipe(strRecipe);
                if(!recipe.isPresent())
                {
                    return;
                }

                if(s.toString().isEmpty())
                {
                    recipe.get().setNumberOfPersons(0);
                }
                else
                {
                    recipe.get().setNumberOfPersons(Integer.valueOf(s.toString()));
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        m_RecyclerView = findViewById(R.id.recyclerViewRecipeItems);
        m_RecyclerView.setHasFixedSize(true);
        m_RecyclerView.setLayoutManager(new LinearLayoutManager(this));
        m_RecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && m_FAB.getVisibility() == View.VISIBLE) {
                    m_FAB.hide();
                    m_FABGroup.hide();
                } else if (dy < 0 && m_FAB.getVisibility() != View.VISIBLE) {
                    m_FAB.show();
                    m_FABGroup.show();
                }
            }
        });

        m_SpinnerRecipesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        for(Recipes.Recipe recipe : m_GroceryPlanning.recipes().getAllRecipes())
        {
            m_SpinnerRecipesAdapter.add(recipe.getName());
        }
        m_SpinnerRecipesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_SpinnerRecipes.setAdapter(m_SpinnerRecipesAdapter);
        m_SpinnerRecipes.setOnItemSelectedListener(this);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String strActiveRecipe = preferences.getString(SettingsActivity.KEY_ACTIVE_RECIPE, "");
        if(strActiveRecipe == null)
        {
            strActiveRecipe = "";
        }
        Optional<Recipes.Recipe> recipe = m_GroceryPlanning.recipes().getRecipe(strActiveRecipe);
        if(recipe.isPresent() && m_GroceryPlanning.recipes().getAllRecipes().contains(recipe.get()))
        {
            m_SpinnerRecipes.setSelection(m_SpinnerRecipesAdapter.getPosition(strActiveRecipe));
            onItemSelected(null, null, m_SpinnerRecipesAdapter.getPosition(strActiveRecipe), 0);
            m_IgnoreNextSpinnerRecipesClick = true;
        }
        registerForContextMenu(m_SpinnerRecipes);

        if(savedInstanceState != null)
        {
            m_SavedActiveRecipe = savedInstanceState.getString("AdapterActiveRecipe");
            m_SavedActiveElement = savedInstanceState.getString("AdapterActiveElement");
        }

        updateVisibility();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_recipe_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_button_rename:
                onRenameRecipe(m_SpinnerRecipes);
                return true;
            case R.id.menu_button_copy:
                onCopyRecipe(m_SpinnerRecipes);
                return true;
            case R.id.menu_button_delete:
            {
                String strName = (String)m_SpinnerRecipes.getSelectedItem();

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getResources().getString(R.string.text_delete_recipe_header));
                builder.setMessage(getResources().getString(R.string.text_delete_recipe, strName));
                builder.setPositiveButton(android.R.string.yes, (DialogInterface dialog, int which) -> onDelRecipe(m_SpinnerRecipes));
                builder.setNegativeButton(android.R.string.no, (DialogInterface dialog, int which) -> {});
                builder.show();

                return true;
            }
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void updateVisibility()
    {
        if(m_SpinnerRecipes.getAdapter().getCount() > 0)
        {
            m_EditTextNrPersons.setVisibility(View.VISIBLE);
            m_textViewNrPersons.setVisibility(View.VISIBLE);
        }
        else
        {
            m_EditTextNrPersons.setVisibility(View.INVISIBLE);
            m_textViewNrPersons.setVisibility(View.INVISIBLE);

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
        m_GroceryPlanning.flush();
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

    public void onAddRecipe(@NonNull View v)
    {
        InputStringDialogFragment newFragment = InputStringDialogFragment.newInstance(getResources().getString(R.string.text_add_recipe));
        newFragment.setListExcludedInputs(m_GroceryPlanning.recipes().getAllRecipeNames());
        newFragment.show(getSupportFragmentManager(), "addRecipe");
    }

    public void onDelRecipe(@NonNull View v)
    {
        if(m_SpinnerRecipes.getSelectedItem() == null)
        {
            return;
        }

        String strName = (String)m_SpinnerRecipes.getSelectedItem();

        m_strRecentlyDeletedRecipe = strName;
        Optional<Recipes.Recipe> recipeToDelete = m_GroceryPlanning.recipes().getRecipe(strName);
        if(!recipeToDelete.isPresent())
        {
            return;
        }
        m_RecentlyDeletedRecipe = recipeToDelete.get();

        m_GroceryPlanning.recipes().removeRecipe(m_RecentlyDeletedRecipe);
        m_SpinnerRecipesAdapter.remove((CharSequence)m_SpinnerRecipes.getSelectedItem());
        m_SpinnerRecipes.setAdapter(m_SpinnerRecipesAdapter);
        updateVisibility();

        // Allow undo

        CoordinatorLayout coordLayout = findViewById(R.id.fabCoordinatorLayout);
        Snackbar snackbar = Snackbar.make(coordLayout, R.string.text_recipe_deleted, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.text_undo, (View view) ->
        {
            m_GroceryPlanning.recipes().addRecipe(m_strRecentlyDeletedRecipe, m_RecentlyDeletedRecipe);
            m_SpinnerRecipesAdapter.add(m_strRecentlyDeletedRecipe);
            m_SpinnerRecipes.setSelection(m_SpinnerRecipesAdapter.getCount() - 1);
            updateVisibility();

            m_strRecentlyDeletedRecipe = "";
            m_RecentlyDeletedRecipe = null;

            Snackbar snackbar1 = Snackbar.make(coordLayout, R.string.text_recipe_restored, Snackbar.LENGTH_SHORT);
            snackbar1.show();
        });
        snackbar.show();
    }

    public void onRenameRecipe(@NonNull View v)
    {
        final String strCurrentRecipe = (String)m_SpinnerRecipes.getSelectedItem();

        InputStringDialogFragment newFragment = InputStringDialogFragment.newInstance(getResources().getString(R.string.text_rename_recipe, strCurrentRecipe));
        newFragment.setDefaultValue(strCurrentRecipe);
        newFragment.setListExcludedInputs(m_GroceryPlanning.recipes().getAllRecipeNames());
        newFragment.show(getSupportFragmentManager(), "renameRecipe");
    }

    public void onCopyRecipe(@NonNull View v)
    {
        final String strCurrentRecipe = (String)m_SpinnerRecipes.getSelectedItem();

        InputStringDialogFragment newFragment = InputStringDialogFragment.newInstance(getResources().getString(R.string.text_copy_recipe, strCurrentRecipe));
        newFragment.setDefaultValue(strCurrentRecipe);
        newFragment.setListExcludedInputs(m_GroceryPlanning.recipes().getAllRecipeNames());
        newFragment.show(getSupportFragmentManager(), "copyRecipe");
    }

    public void onAddRecipeItem(@NonNull View v)
    {
        ArrayList<String> inputList = new ArrayList<>();
        for(Ingredients.Ingredient ingredient : m_GroceryPlanning.ingredients().getAllIngredients())
        {
            RecipeItemsAdapter adapterItems = (RecipeItemsAdapter)m_RecyclerView.getAdapter();
            if(adapterItems == null || adapterItems.containsItem(ingredient.getName()))
            {
                continue;
            }
            inputList.add(ingredient.getName());
        }

        InputStringDialogFragment newFragment = InputStringDialogFragment.newInstance(getResources().getString(R.string.text_add_ingredient));
        newFragment.setListOnlyAllowed(inputList);
        newFragment.show(getSupportFragmentManager(), "addRecipeItem");
    }

    public void onIncreaseAmount(@NonNull View v)
    {
        RecipeItemsAdapter adapter = (RecipeItemsAdapter) m_RecyclerView.getAdapter();
        if(adapter == null)
        {
            return;
        }
        adapter.onChangeAmount(false,true);
    }

    public void onDecreaseAmount(@NonNull View v)
    {
        RecipeItemsAdapter adapter = (RecipeItemsAdapter) m_RecyclerView.getAdapter();
        if(adapter == null)
        {
            return;
        }
        adapter.onChangeAmount(false,false);
    }

    public void onIncreaseAmountMax(@NonNull View v)
    {
        RecipeItemsAdapter adapter = (RecipeItemsAdapter) m_RecyclerView.getAdapter();
        if(adapter == null)
        {
            return;
        }
        adapter.onChangeAmount(true,true);
    }

    public void onDecreaseAmountMax(@NonNull View v)
    {
        RecipeItemsAdapter adapter = (RecipeItemsAdapter) m_RecyclerView.getAdapter();
        if(adapter == null)
        {
            return;
        }
        adapter.onChangeAmount(true,false);
    }

    public void onAddAlternativesGroup(@NonNull View v)
    {
        String strRecipe = (String) m_SpinnerRecipes.getSelectedItem();
        Optional<Recipes.Recipe> recipe = m_GroceryPlanning.recipes().getRecipe(strRecipe);
        if(!recipe.isPresent())
        {
            return;
        }
        ArrayList<String> excludedInputs = recipe.get().getAllGroupNames();

        InputStringDialogFragment newFragment = InputStringDialogFragment.newInstance(getResources().getString(R.string.text_add_group));
        newFragment.setListExcludedInputs(excludedInputs);
        newFragment.show(getSupportFragmentManager(), "addAlternativesGroup");
    }

    @Override
    public void onStringInput(@NonNull String tag, @NonNull String strInput, @NonNull String strAdditonalInformation)
    {
        switch(tag) {
            case "addRecipe":
            {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                final int iNrPersons = preferences.getInt(SettingsActivity.KEY_DEFAULT_NRPERSONS, 4);

                m_GroceryPlanning.recipes().addRecipe(strInput, iNrPersons);
                m_SpinnerRecipesAdapter.add(strInput);
                m_SpinnerRecipes.setSelection(m_SpinnerRecipesAdapter.getCount() - 1);
                updateVisibility();
                break;
            }

            case "renameRecipe":
            {
                final String strCurrentRecipe = (String) m_SpinnerRecipes.getSelectedItem();
                Optional<Recipes.Recipe> recipe = m_GroceryPlanning.recipes().getRecipe(strCurrentRecipe);
                if(!recipe.isPresent())
                {
                    return;
                }
                m_GroceryPlanning.recipes().renameRecipe(recipe.get(), strInput);

                int index = m_SpinnerRecipes.getSelectedItemPosition();
                m_SpinnerRecipesAdapter.remove(strCurrentRecipe);
                m_SpinnerRecipesAdapter.insert(strInput, index);
                m_SpinnerRecipes.setSelection(index);

                Toast.makeText(RecipesActivity.this, getResources().getString(R.string.text_recipe_renamed, strCurrentRecipe, strInput), Toast.LENGTH_SHORT).show();
                break;
            }

            case "copyRecipe":
            {
                final String strCurrentRecipe = (String) m_SpinnerRecipes.getSelectedItem();
                Optional<Recipes.Recipe> recipe = m_GroceryPlanning.recipes().getRecipe(strCurrentRecipe);
                if(!recipe.isPresent())
                {
                    return;
                }
                m_GroceryPlanning.recipes().copyRecipe(recipe.get(), strInput);

                m_SpinnerRecipesAdapter.add(strInput);
                m_SpinnerRecipes.setSelection(m_SpinnerRecipesAdapter.getCount() - 1);
                break;
            }

            case "addAlternativesGroup":
            {
                String strRecipe = (String) m_SpinnerRecipes.getSelectedItem();
                Optional<Recipes.Recipe> recipe = m_GroceryPlanning.recipes().getRecipe(strRecipe);
                if(!recipe.isPresent())
                {
                    return;
                }

                recipe.get().addGroup(strInput);
                RecipeItemsAdapter adapter = (RecipeItemsAdapter) m_RecyclerView.getAdapter();
                if (adapter == null) {
                    return;
                }
                adapter.notifyItemInserted(adapter.getItemCount() - 1);
                adapter.setActiveElement("");

                break;
            }

            case "renameAlternativesGroup":
            {
                String strRecipe = (String) m_SpinnerRecipes.getSelectedItem();
                Optional<Recipes.Recipe> recipe = m_GroceryPlanning.recipes().getRecipe(strRecipe);
                if(!recipe.isPresent())
                {
                    return;
                }
                recipe.get().renameGroup(strAdditonalInformation, strInput);

                RecipeItemsAdapter adapter = (RecipeItemsAdapter) m_RecyclerView.getAdapter();
                if (adapter == null) {
                    return;
                }
                adapter.notifyDataSetChanged();
                adapter.setActiveElement("");
                Toast.makeText(this, getResources().getString(R.string.text_group_renamed, strAdditonalInformation, strInput), Toast.LENGTH_SHORT).show();
                break;
            }

            case "addRecipeItem":
            {
                String strRecipe = (String) m_SpinnerRecipes.getSelectedItem();
                Optional<Recipes.Recipe> recipe = m_GroceryPlanning.recipes().getRecipe(strRecipe);
                if(!recipe.isPresent())
                {
                    return;
                }

                Optional<RecipeItem> item = recipe.get().addRecipeItem(strInput);
                Optional<Ingredients.Ingredient> ingredient = m_GroceryPlanning.ingredients().getIngredient(strInput);
                if(!item.isPresent() || !ingredient.isPresent())
                {
                    return;
                }
                item.get().getAmount().setUnit(ingredient.get().getDefaultUnit());

                RecipeItemsAdapter adapter = (RecipeItemsAdapter) m_RecyclerView.getAdapter();
                if (adapter == null) {
                    return;
                }
                adapter.notifyItemInserted(recipe.get().getAllRecipeItems().size() - 1);
                adapter.setActiveElement(strInput);
                break;
            }

            case "addRecipeItemToGroup":
            {
                String strRecipe = (String) m_SpinnerRecipes.getSelectedItem();
                Optional<Recipes.Recipe> recipe = m_GroceryPlanning.recipes().getRecipe(strRecipe);
                if(!recipe.isPresent())
                {
                    return;
                }

                Optional<RecipeItem> item = recipe.get().addRecipeItemToGroup(strAdditonalInformation, strInput);
                Optional<Ingredients.Ingredient> ingredient = m_GroceryPlanning.ingredients().getIngredient(strInput);
                if(!item.isPresent() || !ingredient.isPresent())
                {
                    return;
                }
                item.get().getAmount().setUnit(ingredient.get().getDefaultUnit());

                RecipeItemsAdapter adapter = (RecipeItemsAdapter) m_RecyclerView.getAdapter();
                if (adapter == null) {
                    return;
                }
                adapter.notifyDataSetChanged();
                adapter.setActiveElement(strInput);
                break;
            }
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        if(m_IgnoreNextSpinnerRecipesClick)
        {
            m_IgnoreNextSpinnerRecipesClick = false;
            return;
        }

        String strRecipe = (String)m_SpinnerRecipes.getSelectedItem();
        Optional<Recipes.Recipe> recipe = m_GroceryPlanning.recipes().getRecipe(strRecipe);
        if(!recipe.isPresent())
        {
            return;
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SettingsActivity.KEY_ACTIVE_RECIPE, strRecipe);
        editor.apply();

        m_EditTextNrPersons.setText(String.format(Locale.getDefault(), "%d", recipe.get().getNumberOfPersons()));

        CoordinatorLayout coordLayout = findViewById(R.id.fabCoordinatorLayout);
        m_Adapter = new RecipeItemsAdapter(coordLayout, m_RecyclerView, recipe.get(), m_GroceryPlanning.ingredients());
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

        if(m_ItemTouchHelper != null)
        {
            m_ItemTouchHelper.attachToRecyclerView(null);
        }
        m_ItemTouchHelper = new ItemTouchHelper(new ReactToTouchActionsCallback((ReactToTouchActionsInterface)m_RecyclerView.getAdapter(),
                                                                                              this,
                                                                                              R.drawable.ic_delete_black_24dp,
                                                                                              false));
        m_ItemTouchHelper.attachToRecyclerView(m_RecyclerView);

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

    public void onNothingSelected(@NonNull AdapterView<?> parent)
    {
    }
}
