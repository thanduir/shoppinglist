package ch.phwidmer.einkaufsliste.helper;

import android.support.v7.widget.RecyclerView;

// Interface related to ReactToTouchActionsCallback. Allows reactions to touch events
public interface ReactToTouchActionsInterface
{
    void reactToSwipe(int position);
    boolean reactToDrag(RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target);
    boolean swipeAllowed(RecyclerView.ViewHolder vh);
    void clearViewBackground(RecyclerView.ViewHolder vh);
}
