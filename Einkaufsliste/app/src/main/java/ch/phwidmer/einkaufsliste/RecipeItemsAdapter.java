package ch.phwidmer.einkaufsliste;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
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
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class RecipeItemsAdapter extends RecyclerView.Adapter<RecipeItemsAdapter.ViewHolder> implements ReactToTouchActionsInterface, AdapterView.OnItemSelectedListener
{
    private Recipes.Recipe m_Recipe;
    private RecyclerView m_RecyclerView;
    private CoordinatorLayout m_CoordLayout;
    private Integer m_iActiveElement;

    private RecipeItem m_RecentlyDeleted;
    private int m_RecentlyDeletedIndex;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView m_TextView;
        private TextView m_TextViewDesc;
        private TableLayout m_TableLayout;
        private View m_View;
        private String m_id;

        private CheckBox m_CheckBoxOptional;
        private Spinner  m_SpinnerAmount;
        private EditText m_EditTextAmount;
        private Spinner  m_SpinnerSize;
        private EditText m_AdditionalInfo;

        public ViewHolder(View v)
        {
            super(v);
            m_View = v;
            m_TextView = v.findViewById(R.id.textView);
            m_TextViewDesc = v.findViewById(R.id.textViewDesc);
            m_TableLayout = v.findViewById(R.id.tableLayoutEditIntegrdient);
            m_TableLayout.setVisibility(View.GONE);

            m_id = "";

            m_SpinnerAmount = v.findViewById(R.id.spinnerAmount);
            m_EditTextAmount = v.findViewById(R.id.editText_Amount);
            m_SpinnerSize = v.findViewById(R.id.spinnerSize);
            m_CheckBoxOptional = v.findViewById(R.id.checkBoxOptional);
            m_AdditionalInfo = v.findViewById(R.id.editText_AdditonalInfo);
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
                text += Helper.formatNumber(item.m_Amount.m_Quantity) + " " + Amount.shortForm(context, item.m_Amount.m_Unit);
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

            if(item.m_Optional)
            {
                m_TextView.setTextColor(Color.GRAY);
                m_TextView.setTypeface(m_TextView.getTypeface(), Typeface.ITALIC);

                m_TextViewDesc.setTextColor(Color.GRAY);
                m_TextViewDesc.setTypeface(m_TextViewDesc.getTypeface(), Typeface.ITALIC);
            }
            else
            {
                m_TextView.setTextColor(Color.BLACK);
                m_TextView.setTypeface(m_TextView.getTypeface(), Typeface.BOLD);

                m_TextViewDesc.setTypeface(null, Typeface.NORMAL);
                m_TextViewDesc.setTextColor(Color.BLACK);
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

    @Override @NonNull
    public RecipeItemsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                            int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_item_edit_recipieitem, parent, false);

        return new RecipeItemsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeItemsAdapter.ViewHolder holder, int position)
    {
        String strRecipeItem = getRecipeItemsList().get(position);
        holder.m_id = strRecipeItem;
        holder.m_TextView.setText(strRecipeItem);

        RecipeItem item = m_Recipe.m_Items.get(position);
        holder.setDescription(holder.itemView.getContext(), item);

        updateViewHolder(holder, m_iActiveElement == position);
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

    @Override
    public void onViewAttachedToWindow(@NonNull RecipeItemsAdapter.ViewHolder holder)
    {
        updateViewHolder(holder, m_iActiveElement == holder.getAdapterPosition());
    }

    void setActiveElement(String strElement)
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
                RecipeItemsAdapter.ViewHolder vh = (RecipeItemsAdapter.ViewHolder) m_RecyclerView.getChildViewHolder(v);
                updateViewHolder(vh, false);
            }
        }

        if(strElement.equals(""))
        {
            m_iActiveElement = -1;
        }
        else
        {
            m_iActiveElement = getRecipeItemsList().indexOf(strElement);

            View child = m_RecyclerView.getLayoutManager().findViewByPosition(m_iActiveElement);
            if(child == null)
            {
                return;
            }

            RecipeItemsAdapter.ViewHolder vh = (RecipeItemsAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(child);
            updateViewHolder(vh, true);

            child.requestFocus();
        }
    }

    private void updateViewHolder(RecipeItemsAdapter.ViewHolder vh, boolean bActive)
    {
        if(!bActive)
        {
            vh.m_TableLayout.setVisibility(View.GONE);
            vh.m_TextViewDesc.setVisibility(View.VISIBLE);

            vh.m_SpinnerAmount.setAdapter(null);
            vh.m_SpinnerSize.setAdapter(null);

            vh.m_View.setBackgroundColor(Color.TRANSPARENT);
        }
        else
        {
            vh.m_TableLayout.setVisibility(View.VISIBLE);
            vh.m_TextViewDesc.setVisibility(View.INVISIBLE);

            vh.m_View.setBackgroundColor(vh.m_View.getResources().getColor(R.color.colorHighlightedBackground));

            RecipeItem item = getRecipeItem(vh.m_id);
            if(item == null)
            {
                return;
            }

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
                viewHolder.setDescription(viewHolder.itemView.getContext(), recipeItem);
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

                    vh.setDescription(vh.itemView.getContext(), item);
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
                        item.m_Amount.m_Quantity = 0.0f;
                    }
                    else
                    {
                        item.m_Amount.m_Quantity = Float.valueOf(s.toString());
                    }

                    vh.setDescription(vh.itemView.getContext(), item);
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

        View child = m_RecyclerView.getLayoutManager().findViewByPosition(m_iActiveElement);
        if(child == null)
        {
            return;
        }
        RecipeItemsAdapter.ViewHolder vh = (RecipeItemsAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(child);
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

        vh.setDescription(view.getContext(), item);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
    }

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

    public boolean swipeAllowed(RecyclerView.ViewHolder vh) { return true; }

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

    private void adjustEditTextAmount(RecipeItemsAdapter.ViewHolder vh, RecipeItem item)
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

