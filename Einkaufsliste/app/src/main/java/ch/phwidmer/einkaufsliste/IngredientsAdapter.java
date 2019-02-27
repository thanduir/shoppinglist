package ch.phwidmer.einkaufsliste;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.ViewHolder> implements ReactToTouchActionsInterface, AdapterView.OnItemSelectedListener
{
    private GroceryPlanning m_GroceryPlanning;
    private RecyclerView m_RecyclerView;
    private Integer m_iActiveElement;

    private String                 m_strRecentlyDeleted;
    private Ingredients.Ingredient m_RecentlyDeleted;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView m_TextView;
        private TextView m_TextViewDesc;
        private TableLayout m_TableLayout;
        private View m_View;
        private String m_id;

        private Spinner m_SpinnerCategory;
        private Spinner m_SpinnerProvenance;
        private Spinner m_SpinnerStdUnit;

        public ViewHolder(View v)
        {
            super(v);
            m_View = v;
            m_TextView = v.findViewById(R.id.textView);
            m_TextViewDesc = v.findViewById(R.id.textViewDesc);
            m_TableLayout = v.findViewById(R.id.tableLayoutEditIntegrdient);
            m_TableLayout.setVisibility(View.GONE);

            m_SpinnerCategory = (Spinner) v.findViewById(R.id.spinnerCategory);
            m_SpinnerProvenance = (Spinner) v.findViewById(R.id.spinnerProvenance);
            m_SpinnerStdUnit = (Spinner) v.findViewById(R.id.spinnerStdUnit);

            m_id = "";
        }

        public String getID()
        {
            return m_id;
        }

        public void setDescription(Ingredients.Ingredient ingredient)
        {
            String text = " (" + ingredient.m_Category.getName() + ", " + ingredient.m_Provenance.toString() + ", " + ingredient.m_DefaultUnit.toString() + ")";
            m_TextViewDesc.setText(text);
        }
    }

    public IngredientsAdapter(RecyclerView recyclerView, GroceryPlanning groceryPlanning)
    {
        m_iActiveElement = -1;
        m_GroceryPlanning = groceryPlanning;
        m_RecyclerView = recyclerView;
    }

    @Override
    public IngredientsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_item_edit_ingredients, parent, false);

        IngredientsAdapter.ViewHolder vh = new IngredientsAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(IngredientsAdapter.ViewHolder holder, int position)
    {
        String strIngredient = getSortedIngredients().get(position);
        holder.m_id = strIngredient;
        holder.m_TextView.setText(strIngredient);

        Ingredients.Ingredient ingredient = m_GroceryPlanning.m_Ingredients.getIngredient(strIngredient);
        holder.setDescription(ingredient);

        updateViewHolder(holder, m_iActiveElement == position);
    }

    @Override
    public int getItemCount()
    {
        return getSortedIngredients().size();
    }

    public String getActiveElement()
    {
        if(m_iActiveElement == -1)
        {
            return "";
        }
        return getSortedIngredients().get(m_iActiveElement);
    }

    public void setActiveElement(String strElement)
    {
        if(m_iActiveElement != -1)
        {
            IngredientsAdapter.ViewHolder vh = (IngredientsAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(m_RecyclerView.getChildAt(m_iActiveElement));
            updateViewHolder(vh, false);
        }

        if(strElement == "")
        {
            m_iActiveElement = -1;
        }
        else
        {
            m_iActiveElement = getSortedIngredients().indexOf(strElement);

            View child = m_RecyclerView.getChildAt(m_iActiveElement);
            if(child == null)
            {
                return;
            }

            IngredientsAdapter.ViewHolder vh = (IngredientsAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(child);
            updateViewHolder(vh, true);
        }
    }

    private void updateViewHolder(IngredientsAdapter.ViewHolder vh, boolean bActive)
    {
        if(!bActive)
        {
            vh.m_TableLayout.setVisibility(View.GONE);
            vh.m_TextViewDesc.setVisibility(View.VISIBLE);

            vh.m_SpinnerCategory.setAdapter(null);
            vh.m_SpinnerProvenance.setAdapter(null);
            vh.m_SpinnerStdUnit.setAdapter(null);

            vh.m_View.setBackgroundColor(Color.TRANSPARENT);
        }
        else
        {
            vh.m_TableLayout.setVisibility(View.VISIBLE);
            vh.m_TextViewDesc.setVisibility(View.INVISIBLE);

            vh.m_View.setBackgroundColor(vh.m_View.getResources().getColor(R.color.colorHighlightedBackground));

            Ingredients.Ingredient ingredient = m_GroceryPlanning.m_Ingredients.getIngredient(vh.m_id);

            ArrayAdapter<CharSequence> adapterCategory = new ArrayAdapter<CharSequence>(vh.m_View.getContext(), R.layout.spinner_item);
            for(String strCategory : m_GroceryPlanning.m_Categories.getAllCategories())
            {
                adapterCategory.add(strCategory);
            }
            adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            vh.m_SpinnerCategory.setAdapter(adapterCategory);
            vh.m_SpinnerCategory.setOnItemSelectedListener(this);
            vh.m_SpinnerCategory.setSelection(adapterCategory.getPosition(ingredient.m_Category.getName()));

            ArrayAdapter<CharSequence> adapterProvenance = new ArrayAdapter<CharSequence>(vh.m_View.getContext(), R.layout.spinner_item);
            for(int i = 0; i < Ingredients.Provenance.values().length; ++i)
            {
                adapterProvenance.add(Ingredients.Provenance.values()[i].toString());
            }
            adapterProvenance.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            vh.m_SpinnerProvenance.setAdapter(adapterProvenance);
            vh.m_SpinnerProvenance.setOnItemSelectedListener(this);
            vh.m_SpinnerProvenance.setSelection(ingredient.m_Provenance.ordinal());

            ArrayAdapter<CharSequence> adapterStdUnit = new ArrayAdapter<CharSequence>(vh.m_View.getContext(), R.layout.spinner_item);
            for(Amount.Unit u : Amount.Unit.values())
            {
                adapterStdUnit.add(u.toString());
            }
            adapterStdUnit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            vh.m_SpinnerStdUnit.setAdapter(adapterStdUnit);
            vh.m_SpinnerStdUnit.setOnItemSelectedListener(this);
            vh.m_SpinnerStdUnit.setSelection(ingredient.m_DefaultUnit.ordinal());
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        if(m_iActiveElement == -1)
        {
            return;
        }
        IngredientsAdapter.ViewHolder vh = (IngredientsAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(m_RecyclerView.getChildAt(m_iActiveElement));
        Ingredients.Ingredient ingredient = m_GroceryPlanning.m_Ingredients.getIngredient(vh.m_id);

        if(parent == vh.m_SpinnerCategory)
        {
            String category = (String)vh.m_SpinnerCategory.getSelectedItem();
            ingredient.m_Category = m_GroceryPlanning.m_Categories.getCategory(category);
        }
        else if(parent == vh.m_SpinnerProvenance)
        {
            String provenance = (String)vh.m_SpinnerProvenance.getSelectedItem();
            ingredient.m_Provenance = Ingredients.Provenance.valueOf(provenance);
        }
        else if(parent == vh.m_SpinnerStdUnit)
        {
            String provenance = (String)vh.m_SpinnerStdUnit.getSelectedItem();
            ingredient.m_DefaultUnit = Amount.Unit.valueOf(provenance);
        }

        vh.setDescription(ingredient);
    }

    public void onNothingSelected(AdapterView<?> parent)
    {
    }

    public void reactToSwipe(int position)
    {
        // Remove element

        View activeItem = m_RecyclerView.getChildAt(position);
        IngredientsAdapter.ViewHolder holder = (IngredientsAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(activeItem);
        String strIngredient = (String)holder.m_TextView.getText();

        if(m_GroceryPlanning.m_Recipes.isIngredientInUse(strIngredient) || m_GroceryPlanning.m_ShoppingList.isIngredientInUse(strIngredient))
        {
            notifyItemChanged(position);
            AlertDialog.Builder builder = new AlertDialog.Builder(m_RecyclerView.getContext());
            builder.setTitle("Deleting ingredient not allowed");
            builder.setMessage("Ingredient \"" + strIngredient + "\" cannot be deleted because it is still in use.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.show();
            return;
        }

        m_strRecentlyDeleted = strIngredient;
        m_RecentlyDeleted = m_GroceryPlanning.m_Ingredients.getIngredient(strIngredient);

        m_GroceryPlanning.m_Ingredients.removeIngredient(strIngredient);
        notifyItemRemoved(position);
        setActiveElement("");

        // Allow undo

        Snackbar snackbar = Snackbar.make(m_RecyclerView, "Item deleted", Snackbar.LENGTH_LONG);
        snackbar.setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_GroceryPlanning.m_Ingredients.addIngredient(m_strRecentlyDeleted);
                Ingredients.Ingredient ingredient = m_GroceryPlanning.m_Ingredients.getIngredient(m_strRecentlyDeleted);
                ingredient.m_Category = m_RecentlyDeleted.m_Category;
                ingredient.m_Provenance = m_RecentlyDeleted.m_Provenance;
                ingredient.m_DefaultUnit = m_RecentlyDeleted.m_DefaultUnit;

                notifyDataSetChanged();

                m_strRecentlyDeleted = "";
                m_RecentlyDeleted = null;

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

    private Vector<String> getSortedIngredients()
    {
        Vector<String> vec = m_GroceryPlanning.m_Ingredients.getAllIngredients();
        Collections.sort(vec, new SortIgnoreCase());
        return vec;
    }

    private class SortIgnoreCase implements Comparator<Object> {
        public int compare(Object o1, Object o2) {
            String s1 = (String) o1;
            String s2 = (String) o2;
            return s1.toLowerCase().compareTo(s2.toLowerCase());
        }
    }
}
