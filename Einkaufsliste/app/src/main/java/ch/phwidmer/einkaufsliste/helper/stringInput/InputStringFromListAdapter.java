package ch.phwidmer.einkaufsliste.helper.stringInput;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ch.phwidmer.einkaufsliste.R;
import ch.phwidmer.einkaufsliste.helper.Helper;

class InputStringFromListAdapter extends RecyclerView.Adapter<InputStringFromListAdapter.ViewHolder> implements Filterable
{
    private ArrayList<String> m_allItems;
    private ArrayList<Pair<String, String>> m_ItemsWithCompareKey;
    private ArrayList<String> m_currentItems;

    private String m_ActiveElement;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private FrameLayout m_Frame;
        private TextView m_TextView;
        private String m_id;
        ViewHolder(@NonNull View v)
        {
            super(v);
            m_Frame = v.findViewById(R.id.frmaeLayout);
            m_TextView = v.findViewById(R.id.textView);
            m_id = "";
        }
    }

    InputStringFromListAdapter(@NonNull ArrayList<String> allItems)
    {
        m_allItems = allItems;
        m_currentItems = allItems;
        m_ItemsWithCompareKey = new ArrayList<>(m_allItems.size());

        for(String item : m_allItems)
        {
            m_ItemsWithCompareKey.add(new Pair<>(item, Helper.stripAccents(item).toLowerCase()));
        }
    }

    @Override @NonNull
    public InputStringFromListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_item_input_string_from_list, parent, false);

        return new InputStringFromListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull InputStringFromListAdapter.ViewHolder holder, int position)
    {
        holder.m_id = m_currentItems.get(position);
        holder.m_TextView.setText(holder.m_id);

        updateAppearance(holder);
    }

    private void updateAppearance(@NonNull InputStringFromListAdapter.ViewHolder holder)
    {
        if(holder.m_id.equals(m_ActiveElement))
        {
            holder.m_TextView.setTypeface(holder.m_TextView.getTypeface(), Typeface.BOLD);
            holder.m_Frame.setBackgroundColor(Color.LTGRAY);
        }
        else
        {
            holder.m_TextView.setTypeface(null, Typeface.NORMAL);
            holder.m_Frame.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    String getActiveElement()
    {
        return m_ActiveElement;
    }

    boolean setActiveElement(int position)
    {
        if(position < 0 || position > m_currentItems.size())
        {
            return false;
        }

        m_ActiveElement = m_currentItems.get(position);
        notifyDataSetChanged();
        return true;
    }

    @Override
    public int getItemCount()
    {
        return m_currentItems.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                m_currentItems = (ArrayList<String>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<String> filteredResults;
                if (constraint.length() == 0) {
                    filteredResults = m_allItems;
                } else {
                    filteredResults = getFilteredResults(constraint.toString().toLowerCase());
                }

                FilterResults results = new FilterResults();
                results.values = filteredResults;

                return results;
            }
        };
    }

    private List<String> getFilteredResults(String constraint) {
        List<String> results = new ArrayList<>();

        String constraintWithoutAccents = Helper.stripAccents(constraint).toLowerCase();
        for (Pair<String, String> item : m_ItemsWithCompareKey) {
            if (item.second.contains(constraintWithoutAccents) || item.first.equals(m_ActiveElement)) {
                results.add(item.first);
            }
        }
        return results;
    }
}
