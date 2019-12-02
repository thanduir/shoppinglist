package ch.phwidmer.einkaufsliste.UI;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.Optional;

import ch.phwidmer.einkaufsliste.data.GroceryPlanningFactory;
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

        m_GroceryPlanning = GroceryPlanningFactory.groceryPlanning(this);
        if(m_GroceryPlanning == null)
        {
            return;
        }

        m_SortedShoppingList = new SortedShoppingList(m_GroceryPlanning.shoppingList(), m_GroceryPlanning.ingredients());

        m_SpinnerSortOrders = findViewById(R.id.spinnerSortOrder);

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        for(Categories.SortOrder sortOrder : m_GroceryPlanning.categories().getAllSortOrders())
        {
            adapter.add(sortOrder.getName());
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_SpinnerSortOrders.setAdapter(adapter);
        m_SpinnerSortOrders.setOnItemSelectedListener(this);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String strCurrentSortOrder = preferences.getString(SettingsActivity.KEY_ACTIVE_SORTORDER_GOSHOPPING, "");
        if(strCurrentSortOrder == null)
        {
            strCurrentSortOrder = "";
        }
        Optional<Categories.SortOrder> sortOrder = m_GroceryPlanning.categories().getSortOrder(strCurrentSortOrder);
        if(sortOrder.isPresent() && m_GroceryPlanning.categories().getAllSortOrders().contains(sortOrder.get()))
        {
            m_SpinnerSortOrders.setSelection(adapter.getPosition(strCurrentSortOrder));
        }
        else
        {
            String strDefaultSortOrder = preferences.getString(SettingsActivity.KEY_DEFAULT_SORORDER, "");
            if(strDefaultSortOrder == null)
            {
                strDefaultSortOrder = "";
            }
            Optional<Categories.SortOrder> defaultSortOrder = m_GroceryPlanning.categories().getSortOrder(strDefaultSortOrder);
            if(defaultSortOrder.isPresent() && m_GroceryPlanning.categories().getAllSortOrders().contains(defaultSortOrder.get()))
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
        m_GroceryPlanning.flush();
        super.finish();
    }

    public void onItemSelected(@NonNull AdapterView<?> parent, @NonNull View view, int pos, long id)
    {
        String strSortOrder = (String)m_SpinnerSortOrders.getSelectedItem();
        Optional<Categories.SortOrder> order = m_GroceryPlanning.categories().getSortOrder(strSortOrder);
        if(!order.isPresent())
        {
            return;
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SettingsActivity.KEY_ACTIVE_SORTORDER_GOSHOPPING, strSortOrder);
        editor.apply();

        m_SortedShoppingList.setSortOrder(strSortOrder, order.get());
        RecyclerView.Adapter adapter = new GoShoppingAdapter(m_SortedShoppingList);
        m_RecyclerView.setAdapter(adapter);
        ItemClickSupport.addTo(m_RecyclerView).setOnItemClickListener(
            (RecyclerView recyclerView, int position, View v) ->
            {
                if(m_SortedShoppingList.isCategory(position))
                {
                    return;
                }

                Optional<SortedShoppingList.CategoryShoppingItem> item = m_SortedShoppingList.getListItem(position);
                if(!item.isPresent())
                {
                    return;
                }
                item.get().invertStatus();

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

    public void onNothingSelected(@NonNull AdapterView<?> parent)
    {
        m_RecyclerView.setAdapter(null);
    }
}
