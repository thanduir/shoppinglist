package ch.phwidmer.einkaufsliste;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
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

import java.util.ArrayList;
import java.util.Locale;

public class ShoppingRecipesAdapter extends RecyclerView.Adapter<ShoppingRecipesAdapter.ViewHolder> implements ReactToTouchActionsInterface, AdapterView.OnItemSelectedListener
{
    private ShoppingList m_ShoppingList;
    private Ingredients m_Ingredients;
    private Integer m_iActiveElement;
    private RecyclerView m_RecyclerView;
    private CoordinatorLayout m_CoordLayout;

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

            m_SpinnerAmount = v.findViewById(R.id.spinnerAmount);
            m_EditTextAmount = v.findViewById(R.id.editText_Amount);
            m_SpinnerSize = v.findViewById(R.id.spinnerSize);
            m_CheckBoxOptional = v.findViewById(R.id.checkBoxOptional);
        }

        Pair<String, String> getID()
        {
            String strItem = m_RecipeItem != null ? m_RecipeItem.m_Ingredient : "";
            return new Pair<>(m_strRecipe, strItem);
        }

        private void updateDescription(Context context)
        {
            if(m_RecipeItem == null)
            {
                m_TextView.setTextColor(Color.BLACK);
                m_TextView.setTypeface(m_TextView.getTypeface(), Typeface.BOLD);

                m_TextViewDesc.setTypeface(null, Typeface.NORMAL);
                m_TextViewDesc.setTextColor(Color.GRAY);
                return;
            }

            String text = " (" + Helper.formatNumber(m_RecipeItem.m_Amount.m_Quantity) + " " + Amount.shortForm(context, m_RecipeItem.m_Amount.m_Unit);
            if(m_RecipeItem.m_Size != RecipeItem.Size.Normal)
            {
                text += ", " + RecipeItem.toUIString(context, m_RecipeItem.m_Size);
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

    ShoppingRecipesAdapter(CoordinatorLayout coordLayout, RecyclerView recyclerView, Ingredients ingredients, ShoppingList shoppingList)
    {
        m_iActiveElement = -1;
        m_ShoppingList = shoppingList;
        m_Ingredients = ingredients;
        m_RecyclerView = recyclerView;
        m_CoordLayout = coordLayout;
    }

    Pair<String, String> getActiveElement()
    {
        if(m_iActiveElement == -1)
        {
            return null;
        }
        return getShoppingRecipes().get(m_iActiveElement);
    }

    void setActiveElement(Pair<String, String> element)
    {
        if(m_RecyclerView.getLayoutManager() == null)
        {
            return;
        }

        if(m_iActiveElement != -1)
        {
            View v = m_RecyclerView.getLayoutManager().findViewByPosition(m_iActiveElement);
            if(v != null)
            {
                ShoppingRecipesAdapter.ViewHolder vh = (ShoppingRecipesAdapter.ViewHolder) m_RecyclerView.getChildViewHolder(v);
                updateViewHolder(vh, false);
            }
        }

        if(element == null)
        {
            m_iActiveElement = -1;
        }
        else
        {
            m_iActiveElement = getShoppingRecipes().indexOf(element);

            View child = m_RecyclerView.getLayoutManager().findViewByPosition(m_iActiveElement);
            if(child == null)
            {
                return;
            }

            ShoppingRecipesAdapter.ViewHolder vh = (ShoppingRecipesAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(child);
            updateViewHolder(vh, true);
        }
    }

    @Override @NonNull
    public ShoppingRecipesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_item_shoppinglist_recipe, parent, false);

        return new ShoppingRecipesAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingRecipesAdapter.ViewHolder holder, int position)
    {
        Pair<String, String> strItem = getShoppingRecipes().get(position);
        ShoppingList.ShoppingRecipe recipe = m_ShoppingList.getShoppingRecipe(strItem.first);
        holder.m_strRecipe = strItem.first;
        holder.m_RecipeItem = null;
        if(strItem.second != null && !strItem.second.isEmpty())
        {
            holder.m_RecipeItem = getShoppingListItem(recipe, strItem.second);
        }

        holder.updateDescription(holder.itemView.getContext());
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
            if(recipe == null)
            {
                return;
            }

            vh.m_TextViewDesc.setText(vh.itemView.getContext().getResources().getString(R.string.text_nrpersons_listvariant, Helper.formatNumber(recipe.m_fScalingFactor)));

            vh.m_TextViewDesc.setOnClickListener((View view) -> onChangeRecipeScaling(strRecipe));

            vh.m_View.setBackgroundColor(Color.TRANSPARENT);

            vh.m_ButtonAddRecipeItem.setVisibility(View.VISIBLE);
            vh.m_ButtonDeleteRecipe.setVisibility(View.VISIBLE);

            vh.m_ButtonDeleteRecipe.setOnClickListener((View view) -> onDelShoppingRecipe(strRecipe));
            vh.m_ButtonAddRecipeItem.setOnClickListener((View view) -> onAddShoppingListItem(strRecipe));

            vh.m_SpinnerAmount.setAdapter(null);
            vh.m_SpinnerSize.setAdapter(null);

            // Recipe -> Header line
            vh.m_TextView.setText(vh.m_strRecipe);
            vh.m_TextView.setOnLongClickListener((View view) ->
            {
                onRenameShoppingRecipe(strRecipe);
                return true;
            });
        }
        else
        {
            // Active ShoppingListItem

            vh.m_TextView.setText(String.format(Locale.getDefault(), "\t\u2022 %s", vh.m_RecipeItem.m_Ingredient));
            vh.m_TextView.setOnLongClickListener(null);

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

            ArrayAdapter<CharSequence> adapterAmount = new ArrayAdapter<>(vh.m_View.getContext(), R.layout.spinner_item);
            for(Amount.Unit u : Amount.Unit.values())
            {
                adapterAmount.add(Amount.toUIString(vh.itemView.getContext(), u));
            }
            adapterAmount.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            vh.m_SpinnerAmount.setAdapter(adapterAmount);
            vh.m_SpinnerAmount.setOnItemSelectedListener(this);
            vh.m_SpinnerAmount.setSelection(item.m_Amount.m_Unit.ordinal());

            ArrayAdapter<CharSequence> adapterSize = new ArrayAdapter<>(vh.m_View.getContext(), R.layout.spinner_item);
            for(RecipeItem.Size size : RecipeItem.Size.values())
            {
                adapterSize.add(RecipeItem.toUIString(vh.itemView.getContext(), size));
            }
            adapterSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            vh.m_SpinnerSize.setAdapter(adapterSize);
            vh.m_SpinnerSize.setOnItemSelectedListener(this);
            vh.m_SpinnerSize.setSelection(item.m_Size.ordinal());

            vh.m_CheckBoxOptional.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) ->
            {
                if(m_RecyclerView.getLayoutManager() == null)
                {
                    return;
                }
                View v = m_RecyclerView.getLayoutManager().findViewByPosition(m_iActiveElement);
                if(v == null)
                {
                    return;
                }
                ShoppingRecipesAdapter.ViewHolder viewHolder = (ShoppingRecipesAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(v);
                ShoppingListItem listItem = viewHolder.m_RecipeItem;
                if(listItem == null)
                {
                    return;
                }

                listItem.m_Optional = isChecked;
                viewHolder.updateDescription(viewHolder.itemView.getContext());
            });
            vh.m_CheckBoxOptional.setChecked(item.m_Optional);

            vh.m_EditTextAmount.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s)
                {
                    if(m_RecyclerView.getLayoutManager() == null)
                    {
                        return;
                    }
                    View child = m_RecyclerView.getLayoutManager().findViewByPosition(m_iActiveElement);
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

                    vh.updateDescription(vh.itemView.getContext());
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
        if(m_RecyclerView.getLayoutManager() == null)
        {
            return;
        }

        View v = m_RecyclerView.getLayoutManager().findViewByPosition(m_iActiveElement);
        if(v == null)
        {
            return;
        }
        ShoppingRecipesAdapter.ViewHolder vh = (ShoppingRecipesAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(v);
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

        vh.updateDescription(vh.itemView.getContext());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
    }

    boolean containsItem(Pair<String, String> strName)
    {
        return getShoppingRecipes().contains(strName);
    }

    public void reactToSwipe(int position)
    {
        // Remove ShoppingListItem at this position

        if(m_RecyclerView.getLayoutManager() == null)
        {
            return;
        }
        View activeItem = m_RecyclerView.getLayoutManager().findViewByPosition(position);
        if(activeItem == null)
        {
            return;
        }
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

        notifyDataSetChanged();
        setActiveElement(null);

        // Allow undo

        Snackbar snackbar = Snackbar.make(m_CoordLayout, R.string.text_item_deleted, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.text_undo, (View view) ->
        {
            if(m_RecentlyDeletedItem == null)
            {
                return;
            }
            m_RecentlyDeleted.m_Items.add(m_RecentlyDeletedIndex, m_RecentlyDeletedItem);

            notifyDataSetChanged();

            m_RecentlyDeletedIndex = -1;
            m_RecentlyDeleted = null;
            m_RecentlyDeletedItem = null;

            Snackbar currentSnackbar = Snackbar.make(m_CoordLayout, R.string.text_item_restored, Snackbar.LENGTH_SHORT);
            currentSnackbar.show();
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

    private ArrayList<Pair<String, String>> getShoppingRecipes()
    {
        ArrayList<Pair<String, String>> vec = new ArrayList<>();
        for(String strRecipe : m_ShoppingList.getAllShoppingRecipes())
        {
            vec.add(new Pair<>(strRecipe, ""));
            ShoppingList.ShoppingRecipe recipe = m_ShoppingList.getShoppingRecipe(strRecipe);
            for(ShoppingListItem item : recipe.m_Items)
            {
                vec.add(new Pair<>(strRecipe, item.m_Ingredient));
            }
        }
        return vec;
    }

    private ShoppingListItem getShoppingListItem(ShoppingList.ShoppingRecipe recipe, String strName)
    {
        for(ShoppingListItem r : recipe.m_Items)
        {
            if(r.m_Ingredient.equals(strName))
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
            vh.m_EditTextAmount.setText(String.format(Locale.getDefault(), "%s", Helper.formatNumber(item.m_Amount.m_Quantity)));
            vh.m_EditTextAmount.setVisibility(View.VISIBLE);
        }
    }

    private void onAddShoppingListItem(final String strRecipe)
    {
        if(m_RecyclerView.getLayoutManager() == null)
        {
            return;
        }

        int index = getShoppingRecipes().indexOf(new Pair<>(strRecipe, ""));
        View v = m_RecyclerView.getLayoutManager().findViewByPosition(index);
        if(v == null)
        {
            return;
        }
        ShoppingList.ShoppingRecipe recipe = m_ShoppingList.getShoppingRecipe(strRecipe);

        ArrayList<String> inputList = new ArrayList<>();
        for(String strName : m_Ingredients.getAllIngredients())
        {
            if(getShoppingListItem(recipe, strName) != null)
            {
                continue;
            }
            inputList.add(strName);
        }

        InputStringDialogFragment newFragment = InputStringDialogFragment.newInstance(v.getContext().getResources().getString(R.string.text_add_ingredient));
        newFragment.setDefaultValue(strRecipe);
        newFragment.setAdditionalInformation(strRecipe);
        newFragment.setListOnlyAllowed(inputList);
        newFragment.show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), "addRecipeItem");
    }

    private void onDelShoppingRecipe(final String strRecipe)
    {
        if(m_RecyclerView.getLayoutManager() == null)
        {
            return;
        }

        int index = getShoppingRecipes().indexOf(new Pair<>(strRecipe, ""));
        View v = m_RecyclerView.getLayoutManager().findViewByPosition(index);
        if(v == null)
        {
            return;
        }
        ShoppingRecipesAdapter.ViewHolder holder = (ShoppingRecipesAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(v);

        if(holder.m_RecipeItem != null)
        {
            return;
        }

        m_RecentlyDeleted = m_ShoppingList.getShoppingRecipe(holder.m_strRecipe);
        m_RecentlyDeletedIndex = -1;
        m_RecentlyDeletedItem = null;
        m_ShoppingList.removeShoppingRecipe(holder.m_strRecipe);

        notifyDataSetChanged();
        setActiveElement(null);

        // Allow undo

        Snackbar snackbar = Snackbar.make(m_CoordLayout, R.string.text_item_deleted, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.text_undo, (View view) ->
        {
            if(m_RecentlyDeletedItem != null)
            {
                return;
            }
            m_ShoppingList.addExistingShoppingRecipe(strRecipe, m_RecentlyDeleted);

            notifyDataSetChanged();

            m_RecentlyDeletedIndex = -1;
            m_RecentlyDeleted = null;
            m_RecentlyDeletedItem = null;

            Snackbar snackbar1 = Snackbar.make(m_CoordLayout, R.string.text_item_restored, Snackbar.LENGTH_SHORT);
            snackbar1.show();
        });
        snackbar.show();
    }

    private void onRenameShoppingRecipe(final String strRecipe)
    {
        InputStringDialogFragment newFragment = InputStringDialogFragment.newInstance(m_RecyclerView.getContext().getResources().getString(R.string.text_rename_recipe, strRecipe));
        newFragment.setDefaultValue(strRecipe);
        newFragment.setAdditionalInformation(strRecipe);
        newFragment.setListExcludedInputs(m_ShoppingList.getAllShoppingRecipes());
        newFragment.show(((AppCompatActivity) m_RecyclerView.getContext()).getSupportFragmentManager(), "renameShoppingRecipe");
    }

    private void onChangeRecipeScaling(final String strRecipe)
    {
        if(m_RecyclerView.getLayoutManager() == null)
        {
            return;
        }

        int index = getShoppingRecipes().indexOf(new Pair<>(strRecipe, ""));
        View v = m_RecyclerView.getLayoutManager().findViewByPosition(index);
        if(v == null)
        {
            return;
        }
        ShoppingList.ShoppingRecipe recipe = m_ShoppingList.getShoppingRecipe(strRecipe);

        InputStringDialogFragment newFragment = InputStringDialogFragment.newInstance(v.getContext().getResources().getString(R.string.text_change_nrpersons, strRecipe));
        newFragment.setAdditionalInformation(strRecipe);
        newFragment.setDefaultValue(recipe.m_fScalingFactor.toString());
        newFragment.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        newFragment.show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), "changeRecipeScaling");
    }

    public void clearViewBackground(RecyclerView.ViewHolder vh)
    {
        if(m_iActiveElement == -1)
        {
            vh.itemView.setBackgroundColor(0);
            return;
        }

        if(m_RecyclerView.getLayoutManager() == null)
        {
            return;
        }
        View v = m_RecyclerView.getLayoutManager().findViewByPosition(m_iActiveElement);

        if(v != null && m_RecyclerView.getChildViewHolder(v) == vh)
        {
            vh.itemView.setBackgroundColor(vh.itemView.getResources().getColor(R.color.colorHighlightedBackground));
        }
        else
        {
            vh.itemView.setBackgroundColor(0);
        }
    }
}
