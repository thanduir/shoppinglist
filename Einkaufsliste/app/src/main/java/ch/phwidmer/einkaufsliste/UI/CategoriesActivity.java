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
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Optional;

import ch.phwidmer.einkaufsliste.data.GroceryPlanningFactory;
import ch.phwidmer.einkaufsliste.helper.InputStringDialogFragment;
import ch.phwidmer.einkaufsliste.R;
import ch.phwidmer.einkaufsliste.data.Categories;
import ch.phwidmer.einkaufsliste.data.GroceryPlanning;
import ch.phwidmer.einkaufsliste.helper.ReactToTouchActionsCallback;
import ch.phwidmer.einkaufsliste.helper.ReactToTouchActionsInterface;

public class CategoriesActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, InputStringDialogFragment.InputStringResponder
{
    private GroceryPlanning m_GroceryPlanning;

    private Spinner                         m_SpinnerSortOrders;
    private RecyclerView                    m_RecyclerView;
    private RecyclerView.Adapter            m_Adapter;

    private ArrayAdapter<CharSequence>      m_SpinnerSortOrdersAdapter;

    private FloatingActionButton            m_FAB;

    private ItemTouchHelper                 m_ItemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        m_GroceryPlanning = GroceryPlanningFactory.groceryPlanning(this);

        m_FAB = findViewById(R.id.fab);
        m_FAB.hide();

        // Manage SortOrders

        m_SpinnerSortOrders = findViewById(R.id.spinnerSortOrder);

        m_SpinnerSortOrdersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        for(Categories.SortOrder sortOrder : m_GroceryPlanning.categories().getAllSortOrders())
        {
            m_SpinnerSortOrdersAdapter.add(sortOrder.getName());
        }
        m_SpinnerSortOrdersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_SpinnerSortOrders.setAdapter(m_SpinnerSortOrdersAdapter);
        m_SpinnerSortOrders.setOnItemSelectedListener(this);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String strActiveSortOrder = preferences.getString(SettingsActivity.KEY_ACTIVE_SORTORDER_CATEGORIES, "");
        if(strActiveSortOrder == null)
        {
            strActiveSortOrder = "";
        }
        Optional<Categories.SortOrder> sortOrder = m_GroceryPlanning.categories().getSortOrder(strActiveSortOrder);
        if(sortOrder.isPresent() && m_GroceryPlanning.categories().getAllSortOrders().contains(sortOrder.get()))
        {
            m_SpinnerSortOrders.setSelection(m_SpinnerSortOrdersAdapter.getPosition(strActiveSortOrder));
        }
        registerForContextMenu(m_SpinnerSortOrders);

