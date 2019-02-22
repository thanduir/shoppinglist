package ch.phwidmer.einkaufsliste;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

public class ReactToTouchActionsCallback<Myadapter extends ReactToTouchActionsInterface> extends ItemTouchHelper.Callback
{
    private Boolean m_bAllowDrag;
    private Myadapter m_Adapter;

    private Drawable  m_DeleteIcon;

    public ReactToTouchActionsCallback(Myadapter adapter, Context context, int swipeIcon, boolean bAllowDrag)
    {
        m_bAllowDrag = bAllowDrag;
        m_Adapter = adapter;

        m_DeleteIcon = ContextCompat.getDrawable(context, swipeIcon);
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder)
    {
        final int swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        final int dragFlags = m_bAllowDrag ? ItemTouchHelper.UP | ItemTouchHelper.DOWN : 0;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        m_Adapter.reactToSwipe(position);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target)
    {
        return m_Adapter.reactToDrag(vh, target);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive)
    {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) {
            return;
        }

        View itemView = viewHolder.itemView;

        int iconIntrinsicHeight = m_DeleteIcon.getIntrinsicHeight();
        int iconIntrinsicWidth = m_DeleteIcon.getIntrinsicWidth();

        int viewHeight = itemView.getHeight();
        int viewTop = itemView.getTop();
        int viewLeft = itemView.getLeft();
        int viewRight = itemView.getRight();

        int iconMargin = (viewHeight - iconIntrinsicHeight) / 2;
        int iconTop = viewTop + (viewHeight - iconIntrinsicHeight) / 2;
        int iconBottom = iconTop + iconIntrinsicHeight;

        if(dX > 0) // Swiping to the right
        {
            int iconLeft = viewLeft + iconMargin ;
            int iconRight = viewLeft + iconMargin + iconIntrinsicWidth;
            m_DeleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
        }
        else if(dX < 0) // Swiping to the left
        {
            int iconLeft = viewRight - iconMargin - iconIntrinsicWidth;
            int iconRight = viewRight - iconMargin;
            m_DeleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
        }
        else // view is unswiped
        {
            m_DeleteIcon.setBounds(0, 0, 0, 0);
        }

        m_DeleteIcon.draw(c);
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder,
                                  int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE)
        {
            viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
        }

        super.onSelectedChanged(viewHolder, actionState);
    }
    @Override
    public void clearView(RecyclerView recyclerView,
                          RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);

        viewHolder.itemView.setBackgroundColor(0);
    }
}
