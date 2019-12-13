package ch.phwidmer.einkaufsliste.UI;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;

import ch.phwidmer.einkaufsliste.data.Amount;
import ch.phwidmer.einkaufsliste.data.GroceryPlanningFactory;
import ch.phwidmer.einkaufsliste.data.Ingredients;
import ch.phwidmer.einkaufsliste.helper.ItemClickSupport;
import ch.phwidmer.einkaufsliste.R;
import ch.phwidmer.einkaufsliste.data.GroceryPlanning;
import ch.phwidmer.einkaufsliste.data.RecipeItem;
import ch.phwidmer.einkaufsliste.data.Recipes;
import ch.phwidmer.einkaufsliste.data.ShoppingList;
import ch.phwidmer.einkaufsliste.data.ShoppingListItem;
import ch.phwidmer.einkaufsliste.helper.ReactToTouchActionsCallback;
import ch.phwidmer.einkaufsliste.helper.ReactToTouchActionsInterface;
import ch.phwidmer.einkaufsliste.helper.stringInput.InputStringFromList;
import ch.phwidmer.einkaufsliste.helper.stringInput.InputStringResponder;

public class ShoppingListActivity extends AppCompatActivity implements InputStringResponder
{
    private GroceryPlanning m_GroceryPlanning;

    private RecyclerView            m_RecyclerViewRecipes;
    private ShoppingRecipesAdapter  m_AdapterRecipes;

    private FloatingActionButton    m_FAB;

