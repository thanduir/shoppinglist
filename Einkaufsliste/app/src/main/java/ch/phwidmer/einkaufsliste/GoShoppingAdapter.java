package ch.phwidmer.einkaufsliste;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class GoShoppingAdapter extends RecyclerView.Adapter<GoShoppingAdapter.ViewHolder> {

    private SortedShoppingList m_SortedList;

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

    public GoShoppingAdapter(SortedShoppingList sortedList)
    {
        m_SortedList = sortedList;
    }

    @Override
    public GoShoppingAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.text_row_item, parent, false);

        GoShoppingAdapter.ViewHolder vh = new GoShoppingAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(GoShoppingAdapter.ViewHolder holder, int position)
    {
        holder.m_TextView.setText(m_SortedList.getName(position));

        updateAppearance(holder, position);
    }

    public void updateAppearance(GoShoppingAdapter.ViewHolder holder, int position)
    {
        if(m_SortedList.isCategory(position))
        {
            holder.m_View.setBackgroundColor(Color.GRAY);
        }
        else
        {
            holder.m_View.setBackgroundColor(m_SortedList.getListItem(position).m_Status == ShoppingListItem.Status.None ? Color.TRANSPARENT : Color.BLACK);
        }
    }

    @Override
    public int getItemCount()
    {
        return m_SortedList.itemsCount();
    }
}
