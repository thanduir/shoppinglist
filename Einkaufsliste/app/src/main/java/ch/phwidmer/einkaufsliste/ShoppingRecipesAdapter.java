package ch.phwidmer.einkaufsliste;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Vector;

public class ShoppingRecipesAdapter extends RecyclerView.Adapter<ShoppingRecipesAdapter.ViewHolder> implements ReactToTouchActionsInterface, AdapterView.OnItemSelectedListener
{
    private ShoppingList m_ShoppingList;
    private Ingredients m_Ingredients;
    private Integer m_iActiveElement;
    private RecyclerView m_RecyclerView;

    private ShoppingList.ShoppingRecipe m_RecentlyDeleted = null;
    private ShoppingListItem m_RecentlyDeletedItem = null;
    private int m_RecentlyDeletedIndex = -1;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView m_TextView;
        private View m_View;

        private String m_strRecipe;
        private ShoppingListItem m_RecipeItem = null;

        private TextView m_TextViewDesc;
        private TableLayout m_TableLayout;

        private ImageView m_ButtonAddRecipeItem;
        private ImageView m_ButtonDeleteRecipe;

        private CheckBox m_CheckBoxOptional;
        private Spinner  m_SpinnerAmount;
        private EditText m_EditTextAmount;
        private Spinner  m_SpinnerSize;

        public ViewHolder(View v)
        {
            super(v);
            m_View = v;
            m_TextView = v.findViewById(R.id.textView);
            m_TextViewDesc = v.findViewById(R.id.textViewDesc);
            m_TableLayout = v.findViewById(R.id.tableLayoutEditRecipeItem);
            m_TableLayout.setVisibility(View.GONE);

            m_ButtonAddRecipeItem = v.findViewById(R.id.button_addRecipeItem);
            m_ButtonDeleteRecipe = v.findViewById(R.id.button_deleteRecipe);

            m_SpinnerAmount = (Spinner) v.findViewById(R.id.spinnerAmount);
            m_EditTextAmount = (EditText) v.findViewById(R.id.editText_Amount);
            m_SpinnerSize = (Spinner) v.findViewById(R.id.spinnerSize);
            m_CheckBoxOptional = (CheckBox) v.findViewById(R.id.checkBoxOptional);
        }

        public Pair<String, String> getID()
        {
            String strItem = m_RecipeItem != null ? m_RecipeItem.m_Ingredient : "";
            return new Pair<String, String>(m_strRecipe, strItem);
        }

