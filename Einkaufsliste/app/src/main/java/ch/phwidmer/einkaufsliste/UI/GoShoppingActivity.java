package ch.phwidmer.einkaufsliste.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import ch.phwidmer.einkaufsliste.helper.ItemClickSupport;
import ch.phwidmer.einkaufsliste.R;
import ch.phwidmer.einkaufsliste.data.SortedShoppingList;
import ch.phwidmer.einkaufsliste.data.Categories;
import ch.phwidmer.einkaufsliste.data.GroceryPlanning;

public class GoShoppingActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{
    private GroceryPlanning m_GroceryPlanning;

    private SortedShoppingList m_SortedShoppingList;

    private Spinner             m_SpinnerSortOrders;
    private RecyclerView        m_RecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_go_shopping);

        Intent intent = getIntent();
        m_GroceryPlanning = intent.getParcelableExtra(MainActivity.EXTRA_GROCERYPLANNING);

        m_SortedShoppingList = new SortedShoppingList(m_GroceryPlanning.m_ShoppingList, m_GroceryPlanning.m_Ingredients);

        m_SpinnerSortOrders = findViewById(R.id.spinnerSortOrder);

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        for(String strName : m_GroceryPlanning.m_Categories.getAllSortOrders())
        {
            adapter.add(strName);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_SpinnerSortOrders.setAdapter(adapter);
        m_SpinnerSortOrders.setOnItemSelectedListener(this);
        String strCurrentSortOrder = m_GroceryPlanning.m_ShoppingList.getCurrentSortOrder();
        if(!strCurrentSortOrder.isEmpty() && m_GroceryPlanning.m_Categories.getAllSortOrders().contains(strCurrentSortOrder))
        {
            m_SpinnerSortOrders.setSelection(adapter.getPosition(strCurrentSortOrder));
        }
        else
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            final String strDefaultSortOrder = preferences.getString(SettingsActivity.KEY_DEFAULT_SORORDER, "");
            if(strDefaultSortOrder != null && !strDefaultSortOrder.isEmpty() && m_GroceryPlanning.m_Categories.getAllSortOrders().contains(strDefaultSortOrder))
            {
                m_SpinnerSortOrders.setSelection(adapter.getPosition(strDefaultSortOrder));
            }
        }

        m_RecyclerView = findViewById(R.id.recyclerViewShoppingListItems);
        m_RecyclerView.setHasFixedSize(true);
        m_RecyclerView.setLayoutManager(new LinearLayoutManager(this));
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
        Intent data = new Intent();
        data.putExtra(MainActivity.EXTRA_GROCERYPLANNING, m_GroceryPlanning);
        setResult(RESULT_OK, data);

        super.finish();
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        String strSortOrder = (String)m_SpinnerSortOrders.getSelectedItem();
        Categories.SortOrder order = m_GroceryPlanning.m_Categories.getSortOrder(strSortOrder);
        m_GroceryPlanning.m_ShoppingList.setCurrentSortOrder(strSortOrder);

        m_SortedShoppingList.setSortOrder(strSortOrder, order);
        RecyclerView.Adapter adapter = new GoShoppingAdapter(m_SortedShoppingList);
        m_RecyclerView.setAdapter(adapter);
        ItemClickSupport.addTo(m_RecyclerView).setOnItemClickListener(
            (RecyclerView recyclerView, int position, View v) ->
            {
                if(m_SortedShoppingList.isCategory(position))
                {
                    return;
                }

                SortedShoppingList.CategoryShoppingItem item = m_SortedShoppingList.getListItem(position);
                item.invertStatus();

                GoShoppingAdapter goShoppingAdapter = (GoShoppingAdapter)recyclerView.getAdapter();
                if(goShoppingAdapter == null)
                {
                    return;
                }
                GoShoppingAdapter.ViewHolder vh = (GoShoppingAdapter.ViewHolder) recyclerView.getChildViewHolder(v);
                goShoppingAdapter.updateAppearance(vh, position);
            }
        );
    }

    public void onNothingSelected(AdapterView<?> parent)
    {
        m_RecyclerView.setAdapter(null);
    }
}
