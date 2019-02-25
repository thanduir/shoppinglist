package ch.phwidmer.einkaufsliste;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.File;

public class GoShoppingActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private GroceryPlanning m_GroceryPlanning;
    private String          m_SaveFilePath;

    private SortedShoppingList m_SortedShoppingList;

    private Spinner                     m_SpinnerSortOrders;
    private RecyclerView                m_RecyclerView;
    private RecyclerView.Adapter        m_Adapter;
    private RecyclerView.LayoutManager  m_LayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_shopping);

        Intent intent = getIntent();
        m_SaveFilePath = intent.getStringExtra(MainActivity.EXTRA_SAVEFILESPATH);
        File file = new File(new File(m_SaveFilePath), MainActivity.c_strSaveFilename);
        m_GroceryPlanning = new GroceryPlanning(file, this);

        m_SortedShoppingList = new SortedShoppingList(m_GroceryPlanning.m_ShoppingList, m_GroceryPlanning.m_Ingredients);

        m_SpinnerSortOrders = (Spinner) findViewById(R.id.spinnerSortOrder);

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        for(String strName : m_GroceryPlanning.m_Categories.getAllSortOrders())
        {
            adapter.add(strName);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_SpinnerSortOrders.setAdapter(adapter);
        m_SpinnerSortOrders.setOnItemSelectedListener(this);
        String strCurrentSortOrder = m_GroceryPlanning.m_ShoppingList.getCurrentSortOrder();
        if(!strCurrentSortOrder.isEmpty())
        {
            m_SpinnerSortOrders.setSelection(adapter.getPosition(strCurrentSortOrder));
        }

        m_RecyclerView = (RecyclerView) findViewById(R.id.recyclerViewShoppingListItems);
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

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        String strSortOrder = (String)m_SpinnerSortOrders.getSelectedItem();
        Categories.SortOrder order = m_GroceryPlanning.m_Categories.getSortOrder(strSortOrder);
        m_GroceryPlanning.m_ShoppingList.setCurrentSortOrder(strSortOrder);

        m_SortedShoppingList.setSortOrder(order);
        m_Adapter = new GoShoppingAdapter(m_RecyclerView, m_SortedShoppingList);
        m_RecyclerView.setAdapter(m_Adapter);
        ItemClickSupport.addTo(m_RecyclerView).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v)
                    {
                        if(m_SortedShoppingList.isCategory(position))
                        {
                            return;
                        }

                        SortedShoppingList.CategoryShoppingItem item = m_SortedShoppingList.getListItem(position);
                        item.invertStatus();

                        GoShoppingAdapter adapter = (GoShoppingAdapter)recyclerView.getAdapter();
                        GoShoppingAdapter.ViewHolder vh = (GoShoppingAdapter.ViewHolder) recyclerView.getChildViewHolder(v);
                        adapter.updateAppearance(vh, position);
                    }
                }
        );
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ReactToTouchActionsCallback((GoShoppingAdapter)m_Adapter,
                                                                                                m_RecyclerView.getContext(),
                                                                                                R.drawable.ic_check_black_24dp,
                                                                                                false));
        itemTouchHelper.attachToRecyclerView(m_RecyclerView);
    }

    public void onNothingSelected(AdapterView<?> parent)
    {
        m_RecyclerView.setAdapter(null);
    }
}
