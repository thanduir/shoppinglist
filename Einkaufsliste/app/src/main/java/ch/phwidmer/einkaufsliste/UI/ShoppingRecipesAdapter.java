package ch.phwidmer.einkaufsliste.UI;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;

import ch.phwidmer.einkaufsliste.data.Amount;
import ch.phwidmer.einkaufsliste.data.Unit;
import ch.phwidmer.einkaufsliste.helper.Helper;
import ch.phwidmer.einkaufsliste.R;
import ch.phwidmer.einkaufsliste.data.Ingredients;
import ch.phwidmer.einkaufsliste.data.RecipeItem;
import ch.phwidmer.einkaufsliste.data.ShoppingList;
import ch.phwidmer.einkaufsliste.data.ShoppingListItem;
import ch.phwidmer.einkaufsliste.helper.ReactToTouchActionsInterface;
import ch.phwidmer.einkaufsliste.helper.stringInput.InputStringFree;
import ch.phwidmer.einkaufsliste.helper.stringInput.InputStringFromListMultiSelect;

public class ShoppingRecipesAdapter extends RecyclerView.Adapter<ShoppingRecipesAdapter.ViewHolder> implements ReactToTouchActionsInterface, AdapterView.OnItemSelectedListener
{
    private static final int TYPE_INACTIVE = 1;
    private static final int TYPE_ACTIVE = 2;
    private static final int TYPE_HEADER = 3;

    private ShoppingList m_ShoppingList;
    private Ingredients m_Ingredients;
    private Integer m_iActiveElement;
    private RecyclerView m_RecyclerView;
    private CoordinatorLayout m_CoordLayout;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView m_TextView;
        View m_View;

        String m_strRecipe;
        ShoppingListItem m_RecipeItem = null;

        public ViewHolder(@NonNull View v)
        {
            super(v);
            m_View = v;
            m_TextView = v.findViewById(R.id.textView);
        }

