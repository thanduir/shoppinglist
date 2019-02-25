package ch.phwidmer.einkaufsliste;

import android.support.v7.widget.RecyclerView;

public interface ReactToTouchActionsInterface
{
    public void reactToSwipe(int position);
    public boolean reactToDrag(RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target);
    public boolean swipeAllowed(RecyclerView.ViewHolder vh);
}
