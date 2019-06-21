package ch.phwidmer.einkaufsliste;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class RecipeItemsAdapter extends RecyclerView.Adapter<RecipeItemsAdapter.ViewHolder> implements ReactToTouchActionsInterface, AdapterView.OnItemSelectedListener
{
    private static final int TYPE_INACTIVE = 1;
    private static final int TYPE_ACTIVE = 2;

    private Recipes.Recipe m_Recipe;
    private RecyclerView m_RecyclerView;
    private CoordinatorLayout m_CoordLayout;
    private Integer m_iActiveElement;

    private RecipeItem m_RecentlyDeleted;
    private int m_RecentlyDeletedIndex;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView    m_TextView;
        View        m_View;
        String      m_id;

        public ViewHolder(View v)
        {
            super(v);
            m_View = v;
            m_TextView = v.findViewById(R.id.textView);

            m_id = "";
        }

        String getID()
        {
            return m_id;
        }
    }

    public static class ViewHolderInactive extends RecipeItemsAdapter.ViewHolder
    {
        private TextView    m_TextViewDesc;

        ViewHolderInactive(View v)
        {
            super(v);
            m_TextViewDesc = v.findViewById(R.id.textViewDesc);
        }

        String getID()
        {
            return m_id;
        }

        void setDescription(Context context, RecipeItem item)
        {
            String text = "";
            if(item.m_Amount.m_Unit != Amount.Unit.Unitless)
            {
                text += Helper.formatNumber(item.m_Amount.m_QuantityMin);
                if(item.m_Amount.isRange())
                {
                    text += "-" + Helper.formatNumber(item.m_Amount.m_QuantityMax);
                }
                text += " " + Amount.shortForm(context, item.m_Amount.m_Unit);
            }
            if(item.m_Size != RecipeItem.Size.Normal)
            {
                if(!text.isEmpty())
                {
                    text += ", ";
                }
                text += RecipeItem.toUIString(context, item.m_Size);
            }
            if(!item.m_AdditionalInfo.isEmpty())
            {
                if(!text.isEmpty())
                {
                    text += ", ";
                }
                text += item.m_AdditionalInfo;
            }

            String fullText = "";
            if(!text.isEmpty() )
            {
                fullText = " (" + text + ")";
            }
            m_TextViewDesc.setText(fullText);
        }
    }

    public static class ViewHolderActive extends RecipeItemsAdapter.ViewHolder
    {
        private CheckBox    m_CheckBoxOptional;
        private Spinner     m_SpinnerAmount;
        private EditText    m_EditTextAmount;
        private Spinner     m_SpinnerSize;
        private EditText    m_AdditionalInfo;
        private TableRow    m_TableRowAmount;
        private CheckBox    m_CheckBoxAmountRange;
        private TableRow    m_TableRowAmountRange;
        private EditText    m_EditTextAmountMax;
        private TextView    m_TextViewAmountMin;

        ViewHolderActive(View v)
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

        String getID()
        {
            return m_id;
        }

        void updateAppearance(RecipeItem item)
        {
            if(item.m_Optional)
            {
                m_TextView.setTextColor(Color.GRAY);
            }
            else
            {
                m_TextView.setTextColor(Color.BLACK);
                m_TextView.setTypeface(m_TextView.getTypeface(), Typeface.BOLD);
            }
        }
    }

    RecipeItemsAdapter(CoordinatorLayout coordLayout, RecyclerView recyclerView, Recipes.Recipe recipe)
    {
        m_iActiveElement = -1;
        m_RecyclerView = recyclerView;
        m_Recipe = recipe;
        m_CoordLayout = coordLayout;
    }

    @Override
    public int getItemViewType(int position)
    {
        if(m_iActiveElement == position)
        {
            return TYPE_ACTIVE;
        }
        else
        {
            return TYPE_INACTIVE;
        }
    }

    @Override @NonNull
    public RecipeItemsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                            int viewType)
    {
        if(viewType == TYPE_INACTIVE)
        {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_item_edit_recipieitem_inactive, parent, false);

            return new RecipeItemsAdapter.ViewHolderInactive(v);
        }
        else
        {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_item_edit_recipieitem_active, parent, false);

            return new RecipeItemsAdapter.ViewHolderActive(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeItemsAdapter.ViewHolder holder, int position)
    {
        String strRecipeItem = getRecipeItemsList().get(position);
        holder.m_id = strRecipeItem;
        holder.m_TextView.setText(strRecipeItem);

        if(m_iActiveElement == position)
        {
            updateViewHolderActive(holder);
        }
        else
        {
            updateViewHolderInactive(holder);
        }
    }

    @Override
    public int getItemCount()
    {
        return getRecipeItemsList().size();
    }

    String getActiveElement()
    {
        if(m_iActiveElement == -1)
        {
            return "";
        }
        return getRecipeItemsList().get(m_iActiveElement);
    }

    void setActiveElement(String strElement)
    {
        if(m_iActiveElement != -1)
        {
            notifyItemChanged(m_iActiveElement);
        }

        if(strElement.equals(""))
        {
            m_iActiveElement = -1;
        }
        else
        {
            m_iActiveElement = getRecipeItemsList().indexOf(strElement);
            notifyItemChanged(m_iActiveElement);
        }
    }

    private void updateViewHolderInactive(RecipeItemsAdapter.ViewHolder holder)
    {
        RecipeItemsAdapter.ViewHolderInactive vh = (RecipeItemsAdapter.ViewHolderInactive)holder;
        if(vh == null)
        {
            return;
        }

        RecipeItem item = getRecipeItem(vh.m_id);
        if(item == null)
        {
            return;
        }
        vh.setDescription(holder.itemView.getContext(), item);

        if(item.m_Optional)
        {
            vh.m_TextView.setTextColor(Color.GRAY);
            vh.m_TextView.setTypeface(vh.m_TextView.getTypeface(), Typeface.ITALIC);

            vh.m_TextViewDesc.setTextColor(Color.GRAY);
            vh.m_TextViewDesc.setTypeface(vh.m_TextViewDesc.getTypeface(), Typeface.ITALIC);
        }
        else
        {
            vh.m_TextView.setTextColor(Color.BLACK);
            vh.m_TextView.setTypeface(vh.m_TextView.getTypeface(), Typeface.BOLD);

            vh.m_TextViewDesc.setTypeface(null, Typeface.NORMAL);
            vh.m_TextViewDesc.setTextColor(Color.BLACK);
        }

        vh.m_TextViewDesc.setVisibility(View.VISIBLE);
        vh.m_View.setBackgroundColor(Color.TRANSPARENT);
    }

    private void updateViewHolderActive(RecipeItemsAdapter.ViewHolder holder)
    {
        RecipeItemsAdapter.ViewHolderActive vh = (RecipeItemsAdapter.ViewHolderActive)holder;
        if(vh == null)
        {
            return;
        }

        RecipeItem item = getRecipeItem(vh.m_id);
        if(item == null)
        {
            return;
        }

        vh.updateAppearance(item);
        vh.m_View.setBackgroundColor(ContextCompat.getColor(vh.m_View.getContext(), R.color.colorHighlightedBackground));

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

        if(m_RecyclerView.getLayoutManager() == null)
        {
            return;
        }

        vh.m_CheckBoxOptional.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) ->
        {
            View child = m_RecyclerView.getLayoutManager().findViewByPosition(m_iActiveElement);
            if(child == null)
            {
                return;
            }
            RecipeItemsAdapter.ViewHolder viewHolder = (RecipeItemsAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(child);
            RecipeItem recipeItem = getRecipeItem(viewHolder.getID());
            if(recipeItem == null)
            {
                return;
            }

            recipeItem.m_Optional = isChecked;
            vh.updateAppearance(recipeItem);
        });
        vh.m_CheckBoxOptional.setChecked(item.m_Optional);

        vh.m_AdditionalInfo.setText(item.m_AdditionalInfo);
        vh.m_AdditionalInfo.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                View child = m_RecyclerView.getLayoutManager().findViewByPosition(m_iActiveElement);
                if(child == null)
                {
                    // Item not visible (yet) -> nothing to do
                    return;
                }

                RecipeItemsAdapter.ViewHolder vh = (RecipeItemsAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(child);
                RecipeItem item = getRecipeItem(vh.getID());
                if(item == null)
                {
                    return;
                }
                item.m_AdditionalInfo = s.toString();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        vh.m_EditTextAmount.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                View child = m_RecyclerView.getLayoutManager().findViewByPosition(m_iActiveElement);
                if(child == null)
                {
                    // Item not visible (yet) -> nothing to do
                    return;
                }

                RecipeItemsAdapter.ViewHolder vh = (RecipeItemsAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(child);
                RecipeItem item = getRecipeItem(vh.getID());
                if(item == null)
                {
                    return;
                }
                if(s.toString().isEmpty())
                {
                    item.m_Amount.m_QuantityMin = 0.0f;
                }
                else
                {
                    item.m_Amount.m_QuantityMin = Float.valueOf(s.toString());
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        vh.m_EditTextAmountMax.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                View child = m_RecyclerView.getLayoutManager().findViewByPosition(m_iActiveElement);
                if(child == null)
                {
                    // Item not visible (yet) -> nothing to do
                    return;
                }

                RecipeItemsAdapter.ViewHolder vh = (RecipeItemsAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(child);
                RecipeItem item = getRecipeItem(vh.getID());
                if(item == null || !item.m_Amount.isRange())
                {
                    return;
                }

                if(s.toString().isEmpty())
                {
                    item.m_Amount.m_QuantityMax = 0.0f;
                }
                else
                {
                    item.m_Amount.m_QuantityMax = Float.valueOf(s.toString());
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        vh.m_CheckBoxAmountRange.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) ->
        {
            View child = m_RecyclerView.getLayoutManager().findViewByPosition(m_iActiveElement);
            if(child == null)
            {
                return;
            }
            RecipeItemsAdapter.ViewHolder viewHolder = (RecipeItemsAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(child);
            RecipeItem recipeItem = getRecipeItem(viewHolder.getID());
            if(recipeItem == null)
            {
                return;
            }

            vh.m_TableRowAmountRange.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            recipeItem.m_Amount.setIsRange(isChecked);

            adjustEditTextAmount(vh, item);
        });
        vh.m_CheckBoxAmountRange.setChecked(item.m_Amount.isRange());

        adjustEditTextAmount(vh, item);
    }

    void onChangeAmount(boolean bChangeMax, boolean bIncrease)
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
        RecipeItemsAdapter.ViewHolderActive vh = (RecipeItemsAdapter.ViewHolderActive)m_RecyclerView.getChildViewHolder(child);
        RecipeItem item = getRecipeItem(vh.getID());
        if(item == null)
        {
            return;
        }

        if(bIncrease)
        {
            if(bChangeMax)
            {
                item.m_Amount.increaseAmountMax();
            }
            else
            {
                item.m_Amount.increaseAmountMin();
            }
        }
        else
        {
            if(bChangeMax)
            {
                item.m_Amount.decreaseAmountMax();
            }
            else
            {
                item.m_Amount.decreaseAmountMin();
            }
        }

        if(bChangeMax)
        {
            vh.m_EditTextAmountMax.setText(String.format(Locale.getDefault(), "%s", Helper.formatNumber(item.m_Amount.m_QuantityMax)));
        }
        else
        {
            vh.m_EditTextAmount.setText(String.format(Locale.getDefault(), "%s", Helper.formatNumber(item.m_Amount.m_QuantityMin)));
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        if(m_iActiveElement == -1 || m_RecyclerView.getLayoutManager() == null)
        {
            return;
        }

        View child = m_RecyclerView.getLayoutManager().findViewByPosition(m_iActiveElement);
        if(child == null)
        {
            return;
        }
        RecipeItemsAdapter.ViewHolderActive vh = (RecipeItemsAdapter.ViewHolderActive)m_RecyclerView.getChildViewHolder(child);
        RecipeItem item = getRecipeItem(vh.getID());
        if(item == null)
        {
            return;
        }

        if(parent == vh.m_SpinnerAmount)
        {
            item.m_Amount.m_Unit = Amount.Unit.values()[vh.m_SpinnerAmount.getSelectedItemPosition()];
            adjustEditTextAmount(vh, item);
        }
        else if(parent == vh.m_SpinnerSize)
        {
            item.m_Size = RecipeItem.Size.values()[vh.m_SpinnerSize.getSelectedItemPosition()];
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
    }

    @Override
    public void reactToSwipe(int position)
    {
        // Remove element

        if(m_RecyclerView.getLayoutManager() == null)
        {
            return;
        }

        View activeItem = m_RecyclerView.getLayoutManager().findViewByPosition(position);
        if(activeItem == null)
        {
            return;
        }
        RecipeItemsAdapter.ViewHolder holder = (RecipeItemsAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(activeItem);

        m_RecentlyDeleted = getRecipeItem(holder.m_id);
        m_RecentlyDeletedIndex = position;
        m_Recipe.m_Items.remove(m_RecentlyDeleted);

        notifyDataSetChanged();
        setActiveElement("");

        // Allow undo

        Snackbar snackbar = Snackbar.make(m_CoordLayout, R.string.text_item_deleted, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.text_undo, (View view) ->
        {
            m_Recipe.m_Items.add(m_RecentlyDeletedIndex, m_RecentlyDeleted);

            notifyDataSetChanged();

            m_RecentlyDeleted = null;
            m_RecentlyDeletedIndex = -1;

            Snackbar snackbar1 = Snackbar.make(m_CoordLayout, R.string.text_item_restored, Snackbar.LENGTH_SHORT);
            snackbar1.show();
        });
        snackbar.show();
    }

    @Override
    public boolean swipeAllowed(RecyclerView.ViewHolder vh) { return true; }

    @Override
    public boolean reactToDrag(RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target)
    {
        return false;
    }

    boolean containsItem(String strName)
    {
        return getRecipeItemsList().contains(strName);
    }

    private ArrayList<String> getRecipeItemsList()
    {
        ArrayList<String> vec = new ArrayList<>();
        for(RecipeItem item : m_Recipe.m_Items)
        {
            vec.add(item.m_Ingredient);
        }
        return vec;
    }

    private RecipeItem getRecipeItem(String strName)
    {
        for(RecipeItem r : m_Recipe.m_Items)
        {
            if(r.m_Ingredient.equals(strName))
            {
                return r;
            }
        }
        return null;
    }

    private void adjustEditTextAmount(RecipeItemsAdapter.ViewHolderActive vh, RecipeItem item)
    {
        if(item.m_Amount.m_Unit == Amount.Unit.Unitless)
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
            vh.m_EditTextAmount.setText(String.format(Locale.getDefault(), "%s", Helper.formatNumber(item.m_Amount.m_QuantityMin)));
            vh.m_CheckBoxAmountRange.setVisibility(View.VISIBLE);
            vh.m_TableRowAmount.setVisibility(View.VISIBLE);

            if(item.m_Amount.isRange())
            {
                vh.m_EditTextAmountMax.setText(String.format(Locale.getDefault(), "%s", Helper.formatNumber(item.m_Amount.m_QuantityMax)));
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

