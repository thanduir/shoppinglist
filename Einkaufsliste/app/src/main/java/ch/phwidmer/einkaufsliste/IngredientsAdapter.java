package ch.phwidmer.einkaufsliste;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;
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

            m_SpinnerCategory = v.findViewById(R.id.spinnerCategory);
            m_SpinnerProvenance = v.findViewById(R.id.spinnerProvenance);
            m_SpinnerStdUnit = v.findViewById(R.id.spinnerStdUnit);

            m_id = "";
        }

        String getID()
        {
            return m_id;
        }

        void setDescription(Ingredients.Ingredient ingredient)
        {
            String text = " (" + ingredient.m_Category.getName();
            if(!ingredient.m_strProvenance.equals(Ingredients.c_strProvenanceEverywhere)) {
                text += ", " + ingredient.m_strProvenance;
            }
            text += ", " + ingredient.m_DefaultUnit.toString() + ")";
            m_TextViewDesc.setText(text);
        }
    }

    IngredientsAdapter(RecyclerView recyclerView, GroceryPlanning groceryPlanning)
    {
        m_iActiveElement = -1;
        m_GroceryPlanning = groceryPlanning;
        m_RecyclerView = recyclerView;
    }

    @Override @NonNull
    public IngredientsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                            int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_item_edit_ingredients, parent, false);

        return new IngredientsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientsAdapter.ViewHolder holder, int position)
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
        return m_GroceryPlanning.m_Ingredients.getIngredientsCount();
    }

    String getActiveElement()
    {
        if(m_iActiveElement == -1)
        {
            return "";
        }
        return getSortedIngredients().get(m_iActiveElement);
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
                IngredientsAdapter.ViewHolder vh = (IngredientsAdapter.ViewHolder) m_RecyclerView.getChildViewHolder(v);
                updateViewHolder(vh, false);
            }
        }

        if(strElement.equals(""))
        {
            m_iActiveElement = -1;
        }
        else
        {
            m_iActiveElement = getSortedIngredients().indexOf(strElement);

            View child = m_RecyclerView.getLayoutManager().findViewByPosition(m_iActiveElement);
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
        final View view = vh.itemView;
        vh.m_TextView.setOnClickListener((View v) -> view.performClick());

        if(!bActive)
        {
            vh.m_TableLayout.setVisibility(View.GONE);
            vh.m_TextViewDesc.setVisibility(View.VISIBLE);

            vh.m_TextView.setOnLongClickListener(null);

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

            final String strIngredient = vh.m_id;

            vh.m_TextView.setOnLongClickListener((View v) ->
            {
                renameIngredient(strIngredient);
                return true;
            });

            final Ingredients.Ingredient ingredient = m_GroceryPlanning.m_Ingredients.getIngredient(vh.m_id);
            if(ingredient == null)
            {
                // This case should only be possible if a renamed object is sorted into the same position as
                // the previous one (then vh.m_id is still the old id which doesn't exist anymore in m_Ingredients)
                return;
            }

            ArrayAdapter<CharSequence> adapterCategory = new ArrayAdapter<>(vh.m_View.getContext(), R.layout.spinner_item);
            for(String strCategory : m_GroceryPlanning.m_Categories.getAllCategories())
            {
                adapterCategory.add(strCategory);
            }
            adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            vh.m_SpinnerCategory.setAdapter(adapterCategory);
            vh.m_SpinnerCategory.setOnItemSelectedListener(this);
            vh.m_SpinnerCategory.setSelection(adapterCategory.getPosition(ingredient.m_Category.getName()));

            ArrayAdapter<CharSequence> adapterProvenance = new ArrayAdapter<>(vh.m_View.getContext(), R.layout.spinner_item);
            adapterProvenance.add(m_RecyclerView.getContext().getResources().getString(R.string.provenance_everywhere));
            for(String strSortOrder : m_GroceryPlanning.m_Categories.getAllSortOrders())
            {
                adapterProvenance.add(strSortOrder);
            }
            adapterProvenance.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            vh.m_SpinnerProvenance.setAdapter(adapterProvenance);
            vh.m_SpinnerProvenance.setOnItemSelectedListener(this);
            if(ingredient.m_strProvenance.equals(Ingredients.c_strProvenanceEverywhere))
            {
                vh.m_SpinnerProvenance.setSelection(0);
            }
            else
            {
                vh.m_SpinnerProvenance.setSelection(adapterProvenance.getPosition(ingredient.m_strProvenance));
            }

            ArrayAdapter<CharSequence> adapterStdUnit = new ArrayAdapter<>(vh.m_View.getContext(), R.layout.spinner_item);
            for(Amount.Unit u : Amount.Unit.values())
            {
                adapterStdUnit.add(Amount.toUIString(vh.itemView.getContext(), u));
            }
            adapterStdUnit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            vh.m_SpinnerStdUnit.setAdapter(adapterStdUnit);
            vh.m_SpinnerStdUnit.setOnItemSelectedListener(this);
            vh.m_SpinnerStdUnit.setSelection(ingredient.m_DefaultUnit.ordinal());
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        if(m_iActiveElement == -1 || m_RecyclerView.getLayoutManager() == null)
        {
            return;
        }

        View v = m_RecyclerView.getLayoutManager().findViewByPosition(m_iActiveElement);
        if(v == null)
        {
            return;
        }
        IngredientsAdapter.ViewHolder vh = (IngredientsAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(v);
        Ingredients.Ingredient ingredient = m_GroceryPlanning.m_Ingredients.getIngredient(vh.m_id);

        if(parent == vh.m_SpinnerCategory)
        {
            String category = (String)vh.m_SpinnerCategory.getSelectedItem();
            ingredient.m_Category = m_GroceryPlanning.m_Categories.getCategory(category);
        }
        else if(parent == vh.m_SpinnerProvenance)
        {
            if(vh.m_SpinnerProvenance.getSelectedItemPosition() == 0)
            {
                ingredient.m_strProvenance = Ingredients.c_strProvenanceEverywhere;
            }
            else
            {
                ingredient.m_strProvenance = (String) vh.m_SpinnerProvenance.getSelectedItem();
            }
        }
        else if(parent == vh.m_SpinnerStdUnit)
        {
            ingredient.m_DefaultUnit = Amount.Unit.values()[vh.m_SpinnerStdUnit.getSelectedItemPosition()];
        }

        vh.setDescription(ingredient);
    }

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
        IngredientsAdapter.ViewHolder holder = (IngredientsAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(activeItem);
        String strIngredient = (String)holder.m_TextView.getText();

        ArrayList<String> recipesUsingIngredient = new ArrayList<>();
        ArrayList<String> shoppinglistUsingIngredient = new ArrayList<>();
        if(m_GroceryPlanning.m_Recipes.isIngredientInUse(strIngredient, recipesUsingIngredient) || m_GroceryPlanning.m_ShoppingList.isIngredientInUse(strIngredient, shoppinglistUsingIngredient))
        {
            notifyItemChanged(position);
            AlertDialog.Builder builder = new AlertDialog.Builder(m_RecyclerView.getContext());
            builder.setTitle(R.string.text_delete_ingredient_disallowed_header);
            builder.setMessage(m_RecyclerView.getContext().getResources().getString(R.string.text_delete_ingredient_disallowed_desc,
                                                                                    strIngredient,
                                                                                    recipesUsingIngredient.toString(),
                                                                                    shoppinglistUsingIngredient.toString()));
            builder.setPositiveButton(android.R.string.ok, (DialogInterface dialog, int which) -> {});
            builder.show();
            return;
        }

        m_strRecentlyDeleted = strIngredient;
        m_RecentlyDeleted = m_GroceryPlanning.m_Ingredients.getIngredient(strIngredient);

        m_GroceryPlanning.m_Ingredients.removeIngredient(strIngredient);
        notifyDataSetChanged();
        setActiveElement("");

        // Allow undo

        Snackbar snackbar = Snackbar.make(m_RecyclerView, R.string.text_item_deleted, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.text_undo, (View view) -> {
            m_GroceryPlanning.m_Ingredients.addIngredient(m_strRecentlyDeleted, m_RecentlyDeleted.m_DefaultUnit);
            Ingredients.Ingredient ingredient = m_GroceryPlanning.m_Ingredients.getIngredient(m_strRecentlyDeleted);
            ingredient.m_Category = m_RecentlyDeleted.m_Category;
            ingredient.m_strProvenance = m_RecentlyDeleted.m_strProvenance;

            notifyDataSetChanged();

            m_strRecentlyDeleted = "";
            m_RecentlyDeleted = null;

            Snackbar snackbar1 = Snackbar.make(m_RecyclerView, R.string.text_item_restored, Snackbar.LENGTH_SHORT);
            snackbar1.show();
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
        return m_GroceryPlanning.m_Ingredients.getAllIngredients();
    }

    private void renameIngredient(final String strIngredient)
    {
        DialogFragment newFragment = InputStringDialogFragment.newInstance(m_RecyclerView.getContext().getResources().getString(R.string.text_rename_ingredient, strIngredient), strIngredient);
        newFragment.show(((AppCompatActivity) m_RecyclerView.getContext()).getSupportFragmentManager(), "renameIngredient");
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
