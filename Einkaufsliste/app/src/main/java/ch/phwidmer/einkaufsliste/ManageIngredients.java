package ch.phwidmer.einkaufsliste;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Toast;

import java.io.File;

public class ManageIngredients extends AppCompatActivity implements InputStringDialogFragment.InputStringResponder
{
    private GroceryPlanning m_GroceryPlanning;
    private String          m_SaveFilePath;

    private RecyclerView               m_RecyclerView;
    private IngredientsAdapter         m_Adapter;
    private RecyclerView.LayoutManager m_LayoutManager;

    private FloatingActionButton m_FAB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_ingredients);

        Intent intent = getIntent();
        m_SaveFilePath = intent.getStringExtra(MainActivity.EXTRA_SAVEFILESPATH);
        File file = new File(new File(m_SaveFilePath), MainActivity.c_strSaveFilename);
        m_GroceryPlanning = new GroceryPlanning(file, this);

        m_FAB = findViewById(R.id.fab);

        m_RecyclerView = findViewById(R.id.recyclerViewIngredients);
        m_RecyclerView.setHasFixedSize(true);
        m_LayoutManager = new LinearLayoutManager(this);
        m_RecyclerView.setLayoutManager(m_LayoutManager);
        m_Adapter = new IngredientsAdapter(m_RecyclerView, m_GroceryPlanning);
        m_RecyclerView.setAdapter(m_Adapter);
        ItemClickSupport.addTo(m_RecyclerView).setOnItemClickListener(
                        new ItemClickSupport.OnItemClickListener() {
                            @Override
                            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                                IngredientsAdapter adapter = (IngredientsAdapter) recyclerView.getAdapter();
                                IngredientsAdapter.ViewHolder vh = (IngredientsAdapter.ViewHolder) recyclerView.getChildViewHolder(v);

                                if(vh.getID().equals(adapter.getActiveElement()))
                                {
                                    adapter.setActiveElement("");
                                }
                                else
                                {
                                    adapter.setActiveElement(vh.getID());
                                }
                            }
                        }
                );
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ReactToTouchActionsCallback<IngredientsAdapter>(m_RecyclerView,
                                                                                              m_RecyclerView.getContext(),
                                                                                              R.drawable.ic_delete_black_24dp,
                                                                                              false));
        itemTouchHelper.attachToRecyclerView(m_RecyclerView);

        if(savedInstanceState != null)
        {
            String strActiveElement = savedInstanceState.getString("AdapterActiveElement");
            m_Adapter.setActiveElement(strActiveElement);
        }

        m_RecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
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
        if(m_Adapter != null)
        {
            savedInstanceState.putString("AdapterActiveElement", m_Adapter.getActiveElement());
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    public void onAddIngredient(View v)
    {
        DialogFragment newFragment = InputStringDialogFragment.newInstance(getResources().getString(R.string.text_add_ingredient), "");
        newFragment.show(getSupportFragmentManager(), "addIngredient");
    }

    public void onStringInput(String tag, String strInput, String strAdditonalInformation)
    {
        if(tag.equals("addIngredient"))
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            final String strDefaultUnit = preferences.getString(SettingsActivity.KEY_DEFAULT_UNIT, Amount.Unit.Count.toString());

            m_GroceryPlanning.m_Ingredients.addIngredient(strInput, Amount.Unit.valueOf(strDefaultUnit));
            IngredientsAdapter adapter = (IngredientsAdapter)m_RecyclerView.getAdapter();
            adapter.setActiveElement(strInput);
            adapter.notifyDataSetChanged();
            m_RecyclerView.scrollToPosition(m_GroceryPlanning.m_Ingredients.getAllIngredients().indexOf(strInput));
        }
        else if(tag.equals("renameIngredient")) // See IngredientsAdapter
        {
            m_GroceryPlanning.m_Ingredients.renameIngredient(strAdditonalInformation, strInput);
            m_GroceryPlanning.m_Recipes.onIngredientRenamed(strAdditonalInformation, strInput);
            m_GroceryPlanning.m_ShoppingList.onIngredientRenamed(strAdditonalInformation, strInput);

            IngredientsAdapter adapter = (IngredientsAdapter)m_RecyclerView.getAdapter();
            adapter.setActiveElement(strInput);
            adapter.notifyDataSetChanged();
            Toast.makeText(m_RecyclerView.getContext(), m_RecyclerView.getContext().getResources().getString(R.string.text_ingredient_renamed, strAdditonalInformation, strInput), Toast.LENGTH_SHORT).show();
        }
    }
}
