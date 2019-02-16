package ch.phwidmer.einkaufsliste;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ManageShoppingList extends AppCompatActivity {

    public static final String EXTRA_SHOPPINGLIST = "ch.phwidmer.einkaufsliste.SHOPPINGLIST";
    public static final String EXTRA_RECIPE_NAME = "ch.phwidmer.einkaufsliste.SHOPPINGLIST_RECIPENAME";
    public static final String EXTRA_INGREDIENTS = "ch.phwidmer.einkaufsliste.SHOPPINGLIST_INGREDIENTS";
    private final int REQUEST_CODE_EditShoppingListRecipe = 1;

    private ShoppingList    m_ShoppingList;
    private Recipes         m_Recipes;
    private Ingredients     m_Ingredients;

    private RecyclerView                m_RecyclerViewRecipes;
    private RecyclerView.Adapter        m_AdapterRecipes;
    private RecyclerView.LayoutManager  m_LayoutManagerRecipes;

    // TODO: Statt nur der Name des Rezepts sollte im entspr. RecyclerView auch die weiteren Inhalte aufgelistet sein!
    // TODO: Kann ich evtl. gleich das ganze EditShoppingListRecipe-Activity in den RecyclerView packen?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_shopping_list);

        Intent intent = getIntent();
        m_ShoppingList = (ShoppingList)intent.getParcelableExtra(MainActivity.EXTRA_SHOPPINGLIST);
        m_Recipes = (Recipes)intent.getParcelableExtra(MainActivity.EXTRA_RECIPES);
        m_Ingredients = (Ingredients)intent.getParcelableExtra(MainActivity.EXTRA_INGREDIENTS);

        m_RecyclerViewRecipes = (RecyclerView) findViewById(R.id.recyclerViewShoppingRecipe);
        m_RecyclerViewRecipes.setHasFixedSize(true);
        m_LayoutManagerRecipes = new LinearLayoutManager(this);
        m_RecyclerViewRecipes.setLayoutManager(m_LayoutManagerRecipes);
        m_AdapterRecipes = new ShoppingRecipesAdapter(m_ShoppingList);
        m_RecyclerViewRecipes.setAdapter(m_AdapterRecipes);
        ItemClickSupport.addTo(m_RecyclerViewRecipes).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v)
                    {
                        ShoppingRecipesAdapter adapter = (ShoppingRecipesAdapter)recyclerView.getAdapter();
                        if(adapter.getActiveElement() != "")
                        {
                            View prevItem = recyclerView.getChildAt(adapter.getActiveElementIndex());
                            prevItem.setBackgroundColor(Color.TRANSPARENT);
                            prevItem.setActivated(false);
                        }

                        ShoppingRecipesAdapter.ViewHolder vh = (ShoppingRecipesAdapter.ViewHolder) recyclerView.getChildViewHolder(v);
                        adapter.setActiveElement((String)vh.m_TextView.getText());
                        v.setBackgroundColor(Color.GRAY);
                        v.setActivated(true);
                    }
                }
        );
    }

    public void onAddShoppingRecipe(View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Import recipe");

        // Set up the input
        final Spinner input = new Spinner(this);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        for(String strName : m_Recipes.getAllRecipes())
        {
            ShoppingRecipesAdapter adapterItems = (ShoppingRecipesAdapter)m_RecyclerViewRecipes.getAdapter();
            if(adapterItems.containsItem(strName))
            {
                continue;
            }
            adapter.add(strName);
        }
        if(adapter.isEmpty())
        {
            Toast.makeText(v.getContext(), "Nothing to add, all recipes already added.", Toast.LENGTH_SHORT).show();
            return;
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        input.setAdapter(adapter);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strRecipe = input.getSelectedItem().toString();

                m_ShoppingList.addFromRecipe(strRecipe, m_Recipes.getRecipe(strRecipe));

                ShoppingRecipesAdapter adapter = (ShoppingRecipesAdapter)m_RecyclerViewRecipes.getAdapter();
                adapter.notifyDataSetChanged();
                adapter.setActiveElement(strRecipe);
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

    public void onDelShoppingRecipe(View v)
    {
        ShoppingRecipesAdapter adapter = (ShoppingRecipesAdapter)m_RecyclerViewRecipes.getAdapter();
        if(adapter.getActiveElement() == "")
        {
            // TODO: Eigentlich sollte stattdessen der Delete-Button deaktiviert sein, wenn kein Element ausgewählt ist.
            Toast.makeText(v.getContext(), "No element selected", Toast.LENGTH_SHORT).show();
            return;
        }

        View activeItem = m_RecyclerViewRecipes.getChildAt(adapter.getActiveElementIndex());
        ShoppingRecipesAdapter.ViewHolder holder = (ShoppingRecipesAdapter.ViewHolder)m_RecyclerViewRecipes.getChildViewHolder(activeItem);

        Toast.makeText(v.getContext(), "Deleteing " + holder.m_TextView.getText(), Toast.LENGTH_SHORT).show();

        m_ShoppingList.removeShoppingRecipe((String)holder.m_TextView.getText());
        adapter.notifyItemRemoved(adapter.getActiveElementIndex());
        adapter.setActiveElement("");

        if(m_RecyclerViewRecipes.getAdapter() != null)
        {
            m_RecyclerViewRecipes.getAdapter().notifyDataSetChanged();
        }
    }

    public void onEditShoppingRecipe(View v)
    {
        ShoppingRecipesAdapter adapter = (ShoppingRecipesAdapter)m_RecyclerViewRecipes.getAdapter();
        if(adapter.getActiveElement() == "")
        {
            return;
        }

        View activeItem = m_RecyclerViewRecipes.getChildAt(adapter.getActiveElementIndex());
        ShoppingRecipesAdapter.ViewHolder holder = (ShoppingRecipesAdapter.ViewHolder)m_RecyclerViewRecipes.getChildViewHolder(activeItem);

        Intent intent = new Intent(this, EditShoppingListRecipe.class);
        intent.putExtra(EXTRA_SHOPPINGLIST, m_ShoppingList);
        intent.putExtra(EXTRA_RECIPE_NAME, holder.m_TextView.getText().toString());
        intent.putExtra(EXTRA_INGREDIENTS, m_Ingredients);
        startActivityForResult(intent, REQUEST_CODE_EditShoppingListRecipe);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode != RESULT_OK)
        {
            return;
        }

        if(requestCode == REQUEST_CODE_EditShoppingListRecipe)
        {
            m_ShoppingList = data.getParcelableExtra(EXTRA_SHOPPINGLIST);
        }
    }

    public void onResetList(View v)
    {
        m_ShoppingList = new ShoppingList();
        m_AdapterRecipes = new ShoppingRecipesAdapter(m_ShoppingList);
        m_RecyclerViewRecipes.setAdapter(m_AdapterRecipes);
    }

    public void onConfirm(View v)
    {
        Intent data = new Intent();
        data.putExtra(MainActivity.EXTRA_SHOPPINGLIST, m_ShoppingList);
        setResult(RESULT_OK, data);
        finish();
    }

    public void onCancel(View v)
    {
        finish();
    }
}
