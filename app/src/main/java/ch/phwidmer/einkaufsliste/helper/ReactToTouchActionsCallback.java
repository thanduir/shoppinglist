package ch.phwidmer.einkaufsliste.helper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

// Add touch reactions to a adapter. The adapter must implement ReactToTouchActionsInterface in order to be able to react to the events.
public class ReactToTouchActionsCallback extends ItemTouchHelper.Callback
{
    private Boolean m_bAllowDrag;
    private ReactToTouchActionsInterface m_Adapter;

    private Drawable  m_SwipeIcon;

    // Remark: We save the recyclerView instead of the adapter in order to still work correctly if the adapter gets reset.
    public ReactToTouchActionsCallback(ReactToTouchActionsInterface adapter, Context context, int swipeIcon, boolean bAllowDrag)
    {
        m_bAllowDrag = bAllowDrag;
        m_Adapter = adapter;

        m_SwipeIcon = ContextCompat.getDrawable(context, swipeIcon);
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder)
    {
        boolean bSwipeAlloed = m_Adapter.swipeAllowed(viewHolder);
        final int swipeFlags = bSwipeAlloed ? ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT : 0;
        final int dragFlags = m_bAllowDrag ? ItemTouchHelper.UP | ItemTouchHelper.DOWN : 0;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        m_Adapter.reactToSwipe(position);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder target)
    {
        return m_Adapter.reactToDrag(vh, target);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive)
    {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) {
            return;
        }

        View itemView = viewHolder.itemView;

        int iconIntrinsicHeight = m_SwipeIcon.getIntrinsicHeight();
        int iconIntrinsicWidth = m_SwipeIcon.getIntrinsicWidth();

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
            m_SwipeIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
        }
        else if(dX < 0) // Swiping to the left
        {
            int iconLeft = viewRight - iconMargin - iconIntrinsicWidth;
            int iconRight = viewRight - iconMargin;
            m_SwipeIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
        }
        else // view is unswiped
        {
            m_SwipeIcon.setBounds(0, 0, 0, 0);
        }

        m_SwipeIcon.draw(c);
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
    public void clearView(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder)
    {
        super.clearView(recyclerView, viewHolder);
        m_Adapter.clearViewBackground(viewHolder);
    }
}
