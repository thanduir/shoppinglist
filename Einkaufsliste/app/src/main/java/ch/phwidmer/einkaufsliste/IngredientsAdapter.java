package ch.phwidmer.einkaufsliste;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.ViewHolder> implements ReactToTouchActionsInterface
{
    private Ingredients m_Ingredients;
    private RecyclerView m_RecyclerView;
    private Integer m_iActiveElement;

    private String                 m_strRecentlyDeleted;
    private Ingredients.Ingredient m_RecentlyDeleted;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView m_TextView;
        public View m_View;
        public ViewHolder(View v)
        {
            super(v);
            m_View = v;
            m_TextView = v.findViewById(R.id.textView);
        }
    }

    public IngredientsAdapter(RecyclerView recyclerView, Ingredients ingredients)
    {
        m_iActiveElement = -1;
        m_Ingredients = ingredients;
        m_RecyclerView = recyclerView;
    }

    public String getActiveElement()
    {
        if(m_iActiveElement == -1)
        {
            return "";
        }
        return getSortedIngredients().get(m_iActiveElement);
    }
    public Integer getActiveElementIndex()
    {
        return m_iActiveElement;
    }

    public void setActiveElement(String strElement)
    {
        if(strElement == "")
        {
            m_iActiveElement = -1;
        }
        else
        {
            m_iActiveElement = getSortedIngredients().indexOf(strElement);
        }
    }

    @Override
    public IngredientsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.text_row_item, parent, false);

        IngredientsAdapter.ViewHolder vh = new IngredientsAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(IngredientsAdapter.ViewHolder holder, int position)
    {
        String strIngredient = getSortedIngredients().get(position);
        holder.m_TextView.setText(strIngredient);

        if(m_iActiveElement == position)
        {
            holder.m_View.setBackgroundColor(Color.GRAY);
            holder.m_View.setActivated(true);
        }
        else
        {
            holder.m_View.setBackgroundColor(Color.TRANSPARENT);
            holder.m_View.setActivated(false);
        }
    }

    @Override
    public int getItemCount()
    {
        return getSortedIngredients().size();
    }

    private Vector<String> getSortedIngredients()
    {
        Vector<String> vec = m_Ingredients.getAllIngredients();
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

    public void reactToSwipe(int position)
    {
        // Remove element

        View activeItem = m_RecyclerView.getChildAt(position);
        IngredientsAdapter.ViewHolder holder = (IngredientsAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(activeItem);

        m_strRecentlyDeleted = (String)holder.m_TextView.getText();
        m_RecentlyDeleted = m_Ingredients.getIngredient(m_strRecentlyDeleted);

        m_Ingredients.removeIngredient(m_strRecentlyDeleted);
        notifyItemRemoved(position);
        setActiveElement("");

        // Allow undo

        Snackbar snackbar = Snackbar.make(m_RecyclerView, "Item deleted", Snackbar.LENGTH_LONG);
        snackbar.setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_Ingredients.addIngredient(m_strRecentlyDeleted);
                Ingredients.Ingredient ingredient = m_Ingredients.getIngredient(m_strRecentlyDeleted);
                ingredient.m_Category = m_RecentlyDeleted.m_Category;
                ingredient.m_Provenance = m_RecentlyDeleted.m_Provenance;
                ingredient.m_DefaultUnit = m_RecentlyDeleted.m_DefaultUnit;

                notifyDataSetChanged();

                Snackbar snackbar1 = Snackbar.make(m_RecyclerView, "Item restored", Snackbar.LENGTH_SHORT);
                snackbar1.show();
            }
        });
        snackbar.show();
    }

    public boolean reactToDrag(RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target)
    {
        return false;
    }
}
