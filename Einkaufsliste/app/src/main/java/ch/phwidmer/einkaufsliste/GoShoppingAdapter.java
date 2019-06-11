package ch.phwidmer.einkaufsliste;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

public class GoShoppingAdapter extends RecyclerView.Adapter<GoShoppingAdapter.ViewHolder> implements ReactToTouchActionsInterface {

    private RecyclerView m_RecyclerView;
    private SortedShoppingList m_SortedList;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView m_TextView;
        private CheckBox m_CheckBox;
        private String m_id;
        private View m_View;
        public ViewHolder(View v)
        {
            super(v);
            m_View = v;
            m_TextView = v.findViewById(R.id.textView);
            m_CheckBox = v.findViewById(R.id.checkBox);
            m_id = "";
        }
    }

    GoShoppingAdapter(RecyclerView recyclerView, SortedShoppingList sortedList)
    {
        m_RecyclerView = recyclerView;
        m_SortedList = sortedList;
    }

    @Override @NonNull
    public GoShoppingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                           int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.text_row_chkbox_item, parent, false);

        return new GoShoppingAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GoShoppingAdapter.ViewHolder holder, int position)
    {
        holder.m_id = m_SortedList.getName(position);

        if(!m_SortedList.isCategory(position))
        {
            SortedShoppingList.CategoryShoppingItem item = m_SortedList.getListItem(position);

            String text = "";
            if(item.getAmount().m_Unit != Amount.Unit.Unitless)
            {
                text = Helper.formatNumber(item.getAmount().m_Quantity) + " " + Amount.shortFormAsPrefix(holder.itemView.getContext(), item.getAmount().m_Unit) + " ";
            }
            text += holder.m_id;
            if(item.getSize() != RecipeItem.Size.Normal)
            {
                text += " (" + RecipeItem.toUIString(holder.itemView.getContext(), item.getSize()) + ")";
            }
            holder.m_CheckBox.setText(text);
        }
        holder.m_TextView.setText(holder.m_id);

        updateAppearance(holder, position);
    }

    void updateAppearance(GoShoppingAdapter.ViewHolder holder, int position)
    {
        if(m_SortedList.isCategory(position))
        {
            holder.m_TextView.setTypeface(holder.m_TextView.getTypeface(), Typeface.BOLD);

            holder.m_CheckBox.setVisibility(View.GONE);
            holder.m_TextView.setVisibility(View.VISIBLE);

            if(m_SortedList.isIncompatibleItemsList(position))
            {
                holder.m_TextView.setTextColor(Color.RED);
            }
            else
            {
                holder.m_TextView.setTextColor(Color.BLACK);
            }

        }
        else
        {
            holder.m_CheckBox.setVisibility(View.VISIBLE);
            holder.m_TextView.setVisibility(View.GONE);

            SortedShoppingList.CategoryShoppingItem item = m_SortedList.getListItem(position);
            if(item.isOptional())
            {
                holder.m_CheckBox.setTextColor(Color.GRAY);
                holder.m_CheckBox.setTypeface(holder.m_CheckBox.getTypeface(), Typeface.ITALIC);
            }
            else
            {
                holder.m_CheckBox.setTextColor(Color.BLACK);
                holder.m_CheckBox.setTypeface(null, Typeface.NORMAL);
            }

            if(item.getStatus() != ShoppingListItem.Status.None)
            {
                holder.m_CheckBox.setPaintFlags(holder.m_CheckBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.m_CheckBox.setChecked(true);
            }
            else
            {
                holder.m_CheckBox.setPaintFlags(holder.m_CheckBox.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                holder.m_CheckBox.setChecked(false);
            }
        }
    }

    @Override
    public int getItemCount()
    {
        return m_SortedList.itemsCount();
    }

    @Override
    public void reactToSwipe(int position)
    {
        SortedShoppingList.CategoryShoppingItem item = m_SortedList.getListItem(position);
        item.invertStatus();
        notifyItemChanged(position);
    }

    public boolean swipeAllowed(RecyclerView.ViewHolder vh)
    {
        return !m_SortedList.isCategory(m_RecyclerView.getChildAdapterPosition(vh.itemView));
    }

    @Override
    public boolean reactToDrag(RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target)
    {
        return false;
    }

    public void clearViewBackground(RecyclerView.ViewHolder vh)
    {
        vh.itemView.setBackgroundColor(0);
    }
}
