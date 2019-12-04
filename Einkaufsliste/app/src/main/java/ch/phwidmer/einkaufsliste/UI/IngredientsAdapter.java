package ch.phwidmer.einkaufsliste.UI;

import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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
import java.util.Optional;

import ch.phwidmer.einkaufsliste.data.Categories;
import ch.phwidmer.einkaufsliste.data.Unit;
import ch.phwidmer.einkaufsliste.helper.InputStringDialogFragment;
import ch.phwidmer.einkaufsliste.R;
import ch.phwidmer.einkaufsliste.data.GroceryPlanning;
import ch.phwidmer.einkaufsliste.data.Ingredients;
import ch.phwidmer.einkaufsliste.helper.ReactToTouchActionsInterface;

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.ViewHolder> implements ReactToTouchActionsInterface, AdapterView.OnItemSelectedListener
{
    private static final int TYPE_INACTIVE = 1;
    private static final int TYPE_ACTIVE = 2;

    private GroceryPlanning m_GroceryPlanning;
    private RecyclerView m_RecyclerView;
    private CoordinatorLayout m_CoordLayout;

    private Integer m_iActiveElement;

    private String                 m_strRecentlyDeleted;
    private Ingredients.Ingredient m_RecentlyDeleted;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView m_TextView;
        View m_View;
        String m_id;

        ViewHolder(@NonNull View v)
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

    public static class ViewHolderInactive extends IngredientsAdapter.ViewHolder
    {
        private TextView m_TextViewDesc;

        ViewHolderInactive(@NonNull View v)
        {
            super(v);
            m_TextViewDesc = v.findViewById(R.id.textViewDesc);
            m_id = "";
        }

        void setDescription(@NonNull Ingredients.Ingredient ingredient)
        {
            String text = " (" + ingredient.getCategory();
            if(!ingredient.getProvenance().equals(Ingredients.c_strProvenanceEverywhere)) {
                text += ", " + ingredient.getProvenance();
            }
            text += ", " + ingredient.getDefaultUnit().toString() + ")";
            m_TextViewDesc.setText(text);
        }
    }

    public static class ViewHolderActive extends IngredientsAdapter.ViewHolder
    {
        private TableLayout m_TableLayout;

        private Spinner m_SpinnerCategory;
        private Spinner m_SpinnerProvenance;
        private Spinner m_SpinnerStdUnit;

        ViewHolderActive(@NonNull View v)
        {
            super(v);
            m_TableLayout = v.findViewById(R.id.tableLayoutEditIntegrdient);
            m_SpinnerCategory = v.findViewById(R.id.spinnerCategory);
            m_SpinnerProvenance = v.findViewById(R.id.spinnerProvenance);
            m_SpinnerStdUnit = v.findViewById(R.id.spinnerStdUnit);
        }
    }

    IngredientsAdapter(@NonNull CoordinatorLayout coordLayout, @NonNull RecyclerView recyclerView, @NonNull GroceryPlanning groceryPlanning)
    {
        m_iActiveElement = -1;
        m_GroceryPlanning = groceryPlanning;
        m_RecyclerView = recyclerView;
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
    public IngredientsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                            int viewType)
    {
        if(viewType == TYPE_INACTIVE)
        {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_item_edit_ingredients_inactive, parent, false);

            return new IngredientsAdapter.ViewHolderInactive(v);
        }
        else
        {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_item_edit_ingredients_active, parent, false);

            return new IngredientsAdapter.ViewHolderActive(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientsAdapter.ViewHolder holder, int position)
    {
        String strIngredient = getSortedIngredients().get(position).getName();
        holder.m_id = strIngredient;
        holder.m_TextView.setText(strIngredient);

        final View view = holder.itemView;
        holder.m_TextView.setOnClickListener((View v) -> view.performClick());

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
        return m_GroceryPlanning.ingredients().getIngredientsCount();
    }

    String getActiveElement()
    {
        if(m_iActiveElement == -1)
        {
            return "";
        }
        return getSortedIngredients().get(m_iActiveElement).getName();
    }

    void setActiveElement(@NonNull String strElement)
    {
        if(m_iActiveElement != -1)
        {
            notifyItemChanged(m_iActiveElement);
        }

        m_iActiveElement = -1;
        if(!strElement.equals(""))
        {
            Optional<Ingredients.Ingredient> ingredient = m_GroceryPlanning.ingredients().getIngredient(strElement);
            if(ingredient.isPresent())
            {
                m_iActiveElement = getSortedIngredients().indexOf(ingredient.get());
                notifyItemChanged(m_iActiveElement);
            }
        }
    }

    private void updateViewHolderInactive(@NonNull IngredientsAdapter.ViewHolder holder)
    {
        IngredientsAdapter.ViewHolderInactive vh = (IngredientsAdapter.ViewHolderInactive)holder;

        Optional<Ingredients.Ingredient> ingredient = m_GroceryPlanning.ingredients().getIngredient(holder.m_id);
        if(!ingredient.isPresent())
        {
            return;
        }
        vh.setDescription(ingredient.get());

        vh.m_TextViewDesc.setVisibility(View.VISIBLE);
        vh.m_TextView.setOnLongClickListener(null);
        vh.m_View.setBackgroundColor(Color.TRANSPARENT);
    }

    private void updateViewHolderActive(@NonNull IngredientsAdapter.ViewHolder holder)
    {
        IngredientsAdapter.ViewHolderActive vh = (IngredientsAdapter.ViewHolderActive)holder;

        vh.m_TableLayout.setVisibility(View.VISIBLE);

        vh.m_View.setBackgroundColor(ContextCompat.getColor(vh.m_View.getContext(), R.color.colorHighlightedBackground));

        final String strIngredient = vh.m_id;

        vh.m_TextView.setOnLongClickListener((View v) ->
        {
            renameIngredient(strIngredient);
            return true;
        });

        final Optional<Ingredients.Ingredient> ingredient = m_GroceryPlanning.ingredients().getIngredient(vh.m_id);
        if(!ingredient.isPresent())
        {
            // This case should only be possible if a renamed object is sorted into the same position as
            // the previous one (then vh.m_id is still the old id which doesn't exist anymore in m_Ingredients)
            return;
        }

        ArrayAdapter<CharSequence> adapterCategory = new ArrayAdapter<>(vh.m_View.getContext(), R.layout.spinner_item);
        for(Categories.Category category : m_GroceryPlanning.categories().getAllCategories())
        {
            adapterCategory.add(category.getName());
        }
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vh.m_SpinnerCategory.setAdapter(adapterCategory);
        vh.m_SpinnerCategory.setOnItemSelectedListener(this);
        vh.m_SpinnerCategory.setSelection(adapterCategory.getPosition(ingredient.get().getCategory()));

        ArrayAdapter<CharSequence> adapterProvenance = new ArrayAdapter<>(vh.m_View.getContext(), R.layout.spinner_item);
        adapterProvenance.add(m_RecyclerView.getContext().getResources().getString(R.string.provenance_everywhere));
        for(Categories.SortOrder sortOrder : m_GroceryPlanning.categories().getAllSortOrders())
        {
            adapterProvenance.add(sortOrder.getName());
        }
        adapterProvenance.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vh.m_SpinnerProvenance.setAdapter(adapterProvenance);
        vh.m_SpinnerProvenance.setOnItemSelectedListener(this);
        if(ingredient.get().getProvenance().equals(Ingredients.c_strProvenanceEverywhere))
        {
            vh.m_SpinnerProvenance.setSelection(0);
        }
        else
        {
            vh.m_SpinnerProvenance.setSelection(adapterProvenance.getPosition(ingredient.get().getProvenance()));
        }

        ArrayAdapter<CharSequence> adapterStdUnit = new ArrayAdapter<>(vh.m_View.getContext(), R.layout.spinner_item);
        for(Unit u : Unit.values())
        {
            adapterStdUnit.add(Unit.toUIString(vh.itemView.getContext(), u));
        }
        adapterStdUnit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vh.m_SpinnerStdUnit.setAdapter(adapterStdUnit);
        vh.m_SpinnerStdUnit.setOnItemSelectedListener(this);
        vh.m_SpinnerStdUnit.setSelection(ingredient.get().getDefaultUnit().ordinal());
    }

    public void onItemSelected(@NonNull AdapterView<?> parent, @NonNull View view, int pos, long id)
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
        IngredientsAdapter.ViewHolderActive vh = (IngredientsAdapter.ViewHolderActive)m_RecyclerView.getChildViewHolder(v);
        if(vh == null)
        {
            return;
        }

        Optional<Ingredients.Ingredient> ingredient = m_GroceryPlanning.ingredients().getIngredient(vh.m_id);
        if(!ingredient.isPresent())
        {
            return;
        }

        if(parent == vh.m_SpinnerCategory)
        {
            ingredient.get().setCategory((String)vh.m_SpinnerCategory.getSelectedItem());
        }
        else if(parent == vh.m_SpinnerProvenance)
        {
            if(vh.m_SpinnerProvenance.getSelectedItemPosition() == 0)
            {
                ingredient.get().setProvenance(Ingredients.c_strProvenanceEverywhere);
            }
            else
            {
                ingredient.get().setProvenance((String) vh.m_SpinnerProvenance.getSelectedItem());
            }
        }
        else if(parent == vh.m_SpinnerStdUnit)
        {
            ingredient.get().setDefaultUnit(Unit.values()[vh.m_SpinnerStdUnit.getSelectedItemPosition()]);
        }
    }

    public void onNothingSelected(@NonNull AdapterView<?> parent)
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
        if(m_GroceryPlanning.recipes().isIngredientInUse(strIngredient, recipesUsingIngredient) || m_GroceryPlanning.shoppingList().isIngredientInUse(strIngredient, shoppinglistUsingIngredient))
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
        Optional<Ingredients.Ingredient> ingredientToDelete = m_GroceryPlanning.ingredients().getIngredient(strIngredient);
        if(!ingredientToDelete.isPresent())
        {
            return;
        }
        m_RecentlyDeleted = ingredientToDelete.get();

        m_GroceryPlanning.ingredients().removeIngredient(m_RecentlyDeleted);
        notifyDataSetChanged();
        setActiveElement("");

        // Allow undo

        Snackbar snackbar = Snackbar.make(m_CoordLayout, R.string.text_item_deleted, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.text_undo, (View view) -> {
            Optional<Categories.Category> category = m_GroceryPlanning.categories().getCategory(m_RecentlyDeleted.getCategory());
            if(!category.isPresent())
            {
                return;
            }
            m_GroceryPlanning.ingredients().addIngredient(m_strRecentlyDeleted, m_RecentlyDeleted.getDefaultUnit(), category.get());
            Optional<Ingredients.Ingredient> ingredient = m_GroceryPlanning.ingredients().getIngredient(m_strRecentlyDeleted);
            if(!ingredient.isPresent())
            {
                return;
            }
            ingredient.get().setProvenance(m_RecentlyDeleted.getProvenance());

            notifyDataSetChanged();

            m_strRecentlyDeleted = "";
            m_RecentlyDeleted = null;

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

    private ArrayList<Ingredients.Ingredient> getSortedIngredients()
    {
        return m_GroceryPlanning.ingredients().getAllIngredients();
    }

    private void renameIngredient(@NonNull final String strIngredient)
    {
        InputStringDialogFragment newFragment = InputStringDialogFragment.newInstance(m_RecyclerView.getContext().getResources().getString(R.string.text_rename_ingredient, strIngredient));
        newFragment.setDefaultValue(strIngredient);
        newFragment.setAdditionalInformation(strIngredient);
        newFragment.setListExcludedInputs(m_GroceryPlanning.ingredients().getAllIngredientNames());
        newFragment.show(((AppCompatActivity) m_RecyclerView.getContext()).getSupportFragmentManager(), "renameIngredient");
    }

    @Override
    public void clearViewBackground(@NonNull RecyclerView.ViewHolder vh)
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
