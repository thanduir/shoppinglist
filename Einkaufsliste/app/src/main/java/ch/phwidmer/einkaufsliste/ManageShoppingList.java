package ch.phwidmer.einkaufsliste;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class ManageShoppingList extends AppCompatActivity implements InputStringDialogFragment.InputStringResponder
{
    private GroceryPlanning m_GroceryPlanning;
    private String          m_SaveFilePath;

    private ShoppingList    m_RecentlyDeletedShoppingList;

    private RecyclerView                m_RecyclerViewRecipes;
    private ShoppingRecipesAdapter      m_AdapterRecipes;
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

        m_FAB = findViewById(R.id.fab);

        m_RecyclerViewRecipes = findViewById(R.id.recyclerViewShoppingRecipe);
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

        if(savedInstanceState != null)
        {
            String strActiveElementFirst = savedInstanceState.getString("AdapterActiveElementFirst");
            String strActiveElementSecond = savedInstanceState.getString("AdapterActiveElementSecond");
            if(strActiveElementFirst != null)
            {
                m_AdapterRecipes.setActiveElement(new Pair<>(strActiveElementFirst, strActiveElementSecond));
            }
        }

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

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState)
    {
        if(m_AdapterRecipes != null && m_AdapterRecipes.getActiveElement() != null)
        {
            savedInstanceState.putString("AdapterActiveElementFirst", m_AdapterRecipes.getActiveElement().first);
            savedInstanceState.putString("AdapterActiveElementSecond", m_AdapterRecipes.getActiveElement().second);
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    public void onAddShoppingRecipe(View v)
    {
        ArrayList<String> inputList = new ArrayList<String>();
        for(String strName : m_GroceryPlanning.m_Recipes.getAllRecipes())
        {
            ShoppingRecipesAdapter adapterItems = (ShoppingRecipesAdapter)m_RecyclerViewRecipes.getAdapter();
            if(adapterItems.containsItem(new Pair<>(strName, "")))
            {
                continue;
            }
            inputList.add(strName);
        }
        if(inputList.isEmpty())
        {
            Toast.makeText(v.getContext(), R.string.text_all_recipes_alreay_added, Toast.LENGTH_SHORT).show();
            return;
        }

        DialogFragment newFragment = InputStringDialogFragment.newInstance(getResources().getString(R.string.text_import_recipe), "", inputList);
        newFragment.show(getSupportFragmentManager(), "addShoppingRecipe");
    }

    public void onStringInput(String tag, String strInput, String strAdditonalInformation)
    {
        if(tag.equals("addRecipeItem")) // See ShoppingRecipesAdapter
        {
            String strIngredient = strInput;
            String strRecipe = strAdditonalInformation;

            ShoppingListItem item = new ShoppingListItem();
            item.m_Ingredient = strIngredient;
            item.m_Amount.m_Unit = m_GroceryPlanning.m_Ingredients.getIngredient(strIngredient).m_DefaultUnit;
            m_GroceryPlanning.m_ShoppingList.getShoppingRecipe(strRecipe).m_Items.add(item);

            Pair<String, String> newItem = new Pair<String, String>(strRecipe, strIngredient);

            ShoppingRecipesAdapter adapter = (ShoppingRecipesAdapter)m_RecyclerViewRecipes.getAdapter();
            adapter.notifyDataSetChanged();
            adapter.setActiveElement(newItem);
        }
        else if(tag.equals("changeRecipeScaling")) // See ShoppingRecipesAdapter
        {
            float fNewValue = Float.valueOf(strInput);

            ShoppingList.ShoppingRecipe recipe = m_GroceryPlanning.m_ShoppingList.getShoppingRecipe(strAdditonalInformation);
            recipe.changeScalingFactor(fNewValue);
            m_RecyclerViewRecipes.getAdapter().notifyDataSetChanged();
        }
        else if(tag.equals("addShoppingRecipe"))
        {
            m_GroceryPlanning.m_ShoppingList.addFromRecipe(strInput, m_GroceryPlanning.m_Recipes.getRecipe(strInput));

            ShoppingRecipesAdapter adapter = (ShoppingRecipesAdapter)m_RecyclerViewRecipes.getAdapter();
            adapter.notifyDataSetChanged();
            adapter.setActiveElement(new Pair<>(strInput, ""));
            m_RecyclerViewRecipes.scrollToPosition(adapter.getItemCount()-1);
        }
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
