package ch.phwidmer.einkaufsliste;

import android.graphics.Color;
import android.graphics.Typeface;
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

import java.util.Vector;

public class RecipeItemsAdapter extends RecyclerView.Adapter<RecipeItemsAdapter.ViewHolder> implements ReactToTouchActionsInterface, AdapterView.OnItemSelectedListener
{
    private Recipes.Recipe m_Recipe;
    private RecyclerView m_RecyclerView;
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

        public ViewHolder(View v)
        {
            super(v);
            m_View = v;
            m_TextView = v.findViewById(R.id.textView);
            m_TextViewDesc = v.findViewById(R.id.textViewDesc);
            m_TableLayout = v.findViewById(R.id.tableLayoutEditIntegrdient);
            m_TableLayout.setVisibility(View.GONE);

            m_id = "";

            m_SpinnerAmount = (Spinner) v.findViewById(R.id.spinnerAmount);
            m_EditTextAmount = (EditText) v.findViewById(R.id.editText_Amount);
            m_SpinnerSize = (Spinner) v.findViewById(R.id.spinnerSize);
            m_CheckBoxOptional = (CheckBox) v.findViewById(R.id.checkBoxOptional);
        }

        public String getID()
        {
            return m_id;
        }

        public void setDescription(RecipeItem item)
        {
            Float f = item.m_Amount.m_Quantity;
            String amount = f.toString();
            if(f == Math.round(f))
            {
                amount = String.valueOf(f.intValue());
            }

            String text = " (" + amount + " " + Amount.shortForm(item.m_Amount.m_Unit);
            if(item.m_Size != RecipeItem.Size.Normal)
            {
                text += ", " + item.m_Size.toString();
            }
            text += ")";

            if(item.m_Amount.m_Unit == Amount.Unit.Unitless)
            {
                text = "";
            }
            m_TextViewDesc.setText(text);

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

    public RecipeItemsAdapter(RecyclerView recyclerView, Recipes.Recipe recipe)
    {
        m_iActiveElement = -1;
        m_RecyclerView = recyclerView;
        m_Recipe = recipe;
    }

    @Override
    public RecipeItemsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_item_edit_recipieitem, parent, false);

        RecipeItemsAdapter.ViewHolder vh = new RecipeItemsAdapter.ViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(RecipeItemsAdapter.ViewHolder holder, int position)
    {
        String strRecipeItem = getRecipeItemsList().get(position);
        holder.m_id = strRecipeItem;
        holder.m_TextView.setText(strRecipeItem);

        RecipeItem item = m_Recipe.m_Items.get(position);
        holder.setDescription(item);

        updateViewHolder(holder, m_iActiveElement == position);
    }

    @Override
    public int getItemCount()
    {
        return getRecipeItemsList().size();
    }

    public String getActiveElement()
    {
        if(m_iActiveElement == -1)
        {
            return "";
        }
        return getRecipeItemsList().get(m_iActiveElement);
    }

    public void setActiveElement(String strElement)
    {
        if(m_iActiveElement != -1)
        {
            RecipeItemsAdapter.ViewHolder vh = (RecipeItemsAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(m_RecyclerView.getChildAt(m_iActiveElement));
            updateViewHolder(vh, false);
        }

        if(strElement == "")
        {
            m_iActiveElement = -1;
        }
        else
        {
            m_iActiveElement = getRecipeItemsList().indexOf(strElement);

            View child = m_RecyclerView.getChildAt(m_iActiveElement);
            if(child == null)
            {
                return;
            }

            RecipeItemsAdapter.ViewHolder vh = (RecipeItemsAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(child);
            updateViewHolder(vh, true);
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
                    RecipeItemsAdapter.ViewHolder vh = (RecipeItemsAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(m_RecyclerView.getChildAt(m_iActiveElement));
                    RecipeItem item = getRecipeItem(vh.getID());
                    if(item == null)
                    {
                        return;
                    }

                    item.m_Optional = isChecked;
                    vh.setDescription(item);
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

                    RecipeItemsAdapter.ViewHolder vh = (RecipeItemsAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(child);
                    RecipeItem item = getRecipeItem(vh.getID());
                    if(s.toString().isEmpty())
                    {
                        item.m_Amount.m_Quantity = 0.0f;
                    }
                    else
                    {
                        item.m_Amount.m_Quantity = Float.valueOf(s.toString());
                    }

                    vh.setDescription(item);
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
        RecipeItemsAdapter.ViewHolder vh = (RecipeItemsAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(m_RecyclerView.getChildAt(m_iActiveElement));
        RecipeItem item = getRecipeItem(vh.getID());

        if(parent == vh.m_SpinnerAmount)
        {
            item.m_Amount.m_Unit = Amount.Unit.values()[vh.m_SpinnerAmount.getSelectedItemPosition()];
            adjustEditTextAmount(vh, item);
        }
        else if(parent == vh.m_SpinnerSize)
        {
            item.m_Size = RecipeItem.Size.values()[vh.m_SpinnerSize.getSelectedItemPosition()];
        }

        vh.setDescription(item);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
    }

    public void reactToSwipe(int position)
    {
        // Remove element

        View activeItem = m_RecyclerView.getChildAt(position);
        RecipeItemsAdapter.ViewHolder holder = (RecipeItemsAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(activeItem);

        m_RecentlyDeleted = getRecipeItem(holder.m_id);
        m_RecentlyDeletedIndex = position;
        m_Recipe.m_Items.remove(m_RecentlyDeleted);

        notifyItemRemoved(position);
        setActiveElement("");

        // Allow undo

        Snackbar snackbar = Snackbar.make(m_RecyclerView, "Item deleted", Snackbar.LENGTH_LONG);
        snackbar.setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_Recipe.m_Items.add(m_RecentlyDeletedIndex, m_RecentlyDeleted);

                notifyDataSetChanged();

                m_RecentlyDeleted = null;
                m_RecentlyDeletedIndex = -1;

                Snackbar snackbar1 = Snackbar.make(m_RecyclerView, "Item restored", Snackbar.LENGTH_SHORT);
                snackbar1.show();
            }
        });
        snackbar.show();
    }

    public boolean swipeAllowed(RecyclerView.ViewHolder vh) { return true; }

    public boolean reactToDrag(RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target)
    {
        return false;
    }

    public boolean containsItem(String strName)
    {
        return getRecipeItemsList().contains(strName);
    }

    private Vector<String> getRecipeItemsList()
    {
        Vector<String> vec = new Vector<String>();
        for(Object obj : m_Recipe.m_Items.toArray())
        {
            RecipeItem item = (RecipeItem) obj;
            vec.add(item.m_Ingredient);
        }
        return vec;
    }

    private RecipeItem getRecipeItem(String strName)
    {
        for(RecipeItem r : m_Recipe.m_Items)
        {
            if(r.m_Ingredient == strName)
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
            vh.m_EditTextAmount.setText(item.m_Amount.m_Quantity.toString());
            vh.m_EditTextAmount.setVisibility(View.VISIBLE);
        }
    }
}