        private void updateDescription()
        {
            if(m_RecipeItem == null)
            {
                m_TextView.setTextColor(Color.BLACK);
                m_TextView.setTypeface(m_TextView.getTypeface(), Typeface.BOLD);
                return;
            }

            Float f = m_RecipeItem.m_Amount.m_Quantity;
            String amount = f.toString();
            if(f == Math.round(f))
            {
                amount = String.valueOf(f.intValue());
            }

            String text = " (" + amount + " " + Amount.shortForm(m_RecipeItem.m_Amount.m_Unit);
            if(m_RecipeItem.m_Size != RecipeItem.Size.Normal)
            {
                text += ", " + m_RecipeItem.m_Size.toString();
            }
            text += ")";

            if(m_RecipeItem.m_Amount.m_Unit == Amount.Unit.Unitless)
            {
                text = "";
            }
            m_TextViewDesc.setText(text);

            if(m_RecipeItem.m_Optional)
            {
                m_TextView.setTextColor(Color.GRAY);
                m_TextView.setTypeface(m_TextView.getTypeface(), Typeface.ITALIC);

                m_TextViewDesc.setTextColor(Color.GRAY);
                m_TextViewDesc.setTypeface(m_TextViewDesc.getTypeface(), Typeface.ITALIC);
            }
            else
            {
                m_TextView.setTextColor(Color.BLACK);
                m_TextView.setTypeface(null, Typeface.NORMAL);

                m_TextViewDesc.setTypeface(null, Typeface.NORMAL);
                m_TextViewDesc.setTextColor(Color.BLACK);
            }
        }
    }

    public ShoppingRecipesAdapter(RecyclerView recyclerView, Ingredients ingredients, ShoppingList shoppingList)
    {
        m_iActiveElement = -1;
        m_ShoppingList = shoppingList;
        m_Ingredients = ingredients;
        m_RecyclerView = recyclerView;
    }

    public Pair<String, String> getActiveElement()
    {
        if(m_iActiveElement == -1)
        {
            return null;
        }
        return getShoppingRecipes().get(m_iActiveElement);
    }

    public void setActiveElement(Pair<String, String> element)
    {
        if(m_iActiveElement != -1)
        {
            ShoppingRecipesAdapter.ViewHolder vh = (ShoppingRecipesAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(m_RecyclerView.getChildAt(m_iActiveElement));
            updateViewHolder(vh, false);
        }

        if(element == null)
        {
            m_iActiveElement = -1;
        }
        else
        {
            m_iActiveElement = getShoppingRecipes().indexOf(element);

            View child = m_RecyclerView.getChildAt(m_iActiveElement);
            if(child == null)
            {
                return;
            }

            ShoppingRecipesAdapter.ViewHolder vh = (ShoppingRecipesAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(child);
            updateViewHolder(vh, true);
        }
    }

    @Override
    public ShoppingRecipesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_item_shoppinglist_recipe, parent, false);

        ShoppingRecipesAdapter.ViewHolder vh = new ShoppingRecipesAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ShoppingRecipesAdapter.ViewHolder holder, int position)
    {
        Pair<String, String> strItem = getShoppingRecipes().get(position);
        ShoppingList.ShoppingRecipe recipe = m_ShoppingList.getShoppingRecipe(strItem.first);
        holder.m_strRecipe = strItem.first;
        holder.m_RecipeItem = null;
        if(!strItem.second.isEmpty())
        {
            holder.m_RecipeItem = getShoppingListItem(recipe, strItem.second);
        }

        holder.updateDescription();
        updateViewHolder(holder, m_iActiveElement == position);
    }

    private void updateViewHolder(ShoppingRecipesAdapter.ViewHolder vh, boolean bActive)
    {
        if(vh.m_RecipeItem == null || !bActive)
        {
            vh.m_SpinnerAmount.setAdapter(null);
            vh.m_SpinnerSize.setAdapter(null);
        }

        if(vh.m_RecipeItem == null)
        {
            vh.m_TableLayout.setVisibility(View.GONE);

            final String strRecipe = vh.m_strRecipe;

            ShoppingList.ShoppingRecipe recipe = m_ShoppingList.getShoppingRecipe(strRecipe);

            Float f = recipe.m_fScalingFactor;
            String factor = f.toString();
            if(f == Math.round(f))
            {
                factor = String.valueOf(f.intValue());
            }
            vh.m_TextViewDesc.setText(" (" + factor + " Pers. [+] )");

            vh.m_TextViewDesc.setOnClickListener(new View.OnClickListener() {
                                                     @Override
                                                     public void onClick(View view) {
                                                       onChangeRecipeScaling(strRecipe);
                                                     };
                                                 }

            );

            vh.m_View.setBackgroundColor(Color.TRANSPARENT);

            vh.m_ButtonAddRecipeItem.setVisibility(View.VISIBLE);
            vh.m_ButtonDeleteRecipe.setVisibility(View.VISIBLE);

            vh.m_ButtonDeleteRecipe.setOnClickListener(new View.OnClickListener() {
                                                           @Override
                                                           public void onClick(View view) {
                                                               onDelShoppingRecipe(strRecipe);
                                                           }
                                                       }
            );
            vh.m_ButtonAddRecipeItem.setOnClickListener(new View.OnClickListener() {
                                                           @Override
                                                           public void onClick(View view) {
                                                               onAddShoppingListItem(strRecipe);
                                                           }
                                                       }
            );

            vh.m_SpinnerAmount.setAdapter(null);
            vh.m_SpinnerSize.setAdapter(null);

            // Recipe -> Header line
            vh.m_TextView.setText(vh.m_strRecipe);
        }
        else
        {
            // Active ShoppingListItem

            vh.m_TextView.setText("\t\u2022 " + vh.m_RecipeItem.m_Ingredient);

            vh.m_TextViewDesc.setOnClickListener(null);
            vh.m_TextViewDesc.setClickable(false);

            vh.m_ButtonAddRecipeItem.setVisibility(View.GONE);
            vh.m_ButtonDeleteRecipe.setVisibility(View.GONE);

            if(!bActive)
            {
                vh.m_TableLayout.setVisibility(View.GONE);
                vh.m_TextViewDesc.setVisibility(View.VISIBLE);

                vh.m_View.setBackgroundColor(Color.TRANSPARENT);

                return;
            }

            vh.m_TableLayout.setVisibility(View.VISIBLE);
            vh.m_TextViewDesc.setVisibility(View.INVISIBLE);

            vh.m_View.setBackgroundColor(vh.m_View.getResources().getColor(R.color.colorHighlightedBackground));

            ShoppingListItem item = vh.m_RecipeItem;

            ArrayAdapter<CharSequence> adapterAmount = new ArrayAdapter<CharSequence>(vh.m_View.getContext(), R.layout.spinner_item);
            for(int i = 0; i < Amount.Unit.values().length; ++i)
            {
                adapterAmount.add(Amount.Unit.values()[i].toString());
            }
            adapterAmount.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            vh.m_SpinnerAmount.setAdapter(adapterAmount);
            vh.m_SpinnerAmount.setOnItemSelectedListener(this);
            vh.m_SpinnerAmount.setSelection(item.m_Amount.m_Unit.ordinal());

            ArrayAdapter<CharSequence> adapterSize = new ArrayAdapter<CharSequence>(vh.m_View.getContext(), R.layout.spinner_item);
            for(int i = 0; i < RecipeItem.Size.values().length; ++i)
            {
                adapterSize.add(RecipeItem.Size.values()[i].toString());
            }
            adapterSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            vh.m_SpinnerSize.setAdapter(adapterSize);
            vh.m_SpinnerSize.setOnItemSelectedListener(this);
            vh.m_SpinnerSize.setSelection(item.m_Size.ordinal());

            vh.m_CheckBoxOptional.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    ShoppingRecipesAdapter.ViewHolder vh = (ShoppingRecipesAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(m_RecyclerView.getChildAt(m_iActiveElement));
                    ShoppingListItem item = vh.m_RecipeItem;
                    if(item == null)
                    {
                        return;
                    }

                    item.m_Optional = isChecked;
                    vh.updateDescription();
                }
            });
            vh.m_CheckBoxOptional.setChecked(item.m_Optional);

            vh.m_EditTextAmount.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                    View child = m_RecyclerView.getChildAt(m_iActiveElement);
                    if(child == null)
                    {
                        // Item not visible (yet) -> nothing to do
                        return;
                    }

                    ShoppingRecipesAdapter.ViewHolder vh = (ShoppingRecipesAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(child);
                    ShoppingListItem item = vh.m_RecipeItem;
                    if(s.toString().isEmpty())
                    {
                        item.m_Amount.m_Quantity = 0.0f;
                    }
                    else
                    {
                        item.m_Amount.m_Quantity = Float.valueOf(s.toString());
                    }

                    vh.updateDescription();
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
            });
            adjustEditTextAmount(vh, item);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        ShoppingRecipesAdapter.ViewHolder vh = (ShoppingRecipesAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(m_RecyclerView.getChildAt(m_iActiveElement));
        ShoppingListItem item = vh.m_RecipeItem;

        if(parent == vh.m_SpinnerAmount)
        {
            item.m_Amount.m_Unit = Amount.Unit.values()[vh.m_SpinnerAmount.getSelectedItemPosition()];
            adjustEditTextAmount(vh, item);
        }
        else if(parent == vh.m_SpinnerSize)
        {
            item.m_Size = RecipeItem.Size.values()[vh.m_SpinnerSize.getSelectedItemPosition()];
        }

        vh.updateDescription();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
    }

    public boolean containsItem(Pair<String, String> strName)
    {
        return getShoppingRecipes().contains(strName);
    }

    public void reactToSwipe(int position)
    {
        // Remove ShoppingListItem at this position

        View activeItem = m_RecyclerView.getChildAt(position);
        ShoppingRecipesAdapter.ViewHolder holder = (ShoppingRecipesAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(activeItem);

        if(holder.m_RecipeItem == null)
        {
            return;
        }

        ShoppingList.ShoppingRecipe recipe = m_ShoppingList.getShoppingRecipe(holder.m_strRecipe);

        m_RecentlyDeletedIndex = recipe.m_Items.indexOf(holder.m_RecipeItem);
        m_RecentlyDeleted = recipe;
        m_RecentlyDeletedItem = holder.m_RecipeItem;
        recipe.m_Items.remove(holder.m_RecipeItem);

        notifyItemRemoved(position);
        setActiveElement(null);

        // Allow undo

        Snackbar snackbar = Snackbar.make(m_RecyclerView, "Item deleted", Snackbar.LENGTH_LONG);
        snackbar.setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(m_RecentlyDeletedItem == null)
                {
                    return;
                }
                m_RecentlyDeleted.m_Items.add(m_RecentlyDeletedIndex, m_RecentlyDeletedItem);

                notifyDataSetChanged();

                m_RecentlyDeletedIndex = -1;
                m_RecentlyDeleted = null;
                m_RecentlyDeletedItem = null;

                Snackbar snackbar1 = Snackbar.make(m_RecyclerView, "Item restored", Snackbar.LENGTH_SHORT);
                snackbar1.show();
            }
        });
        snackbar.show();
    }

    public boolean swipeAllowed(RecyclerView.ViewHolder vh)
    {
        ShoppingRecipesAdapter.ViewHolder holder = (ShoppingRecipesAdapter.ViewHolder)vh;
        return holder.m_RecipeItem != null;
    }

    public boolean reactToDrag(RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target)
    {
        return false;
    }

    @Override
    public int getItemCount()
    {
        return getShoppingRecipes().size();
    }

    private Vector<Pair<String, String>> getShoppingRecipes()
    {
        Vector<Pair<String, String>> vec = new Vector<Pair<String, String>>();
        for(String strRecipe : m_ShoppingList.getAllShoppingRecipes())
        {
            vec.add(new Pair<String, String>(strRecipe, ""));
            ShoppingList.ShoppingRecipe recipe = m_ShoppingList.getShoppingRecipe(strRecipe);
            for(ShoppingListItem item : recipe.m_Items)
            {
                vec.add(new Pair<String, String>(strRecipe, item.m_Ingredient));
            }
        }

        return vec;
    }

    private ShoppingListItem getShoppingListItem(ShoppingList.ShoppingRecipe recipe, String strName)
    {
        for(ShoppingListItem r : recipe.m_Items)
        {
            if(r.m_Ingredient == strName)
            {
                return r;
            }
        }
        return null;
    }

    private void adjustEditTextAmount(ShoppingRecipesAdapter.ViewHolder vh, ShoppingListItem item)
    {
        if(item.m_Amount.m_Unit == Amount.Unit.Unitless)
        {
            vh.m_EditTextAmount.setText("");
            vh.m_EditTextAmount.setVisibility(View.INVISIBLE);
        }
        else
        {
            vh.m_EditTextAmount.setText(item.m_Amount.m_Quantity.toString());
            vh.m_EditTextAmount.setVisibility(View.VISIBLE);
        }
    }

    private void onAddShoppingListItem(final String strRecipe)
    {
        int index = getShoppingRecipes().indexOf(new Pair<String, String>(strRecipe, ""));
        View v = m_RecyclerView.getChildAt(index);
        ShoppingRecipesAdapter.ViewHolder holder = (ShoppingRecipesAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(v);
        ShoppingList.ShoppingRecipe recipe = m_ShoppingList.getShoppingRecipe(strRecipe);
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle("Add ingredient");

        // Set up the input
        final Spinner input = new Spinner(v.getContext());
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(v.getContext(), android.R.layout.simple_spinner_item);
        for(String strName : m_Ingredients.getAllIngredients())
        {
            if(getShoppingListItem(recipe, strName) != null)
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
                item.m_Amount.m_Unit = m_Ingredients.getIngredient(strIngredient).m_DefaultUnit;
                m_ShoppingList.getShoppingRecipe(strRecipe).m_Items.add(item);

                Pair<String, String> newItem = new Pair<String, String>(strRecipe, strIngredient);

                notifyItemInserted(getShoppingRecipes().indexOf(newItem));
                setActiveElement(newItem);
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

    private void onDelShoppingRecipe(final String strRecipe)
    {
        int index = getShoppingRecipes().indexOf(new Pair<String, String>(strRecipe, ""));
        View v = m_RecyclerView.getChildAt(index);
        ShoppingRecipesAdapter.ViewHolder holder = (ShoppingRecipesAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(v);

        if(holder.m_RecipeItem != null)
        {
            return;
        }

        ShoppingList.ShoppingRecipe recipe = m_ShoppingList.getShoppingRecipe(holder.m_strRecipe);

        m_RecentlyDeleted = recipe;
        m_RecentlyDeletedIndex = -1;
        m_RecentlyDeletedItem = null;
        m_ShoppingList.removeShoppingRecipe(holder.m_strRecipe);

        notifyDataSetChanged();
        setActiveElement(null);

        // Allow undo

        Snackbar snackbar = Snackbar.make(m_RecyclerView, "Item deleted", Snackbar.LENGTH_LONG);
        snackbar.setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(m_RecentlyDeletedItem != null)
                {
                    return;
                }
                m_ShoppingList.addExistingShoppingRecipe(strRecipe, m_RecentlyDeleted);

                notifyDataSetChanged();

                m_RecentlyDeletedIndex = -1;
                m_RecentlyDeleted = null;
                m_RecentlyDeletedItem = null;

                Snackbar snackbar1 = Snackbar.make(m_RecyclerView, "Item restored", Snackbar.LENGTH_SHORT);
                snackbar1.show();
            }
        });
        snackbar.show();
    }

    private void onChangeRecipeScaling(final String strRecipe)
    {
        // TODO!
        int index = getShoppingRecipes().indexOf(new Pair<String, String>(strRecipe, ""));
        View v = m_RecyclerView.getChildAt(index);
        ShoppingRecipesAdapter.ViewHolder holder = (ShoppingRecipesAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(v);
        ShoppingList.ShoppingRecipe recipe = m_ShoppingList.getShoppingRecipe(strRecipe);

        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle("Change number of persons of recipe \"" + strRecipe + "\"");

        // Set up the input
        final EditText input = new EditText(v.getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText(recipe.m_fScalingFactor.toString());
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(input.getText().toString().isEmpty())
                {
                    return;
                }
                float fNewValue = Float.valueOf(input.getText().toString());

                ShoppingList.ShoppingRecipe recipe = m_ShoppingList.getShoppingRecipe(strRecipe);
                recipe.changeScalingFactor(fNewValue);
                notifyDataSetChanged();
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
}
