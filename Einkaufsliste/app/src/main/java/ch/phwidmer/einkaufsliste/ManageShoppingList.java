package ch.phwidmer.einkaufsliste;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class ManageShoppingList extends AppCompatActivity implements InputStringDialogFragment.InputStringResponder
{
    private GroceryPlanning         m_GroceryPlanning;

    private ShoppingList            m_RecentlyDeletedShoppingList;

    private RecyclerView            m_RecyclerViewRecipes;
    private ShoppingRecipesAdapter  m_AdapterRecipes;

    private FloatingActionButton    m_FAB;

    private ItemTouchHelper         m_ItemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_shopping_list);

        Intent intent = getIntent();
        m_GroceryPlanning = intent.getParcelableExtra(MainActivity.EXTRA_GROCERYPLANNING);

        m_FAB = findViewById(R.id.fab);
        CoordinatorLayout coordLayout = findViewById(R.id.fabCoordinatorLayout);

        m_RecyclerViewRecipes = findViewById(R.id.recyclerViewShoppingRecipe);
        m_RecyclerViewRecipes.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManagerRecipes = new LinearLayoutManager(this);
        m_RecyclerViewRecipes.setLayoutManager(layoutManagerRecipes);
        m_AdapterRecipes = new ShoppingRecipesAdapter(coordLayout, m_RecyclerViewRecipes, m_GroceryPlanning.m_Ingredients, m_GroceryPlanning.m_ShoppingList);
        m_RecyclerViewRecipes.setAdapter(m_AdapterRecipes);
        ItemClickSupport.addTo(m_RecyclerViewRecipes).setOnItemClickListener(
                (RecyclerView recyclerView, int position, View v) ->
                {
                    ShoppingRecipesAdapter adapter = (ShoppingRecipesAdapter) recyclerView.getAdapter();
                    ShoppingRecipesAdapter.ViewHolder vh = (ShoppingRecipesAdapter.ViewHolder) recyclerView.getChildViewHolder(v);
                    if(adapter == null)
                    {
                        return;
                    }

                    if(vh.getID().equals(adapter.getActiveElement()))
                    {
                        adapter.setActiveElement(null);
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
        m_ItemTouchHelper = new ItemTouchHelper(new ReactToTouchActionsCallback((ReactToTouchActionsInterface)m_RecyclerViewRecipes.getAdapter(),
                                                                                              this,
                                                                                              R.drawable.ic_delete_black_24dp,
                                                                                              false));
        m_ItemTouchHelper.attachToRecyclerView(m_RecyclerViewRecipes);

        if(savedInstanceState != null)
        {
            String strActiveElementFirst = savedInstanceState.getString("AdapterActiveElementFirst");
            String strActiveElementSecond = savedInstanceState.getString("AdapterActiveElementSecond");
            if(strActiveElementFirst != null)
            {
                m_AdapterRecipes.setActiveElement(new Pair<>(strActiveElementFirst, strActiveElementSecond));
            }
        }

        m_RecyclerViewRecipes.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        if(m_AdapterRecipes != null && m_AdapterRecipes.getActiveElement() != null)
        {
            savedInstanceState.putString("AdapterActiveElementFirst", m_AdapterRecipes.getActiveElement().first);
            savedInstanceState.putString("AdapterActiveElementSecond", m_AdapterRecipes.getActiveElement().second);
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    public void onAddShoppingRecipe(View v)
    {
        ArrayList<String> inputList = new ArrayList<>();
        for(String strName : m_GroceryPlanning.m_Recipes.getAllRecipes())
        {
            ShoppingRecipesAdapter adapterItems = (ShoppingRecipesAdapter)m_RecyclerViewRecipes.getAdapter();
            if(adapterItems == null)
            {
                continue;
            }
            inputList.add(strName);
        }
        if(inputList.isEmpty())
        {
            Toast.makeText(v.getContext(), R.string.text_all_recipes_alreay_added, Toast.LENGTH_SHORT).show();
            return;
        }

        InputStringDialogFragment newFragment = InputStringDialogFragment.newInstance(getResources().getString(R.string.text_import_recipe));
        newFragment.setListOnlyAllowed(inputList);
        newFragment.show(getSupportFragmentManager(), "addShoppingRecipe");
    }

    public void onIncreaseAmount(View v)
    {
        ShoppingRecipesAdapter adapter = (ShoppingRecipesAdapter)m_RecyclerViewRecipes.getAdapter();
        if(adapter == null)
        {
            return;
        }
        adapter.onChangeAmount(true);
    }

    public void onDecreaseAmount(View v)
    {
        ShoppingRecipesAdapter adapter = (ShoppingRecipesAdapter)m_RecyclerViewRecipes.getAdapter();
        if(adapter == null)
        {
            return;
        }
        adapter.onChangeAmount(false);
    }

    public void onStringInput(String tag, String strInput, String strAdditonalInformation)
    {
        ShoppingRecipesAdapter adapter = (ShoppingRecipesAdapter)m_RecyclerViewRecipes.getAdapter();
        if(adapter == null)
        {
            return;
        }

        switch(tag)
        {
            case "addRecipeItem": // See ShoppingRecipesAdapter
            {
                ShoppingListItem item = new ShoppingListItem();
                item.m_Ingredient = strInput;
                item.m_Amount.m_Unit = m_GroceryPlanning.m_Ingredients.getIngredient(strInput).m_DefaultUnit;
                m_GroceryPlanning.m_ShoppingList.getShoppingRecipe(strAdditonalInformation).m_Items.add(item);

                Pair<String, String> newItem = new Pair<>(strAdditonalInformation, strInput);
                adapter.notifyDataSetChanged();
                adapter.setActiveElement(newItem);
                break;
            }

            case "changeRecipeScaling": // See ShoppingRecipesAdapter
            {
                float fNewValue = Float.valueOf(strInput);

                ShoppingList.ShoppingRecipe recipe = m_GroceryPlanning.m_ShoppingList.getShoppingRecipe(strAdditonalInformation);
                recipe.changeScalingFactor(fNewValue);
                adapter.notifyDataSetChanged();
                break;
            }

            case "addShoppingRecipe":
            {
                String strRecipe = strInput;
                int iNr = 2;
                while(adapter.containsItem(new Pair<>(strRecipe, "")))
                {
                    strRecipe = String.format(Locale.getDefault(), "%s (%d)", strInput, iNr);
                    ++iNr;
                }

                m_GroceryPlanning.m_ShoppingList.addFromRecipe(strRecipe, m_GroceryPlanning.m_Recipes.getRecipe(strInput));

                adapter.notifyDataSetChanged();
                adapter.setActiveElement(new Pair<>(strRecipe, ""));
                m_RecyclerViewRecipes.scrollToPosition(adapter.getItemCount()-1);
                break;
            }

            case "renameShoppingRecipe":
            {
                m_GroceryPlanning.m_ShoppingList.renameRecipe(strAdditonalInformation, strInput);

                adapter.notifyDataSetChanged();
                adapter.setActiveElement(new Pair<>(strInput, ""));
                m_RecyclerViewRecipes.scrollToPosition(adapter.getItemCount()-1);
                Toast.makeText(this, getResources().getString(R.string.text_recipe_renamed, strAdditonalInformation, strInput), Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    public void onResetList(View v)
    {
        m_RecentlyDeletedShoppingList = m_GroceryPlanning.m_ShoppingList;

        CoordinatorLayout coordLayout = findViewById(R.id.fabCoordinatorLayout);

        m_GroceryPlanning.m_ShoppingList = new ShoppingList();
        m_AdapterRecipes = new ShoppingRecipesAdapter(coordLayout, m_RecyclerViewRecipes, m_GroceryPlanning.m_Ingredients, m_GroceryPlanning.m_ShoppingList);
        m_RecyclerViewRecipes.setAdapter(m_AdapterRecipes);

        // Allow undo

        Snackbar snackbar = Snackbar.make(coordLayout, R.string.text_shoppnglist_reset, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.text_undo, (View view) ->
        {
            m_GroceryPlanning.m_ShoppingList = m_RecentlyDeletedShoppingList;
            m_AdapterRecipes = new ShoppingRecipesAdapter(coordLayout, m_RecyclerViewRecipes, m_GroceryPlanning.m_Ingredients, m_GroceryPlanning.m_ShoppingList);
            m_RecyclerViewRecipes.setAdapter(m_AdapterRecipes);

            m_RecentlyDeletedShoppingList = null;

            Snackbar snackbar1 = Snackbar.make(coordLayout, R.string.text_shoppnglist_restored, Snackbar.LENGTH_SHORT);
            snackbar1.show();
        });
        snackbar.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reset_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.actionbar_button_reset)
        {
            onResetList(null);
        }
        else if(id == android.R.id.home)
        {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
