package ch.phwidmer.einkaufsliste;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.File;

public class ManageCategories extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{
    private GroceryPlanning m_GroceryPlanning;
    private String          m_SaveFilePath;

    private Spinner                    m_SpinnerSortOrders;
    private Button                     m_ButtonDelSortOrder;
    private RecyclerView               m_RecyclerView;
    private RecyclerView.Adapter       m_Adapter;
    private RecyclerView.LayoutManager m_LayoutManager;

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

        // Manage SortOrders

        m_SpinnerSortOrders = (Spinner) findViewById(R.id.spinnerSortOrder);
        m_ButtonDelSortOrder = (Button) findViewById(R.id.buttonDelSortOrder);

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        for(String strName : m_GroceryPlanning.m_Categories.getAllSortOrders())
        {
            adapter.add(strName);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_SpinnerSortOrders.setAdapter(adapter);
        m_SpinnerSortOrders.setOnItemSelectedListener(this);

        m_RecyclerView = (RecyclerView) findViewById(R.id.recyclerViewSortOrders);
        m_RecyclerView.setHasFixedSize(true);
        m_LayoutManager = new LinearLayoutManager(this);
        m_RecyclerView.setLayoutManager(m_LayoutManager);

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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.button_add_category);

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        // Set up the buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_GroceryPlanning.m_Categories.addCategory(input.getText().toString());
                CategoriesAdapter adapter = (CategoriesAdapter)m_RecyclerView.getAdapter();
                adapter.notifyItemInserted(adapter.getItemCount()-1);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog d = builder.create();
        d.setView(input, 50, 0 ,50,0);
        d.show();
    }

    public void onAddSortOrder(View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.text_add_sortorder);

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_GroceryPlanning.m_Categories.addSortOrder(input.getText().toString());
                ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>)m_SpinnerSortOrders.getAdapter();
                adapter.add(input.getText().toString());
                m_SpinnerSortOrders.setSelection(adapter.getCount() - 1);

                m_ButtonDelSortOrder.setEnabled(adapter.getCount() > 0);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog d = builder.create();
        d.setView(input, 50, 0 ,50,0);
        d.show();
    }

    public void onDeleteSortOrder(View v)
    {
        if(m_SpinnerSortOrders.getSelectedItem() == null)
        {
            return;
        }

        String strName = (String)m_SpinnerSortOrders.getSelectedItem();
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
        String strSortOrder = (String)m_SpinnerSortOrders.getSelectedItem();
        Categories.SortOrder order = m_GroceryPlanning.m_Categories.getSortOrder(strSortOrder);

        m_Adapter = new CategoriesAdapter(m_RecyclerView, m_GroceryPlanning.m_Categories, order, m_GroceryPlanning.m_Ingredients);
        m_RecyclerView.setAdapter(m_Adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ReactToTouchActionsCallback((CategoriesAdapter)m_Adapter,
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
