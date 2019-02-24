package ch.phwidmer.einkaufsliste;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class EditShoppingListRecipe extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private GroceryPlanning m_GroceryPlanning;
    private String          m_SaveFilePath;

    private ShoppingList.ShoppingRecipe m_Recipe;

    private RecyclerView               m_RecyclerView;
    private RecyclerView.Adapter       m_Adapter;
    private RecyclerView.LayoutManager m_LayoutManager;

    private CheckBox    m_CheckBoxOptional;
    private TextView    m_TextViewAmount;
    private Spinner     m_SpinnerAmount;
    private EditText    m_EditTextAmount;
    private TextView    m_TextViewSize;
    private Spinner     m_SpinnerSize;

    // TODO: Statt nur der Name der Zutat sollten im entspr. RecyclerView auch die weiteren Inhalte aufgelistet sein!
    // TODO: Back-Button führt zu Crash!

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_shopping_list_recipe);

        Intent intent = getIntent();
        m_SaveFilePath = intent.getStringExtra(MainActivity.EXTRA_SAVEFILESPATH);
        File file = new File(m_SaveFilePath, MainActivity.c_strSaveFilename);
        m_GroceryPlanning = new GroceryPlanning(file, this);
        m_Recipe = m_GroceryPlanning.m_ShoppingList.getShoppingRecipe(intent.getStringExtra(ManageShoppingList.EXTRA_RECIPE_NAME));

        m_CheckBoxOptional = (CheckBox) findViewById(R.id.checkBoxOptional);
        m_TextViewAmount  = (TextView) findViewById(R.id.textViewAmount);
        m_SpinnerAmount = (Spinner) findViewById(R.id.spinnerAmount);
        m_EditTextAmount = (EditText) findViewById(R.id.editText_Amount);
        m_TextViewSize  = (TextView) findViewById(R.id.textViewSize);
        m_SpinnerSize = (Spinner) findViewById(R.id.spinnerSize);

        m_CheckBoxOptional.setVisibility(View.INVISIBLE);
        m_EditTextAmount.setVisibility(View.INVISIBLE);
        m_TextViewAmount.setVisibility(View.INVISIBLE);
        m_TextViewSize.setVisibility(View.INVISIBLE);

        m_RecyclerView = (RecyclerView) findViewById(R.id.recyclerViewShoppingListItems);
        m_RecyclerView.setHasFixedSize(true);
        m_LayoutManager = new LinearLayoutManager(this);
        m_RecyclerView.setLayoutManager(m_LayoutManager);
        m_Adapter = new EditShoppingRecipeAdapter(m_Recipe);
        m_RecyclerView.setAdapter(m_Adapter);
        ItemClickSupport.addTo(m_RecyclerView).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v)
                    {
                        EditShoppingRecipeAdapter adapter = (EditShoppingRecipeAdapter)recyclerView.getAdapter();
                        if(adapter.getActiveElement() != "")
                        {
                            View prevItem = recyclerView.getChildAt(adapter.getActiveElementIndex());
                            prevItem.setBackgroundColor(Color.TRANSPARENT);
                            prevItem.setActivated(false);
                        }

                        EditShoppingRecipeAdapter.ViewHolder vh = (EditShoppingRecipeAdapter.ViewHolder) recyclerView.getChildViewHolder(v);
                        adapter.setActiveElement((String)vh.m_TextView.getText());
                        v.setBackgroundColor(Color.GRAY);
                        v.setActivated(true);

                        handleShoppingListItemSelected(adapter.getActiveElement());
                    }
                }
        );

        ArrayAdapter<CharSequence> adapterAmount = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        for(int i = 0; i < Amount.Unit.values().length; ++i)
        {
            adapterAmount.add(Amount.Unit.values()[i].toString());
        }
        adapterAmount.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_SpinnerAmount.setAdapter(adapterAmount);
        m_SpinnerAmount.setOnItemSelectedListener(this);
        m_SpinnerAmount.setVisibility(View.INVISIBLE);

        ArrayAdapter<CharSequence> adapterSize = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        for(int i = 0; i < RecipeItem.Size.values().length; ++i)
        {
            adapterSize.add(RecipeItem.Size.values()[i].toString());
        }
        adapterSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_SpinnerSize.setAdapter(adapterSize);
        m_SpinnerSize.setOnItemSelectedListener(this);
        m_SpinnerSize.setVisibility(View.INVISIBLE);

        m_CheckBoxOptional.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                ShoppingListItem item = getSelectedShoppingListItem();
                if(item == null)
                {
                    return;
                }

                item.m_Optional = isChecked;
            }
        });

        m_EditTextAmount.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                ShoppingListItem item = getSelectedShoppingListItem();
                if(item == null)
                {
                    return;
                }

                if(s.toString().isEmpty())
                {
                    item.m_Amount.m_Quantity = 0.0f;
                }
                else
                {
                    item.m_Amount.m_Quantity = Float.valueOf(s.toString());
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    @Override
    protected void onPause()
    {
        File file = new File(new File(m_SaveFilePath), MainActivity.c_strSaveFilename);
        m_GroceryPlanning.saveDataToFile(file);

        super.onPause();
    }

    public void onAddIngredient(View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add ingredient");

        // Set up the input
        final Spinner input = new Spinner(this);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);
        for(String strName : m_GroceryPlanning.m_Ingredients.getAllIngredients())
        {
            EditShoppingRecipeAdapter adapterItems = (EditShoppingRecipeAdapter)m_RecyclerView.getAdapter();
            if(adapterItems.containsItem(strName))
            {
                continue;
            }
            adapter.add(strName);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        input.setAdapter(adapter);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strIngredient = input.getSelectedItem().toString();

                ShoppingListItem item = new ShoppingListItem();
                item.m_Ingredient = strIngredient;
                item.m_Amount.m_Unit = m_GroceryPlanning.m_Ingredients.getIngredient(strIngredient).m_DefaultUnit;
                m_Recipe.m_Items.add(item);

                EditShoppingRecipeAdapter adapter = (EditShoppingRecipeAdapter)m_RecyclerView.getAdapter();
                adapter.setActiveElement(strIngredient);
                adapter.notifyDataSetChanged();

                handleShoppingListItemSelected(strIngredient);
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

    public void onDelIngredient(View v)
    {
        EditShoppingRecipeAdapter adapter = (EditShoppingRecipeAdapter)m_RecyclerView.getAdapter();
        if(adapter.getActiveElement() == "")
        {
            // TODO: Eigentlich sollte stattdessen der Delete-Button deaktiviert sein, wenn kein Element ausgewählt ist.
            Toast.makeText(v.getContext(), "No element selected", Toast.LENGTH_SHORT).show();
            return;
        }

        View activeItem = m_RecyclerView.getChildAt(adapter.getActiveElementIndex());
        EditShoppingRecipeAdapter.ViewHolder holder = (EditShoppingRecipeAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(activeItem);

        Toast.makeText(v.getContext(), "Deleteing " + holder.m_TextView.getText(), Toast.LENGTH_SHORT).show();

        ShoppingListItem item = getSelectedShoppingListItem();
        m_Recipe.m_Items.remove(item);

        adapter.notifyItemRemoved(adapter.getActiveElementIndex());
        adapter.setActiveElement("");
        if(m_RecyclerView.getAdapter() != null)
        {
            m_RecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        ShoppingListItem item = getSelectedShoppingListItem();
        if(item == null)
        {
            return;
        }

        if(parent == m_SpinnerAmount)
        {
            item.m_Amount.m_Unit = Amount.Unit.values()[m_SpinnerAmount.getSelectedItemPosition()];

            adjustEditTextAmount(item);
        }
        else if(parent == m_SpinnerSize)
        {
            item.m_Size = RecipeItem.Size.values()[m_SpinnerSize.getSelectedItemPosition()];
        }
    }

    public void onNothingSelected(AdapterView<?> parent)
    {
    }

    private void handleShoppingListItemSelected(String strActiveElement)
    {
        if(strActiveElement == "")
        {
            m_CheckBoxOptional.setVisibility(View.INVISIBLE);
            m_SpinnerSize.setVisibility(View.INVISIBLE);
            m_SpinnerAmount.setVisibility(View.INVISIBLE);
            m_EditTextAmount.setVisibility(View.INVISIBLE);
            m_TextViewAmount.setVisibility(View.INVISIBLE);
            m_TextViewSize.setVisibility(View.INVISIBLE);
            return;
        }

        m_CheckBoxOptional.setVisibility(View.VISIBLE);
        m_TextViewSize.setVisibility(View.VISIBLE);
        m_SpinnerSize.setVisibility(View.VISIBLE);
        m_TextViewAmount.setVisibility(View.VISIBLE);
        m_SpinnerAmount.setVisibility(View.VISIBLE);
        m_EditTextAmount.setVisibility(View.VISIBLE);

        ShoppingListItem item = getSelectedShoppingListItem();
        if(item == null)
        {
            return;
        }

        m_CheckBoxOptional.setChecked(item.m_Optional);

        m_SpinnerAmount.setSelection(item.m_Amount.m_Unit.ordinal());
        adjustEditTextAmount(item);

        m_SpinnerSize.setSelection(item.m_Size.ordinal());
    }

    private ShoppingListItem getSelectedShoppingListItem()
    {
        EditShoppingRecipeAdapter adapter = (EditShoppingRecipeAdapter)m_RecyclerView.getAdapter();
        if(adapter == null)
        {
            return null;
        }

        ShoppingListItem item = null;
        for(ShoppingListItem r : m_Recipe.m_Items)
        {
            if(r.m_Ingredient == adapter.getActiveElement())
            {
                return r;
            }
        }

        return null;
    }

    private void adjustEditTextAmount(ShoppingListItem item)
    {
        if(item.m_Amount.m_Unit == Amount.Unit.Unitless)
        {
            m_EditTextAmount.setText("");
            m_EditTextAmount.setVisibility(View.INVISIBLE);
        }
        else
        {
            m_EditTextAmount.setText(item.m_Amount.m_Quantity.toString());
            m_EditTextAmount.setVisibility(View.VISIBLE);
        }
    }

    public void onConfirm(View v)
    {
        Intent intent = new Intent(this, ManageCategories.class);
        intent.putExtra(MainActivity.EXTRA_SAVEFILESPATH, m_SaveFilePath);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onCancel(View v)
    {
        finish();
    }
}
