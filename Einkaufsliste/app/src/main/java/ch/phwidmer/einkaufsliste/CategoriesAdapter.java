package ch.phwidmer.einkaufsliste;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder>
{
    private Categories m_Categories;
    private Categories.SortOrder m_SortOrder;
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

    public CategoriesAdapter(Categories categories, Categories.SortOrder sortOrder)
    {
        m_iActiveElement = -1;
        m_Categories = categories;
        m_SortOrder = sortOrder;
    }

    public String getActiveElement()
    {
        if(m_iActiveElement == -1)
        {
            return "";
        }
        return m_SortOrder.m_CategoriesOrder.get(m_iActiveElement).getName();
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
            m_iActiveElement = m_SortOrder.m_CategoriesOrder.indexOf(m_Categories.getCategory(strElement));
        }
    }

    public boolean switchPositionActiveElementWithNeighbor(boolean bPrevious)
    {
        if(m_iActiveElement == -1)
        {
            return false;
        }

        Integer index0 = m_iActiveElement;
        Integer index1 = bPrevious ? index0 - 1 : index0 + 1;

        if(index1 < 0 || index1 >= m_SortOrder.m_CategoriesOrder.size())
        {
            return false;
        }

        Categories.Category c0 = m_SortOrder.m_CategoriesOrder.get(index0);
        Categories.Category c1 = m_SortOrder.m_CategoriesOrder.get(index1);

        m_SortOrder.m_CategoriesOrder.set(index0, c1);
        m_SortOrder.m_CategoriesOrder.set(index1, c0);

        m_iActiveElement = index1;

        notifyItemChanged(index0);
        notifyItemChanged(index1);

        return true;
    }

    @Override
    public CategoriesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.text_row_item, parent, false);

        CategoriesAdapter.ViewHolder vh = new CategoriesAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(CategoriesAdapter.ViewHolder holder, int position)
    {
        Categories.Category category = m_SortOrder.m_CategoriesOrder.get(position);
        holder.m_TextView.setText(category.getName());

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
        return m_SortOrder.m_CategoriesOrder.size();
    }
}