    private ItemTouchHelper         m_ItemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_shopping_list);

        m_GroceryPlanning = GroceryPlanningFactory.groceryPlanning(this);

        m_FAB = findViewById(R.id.fab);
        CoordinatorLayout coordLayout = findViewById(R.id.fabCoordinatorLayout);

        m_RecyclerViewRecipes = findViewById(R.id.recyclerViewShoppingRecipe);
        m_RecyclerViewRecipes.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManagerRecipes = new LinearLayoutManager(this);
        m_RecyclerViewRecipes.setLayoutManager(layoutManagerRecipes);
        m_AdapterRecipes = new ShoppingRecipesAdapter(coordLayout, m_RecyclerViewRecipes, m_GroceryPlanning.ingredients(), m_GroceryPlanning.shoppingList());
        m_RecyclerViewRecipes.setAdapter(m_AdapterRecipes);
        ItemClickSupport.addTo(m_RecyclerViewRecipes).setOnItemClickListener(
                (RecyclerView recyclerView, int position, View v) ->
                {
                    ShoppingRecipesAdapter adapter = (ShoppingRecipesAdapter) recyclerView.getAdapter();
                    ShoppingRecipesAdapter.ViewHolder vh = (ShoppingRecipesAdapter.ViewHolder) recyclerView.getChildViewHolder(v);
                    if(adapter == null)
                    {
                        return;
                    }

                    Optional<Pair<String, String>> activeElement = adapter.getActiveElement();
                    if(activeElement.isPresent() && vh.getID().equals(activeElement.get()))
                    {
                        adapter.setActiveElement(null);
                    }
                    else
                    {
                        adapter.setActiveElement(vh.getID());
                    }
                }
        );
        initTouchHelper();

        if(savedInstanceState != null)
        {
            String strActiveElementFirst = savedInstanceState.getString("AdapterActiveElementFirst");
            String strActiveElementSecond = savedInstanceState.getString("AdapterActiveElementSecond");
            if(strActiveElementFirst != null)
            {
                m_AdapterRecipes.setActiveElement(new Pair<>(strActiveElementFirst, strActiveElementSecond));
            }
        }

        if(m_GroceryPlanning.recipes().getAllRecipeNames().size() == 0)
        {
            m_FAB.hide();
        }

        m_RecyclerViewRecipes.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && m_FAB.getVisibility() == View.VISIBLE) {
                    m_FAB.hide();
                } else if (dy <= 0 && m_FAB.getVisibility() != View.VISIBLE) {
                    m_FAB.show();
                }
            }
        });
    }

    private void initTouchHelper()
    {
        if(m_RecyclerViewRecipes == null)
        {
            return;
        }

        if(m_ItemTouchHelper != null)
        {
            m_ItemTouchHelper.attachToRecyclerView(null);
        }
        m_ItemTouchHelper = new ItemTouchHelper(new ReactToTouchActionsCallback((ReactToTouchActionsInterface)m_RecyclerViewRecipes.getAdapter(),
                this,
                R.drawable.ic_delete_black_24dp,
                false));
        m_ItemTouchHelper.attachToRecyclerView(m_RecyclerViewRecipes);
    }

    @Override
    public void finish()
    {
        m_GroceryPlanning.flush();
        super.finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState)
    {
        Optional<Pair<String, String>> activeElement = m_AdapterRecipes.getActiveElement();
        if(m_AdapterRecipes != null && activeElement.isPresent())
        {
            savedInstanceState.putString("AdapterActiveElementFirst", activeElement.get().first);
            savedInstanceState.putString("AdapterActiveElementSecond", activeElement.get().second);
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    public void onAddShoppingRecipe(@NonNull View v)
    {
        ArrayList<String> inputList = new ArrayList<>();
        for(Recipes.Recipe recipe : m_GroceryPlanning.recipes().getAllRecipes())
        {
            ShoppingRecipesAdapter adapterItems = (ShoppingRecipesAdapter)m_RecyclerViewRecipes.getAdapter();
            if(adapterItems == null)
            {
                continue;
            }
            inputList.add(recipe.getName());
        }
        if(inputList.isEmpty())
        {
            Toast.makeText(v.getContext(), R.string.text_all_recipes_alreay_added, Toast.LENGTH_SHORT).show();
            return;
        }

        InputStringFromList newFragment = InputStringFromList.newInstance(getResources().getString(R.string.text_import_recipe), inputList, "");
        newFragment.show(getSupportFragmentManager(), "addShoppingRecipe");
    }

    public void onIncreaseAmount(@NonNull View v)
    {
        ShoppingRecipesAdapter adapter = (ShoppingRecipesAdapter)m_RecyclerViewRecipes.getAdapter();
        if(adapter == null)
        {
            return;
        }
        adapter.onChangeAmount(false,true);
    }

    public void onDecreaseAmount(@NonNull View v)
    {
        ShoppingRecipesAdapter adapter = (ShoppingRecipesAdapter)m_RecyclerViewRecipes.getAdapter();
        if(adapter == null)
        {
            return;
        }
        adapter.onChangeAmount(false,false);
    }

    public void onIncreaseAmountMax(@NonNull View v)
    {
        ShoppingRecipesAdapter adapter = (ShoppingRecipesAdapter)m_RecyclerViewRecipes.getAdapter();
        if(adapter == null)
        {
            return;
        }
        adapter.onChangeAmount(true,true);
    }

    public void onDecreaseAmountMax(@NonNull View v)
    {
        ShoppingRecipesAdapter adapter = (ShoppingRecipesAdapter)m_RecyclerViewRecipes.getAdapter();
        if(adapter == null)
        {
            return;
        }
        adapter.onChangeAmount(true,false);
    }

    @Override
    public void onStringInput(@NonNull String tag, @NonNull String strInput, @NonNull String strAdditonalInformation)
    {
        ShoppingRecipesAdapter adapter = (ShoppingRecipesAdapter)m_RecyclerViewRecipes.getAdapter();
        if(adapter == null)
        {
            return;
        }

        switch(tag)
        {
            case "addRecipeItem": // See ShoppingRecipesAdapter
            {
                Optional<ShoppingList.ShoppingRecipe> recipe = m_GroceryPlanning.shoppingList().getShoppingRecipe(strAdditonalInformation);
                if(!recipe.isPresent())
                {
                    return;
                }
                Optional<ShoppingListItem> item = recipe.get().addItem(strInput);
                Optional<Ingredients.Ingredient> ingredient = m_GroceryPlanning.ingredients().getIngredient(strInput);
                if(!item.isPresent() || !ingredient.isPresent())
                {
                    return;
                }
                Amount amount = item.get().getAmount();
                amount.setUnit(ingredient.get().getDefaultUnit());
                item.get().setAmount(amount);

                Pair<String, String> newItem = new Pair<>(strAdditonalInformation, strInput);
                adapter.notifyDataSetChanged();
                adapter.setActiveElement(newItem);
                break;
            }

            case "changeRecipeScaling": // See ShoppingRecipesAdapter
            {
                float fNewValue = Float.valueOf(strInput);

                Optional<ShoppingList.ShoppingRecipe> recipe = m_GroceryPlanning.shoppingList().getShoppingRecipe(strAdditonalInformation);
                if(!recipe.isPresent())
                {
                    return;
                }
                recipe.get().changeScalingFactor(fNewValue);
                adapter.notifyDataSetChanged();
                break;
            }

            case "addShoppingRecipe":
            {
                // AdditionalInformation: First the recipe name, then the chosen elements from the different groups afterwards (if any).
                if(!strAdditonalInformation.isEmpty())
                {
                    strAdditonalInformation += ";";
                }
                strAdditonalInformation += strInput;
                String[] infosSet = strAdditonalInformation.split(";");
                Optional<Recipes.Recipe> recipe = m_GroceryPlanning.recipes().getRecipe(infosSet[0]);
                if(!recipe.isPresent())
                {
                    return;
                }

                // Verify if for all groups an item has been chosen already. If not, ask for it and call this method again afterwards
                if(infosSet.length < recipe.get().getAllGroupNames().size() + 1)
                {
                    // There are more groups to ask about
                    String strGroup = recipe.get().getAllGroupNames().get(infosSet.length - 1);
                    ArrayList<RecipeItem> recipeItems = recipe.get().getAllRecipeItemsInGroup(strGroup);
                    if(recipeItems.size() < 2)
                    {
                        String itemName = "-";
                        if(recipeItems.size() == 1)
                        {
                            itemName = recipeItems.get(0).getIngredient();
                        }
                        onStringInput(tag, itemName, strAdditonalInformation);
                        return;
                    }

                    ArrayList<String> groupItems = new ArrayList<>();
                    for(RecipeItem item : recipeItems)
                    {
                        groupItems.add(item.getIngredient());
                    }
                    InputStringFromList newFragment = InputStringFromList.newInstance(getResources().getString(R.string.text_chose_from_group, strGroup), groupItems, strAdditonalInformation);
                    newFragment.show(getSupportFragmentManager(), "addShoppingRecipe");
                    break;
                }

                String strRecipe = infosSet[0];
                int iNr = 2;
                while(adapter.containsItem(new Pair<>(strRecipe, "")))
                {
                    strRecipe = String.format(Locale.getDefault(), "%s (%d)", infosSet[0], iNr);
                    ++iNr;
                }

                m_GroceryPlanning.shoppingList().addFromRecipe(strRecipe, recipe.get());
                Optional<ShoppingList.ShoppingRecipe> shoppingRecipe = m_GroceryPlanning.shoppingList().getShoppingRecipe(strRecipe);
                if(!shoppingRecipe.isPresent())
                {
                    return;
                }

                int i = 1;
                for(String strGroup : recipe.get().getAllGroupNames())
                {
                    String strIngredient = infosSet[i];
                    for(RecipeItem item : recipe.get().getAllRecipeItemsInGroup(strGroup))
                    {
                        if(item.getIngredient().equals(strIngredient))
                        {
                            shoppingRecipe.get().addItem(item);
                            break;
                        }
                    }
                    ++i;
                }

                adapter.notifyDataSetChanged();
                adapter.setActiveElement(new Pair<>(strRecipe, ""));
                m_RecyclerViewRecipes.scrollToPosition(adapter.getItemCount()-1);
                break;
            }

            case "renameShoppingRecipe":
            {
                Optional<ShoppingList.ShoppingRecipe> recipe = m_GroceryPlanning.shoppingList().getShoppingRecipe(strAdditonalInformation);
                if(!recipe.isPresent())
                {
                    return;
                }
                m_GroceryPlanning.shoppingList().renameShoppingRecipe(recipe.get(), strInput);

                adapter.notifyDataSetChanged();
                adapter.setActiveElement(new Pair<>(strInput, ""));
                m_RecyclerViewRecipes.scrollToPosition(adapter.getItemCount()-1);
                Toast.makeText(this, getResources().getString(R.string.text_recipe_renamed, strAdditonalInformation, strInput), Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }

    public void onResetList(View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.text_reset_shoppinglist_header));
        builder.setMessage(getResources().getString(R.string.text_reset_shoppnglist));
        builder.setPositiveButton(android.R.string.yes, (DialogInterface dialog, int which) ->
        {
            CoordinatorLayout coordLayout = findViewById(R.id.fabCoordinatorLayout);
            m_GroceryPlanning.shoppingList().clearShoppingList();
            m_AdapterRecipes = new ShoppingRecipesAdapter(coordLayout, m_RecyclerViewRecipes, m_GroceryPlanning.ingredients(), m_GroceryPlanning.shoppingList());
            m_RecyclerViewRecipes.setAdapter(m_AdapterRecipes);
            initTouchHelper();
        });
        builder.setNegativeButton(android.R.string.no, (DialogInterface dialog, int which) -> {});
        builder.show();
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
        else if(id == android.R.id.home)
        {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
