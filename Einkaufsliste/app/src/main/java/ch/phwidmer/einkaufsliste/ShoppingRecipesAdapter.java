package ch.phwidmer.einkaufsliste;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Vector;

public class ShoppingRecipesAdapter extends RecyclerView.Adapter<ShoppingRecipesAdapter.ViewHolder>
{
    private ShoppingList m_ShoppingList;
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

    public ShoppingRecipesAdapter(ShoppingList shoppingList)
    {
        m_iActiveElement = -1;
        m_ShoppingList = shoppingList;
    }

    public String getActiveElement()
    {
        if(m_iActiveElement == -1)
        {
            return "";
        }
        return getSortedShoppingRecipes().get(m_iActiveElement);
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
            m_iActiveElement = getSortedShoppingRecipes().indexOf(strElement);
        }
    }

    @Override
    public ShoppingRecipesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.text_row_item, parent, false);

        ShoppingRecipesAdapter.ViewHolder vh = new ShoppingRecipesAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ShoppingRecipesAdapter.ViewHolder holder, int position)
    {
        String strRecipe = getSortedShoppingRecipes().get(position);
        holder.m_TextView.setText(strRecipe);

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
        return getSortedShoppingRecipes().contains(strName);
    }

    @Override
    public int getItemCount()
    {
        return getSortedShoppingRecipes().size();
    }

    private Vector<String> getSortedShoppingRecipes()
    {
        Vector<String> vec = m_ShoppingList.getAllShoppingRecipes();
        //Collections.sort(vec, new SortIgnoreCase());
        return vec;
    }

    /*private class SortIgnoreCase implements Comparator<Object> {
        public int compare(Object o1, Object o2) {
            String s1 = (String) o1;
            String s2 = (String) o2;
            return s1.toLowerCase().compareTo(s2.toLowerCase());
        }
    }*/
}
