package ch.phwidmer.einkaufsliste;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class ManageCategories extends AppCompatActivity implements AdapterView.OnItemSelectedListener, InputStringDialogFragment.InputStringResponder
{
    private GroceryPlanning m_GroceryPlanning;
    private String          m_SaveFilePath;

    private Spinner                    m_SpinnerSortOrders;
    private Button                     m_ButtonDelSortOrder;
    private RecyclerView               m_RecyclerView;
    private RecyclerView.Adapter       m_Adapter;
    private RecyclerView.LayoutManager m_LayoutManager;

    private FloatingActionButton       m_FAB;

    private String                      m_strRecentlyDeletedSortOrder;
    private Categories.SortOrder        m_RecentlyDeletedSortOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        Intent intent = getIntent();
        m_SaveFilePath = intent.getStringExtra(MainActivity.EXTRA_SAVEFILESPATH);
        File file = new File(new File(m_SaveFilePath), MainActivity.c_strSaveFilename);
        m_GroceryPlanning = new GroceryPlanning(file, this);

        m_FAB = findViewById(R.id.fab);

        // Manage SortOrders

        m_SpinnerSortOrders = findViewById(R.id.spinnerSortOrder);
        m_ButtonDelSortOrder = findViewById(R.id.buttonDelSortOrder);

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        for(String strName : m_GroceryPlanning.m_Categories.getAllSortOrders())
        {
            adapter.add(strName);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_SpinnerSortOrders.setAdapter(adapter);
        m_SpinnerSortOrders.setOnItemSelectedListener(this);
        String strActiveSortOrder = m_GroceryPlanning.m_Categories.getActiveSortOrder();
        if(!strActiveSortOrder.isEmpty() && m_GroceryPlanning.m_Categories.getAllSortOrders().contains(strActiveSortOrder))
        {
            m_SpinnerSortOrders.setSelection(adapter.getPosition(strActiveSortOrder));
        }

        m_RecyclerView = findViewById(R.id.recyclerViewSortOrders);
        m_RecyclerView.setHasFixedSize(true);
        m_LayoutManager = new LinearLayoutManager(this);
        m_RecyclerView.setLayoutManager(m_LayoutManager);

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

        m_ButtonDelSortOrder.setEnabled(adapter.getCount() > 0);
    }

    @Override
    protected void onPause()
    {
        m_GroceryPlanning.m_Ingredients.updateCategories(m_GroceryPlanning.m_Categories);

        File file = new File(new File(m_SaveFilePath), MainActivity.c_strSaveFilename);
        m_GroceryPlanning.saveDataToFile(file, null);

        super.onPause();
    }

    public void onAddCategory(View v)
    {
        DialogFragment newFragment = InputStringDialogFragment.newInstance(getResources().getString(R.string.button_add_category), "");
        newFragment.show(getSupportFragmentManager(), "addCategory");
    }

    public void onAddSortOrder(View v)
    {
        DialogFragment newFragment = InputStringDialogFragment.newInstance(getResources().getString(R.string.text_add_sortorder), "");
        newFragment.show(getSupportFragmentManager(), "addSortOrder");
    }

    public void onStringInput(String tag, String strInput, String strAdditonalInformation)
    {
        if(tag.equals("addCategory"))
        {
            m_GroceryPlanning.m_Categories.addCategory(strInput);
            CategoriesAdapter adapter = (CategoriesAdapter) m_RecyclerView.getAdapter();
            adapter.notifyItemInserted(adapter.getItemCount() - 1);
        }
        else if(tag.equals("addSortOrder"))
        {
            m_GroceryPlanning.m_Categories.addSortOrder(strInput);
            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>)m_SpinnerSortOrders.getAdapter();
            adapter.add(strInput);
            m_SpinnerSortOrders.setSelection(adapter.getCount() - 1);

            m_ButtonDelSortOrder.setEnabled(adapter.getCount() > 0);
        }
        else if(tag.equals("renameCategory")) // See CategoriesAdapter
        {
            Categories.Category category = m_GroceryPlanning.m_Categories.getCategory(strAdditonalInformation);

            m_GroceryPlanning.m_Categories.renameCategory(category, strInput);
            m_GroceryPlanning.m_Ingredients.onCategoryRenamed(category, m_GroceryPlanning.m_Categories.getCategory(strInput));

            m_RecyclerView.getAdapter().notifyDataSetChanged();
            Toast.makeText(this, getResources().getString(R.string.text_category_renamed, category.getName(), strInput), Toast.LENGTH_SHORT).show();
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
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>)m_SpinnerSortOrders.getAdapter();
        adapter.remove(strName);
        m_SpinnerSortOrders.setAdapter(adapter);
        m_ButtonDelSortOrder.setEnabled(adapter.getCount() > 0);
        if(adapter.getCount() == 0)
        {
            m_Adapter = null;
            m_RecyclerView.setAdapter(null);
        }

        // Allow undo

        Snackbar snackbar = Snackbar.make(m_SpinnerSortOrders, R.string.text_sortorder_deleted, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.text_undo, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_GroceryPlanning.m_Categories.addSortOrder(m_strRecentlyDeletedSortOrder, m_RecentlyDeletedSortOrder);
                ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>)m_SpinnerSortOrders.getAdapter();
                adapter.add(m_strRecentlyDeletedSortOrder);
                m_SpinnerSortOrders.setSelection(adapter.getCount() - 1);

                m_ButtonDelSortOrder.setEnabled(adapter.getCount() > 0);

                m_strRecentlyDeletedSortOrder = "";
                m_RecentlyDeletedSortOrder = null;

                Snackbar snackbar1 = Snackbar.make(m_SpinnerSortOrders, R.string.text_sortorder_restored, Snackbar.LENGTH_SHORT);
                snackbar1.show();
            }
        });
        snackbar.show();
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        // Sort order selected -> fill corresponding RecyclerView

        String strSortOrder = (String)m_SpinnerSortOrders.getSelectedItem();
        Categories.SortOrder order = m_GroceryPlanning.m_Categories.getSortOrder(strSortOrder);
        m_GroceryPlanning.m_Categories.setActiveSortOrder(strSortOrder);

        m_Adapter = new CategoriesAdapter(m_RecyclerView, m_GroceryPlanning.m_Categories, order, m_GroceryPlanning.m_Ingredients);
        m_RecyclerView.setAdapter(m_Adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ReactToTouchActionsCallback<CategoriesAdapter>(m_RecyclerView,
                                                                                                m_RecyclerView.getContext(),
                                                                                                R.drawable.ic_delete_black_24dp,
                                                                                                true));
        ((CategoriesAdapter) m_Adapter).setTouchHelper(itemTouchHelper);
        itemTouchHelper.attachToRecyclerView(m_RecyclerView);
    }

    public void onNothingSelected(AdapterView<?> parent)
    {
        m_RecyclerView.setAdapter(null);
    }
}
