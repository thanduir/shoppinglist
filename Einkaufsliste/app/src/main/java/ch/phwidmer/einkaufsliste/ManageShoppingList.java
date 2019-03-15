package ch.phwidmer.einkaufsliste;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;

public class ManageShoppingList extends AppCompatActivity
{
    private GroceryPlanning m_GroceryPlanning;
    private String          m_SaveFilePath;

    private ShoppingList    m_RecentlyDeletedShoppingList;

    private RecyclerView                m_RecyclerViewRecipes;
    private RecyclerView.Adapter        m_AdapterRecipes;
    private RecyclerView.LayoutManager  m_LayoutManagerRecipes;

    private FloatingActionButton m_FAB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_shopping_list);

        Intent intent = getIntent();
        m_SaveFilePath = intent.getStringExtra(MainActivity.EXTRA_SAVEFILESPATH);
        File file = new File(m_SaveFilePath, MainActivity.c_strSaveFilename);
        m_GroceryPlanning = new GroceryPlanning(file, this);

        m_FAB = (FloatingActionButton)findViewById(R.id.fab);

        m_RecyclerViewRecipes = (RecyclerView) findViewById(R.id.recyclerViewShoppingRecipe);
        m_RecyclerViewRecipes.setHasFixedSize(true);
        m_LayoutManagerRecipes = new LinearLayoutManager(this);
        m_RecyclerViewRecipes.setLayoutManager(m_LayoutManagerRecipes);
        m_AdapterRecipes = new ShoppingRecipesAdapter(m_RecyclerViewRecipes, m_GroceryPlanning.m_Ingredients, m_GroceryPlanning.m_ShoppingList);
        m_RecyclerViewRecipes.setAdapter(m_AdapterRecipes);
        ItemClickSupport.addTo(m_RecyclerViewRecipes).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v)
                    {
                        ShoppingRecipesAdapter adapter = (ShoppingRecipesAdapter) recyclerView.getAdapter();
                        ShoppingRecipesAdapter.ViewHolder vh = (ShoppingRecipesAdapter.ViewHolder) recyclerView.getChildViewHolder(v);

                        if(vh.getID().equals(adapter.getActiveElement()))
                        {
                            adapter.setActiveElement(null);
                        }
                        else
                        {
                            adapter.setActiveElement(vh.getID());
                        }
                    }
                }
        );
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ReactToTouchActionsCallback<ShoppingRecipesAdapter>(m_RecyclerViewRecipes,
                                                                            m_RecyclerViewRecipes.getContext(),
                                                                            R.drawable.ic_delete_black_24dp,
                                                                             false));
        itemTouchHelper.attachToRecyclerView(m_RecyclerViewRecipes);

        m_RecyclerViewRecipes.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
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
    protected void onPause()
    {
        File file = new File(new File(m_SaveFilePath), MainActivity.c_strSaveFilename);
        m_GroceryPlanning.saveDataToFile(file, null);

        super.onPause();
    }

    public void onAddShoppingRecipe(View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.text_import_recipe);

        // Set up the input
        final Spinner input = new Spinner(this);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        for(String strName : m_GroceryPlanning.m_Recipes.getAllRecipes())
        {
            ShoppingRecipesAdapter adapterItems = (ShoppingRecipesAdapter)m_RecyclerViewRecipes.getAdapter();
            if(adapterItems.containsItem(new Pair<String, String>(strName, "")))
            {
                continue;
            }
            adapter.add(strName);
        }
        if(adapter.isEmpty())
        {
            Toast.makeText(v.getContext(), R.string.text_all_recipes_alreay_added, Toast.LENGTH_SHORT).show();
            return;
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        input.setAdapter(adapter);

        // Set up the buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strRecipe = input.getSelectedItem().toString();

                m_GroceryPlanning.m_ShoppingList.addFromRecipe(strRecipe, m_GroceryPlanning.m_Recipes.getRecipe(strRecipe));

                ShoppingRecipesAdapter adapter = (ShoppingRecipesAdapter)m_RecyclerViewRecipes.getAdapter();
                adapter.notifyDataSetChanged();
                adapter.setActiveElement(new Pair<String, String>(strRecipe, ""));
                m_RecyclerViewRecipes.scrollToPosition(adapter.getItemCount()-1);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog d = builder.create();
        d.setView(input, 50, 20 ,20,0);
        d.show();
    }

    public void onResetList(View v)
    {
        m_RecentlyDeletedShoppingList = m_GroceryPlanning.m_ShoppingList;

        m_GroceryPlanning.m_ShoppingList = new ShoppingList();
        m_AdapterRecipes = new ShoppingRecipesAdapter(m_RecyclerViewRecipes, m_GroceryPlanning.m_Ingredients, m_GroceryPlanning.m_ShoppingList);
        m_RecyclerViewRecipes.setAdapter(m_AdapterRecipes);

        // Allow undo

        Snackbar snackbar = Snackbar.make(m_RecyclerViewRecipes, R.string.text_shoppnglist_reset, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.text_undo, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_GroceryPlanning.m_ShoppingList = m_RecentlyDeletedShoppingList;
                m_AdapterRecipes = new ShoppingRecipesAdapter(m_RecyclerViewRecipes, m_GroceryPlanning.m_Ingredients, m_GroceryPlanning.m_ShoppingList);
                m_RecyclerViewRecipes.setAdapter(m_AdapterRecipes);

                m_RecentlyDeletedShoppingList = null;

                Snackbar snackbar1 = Snackbar.make(m_RecyclerViewRecipes, R.string.text_shoppnglist_restored, Snackbar.LENGTH_SHORT);
                snackbar1.show();
            }
        });
        snackbar.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reset_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.actionbar_button_reset)
        {
            onResetList(null);
        }
        return super.onOptionsItemSelected(item);
    }
}
