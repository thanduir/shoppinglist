package ch.phwidmer.einkaufsliste;

import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
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

    private Ingredients m_Ingredients;

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

    public CategoriesAdapter(RecyclerView recyclerView, Categories categories, Categories.SortOrder sortOrder, Ingredients ingredients)
    {
        m_Categories = categories;
        m_SortOrder = sortOrder;
        m_RecyclerView = recyclerView;
        m_Ingredients = ingredients;
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

        if(m_Ingredients.isCategoryInUse(category))
        {
            notifyItemChanged(position);
            AlertDialog.Builder builder = new AlertDialog.Builder(m_RecyclerView.getContext());
            builder.setTitle(m_RecyclerView.getContext().getResources().getString(R.string.text_delete_category_disallowed_header));
            builder.setMessage(m_RecyclerView.getContext().getResources().getString(R.string.text_delete_category_disallowed_desc, category.getName()));
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.show();
            return;
        }

        m_Categories.removeCategory(category.getName());
        notifyItemRemoved(position);

        // Allow undo

        m_RecentlyDeleted = category;
        Snackbar snackbar = Snackbar.make(m_RecyclerView, R.string.text_item_deleted, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.text_undo, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_Categories.addCategory(m_RecentlyDeleted.getName());
                notifyDataSetChanged();

                m_RecentlyDeleted = null;

                Snackbar snackbar1 = Snackbar.make(m_RecyclerView, R.string.text_item_restored, Snackbar.LENGTH_SHORT);
                snackbar1.show();
            }
        });
        snackbar.show();
    }

    public boolean swipeAllowed(RecyclerView.ViewHolder vh) { return true; }

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
