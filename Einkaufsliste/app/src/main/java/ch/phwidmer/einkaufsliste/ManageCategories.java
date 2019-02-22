package ch.phwidmer.einkaufsliste;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class ManageCategories extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{
    private Categories m_Categories;

    private Spinner                    m_SpinnerSortOrders;
    private RecyclerView               m_RecyclerView;
    private RecyclerView.Adapter       m_Adapter;
    private RecyclerView.LayoutManager m_LayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        Intent intent = getIntent();
        m_Categories = (Categories)intent.getParcelableExtra(MainActivity.EXTRA_CATEGORIES);

        // Manage SortOrders

        m_SpinnerSortOrders = (Spinner) findViewById(R.id.spinnerSortOrder);

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        for(String strName : m_Categories.getAllSortOrders())
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
    }

    public void onAddCategory(View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add category");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Categories.addCategory(input.getText().toString());
                CategoriesAdapter adapter = (CategoriesAdapter)m_RecyclerView.getAdapter();
                adapter.notifyItemInserted(adapter.getItemCount()-1);
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

    public void onAddSortOrder(View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add sort order");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Categories.addSortOrder(input.getText().toString());
                ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>)m_SpinnerSortOrders.getAdapter();
                adapter.add(input.getText().toString());
                m_SpinnerSortOrders.setSelection(adapter.getCount() - 1);
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

    public void onDeleteSortOrder(View v)
    {
        String strName = (String)m_SpinnerSortOrders.getSelectedItem();
        m_Categories.removeSortOrder(strName);
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>)m_SpinnerSortOrders.getAdapter();
        adapter.remove((CharSequence)m_SpinnerSortOrders.getSelectedItem());
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        String strSortOrder = (String)m_SpinnerSortOrders.getSelectedItem();
        Categories.SortOrder order = m_Categories.getSortOrder(strSortOrder);

        m_Adapter = new CategoriesAdapter(m_RecyclerView, m_Categories, order);
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

    public void onConfirm(View v)
    {
        Intent data = new Intent();
        data.putExtra(MainActivity.EXTRA_CATEGORIES, m_Categories);
        setResult(RESULT_OK, data);
        finish();
    }

    public void onCancel(View v)
    {
        finish();
    }
}