        Pair<String, String> getID()
        {
            String strItem = m_RecipeItem != null ? m_RecipeItem.getIngredient() : "";
            return new Pair<>(m_strRecipe, strItem);
        }
    }

    public static class ViewHolderHeader extends ShoppingRecipesAdapter.ViewHolder
    {
        private TextView m_TextViewDesc;

        private ImageView m_ButtonAddRecipeItem;
        private ImageView m_ButtonDeleteRecipe;

        ViewHolderHeader(@NonNull View v)
        {
            super(v);
            m_TextViewDesc = v.findViewById(R.id.textViewDesc);
            m_ButtonAddRecipeItem = v.findViewById(R.id.button_addRecipeItem);
            m_ButtonDeleteRecipe = v.findViewById(R.id.button_deleteRecipe);
        }
    }

    public static class ViewHolderActive extends ShoppingRecipesAdapter.ViewHolder
    {
        private CheckBox m_CheckBoxOptional;
        private Spinner  m_SpinnerAmount;
        private EditText m_EditTextAmount;
        private Spinner  m_SpinnerSize;
        private EditText m_AdditionalInfo;
        private TableRow m_TableRowAmount;
        private CheckBox    m_CheckBoxAmountRange;
        private TableRow    m_TableRowAmountRange;
        private EditText    m_EditTextAmountMax;
        private TextView    m_TextViewAmountMin;

        ViewHolderActive(@NonNull View v)
        {
            super(v);
            m_SpinnerAmount = v.findViewById(R.id.spinnerAmount);
            m_EditTextAmount = v.findViewById(R.id.editText_Amount);
            m_SpinnerSize = v.findViewById(R.id.spinnerSize);
            m_CheckBoxOptional = v.findViewById(R.id.checkBoxOptional);
            m_AdditionalInfo = v.findViewById(R.id.editText_AdditonalInfo);
            m_TableRowAmount = v.findViewById(R.id.tableRowAmount);
            m_CheckBoxAmountRange = v.findViewById(R.id.checkBoxAmountRange);
            m_TableRowAmountRange = v.findViewById(R.id.tableRowAmountRange);
            m_EditTextAmountMax = v.findViewById(R.id.editText_AmountMax);
            m_TextViewAmountMin = v.findViewById(R.id.textViewMinAmount);
        }

        private void updateAppearance()
        {
            if(m_RecipeItem.isOptional())
            {
                m_TextView.setTextColor(Color.GRAY);
                m_TextView.setTypeface(m_TextView.getTypeface(), Typeface.ITALIC);
            }
            else
            {
                m_TextView.setTextColor(Color.BLACK);
                m_TextView.setTypeface(null, Typeface.NORMAL);
            }
        }
    }

    public static class ViewHolderInactive extends ShoppingRecipesAdapter.ViewHolder
    {
        private TextView m_TextViewDesc;

        ViewHolderInactive(@NonNull View v)
        {
            super(v);
            m_TextViewDesc = v.findViewById(R.id.textViewDesc);
        }

        private void updateDescription(@NonNull Context context)
        {
            String text = "";
            if(m_RecipeItem.getAmount().getUnit() != Unit.Unitless)
            {
                text += Helper.formatNumber(m_RecipeItem.getAmount().getQuantityMin());
                if(m_RecipeItem.getAmount().isRange())
                {
                    text += "-" + Helper.formatNumber(m_RecipeItem.getAmount().getQuantityMax());
                }
                text += " " + Unit.shortForm(context, m_RecipeItem.getAmount().getUnit());
            }
            if(m_RecipeItem.getSize() != RecipeItem.Size.Normal)
            {
                if(!text.isEmpty())
                {
                    text += ", ";
                }
                text += RecipeItem.Size.toUIString(context, m_RecipeItem.getSize());
            }
            if(!m_RecipeItem.getAdditionalInfo().isEmpty())
            {
                if(!text.isEmpty())
                {
                    text += ", ";
                }
                text += m_RecipeItem.getAdditionalInfo();
            }

            String fullText = "";
            if(!text.isEmpty() )
            {
                fullText = " (" + text + ")";
            }
            m_TextViewDesc.setText(fullText);
        }
    }

    ShoppingRecipesAdapter(@NonNull CoordinatorLayout coordLayout, @NonNull RecyclerView recyclerView, @NonNull Ingredients ingredients, @NonNull ShoppingList shoppingList)
    {
        m_iActiveElement = -1;
        m_ShoppingList = shoppingList;
        m_Ingredients = ingredients;
        m_RecyclerView = recyclerView;
        m_CoordLayout = coordLayout;
    }

    @Override
    public int getItemViewType(int position)
    {
        Pair<String, String> strItem = getShoppingRecipes().get(position);
        if(strItem.second == null || strItem.second.isEmpty())
        {
            return TYPE_HEADER;
        }
        else if(m_iActiveElement == position)
        {
            return TYPE_ACTIVE;
        }
        else
        {
            return TYPE_INACTIVE;
        }
    }

    @Override @NonNull
    public ShoppingRecipesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                int viewType)
    {
        if(viewType == TYPE_INACTIVE)
        {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_item_shoppinglist_recipe_inactive, parent, false);

            return new ShoppingRecipesAdapter.ViewHolderInactive(v);
        }
        else if(viewType == TYPE_ACTIVE)
        {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_item_shoppinglist_recipe_active, parent, false);

            return new ShoppingRecipesAdapter.ViewHolderActive(v);
        }
        else
        {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_item_shoppinglist_recipe_header, parent, false);

            return new ShoppingRecipesAdapter.ViewHolderHeader(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingRecipesAdapter.ViewHolder holder, int position)
    {
        Pair<String, String> strItem = getShoppingRecipes().get(position);
        if(strItem.first == null)
        {
            return;
        }
        Optional<ShoppingList.ShoppingRecipe> recipe = m_ShoppingList.getShoppingRecipe(strItem.first);
        if(!recipe.isPresent())
        {
            return;
        }
        holder.m_strRecipe = strItem.first;
        holder.m_RecipeItem = null;
        if(strItem.second != null && !strItem.second.isEmpty())
        {
            Optional<ShoppingListItem> item = getShoppingListItem(recipe.get(), strItem.second);
            if(!item.isPresent())
            {
                return;
            }
            holder.m_RecipeItem = item.get();

            if(m_iActiveElement == position)
            {
                updateViewHolderActive(holder);
            }
            else
            {
                updateViewHolderInactive(holder);
            }
        }
        else
        {
            updateViewHolderHeader(holder);
        }
    }

    Optional<Pair<String, String>> getActiveElement()
    {
        if(m_iActiveElement == -1)
        {
            return Optional.empty();
        }
        return Optional.of(getShoppingRecipes().get(m_iActiveElement));
    }

    void setActiveElement(Pair<String, String> element)
    {
        if(m_RecyclerView.getLayoutManager() == null)
        {
            return;
        }

        if(m_iActiveElement != -1)
        {
            notifyItemChanged(m_iActiveElement);
        }

        if(element == null)
        {
            m_iActiveElement = -1;
        }
        else
        {
            m_iActiveElement = getShoppingRecipes().indexOf(element);
            notifyItemChanged(m_iActiveElement);
        }
    }

    private void updateViewHolderHeader(@NonNull ShoppingRecipesAdapter.ViewHolder holder)
    {
        ShoppingRecipesAdapter.ViewHolderHeader vh = (ShoppingRecipesAdapter.ViewHolderHeader)holder;

        final String strRecipe = vh.m_strRecipe;

        Optional<ShoppingList.ShoppingRecipe> recipe = m_ShoppingList.getShoppingRecipe(strRecipe);
        if(!recipe.isPresent())
        {
            return;
        }

        vh.m_TextView.setTextColor(Color.BLACK);
        vh.m_TextView.setTypeface(vh.m_TextView.getTypeface(), Typeface.BOLD);

        vh.m_TextViewDesc.setTypeface(null, Typeface.NORMAL);
        vh.m_TextViewDesc.setTextColor(Color.GRAY);

        vh.m_TextViewDesc.setText(vh.itemView.getContext().getResources().getString(R.string.text_nrpersons_listvariant, Helper.formatNumber(recipe.get().getScalingFactor())));

        vh.m_TextViewDesc.setOnClickListener((View view) -> onChangeRecipeScaling(strRecipe));

        vh.m_ButtonDeleteRecipe.setOnClickListener((View view) -> onDelShoppingRecipe(strRecipe));
        vh.m_ButtonAddRecipeItem.setOnClickListener((View view) -> onAddShoppingListItem(strRecipe));

        // Recipe -> Header line
        vh.m_TextView.setText(vh.m_strRecipe);
        vh.m_TextView.setOnLongClickListener((View view) ->
        {
            onRenameShoppingRecipe(strRecipe);
            return true;
        });
    }

    private void updateViewHolderInactive(@NonNull ShoppingRecipesAdapter.ViewHolder holder)
    {
        ShoppingRecipesAdapter.ViewHolderInactive vh = (ShoppingRecipesAdapter.ViewHolderInactive)holder;

        vh.updateDescription(holder.itemView.getContext());

        if(vh.m_RecipeItem.isOptional())
        {
            vh.m_TextView.setTextColor(Color.GRAY);
            vh.m_TextView.setTypeface(vh.m_TextView.getTypeface(), Typeface.ITALIC);

            vh.m_TextViewDesc.setTextColor(Color.GRAY);
            vh.m_TextViewDesc.setTypeface(vh.m_TextViewDesc.getTypeface(), Typeface.ITALIC);
        }
        else
        {
            vh.m_TextView.setTextColor(Color.BLACK);
            vh.m_TextView.setTypeface(null, Typeface.NORMAL);

            vh.m_TextViewDesc.setTypeface(null, Typeface.NORMAL);
            vh.m_TextViewDesc.setTextColor(Color.BLACK);
        }

        vh.m_TextView.setText(String.format(Locale.getDefault(), "\t\u2022 %s", vh.m_RecipeItem.getIngredient()));
        vh.m_TextViewDesc.setClickable(false);

        vh.m_View.setBackgroundColor(Color.TRANSPARENT);
    }

    private void updateViewHolderActive(@NonNull ShoppingRecipesAdapter.ViewHolder holder)
    {
        ShoppingRecipesAdapter.ViewHolderActive vh = (ShoppingRecipesAdapter.ViewHolderActive)holder;

        vh.updateAppearance();

        vh.m_TextView.setText(String.format(Locale.getDefault(), "\t\u2022 %s", vh.m_RecipeItem.getIngredient()));
        vh.m_TextView.setOnLongClickListener(null);

        vh.m_View.setBackgroundColor(ContextCompat.getColor(vh.m_View.getContext(), R.color.colorHighlightedBackground));

        ShoppingListItem item = vh.m_RecipeItem;

        ArrayAdapter<CharSequence> adapterAmount = new ArrayAdapter<>(vh.m_View.getContext(), R.layout.spinner_item);
        for(Unit u : Unit.values())
        {
            adapterAmount.add(Unit.toUIString(vh.itemView.getContext(), u));
        }
        adapterAmount.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vh.m_SpinnerAmount.setAdapter(adapterAmount);
        vh.m_SpinnerAmount.setOnItemSelectedListener(this);
        vh.m_SpinnerAmount.setSelection(item.getAmount().getUnit().ordinal());

        ArrayAdapter<CharSequence> adapterSize = new ArrayAdapter<>(vh.m_View.getContext(), R.layout.spinner_item);
        for(RecipeItem.Size size : RecipeItem.Size.values())
        {
            adapterSize.add(RecipeItem.Size.toUIString(vh.itemView.getContext(), size));
        }
        adapterSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vh.m_SpinnerSize.setAdapter(adapterSize);
        vh.m_SpinnerSize.setOnItemSelectedListener(this);
        vh.m_SpinnerSize.setSelection(item.getSize().ordinal());

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
            ShoppingRecipesAdapter.ViewHolderActive viewHolder = (ShoppingRecipesAdapter.ViewHolderActive)m_RecyclerView.getChildViewHolder(v);
            ShoppingListItem listItem = viewHolder.m_RecipeItem;
            if(listItem == null)
            {
                return;
            }

            listItem.setIsOptional(isChecked);
            viewHolder.updateAppearance();
        });
        vh.m_CheckBoxOptional.setChecked(item.isOptional());

        vh.m_AdditionalInfo.setText(item.getAdditionalInfo());
        vh.m_AdditionalInfo.addTextChangedListener(new TextWatcher() {

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

                ShoppingRecipesAdapter.ViewHolderActive vh = (ShoppingRecipesAdapter.ViewHolderActive)m_RecyclerView.getChildViewHolder(child);
                ShoppingListItem item = vh.m_RecipeItem;
                if(item == null)
                {
                    return;
                }
                item.setAdditionInfo(s.toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

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

                Amount amount = item.getAmount();
                if(s.toString().isEmpty())
                {
                    amount.setQuantityMin(0.0f);
                }
                else
                {
                    amount.setQuantityMin(Float.valueOf(s.toString()));
                }
                item.setAmount(amount);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        vh.m_EditTextAmountMax.addTextChangedListener(new TextWatcher() {

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
                if(item == null || !item.getAmount().isRange())
                {
                    return;
                }

                Amount amount = item.getAmount();
                if(s.toString().isEmpty())
                {
                    amount.setQuantityMax(0.0f);
                }
                else
                {
                    amount.setQuantityMax(Float.valueOf(s.toString()));
                }
                item.setAmount(amount);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        vh.m_CheckBoxAmountRange.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) ->
        {
            if(m_RecyclerView.getLayoutManager() == null)
            {
                return;
            }

            View child = m_RecyclerView.getLayoutManager().findViewByPosition(m_iActiveElement);
            if(child == null)
            {
                return;
            }
            ShoppingRecipesAdapter.ViewHolder viewHolder = (ShoppingRecipesAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(child);
            ShoppingListItem recipeItem = viewHolder.m_RecipeItem;
            if(recipeItem == null)
            {
                return;
            }

            vh.m_TableRowAmountRange.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            Amount amount = recipeItem.getAmount();
            amount.setIsRange(isChecked);
            recipeItem.setAmount(amount);

            adjustEditTextAmount(vh, item);
        });
        vh.m_CheckBoxAmountRange.setChecked(item.getAmount().isRange());

        adjustEditTextAmount(vh, item);
    }

    void onChangeAmount(boolean bChangeMax, boolean bIncrease)
    {
        if(m_RecyclerView.getLayoutManager() == null)
        {
            return;
        }
        View activeItem = m_RecyclerView.getLayoutManager().findViewByPosition(m_iActiveElement);
        if(activeItem == null)
        {
            return;
        }
        ShoppingRecipesAdapter.ViewHolderActive holder = (ShoppingRecipesAdapter.ViewHolderActive)m_RecyclerView.getChildViewHolder(activeItem);
        if(holder.m_RecipeItem == null)
        {
            return;
        }
        ShoppingListItem item = holder.m_RecipeItem;

        Amount amount = item.getAmount();
        if(bIncrease)
        {
            if(bChangeMax)
            {
                amount.increaseAmountMax();
            }
            else
            {
                amount.increaseAmountMin();
            }
        }
        else
        {
            if(bChangeMax)
            {
                amount.decreaseAmountMax();
            }
            else
            {
                amount.decreaseAmountMin();
            }
        }
        item.setAmount(amount);

        if(bChangeMax)
        {
            holder.m_EditTextAmountMax.setText(String.format(Locale.getDefault(), "%s", Helper.formatNumber(item.getAmount().getQuantityMax())));
        }
        else
        {
            holder.m_EditTextAmount.setText(String.format(Locale.getDefault(), "%s", Helper.formatNumber(item.getAmount().getQuantityMin())));
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
        ShoppingRecipesAdapter.ViewHolderActive vh = (ShoppingRecipesAdapter.ViewHolderActive)m_RecyclerView.getChildViewHolder(v);
        ShoppingListItem item = vh.m_RecipeItem;

        if(parent == vh.m_SpinnerAmount)
        {
            Amount amount = item.getAmount();
            amount.setUnit(Unit.values()[vh.m_SpinnerAmount.getSelectedItemPosition()]);
            item.setAmount(amount);
            adjustEditTextAmount(vh, item);
        }
        else if(parent == vh.m_SpinnerSize)
        {
            item.setSize(RecipeItem.Size.values()[vh.m_SpinnerSize.getSelectedItemPosition()]);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
    }

    boolean containsItem(@NonNull Pair<String, String> strName)
    {
        return getShoppingRecipes().contains(strName);
    }

    @Override
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

        Optional<ShoppingList.ShoppingRecipe> recipe = m_ShoppingList.getShoppingRecipe(holder.m_strRecipe);
        if(!recipe.isPresent())
        {
            return;
        }

        final UndoData.ShoppingListItemUndoData recentlyDeletedItem = new UndoData.ShoppingListItemUndoData(holder.m_RecipeItem);
        recipe.get().removeItem(holder.m_RecipeItem);

        notifyDataSetChanged();
        setActiveElement(null);

        // Allow undo

        Snackbar snackbar = Snackbar.make(m_CoordLayout, R.string.text_item_deleted, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.text_undo, (View view) ->
        {
            Optional<ShoppingListItem> newItem = recipe.get().addItem(recentlyDeletedItem.getIngredient());
            if(!newItem.isPresent())
            {
                return;
            }
            recentlyDeletedItem.initializeItem(newItem.get());

            notifyDataSetChanged();

            Snackbar currentSnackbar = Snackbar.make(m_CoordLayout, R.string.text_item_restored, Snackbar.LENGTH_SHORT);
            currentSnackbar.show();
        });
        snackbar.show();
    }

    @Override
    public boolean swipeAllowed(RecyclerView.ViewHolder vh)
    {
        ShoppingRecipesAdapter.ViewHolder holder = (ShoppingRecipesAdapter.ViewHolder)vh;
        return holder.m_RecipeItem != null;
    }

    @Override
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
        for(ShoppingList.ShoppingRecipe recipe : m_ShoppingList.getAllShoppingRecipes())
        {
            vec.add(new Pair<>(recipe.getName(), ""));
            for(ShoppingListItem item : recipe.getAllItems())
            {
                vec.add(new Pair<>(recipe.getName(), item.getIngredient()));
            }
        }
        return vec;
    }

    private Optional<ShoppingListItem> getShoppingListItem(@NonNull ShoppingList.ShoppingRecipe recipe, @NonNull String strName)
    {
        for(ShoppingListItem r : recipe.getAllItems())
        {
            if(r.getIngredient().equals(strName))
            {
                return Optional.of(r);
            }
        }
        return Optional.empty();
    }

    private void adjustEditTextAmount(@NonNull ShoppingRecipesAdapter.ViewHolderActive vh, @NonNull ShoppingListItem item)
    {
        if(item.getAmount().getUnit() == Unit.Unitless)
        {
            vh.m_EditTextAmount.setText("");
            vh.m_EditTextAmountMax.setText("");
            vh.m_CheckBoxAmountRange.setChecked(false);
            vh.m_CheckBoxAmountRange.setVisibility(View.GONE);
            vh.m_TableRowAmount.setVisibility(View.GONE);
            vh.m_TableRowAmountRange.setVisibility(View.GONE);
        }
        else
        {
            vh.m_EditTextAmount.setText(String.format(Locale.getDefault(), "%s", Helper.formatNumber(item.getAmount().getQuantityMin())));
            vh.m_CheckBoxAmountRange.setVisibility(View.VISIBLE);
            vh.m_TableRowAmount.setVisibility(View.VISIBLE);

            if(item.getAmount().isRange())
            {
                vh.m_EditTextAmountMax.setText(String.format(Locale.getDefault(), "%s", Helper.formatNumber(item.getAmount().getQuantityMax())));
                vh.m_TableRowAmountRange.setVisibility(View.VISIBLE);
                vh.m_TextViewAmountMin.setVisibility(View.VISIBLE);
            }
            else
            {
                vh.m_EditTextAmountMax.setText("");
                vh.m_TableRowAmountRange.setVisibility(View.GONE);
                vh.m_TextViewAmountMin.setVisibility(View.GONE);
            }
        }
    }

    private void onAddShoppingListItem(@NonNull final String strRecipe)
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
        Optional<ShoppingList.ShoppingRecipe> recipe = m_ShoppingList.getShoppingRecipe(strRecipe);
        if(!recipe.isPresent())
        {
            return;
        }

        ArrayList<String> inputList = new ArrayList<>();
        for(Ingredients.Ingredient ingredient : m_Ingredients.getAllIngredients())
        {
            if(getShoppingListItem(recipe.get(), ingredient.getName()).isPresent())
            {
                continue;
            }
            inputList.add(ingredient.getName());
        }

        InputStringFromListMultiSelect newFragment = InputStringFromListMultiSelect.newInstance(v.getContext().getResources().getString(R.string.text_add_ingredient), inputList, strRecipe, InputStringFromListMultiSelect.SelectionType.MultiSelectDifferentElements);
        newFragment.show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), "addRecipeItem");
    }

    private void onDelShoppingRecipe(@NonNull final String strRecipe)
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

        Optional<ShoppingList.ShoppingRecipe> recipeToDelete = m_ShoppingList.getShoppingRecipe(holder.m_strRecipe);
        if(!recipeToDelete.isPresent())
        {
            return;
        }
        final UndoData.ShoppingRecipeUndoData recentlyDeletedRecipe = new UndoData.ShoppingRecipeUndoData(recipeToDelete.get());
        m_ShoppingList.removeShoppingRecipe(recipeToDelete.get());

        notifyDataSetChanged();
        setActiveElement(null);

        // Allow undo

        Snackbar snackbar = Snackbar.make(m_CoordLayout, R.string.text_item_deleted, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.text_undo, (View view) ->
        {
            Optional<ShoppingList.ShoppingRecipe> newRecipe = m_ShoppingList.addNewRecipe(recentlyDeletedRecipe.getName());
            if(!newRecipe.isPresent())
            {
                return;
            }
            recentlyDeletedRecipe.initializeRecipe(newRecipe.get());

            notifyDataSetChanged();

            Snackbar snackbar1 = Snackbar.make(m_CoordLayout, R.string.text_item_restored, Snackbar.LENGTH_SHORT);
            snackbar1.show();
        });
        snackbar.show();
    }

    private void onRenameShoppingRecipe(@NonNull final String strRecipe)
    {
        InputStringFree newFragment = InputStringFree.newInstance(m_RecyclerView.getContext().getResources().getString(R.string.text_rename_recipe, strRecipe));
        newFragment.setDefaultValue(strRecipe);
        newFragment.setAdditionalInformation(strRecipe);
        newFragment.setListExcludedInputs(m_ShoppingList.getAllShoppingRecipeNames());
        newFragment.show(((AppCompatActivity) m_RecyclerView.getContext()).getSupportFragmentManager(), "renameShoppingRecipe");
    }

    private void onChangeRecipeScaling(@NonNull final String strRecipe)
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
        Optional<ShoppingList.ShoppingRecipe> recipe = m_ShoppingList.getShoppingRecipe(strRecipe);
        if(!recipe.isPresent())
        {
            return;
        }

        InputStringFree newFragment = InputStringFree.newInstance(v.getContext().getResources().getString(R.string.text_change_nrpersons, strRecipe));
        newFragment.setAdditionalInformation(strRecipe);
        // Convert float to string without trailing zeros
        float fFactor = recipe.get().getScalingFactor();
        int i = (int) fFactor;
        String strFactor = fFactor == i ? String.valueOf(i) : String.valueOf(fFactor);
        newFragment.setDefaultValue(strFactor);
        newFragment.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        newFragment.show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), "changeRecipeScaling");
    }

    @Override
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
            vh.itemView.setBackgroundColor(ContextCompat.getColor(vh.itemView.getContext(), R.color.colorHighlightedBackground));
        }
        else
        {
            vh.itemView.setBackgroundColor(0);
        }
    }
}
