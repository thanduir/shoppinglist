package ch.phwidmer.einkaufsliste.UI;

import android.annotation.SuppressLint;
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

import java.util.Optional;

import ch.phwidmer.einkaufsliste.data.Unit;
import ch.phwidmer.einkaufsliste.helper.Helper;
import ch.phwidmer.einkaufsliste.R;
import ch.phwidmer.einkaufsliste.data.RecipeItem;
import ch.phwidmer.einkaufsliste.data.ShoppingListItem;
import ch.phwidmer.einkaufsliste.helper.sortedshoppinglist.CategoryShoppingItem;
import ch.phwidmer.einkaufsliste.helper.sortedshoppinglist.SortedListItem;
import ch.phwidmer.einkaufsliste.helper.sortedshoppinglist.SortedShoppingList;

public class GoShoppingAdapter extends RecyclerView.Adapter<GoShoppingAdapter.ViewHolder>
{
    private SortedShoppingList m_SortedList;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView m_TextView;
        private CheckBox m_CheckBox;
        private TextView m_TextViewAdditionalInfo;
        private String m_id;
        public ViewHolder(@NonNull View v)
        {
            super(v);
            m_TextView = v.findViewById(R.id.textView);
            m_CheckBox = v.findViewById(R.id.checkBox);
            m_TextViewAdditionalInfo = v.findViewById(R.id.textViewAdditionalInfo);
            m_id = "";
        }
    }

    GoShoppingAdapter(@NonNull SortedShoppingList sortedList)
    {
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
        Optional<SortedListItem> listItem = m_SortedList.getItem(position);
        if(!listItem.isPresent())
        {
            return;
        }
        holder.m_id = listItem.get().getName();

        if(listItem.get().getType() == SortedListItem.ShoppingItemType.INGREDIENT)
        {
            Optional<CategoryShoppingItem> item = listItem.get().getShoppingItem();
            if(!item.isPresent())
            {
                return;
            }

            String text = "";
            if(item.get().getAmount().getUnit() != Unit.Unitless)
            {
                text = Helper.formatNumber(item.get().getAmount().getQuantityMin());
                if(item.get().getAmount().isRange())
                {
                    text += "-" + Helper.formatNumber(item.get().getAmount().getQuantityMax());
                }
                text += " " + Unit.shortFormAsPrefix(holder.itemView.getContext(), item.get().getAmount().getUnit()) + " ";
            }
            text += holder.m_id;
            holder.m_CheckBox.setText(text);

            String additionalText = "";
            if(item.get().getSize() != RecipeItem.Size.Normal)
            {
                additionalText += RecipeItem.Size.toUIString(holder.itemView.getContext(), item.get().getSize());
            }
            if(!item.get().getAdditionalInfo().isEmpty())
            {
                if(!additionalText.isEmpty())
                {
                    additionalText += ", ";
                }
                additionalText += item.get().getAdditionalInfo();
            }
            if(!additionalText.isEmpty())
            {
                additionalText = " (" + additionalText + ")";
            }
            holder.m_TextViewAdditionalInfo.setText(additionalText);
        }
        holder.m_TextView.setText(holder.m_id);

        updateAppearance(holder, listItem.get());
    }

    @SuppressLint("SetTextI18n")
    void updateAppearance(@NonNull GoShoppingAdapter.ViewHolder holder, SortedListItem listItem)
    {
        if(listItem.getType() != SortedListItem.ShoppingItemType.INGREDIENT)
        {
            holder.m_TextView.setTypeface(holder.m_TextView.getTypeface(), Typeface.BOLD);

            holder.m_CheckBox.setVisibility(View.GONE);
            holder.m_TextViewAdditionalInfo.setVisibility(View.GONE);
            holder.m_TextView.setVisibility(View.VISIBLE);

            if(listItem.getType() == SortedListItem.ShoppingItemType.TOPLEVEL_HEADER)
            {
                holder.m_TextView.setTextColor(Color.BLUE);
            }
            else if(listItem.getType() == SortedListItem.ShoppingItemType.CATEGORY_HEADER)
            {
                holder.m_TextView.setTextColor(Color.BLACK);
                holder.m_TextView.setText("  " + holder.m_TextView.getText());
            }
            else // INCOMPATIBLE_ITEMS_HEADER
            {
                holder.m_TextView.setTextColor(Color.RED);
                holder.m_TextView.setText("  " + holder.m_TextView.getText());
            }

        }
        else
        {
            holder.m_CheckBox.setVisibility(View.VISIBLE);
            holder.m_TextViewAdditionalInfo.setVisibility(View.VISIBLE);
            holder.m_TextView.setVisibility(View.GONE);

            holder.m_TextViewAdditionalInfo.setTextColor(Color.GRAY);

            Optional<CategoryShoppingItem> item = listItem.getShoppingItem();
            if(!item.isPresent())
            {
                return;
            }

            if(item.get().isOptional())
            {
                holder.m_CheckBox.setTextColor(Color.GRAY);
                holder.m_CheckBox.setTypeface(holder.m_CheckBox.getTypeface(), Typeface.ITALIC);
            }
            else
            {
                holder.m_CheckBox.setTextColor(Color.BLACK);
                holder.m_CheckBox.setTypeface(null, Typeface.NORMAL);
            }

            Optional<ShoppingListItem.Status> itemStatus = item.get().getStatus();
            if(itemStatus.isPresent() && itemStatus.get() != ShoppingListItem.Status.None)
            {
                holder.m_CheckBox.setPaintFlags(holder.m_CheckBox.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.m_CheckBox.setChecked(true);
                holder.m_TextViewAdditionalInfo.setPaintFlags(holder.m_TextViewAdditionalInfo.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            else
            {
                holder.m_CheckBox.setPaintFlags(holder.m_CheckBox.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                holder.m_CheckBox.setChecked(false);
                holder.m_TextViewAdditionalInfo.setPaintFlags(holder.m_TextViewAdditionalInfo.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        }
    }

    @Override
    public int getItemCount()
    {
        return m_SortedList.itemsCount();
    }
}
