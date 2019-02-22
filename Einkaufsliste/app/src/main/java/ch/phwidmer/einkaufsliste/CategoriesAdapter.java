package ch.phwidmer.einkaufsliste;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> implements ReactToTouchActionsInterface
{
    private Categories m_Categories;
    private Categories.SortOrder m_SortOrder;

    private Categories.Category m_RecentlyDeleted;
    private RecyclerView m_RecyclerView;
    private ItemTouchHelper m_TouchHelper = null;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView m_TextView;
        public View m_View;
        public ImageView m_ReorderView;
        public ViewHolder(View v)
        {
            super(v);
            m_View = v;
            m_TextView = v.findViewById(R.id.textView);
            m_ReorderView = v.findViewById(R.id.reorder_view);
        }
    }

    public CategoriesAdapter(RecyclerView recyclerView, Categories categories, Categories.SortOrder sortOrder)
    {
        m_Categories = categories;
        m_SortOrder = sortOrder;
        m_RecyclerView = recyclerView;
    }



    public void setTouchHelper(ItemTouchHelper touchHelper)
    {
        m_TouchHelper = touchHelper;
    }

    @Override
    public CategoriesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.text_row_item_drag, parent, false);

        CategoriesAdapter.ViewHolder vh = new CategoriesAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final CategoriesAdapter.ViewHolder holder, int position)
    {
        Categories.Category category = m_SortOrder.m_CategoriesOrder.get(position);
        holder.m_TextView.setText(category.getName());

        holder.m_ReorderView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    m_TouchHelper.startDrag(holder);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return m_SortOrder.m_CategoriesOrder.size();
    }

    public void reactToSwipe(int position)
    {
        // Remove element

        Categories.Category category = m_SortOrder.m_CategoriesOrder.get(position);
        m_Categories.removeCategory(category.getName());
        notifyItemRemoved(position);

        // Allow undo

        m_RecentlyDeleted = category;
        Snackbar snackbar = Snackbar.make(m_RecyclerView, "Item deleted", Snackbar.LENGTH_LONG);
        snackbar.setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_Categories.addCategory(m_RecentlyDeleted.getName());
                notifyDataSetChanged();

                Snackbar snackbar1 = Snackbar.make(m_RecyclerView, "Item restored", Snackbar.LENGTH_SHORT);
                snackbar1.show();
            }
        });
        snackbar.show();
    }

    public boolean reactToDrag(RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target)
    {
        int oldPos = vh.getAdapterPosition();
        int newPos = target.getAdapterPosition();

        Categories.Category category = m_SortOrder.m_CategoriesOrder.get(oldPos);
        m_SortOrder.m_CategoriesOrder.remove(oldPos);
        m_SortOrder.m_CategoriesOrder.insertElementAt(category, newPos);
        notifyItemMoved(oldPos, newPos);
        return true;
    }
}
