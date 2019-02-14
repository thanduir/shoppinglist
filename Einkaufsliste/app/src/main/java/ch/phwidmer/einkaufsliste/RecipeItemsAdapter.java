package ch.phwidmer.einkaufsliste;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Vector;

public class RecipeItemsAdapter extends RecyclerView.Adapter<RecipeItemsAdapter.ViewHolder>
{
    private Recipes.Recipe m_Recipe;
    private Integer m_iActiveElement;

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

    public RecipeItemsAdapter(Recipes.Recipe recipe)
    {
        m_iActiveElement = -1;
        m_Recipe = recipe;
    }

    public String getActiveElement()
    {
        if(m_iActiveElement == -1)
        {
            return "";
        }
        return getSortedRecipeItems().get(m_iActiveElement);
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
            m_iActiveElement = getSortedRecipeItems().indexOf(strElement);
        }
    }

    @Override
    public RecipeItemsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.text_row_item, parent, false);

        RecipeItemsAdapter.ViewHolder vh = new RecipeItemsAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecipeItemsAdapter.ViewHolder holder, int position)
    {
        String strRecipeItem = getSortedRecipeItems().get(position);
        holder.m_TextView.setText(strRecipeItem);

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

    public boolean containsItem(String strName)
    {
        return getSortedRecipeItems().contains(strName);
    }

    @Override
    public int getItemCount()
    {
        return getSortedRecipeItems().size();
    }

    private Vector<String> getSortedRecipeItems()
    {
        Vector<String> vec = new Vector<String>();
        for(Object obj : m_Recipe.m_Items.toArray())
        {
            RecipeItem item = (RecipeItem) obj;
            vec.add(item.m_Ingredient);
        }
        return vec;
    }
}

