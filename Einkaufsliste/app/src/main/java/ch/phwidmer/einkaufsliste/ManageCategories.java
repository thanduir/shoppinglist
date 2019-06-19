package ch.phwidmer.einkaufsliste;

import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class ManageCategories extends AppCompatActivity implements AdapterView.OnItemSelectedListener, InputStringDialogFragment.InputStringResponder
{
    private GroceryPlanning             m_GroceryPlanning;

    private Spinner                     m_SpinnerSortOrders;
    private ImageView                   m_ImageViewDelSortOrder;
    private RecyclerView                m_RecyclerView;
    private RecyclerView.Adapter        m_Adapter;

    private ArrayAdapter<CharSequence>  m_SpinnerSortOrdersAdapter;

    private FloatingActionButton        m_FAB;

    private ItemTouchHelper             m_ItemTouchHelper;

    private String                      m_strRecentlyDeletedSortOrder;
    private Categories.SortOrder        m_RecentlyDeletedSortOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        Intent intent = getIntent();
        m_GroceryPlanning = intent.getParcelableExtra(MainActivity.EXTRA_GROCERYPLANNING);

        m_FAB = findViewById(R.id.fab);

        // Manage SortOrders

        m_SpinnerSortOrders = findViewById(R.id.spinnerSortOrder);
        m_ImageViewDelSortOrder = findViewById(R.id.imageViewDelSortOrder);

        m_SpinnerSortOrdersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        for(String strName : m_GroceryPlanning.m_Categories.getAllSortOrders())
        {
            m_SpinnerSortOrdersAdapter.add(strName);
        }
        m_SpinnerSortOrdersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_SpinnerSortOrders.setAdapter(m_SpinnerSortOrdersAdapter);
        m_SpinnerSortOrders.setOnItemSelectedListener(this);
        String strActiveSortOrder = m_GroceryPlanning.m_Categories.getActiveSortOrder();
        if(!strActiveSortOrder.isEmpty() && m_GroceryPlanning.m_Categories.getAllSortOrders().contains(strActiveSortOrder))
        {
            m_SpinnerSortOrders.setSelection(m_SpinnerSortOrdersAdapter.getPosition(strActiveSortOrder));
        }

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

        m_ImageViewDelSortOrder.setEnabled(m_SpinnerSortOrdersAdapter.getCount() > 0);
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
        m_GroceryPlanning.m_Ingredients.updateCategories(m_GroceryPlanning.m_Categories);

        Intent data = new Intent();
        data.putExtra(MainActivity.EXTRA_GROCERYPLANNING, m_GroceryPlanning);
        setResult(RESULT_OK, data);

        super.finish();
    }

    public void onAddCategory(View v)
    {
        InputStringDialogFragment newFragment = InputStringDialogFragment.newInstance(getResources().getString(R.string.button_add_category));
        newFragment.setListExcludedInputs(m_GroceryPlanning.m_Categories.getAllCategories());
        newFragment.show(getSupportFragmentManager(), "addCategory");
    }

    public void onAddSortOrder(View v)
    {
        InputStringDialogFragment newFragment = InputStringDialogFragment.newInstance(getResources().getString(R.string.text_add_sortorder));
        newFragment.setListExcludedInputs(m_GroceryPlanning.m_Categories.getAllSortOrders());
        newFragment.show(getSupportFragmentManager(), "addSortOrder");
    }

    public void onStringInput(String tag, String strInput, String strAdditonalInformation)
    {
        if(m_RecyclerView.getAdapter() == null)
        {
            return;
        }

        switch(tag) {
            case "addCategory":
            {
                m_GroceryPlanning.m_Categories.addCategory(strInput);
                CategoriesAdapter adapter = (CategoriesAdapter) m_RecyclerView.getAdapter();
                adapter.notifyItemInserted(adapter.getItemCount() - 1);
                break;
            }

            case "addSortOrder":
            {
                m_GroceryPlanning.m_Categories.addSortOrder(strInput);
                m_SpinnerSortOrdersAdapter.add(strInput);
                m_SpinnerSortOrders.setSelection(m_SpinnerSortOrdersAdapter.getCount() - 1);

                m_ImageViewDelSortOrder.setEnabled(m_SpinnerSortOrdersAdapter.getCount() > 0);
                break;
            }

            case "renameCategory": // See CategoriesAdapter
            {
                Categories.Category category = m_GroceryPlanning.m_Categories.getCategory(strAdditonalInformation);

                m_GroceryPlanning.m_Categories.renameCategory(category, strInput);
                m_GroceryPlanning.m_Ingredients.onCategoryRenamed(category, m_GroceryPlanning.m_Categories.getCategory(strInput));

                m_RecyclerView.getAdapter().notifyDataSetChanged();
                Toast.makeText(this, getResources().getString(R.string.text_category_renamed, category.getName(), strInput), Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    public void onDeleteSortOrder(View v)
    {
        if(m_SpinnerSortOrders.getSelectedItem() == null)
        {
            return;
        }

        String strName = (String)m_SpinnerSortOrders.getSelectedItem();

        ArrayList<String> ingredientsUsingSortOrder = new ArrayList<>();
        if(m_GroceryPlanning.m_Ingredients.isSortOrderInUse(strName, ingredientsUsingSortOrder))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(m_RecyclerView.getContext());
            builder.setTitle(m_RecyclerView.getContext().getResources().getString(R.string.text_delete_sortorder_disallowed_header));
            builder.setMessage(m_RecyclerView.getContext().getResources().getString(R.string.text_delete_sortorder_disallowed_desc, strName, ingredientsUsingSortOrder.toString()));
            builder.setPositiveButton(android.R.string.ok, (DialogInterface dialog, int which) -> {});
            builder.show();
            return;
        }

        m_strRecentlyDeletedSortOrder = strName;
        m_RecentlyDeletedSortOrder = m_GroceryPlanning.m_Categories.getSortOrder(strName);
        m_GroceryPlanning.m_Categories.removeSortOrder(strName);
        m_SpinnerSortOrdersAdapter.remove(strName);
        m_ImageViewDelSortOrder.setEnabled(m_SpinnerSortOrdersAdapter.getCount() > 0);
        if(m_SpinnerSortOrdersAdapter.getCount() == 0)
        {
            m_Adapter = null;
            m_RecyclerView.setAdapter(null);
        }

        // Allow undo

        CoordinatorLayout coordLayout = findViewById(R.id.fabCoordinatorLayout);
        Snackbar snackbar = Snackbar.make(coordLayout, R.string.text_sortorder_deleted, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.text_undo, (View view) -> {
                m_GroceryPlanning.m_Categories.addSortOrder(m_strRecentlyDeletedSortOrder, m_RecentlyDeletedSortOrder);
                m_SpinnerSortOrdersAdapter.add(m_strRecentlyDeletedSortOrder);
                m_SpinnerSortOrders.setSelection(m_SpinnerSortOrdersAdapter.getCount() - 1);

                m_ImageViewDelSortOrder.setEnabled(m_SpinnerSortOrdersAdapter.getCount() > 0);

                m_strRecentlyDeletedSortOrder = "";
                m_RecentlyDeletedSortOrder = null;

                Snackbar snackbar1 = Snackbar.make(coordLayout, R.string.text_sortorder_restored, Snackbar.LENGTH_SHORT);
                snackbar1.show();
        });
        snackbar.show();
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        // Sort order selected -> fill corresponding RecyclerView

        String strSortOrder = (String)m_SpinnerSortOrders.getSelectedItem();
        Categories.SortOrder order = m_GroceryPlanning.m_Categories.getSortOrder(strSortOrder);
        m_GroceryPlanning.m_Categories.setActiveSortOrder(strSortOrder);

        CoordinatorLayout coordLayout = findViewById(R.id.fabCoordinatorLayout);
        m_Adapter = new CategoriesAdapter(coordLayout, m_RecyclerView, m_GroceryPlanning.m_Categories, order, m_GroceryPlanning.m_Ingredients);
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

    public void onNothingSelected(AdapterView<?> parent)
    {
        m_RecyclerView.setAdapter(null);
    }
}
