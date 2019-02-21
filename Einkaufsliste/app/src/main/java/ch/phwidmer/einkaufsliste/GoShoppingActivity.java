package ch.phwidmer.einkaufsliste;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class GoShoppingActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private ShoppingList    m_ShoppingList;
    private Categories      m_Categories;
    private Ingredients     m_Ingredients;

    private SortedShoppingList m_SortedShoppingList;

    private Spinner                     m_SpinnerSortOrders;
    private RecyclerView                m_RecyclerView;
    private RecyclerView.Adapter        m_Adapter;
    private RecyclerView.LayoutManager  m_LayoutManager;

    // TODO: Nicht nur die Namen anzeigen für die Items, sondern alles relevante (Menge etc.)!

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_shopping);

        Intent intent = getIntent();
        m_ShoppingList = (ShoppingList)intent.getParcelableExtra(MainActivity.EXTRA_SHOPPINGLIST);
        m_Categories = (Categories)intent.getParcelableExtra(MainActivity.EXTRA_CATEGORIES);
        m_Ingredients = (Ingredients)intent.getParcelableExtra(MainActivity.EXTRA_INGREDIENTS);

        m_SortedShoppingList = new SortedShoppingList(m_ShoppingList, m_Ingredients);

        m_SpinnerSortOrders = (Spinner) findViewById(R.id.spinnerSortOrder);

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        for(String strName : m_Categories.getAllSortOrders())
        {
            adapter.add(strName);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_SpinnerSortOrders.setAdapter(adapter);
        m_SpinnerSortOrders.setOnItemSelectedListener(this);
        String strCurrentSortOrder = m_ShoppingList.getCurrentSortOrder();
        if(!strCurrentSortOrder.isEmpty())
        {
            m_SpinnerSortOrders.setSelection(adapter.getPosition(strCurrentSortOrder));
        }

        m_RecyclerView = (RecyclerView) findViewById(R.id.recyclerViewShoppingListItems);
        m_RecyclerView.setHasFixedSize(true);
        m_LayoutManager = new LinearLayoutManager(this);
        m_RecyclerView.setLayoutManager(m_LayoutManager);
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        String strSortOrder = (String)m_SpinnerSortOrders.getSelectedItem();
        Categories.SortOrder order = m_Categories.getSortOrder(strSortOrder);
        m_ShoppingList.setCurrentSortOrder(strSortOrder);

        // TODO: Also allow swipe to setChecked?
        m_SortedShoppingList.setSortOrder(order);
        m_Adapter = new GoShoppingAdapter(m_SortedShoppingList);
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
    }

    public void onNothingSelected(AdapterView<?> parent)
    {
        m_RecyclerView.setAdapter(null);
    }

    public void onConfirm(View v)
    {
        Intent data = new Intent();
        data.putExtra(MainActivity.EXTRA_SHOPPINGLIST, m_ShoppingList);
        setResult(RESULT_OK, data);
        finish();
    }
}
