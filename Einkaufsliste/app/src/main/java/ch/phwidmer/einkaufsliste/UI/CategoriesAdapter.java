package ch.phwidmer.einkaufsliste.UI;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ch.phwidmer.einkaufsliste.helper.InputStringDialogFragment;
import ch.phwidmer.einkaufsliste.R;
import ch.phwidmer.einkaufsliste.data.Categories;
import ch.phwidmer.einkaufsliste.data.Ingredients;
import ch.phwidmer.einkaufsliste.helper.ReactToTouchActionsInterface;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> implements ReactToTouchActionsInterface
{
    private Categories m_Categories;
    private Categories.SortOrder m_SortOrder;

    private Ingredients m_Ingredients;

    private Categories.Category m_RecentlyDeleted;
    private RecyclerView m_RecyclerView;
    private ItemTouchHelper m_TouchHelper = null;
    private CoordinatorLayout m_CoordLayout;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView m_TextView;
        View m_View;
        ImageView m_ReorderView;
        public ViewHolder(View v)
        {
            super(v);
            m_View = v;
            m_TextView = v.findViewById(R.id.textView);
            m_ReorderView = v.findViewById(R.id.reorder_view);
        }
    }

    CategoriesAdapter(CoordinatorLayout coordLayout, RecyclerView recyclerView, Categories categories, Categories.SortOrder sortOrder, Ingredients ingredients)
    {
        m_Categories = categories;
        m_SortOrder = sortOrder;
        m_RecyclerView = recyclerView;
        m_Ingredients = ingredients;
        m_CoordLayout = coordLayout;
    }



    void setTouchHelper(ItemTouchHelper touchHelper)
    {
        m_TouchHelper = touchHelper;
    }

    @Override @NonNull
    public CategoriesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                           int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.text_row_item_drag, parent, false);

        return new CategoriesAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final CategoriesAdapter.ViewHolder holder, int position)
    {
        final Categories.Category category = m_SortOrder.getOrder().get(position);
        holder.m_TextView.setText(category.getName());

        holder.m_TextView.setOnLongClickListener((View v) ->
        {
                renameCategory(category);
                return true;
        });

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
        return m_Categories.getCategoriesCount();
    }

    public void reactToSwipe(int position)
    {
        // Remove element

        Categories.Category category = m_SortOrder.getOrder().get(position);

        ArrayList<String> ingredientsUsingCategory = new ArrayList<>();
        if(m_Ingredients.isCategoryInUse(category, ingredientsUsingCategory))
        {
            notifyItemChanged(position);
            AlertDialog.Builder builder = new AlertDialog.Builder(m_RecyclerView.getContext());
            builder.setTitle(m_RecyclerView.getContext().getResources().getString(R.string.text_delete_category_disallowed_header));
            builder.setMessage(m_RecyclerView.getContext().getResources().getString(R.string.text_delete_category_disallowed_desc,
                                                                                    category.getName(),
                                                                                    ingredientsUsingCategory.toString()));
            builder.setPositiveButton(android.R.string.ok, (DialogInterface dialog, int which) -> {});
            builder.show();
            return;
        }

        m_Categories.removeCategory(category);
        notifyDataSetChanged();

        // Allow undo

        m_RecentlyDeleted = category;
        Snackbar snackbar = Snackbar.make(m_CoordLayout, R.string.text_item_deleted, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.text_undo, (View view) ->
        {
            m_Categories.addCategory(m_RecentlyDeleted.getName());
            notifyDataSetChanged();

            m_RecentlyDeleted = null;

            Snackbar snackbar1 = Snackbar.make(m_CoordLayout, R.string.text_item_restored, Snackbar.LENGTH_SHORT);
            snackbar1.show();
        });
        snackbar.show();
    }

    public boolean swipeAllowed(RecyclerView.ViewHolder vh) { return true; }

    public boolean reactToDrag(RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target)
    {
        int oldPos = vh.getAdapterPosition();
        int newPos = target.getAdapterPosition();

        Categories.Category category = m_SortOrder.getOrder().get(oldPos);
        m_SortOrder.moveCategory(category, newPos);
        notifyItemMoved(oldPos, newPos);
        return true;
    }

    private void renameCategory(final Categories.Category category)
    {
        InputStringDialogFragment newFragment = InputStringDialogFragment.newInstance(m_RecyclerView.getContext().getResources().getString(R.string.text_rename_category, category.getName()));
        newFragment.setDefaultValue(category.getName());
        newFragment.setAdditionalInformation(category.getName());
        newFragment.setListExcludedInputs(m_Categories.getAllCategorieNames());
        newFragment.show(((AppCompatActivity) m_RecyclerView.getContext()).getSupportFragmentManager(), "renameCategory");
    }

    public void clearViewBackground(RecyclerView.ViewHolder vh)
    {
        vh.itemView.setBackgroundColor(0);
    }
}
