package ch.phwidmer.einkaufsliste.UI;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import java.security.InvalidParameterException;
import java.util.Optional;

import ch.phwidmer.einkaufsliste.data.Categories;
import ch.phwidmer.einkaufsliste.data.GroceryPlanningFactory;
import ch.phwidmer.einkaufsliste.data.Ingredients;
import ch.phwidmer.einkaufsliste.data.Unit;
import ch.phwidmer.einkaufsliste.helper.ItemClickSupport;
import ch.phwidmer.einkaufsliste.R;
import ch.phwidmer.einkaufsliste.data.GroceryPlanning;
import ch.phwidmer.einkaufsliste.helper.ReactToTouchActionsCallback;
import ch.phwidmer.einkaufsliste.helper.ReactToTouchActionsInterface;
import ch.phwidmer.einkaufsliste.helper.stringInput.InputStringFree;
import ch.phwidmer.einkaufsliste.helper.stringInput.InputStringResponder;

public class IngredientsActivity extends AppCompatActivity implements InputStringResponder
{
    private GroceryPlanning m_GroceryPlanning;

    private RecyclerView                m_RecyclerView;
    private IngredientsAdapter          m_Adapter;

    private FloatingActionButton        m_FAB;
    private ItemTouchHelper             m_ItemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_ingredients);

        try {
            m_GroceryPlanning = GroceryPlanningFactory.groceryPlanning(this);
        }
        catch(InvalidParameterException e)
        {
            MainActivity.showErrorDialog(getString(R.string.text_load_file_failed), e.getMessage(), this);
        }

        m_FAB = findViewById(R.id.fab);
        CoordinatorLayout coordLayout = findViewById(R.id.fabCoordinatorLayout);

        m_RecyclerView = findViewById(R.id.recyclerViewIngredients);
        m_RecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        m_RecyclerView.setLayoutManager(layoutManager);
        m_Adapter = new IngredientsAdapter(coordLayout, m_RecyclerView, m_GroceryPlanning);
        m_RecyclerView.setAdapter(m_Adapter);
        ItemClickSupport.addTo(m_RecyclerView).setOnItemClickListener(
            (RecyclerView recyclerView, int position, View v) ->
            {
                IngredientsAdapter adapter = (IngredientsAdapter) recyclerView.getAdapter();
                IngredientsAdapter.ViewHolder vh = (IngredientsAdapter.ViewHolder) recyclerView.getChildViewHolder(v);
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

        if(savedInstanceState != null)
        {
            String strActiveElement = savedInstanceState.getString("AdapterActiveElement");
            if(strActiveElement == null)
            {
                strActiveElement = "";
            }
            m_Adapter.setActiveElement(strActiveElement);
        }

        if(m_GroceryPlanning.categories().getCategoriesCount() == 0 || m_GroceryPlanning.categories().getAllSortOrderNames().size() == 0)
        {
            m_FAB.hide();
        }

        m_RecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && m_FAB.getVisibility() == View.VISIBLE) {
                    m_FAB.hide();
                } else if (dy <= 0 && m_FAB.getVisibility() != View.VISIBLE) {
                    m_FAB.show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ingredients_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if(id == android.R.id.home)
        {
            onBackPressed();
            return true;
        }
        else if (id == R.id.action_search)
        {
            SearchView view = (SearchView)item.getActionView();
            view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String text) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String text) {
                    IngredientsAdapter adapter = (IngredientsAdapter)m_RecyclerView.getAdapter();
                    if(adapter == null)
                    {
                        return true;
                    }
                    adapter.getFilter().filter(text);
                    return true;
                }
            });
            view.setIconified(false);
            view.requestFocus();
            item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {
                    return true;
                }

                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {
                    // Write your code here
                    m_Adapter.getFilter().filter("");
                    view.setQuery("", false);
                    view.clearFocus();
                    return true;
                }
            });
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
            savedInstanceState.putString("AdapterActiveElement", m_Adapter.getActiveElement());
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    public void onAddIngredient(@NonNull View v)
    {
        InputStringFree newFragment = InputStringFree.newInstance(getResources().getString(R.string.text_add_ingredient));
        newFragment.setListExcludedInputs(m_GroceryPlanning.ingredients().getAllIngredientNames());
        newFragment.show(getSupportFragmentManager(), "addIngredient");
    }

    public void onStringInput(@NonNull String tag, @NonNull String strInput, @NonNull String strAdditonalInformation)
    {
        IngredientsAdapter adapter = (IngredientsAdapter)m_RecyclerView.getAdapter();
        if(adapter == null)
        {
            return;
        }

        if(tag.equals("addIngredient"))
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            final String strDefaultUnit = preferences.getString(SettingsActivity.KEY_DEFAULT_UNIT, SettingsActivity.defaultUnit.toString());

            Optional<Categories.Category> defaultCategory = m_GroceryPlanning.categories().getDefaultCategory();
            if(!defaultCategory.isPresent())
            {
                return;
            }
            Optional<Ingredients.Ingredient> ingredient = m_GroceryPlanning.ingredients().addIngredient(strInput,
                                                                                                        Unit.valueOf(strDefaultUnit),
                                                                                                        defaultCategory.get());
            if(!ingredient.isPresent())
            {
                return;
            }
            adapter.updateFilteredListForElementAdded(ingredient.get());
            adapter.setActiveElement(strInput);
            adapter.notifyDataSetChanged();
            m_RecyclerView.scrollToPosition(adapter.getCurrentPositionOfElement(ingredient.get()));
        }
        else if(tag.equals("renameIngredient")) // See IngredientsAdapter
        {
            Optional<Ingredients.Ingredient> ingredient = m_GroceryPlanning.ingredients().getIngredient(strAdditonalInformation);
            if(!ingredient.isPresent())
            {
                return;
            }
            m_GroceryPlanning.ingredients().renameIngredient(ingredient.get(), strInput);
            m_GroceryPlanning.recipes().onIngredientRenamed(strAdditonalInformation, strInput);
            m_GroceryPlanning.shoppingList().onIngredientRenamed(strAdditonalInformation, strInput);

            adapter.setActiveElement(strInput);
            adapter.notifyDataSetChanged();
            Toast.makeText(m_RecyclerView.getContext(), m_RecyclerView.getContext().getResources().getString(R.string.text_ingredient_renamed, strAdditonalInformation, strInput), Toast.LENGTH_SHORT).show();
        }
    }
}