        m_RecyclerView = findViewById(R.id.recyclerViewSortOrders);
        m_RecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager lLayoutManager = new LinearLayoutManager(this);
        m_RecyclerView.setLayoutManager(lLayoutManager);

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
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_sortorder_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_button_rename:
                onRenameSortOrder(m_SpinnerSortOrders);
                return true;
            case R.id.menu_button_delete:
            {
                onDeleteSortOrder(m_SpinnerSortOrders);
                return true;
            }
            default:
                return super.onContextItemSelected(item);
        }
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

    public void onAddCategory(@NonNull View v)
    {
        InputStringDialogFragment newFragment = InputStringDialogFragment.newInstance(getResources().getString(R.string.button_add_category));
        newFragment.setListExcludedInputs(m_GroceryPlanning.categories().getAllCategorieNames());
        newFragment.show(getSupportFragmentManager(), "addCategory");
    }

    public void onAddSortOrder(@NonNull View v)
    {
        InputStringDialogFragment newFragment = InputStringDialogFragment.newInstance(getResources().getString(R.string.text_add_sortorder));
        newFragment.setListExcludedInputs(m_GroceryPlanning.categories().getAllSortOrderNames());
        newFragment.show(getSupportFragmentManager(), "addSortOrder");
    }

    public void onStringInput(@NonNull String tag, @NonNull String strInput, @NonNull String strAdditonalInformation)
    {
        switch(tag) {
            case "addSortOrder":
            {
                m_GroceryPlanning.categories().addSortOrder(strInput);
                m_SpinnerSortOrdersAdapter.add(strInput);
                m_SpinnerSortOrders.setSelection(m_SpinnerSortOrdersAdapter.getCount() - 1);
                break;
            }

            case "renameSortOrder":
            {
                Optional<Categories.SortOrder> sortOrder = m_GroceryPlanning.categories().getSortOrder(strAdditonalInformation);
                if(!sortOrder.isPresent())
                {
                    return;
                }

                m_GroceryPlanning.categories().renameSortOrder(sortOrder.get(), strInput);
                m_GroceryPlanning.ingredients().onSortOrderRenamed(strAdditonalInformation, strInput);

                int index = m_SpinnerSortOrders.getSelectedItemPosition();
                m_SpinnerSortOrdersAdapter.remove(strAdditonalInformation);
                m_SpinnerSortOrdersAdapter.insert(strInput, index);
                m_SpinnerSortOrders.setSelection(index);

                Toast.makeText(this, getResources().getString(R.string.text_sortorder_renamed, strAdditonalInformation, strInput), Toast.LENGTH_SHORT).show();
                break;
            }

            case "addCategory":
            {
                if(m_RecyclerView.getAdapter() == null)
                {
                    return;
                }

                m_GroceryPlanning.categories().addCategory(strInput);
                CategoriesAdapter adapter = (CategoriesAdapter) m_RecyclerView.getAdapter();
                adapter.notifyItemInserted(adapter.getItemCount() - 1);
                break;
            }

            case "renameCategory": // See CategoriesAdapter
            {
                if(m_RecyclerView.getAdapter() == null)
                {
                    return;
                }

                Optional<Categories.Category> category = m_GroceryPlanning.categories().getCategory(strAdditonalInformation);
                if(!category.isPresent())
                {
                    return;
                }

                m_GroceryPlanning.categories().renameCategory(category.get(), strInput);
                m_GroceryPlanning.ingredients().onCategoryRenamed(strAdditonalInformation, strInput);

                m_RecyclerView.getAdapter().notifyDataSetChanged();
                Toast.makeText(this, getResources().getString(R.string.text_category_renamed, category.get().getName(), strInput), Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    public void onDeleteSortOrder(@NonNull View v)
    {
        if(m_SpinnerSortOrders.getSelectedItem() == null)
        {
            return;
        }

        String strName = (String)m_SpinnerSortOrders.getSelectedItem();
        Optional<Categories.SortOrder> sortOrderToDelete = m_GroceryPlanning.categories().getSortOrder(strName);
        if(!sortOrderToDelete.isPresent())
        {
            return;
        }

        ArrayList<String> ingredientsUsingSortOrder = new ArrayList<>();
        if(m_GroceryPlanning.ingredients().isSortOrderInUse(sortOrderToDelete.get(), ingredientsUsingSortOrder))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(m_RecyclerView.getContext());
            builder.setTitle(m_RecyclerView.getContext().getResources().getString(R.string.text_delete_sortorder_disallowed_header));
            builder.setMessage(m_RecyclerView.getContext().getResources().getString(R.string.text_delete_sortorder_disallowed_desc, strName, ingredientsUsingSortOrder.toString()));
            builder.setPositiveButton(android.R.string.ok, (DialogInterface dialog, int which) -> {});
            builder.show();
            return;
        }

        final UndoData.SortOrderUndoData recentlyDeletedSortOrder = new UndoData.SortOrderUndoData(sortOrderToDelete.get());
        m_GroceryPlanning.categories().removeSortOrder(sortOrderToDelete.get());
        m_SpinnerSortOrdersAdapter.remove(strName);
        if(m_SpinnerSortOrdersAdapter.getCount() == 0)
        {
            m_FAB.hide();
            m_Adapter = null;
            m_RecyclerView.setAdapter(null);
        }

        // Allow undo

        CoordinatorLayout coordLayout = findViewById(R.id.fabCoordinatorLayout);
        Snackbar snackbar = Snackbar.make(coordLayout, R.string.text_sortorder_deleted, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.text_undo, (View view) -> {
                m_GroceryPlanning.categories().addSortOrder(recentlyDeletedSortOrder.getName()).setOrder(recentlyDeletedSortOrder.getOrder());
                m_SpinnerSortOrdersAdapter.add(recentlyDeletedSortOrder.getName());
                m_SpinnerSortOrders.setSelection(m_SpinnerSortOrdersAdapter.getCount() - 1);

                Snackbar snackbar1 = Snackbar.make(coordLayout, R.string.text_sortorder_restored, Snackbar.LENGTH_SHORT);
                snackbar1.show();
        });
        snackbar.show();
    }

    public void onRenameSortOrder(@NonNull View v)
    {
        final String strCurrentSortOrder = (String)m_SpinnerSortOrders.getSelectedItem();

        InputStringDialogFragment newFragment = InputStringDialogFragment.newInstance(getResources().getString(R.string.text_rename_sortorder, strCurrentSortOrder));
        newFragment.setDefaultValue(strCurrentSortOrder);
        newFragment.setAdditionalInformation(strCurrentSortOrder);
        newFragment.setListExcludedInputs(m_GroceryPlanning.categories().getAllSortOrderNames());
        newFragment.show(getSupportFragmentManager(), "renameSortOrder");
    }

    public void onItemSelected(@NonNull AdapterView<?> parent, @NonNull View view, int pos, long id)
    {
        // Sort order selected -> fill corresponding RecyclerView

        String strSortOrder = (String)m_SpinnerSortOrders.getSelectedItem();
        Optional<Categories.SortOrder> order = m_GroceryPlanning.categories().getSortOrder(strSortOrder);
        if(!order.isPresent())
        {
            return;
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SettingsActivity.KEY_ACTIVE_SORTORDER_CATEGORIES, strSortOrder);
        editor.apply();

        m_FAB.show();

        CoordinatorLayout coordLayout = findViewById(R.id.fabCoordinatorLayout);
        m_Adapter = new CategoriesAdapter(coordLayout, m_RecyclerView, m_GroceryPlanning.categories(), order.get(), m_GroceryPlanning.ingredients());
        m_RecyclerView.setAdapter(m_Adapter);
        if(m_ItemTouchHelper != null)
        {
            m_ItemTouchHelper.attachToRecyclerView(null);
        }
        m_ItemTouchHelper = new ItemTouchHelper(new ReactToTouchActionsCallback((ReactToTouchActionsInterface)m_RecyclerView.getAdapter(),
                                                                                              this,
                                                                                              R.drawable.ic_delete_black_24dp,
                                                                                              true));
        ((CategoriesAdapter) m_Adapter).setTouchHelper(m_ItemTouchHelper);
        m_ItemTouchHelper.attachToRecyclerView(m_RecyclerView);
    }

    public void onNothingSelected(@NonNull AdapterView<?> parent)
    {
        m_RecyclerView.setAdapter(null);
    }
}
