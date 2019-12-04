package ch.phwidmer.einkaufsliste.UI;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;

import ch.phwidmer.einkaufsliste.data.Unit;
import ch.phwidmer.einkaufsliste.helper.Helper;
import ch.phwidmer.einkaufsliste.helper.InputStringDialogFragment;
import ch.phwidmer.einkaufsliste.R;
import ch.phwidmer.einkaufsliste.data.Ingredients;
import ch.phwidmer.einkaufsliste.data.RecipeItem;
import ch.phwidmer.einkaufsliste.data.Recipes;
import ch.phwidmer.einkaufsliste.helper.ReactToTouchActionsInterface;

public class RecipeItemsAdapter extends RecyclerView.Adapter<RecipeItemsAdapter.ViewHolder> implements ReactToTouchActionsInterface, AdapterView.OnItemSelectedListener
{
    private static final int TYPE_INACTIVE = 1;
    private static final int TYPE_ACTIVE = 2;
    private static final int TYPE_GROUP_HEADER = 3;
    private static final int TYPE_GROUP_ELEMENT_ACTIVE = 4;
    private static final int TYPE_GROUP_ELEMENT_INACTIVE = 5;

    private static final String GROUP_PREFIX = "#Group_";

    private Recipes.Recipe      m_Recipe;
    private Ingredients         m_Ingredients;
    private RecyclerView        m_RecyclerView;
    private CoordinatorLayout   m_CoordLayout;
    private Integer             m_iActiveElement;

    private RecipeItem              m_RecentlyDeleted;
    private String                  m_GroupOfRecentlyDeletedItem;
    private int                     m_RecentlyDeletedIndex;
    private ArrayList<RecipeItem>   m_ItemsOfRecentlyDeletedGroup;

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        boolean     m_PartOfGroup;
        TextView    m_TextView;
        View        m_View;
        String      m_id;

        public ViewHolder(@NonNull View v, boolean partOfGroup)
        {
            super(v);
            m_View = v;
            m_TextView = v.findViewById(R.id.textView);
            m_PartOfGroup = partOfGroup;
            m_id = "";
        }

        boolean isPartOfGroup()
        {
            return m_PartOfGroup;
        }

        String getID()
        {
            return m_id;
        }
    }

    public static class ViewHolderInactive extends RecipeItemsAdapter.ViewHolder
    {
        private TextView    m_TextViewDesc;

        ViewHolderInactive(@NonNull View v, boolean partOfGroup)
        {
            super(v, partOfGroup);
            m_TextViewDesc = v.findViewById(R.id.textViewDesc);
        }

        void setDescription(@NonNull Context context, @NonNull RecipeItem item)
        {
            String text = "";
            if(item.getAmount().getUnit() != Unit.Unitless)
            {
                text += Helper.formatNumber(item.getAmount().getQuantityMin());
                if(item.getAmount().isRange())
                {
                    text += "-" + Helper.formatNumber(item.getAmount().getQuantityMax());
                }
                text += " " + Unit.shortForm(context, item.getAmount().getUnit());
            }
            if(item.getSize() != RecipeItem.Size.Normal)
            {
                if(!text.isEmpty())
                {
                    text += ", ";
                }
                text += RecipeItem.toUIString(context, item.getSize());
            }
            if(!item.getAdditionalInfo().isEmpty())
            {
                if(!text.isEmpty())
                {
                    text += ", ";
                }
                text += item.getAdditionalInfo();
            }

            String fullText = "";
            if(!text.isEmpty() )
            {
                fullText = " (" + text + ")";
            }
            m_TextViewDesc.setText(fullText);
        }
    }

    public static class ViewHolderActive extends RecipeItemsAdapter.ViewHolder
    {
        private CheckBox    m_CheckBoxOptional;
        private Spinner     m_SpinnerAmount;
        private EditText    m_EditTextAmount;
        private Spinner     m_SpinnerSize;
        private EditText    m_AdditionalInfo;
        private TableRow    m_TableRowAmount;
        private CheckBox    m_CheckBoxAmountRange;
        private TableRow    m_TableRowAmountRange;
        private EditText    m_EditTextAmountMax;
        private TextView    m_TextViewAmountMin;

        ViewHolderActive(@NonNull View v, boolean partOfGroup)
        {
            super(v, partOfGroup);
            m_SpinnerAmount = v.findViewById(R.id.spinnerAmount);
            m_EditTextAmount = v.findViewById(R.id.editText_Amount);
            m_SpinnerSize = v.findViewById(R.id.spinnerSize);
            m_CheckBoxOptional = v.findViewById(R.id.checkBoxOptional);
            m_AdditionalInfo = v.findViewById(R.id.editText_AdditonalInfo);
            m_TableRowAmount = v.findViewById(R.id.tableRowAmount);
            m_CheckBoxAmountRange = v.findViewById(R.id.checkBoxAmountRange);
            m_TableRowAmountRange = v.findViewById(R.id.tableRowAmountRange);
            m_EditTextAmountMax = v.findViewById(R.id.editText_AmountMax);
            m_TextViewAmountMin = v.findViewById(R.id.textViewMinAmount);
        }

        void updateAppearance(@NonNull RecipeItem item)
        {
            if(item.isOptional())
            {
                m_TextView.setTextColor(Color.GRAY);
            }
            else
            {
                m_TextView.setTextColor(Color.BLACK);
                if(isPartOfGroup())
                {
                    m_TextView.setTypeface(null, Typeface.NORMAL);
                }
                else
                {
                    m_TextView.setTypeface(m_TextView.getTypeface(), Typeface.BOLD);
                }
            }
        }
    }

    public static class ViewHolderGroupHeader extends RecipeItemsAdapter.ViewHolder
    {
        ImageView m_ImageViewAddRecipeItemToGroup;
        ImageView m_ImageViewDeleteGroup;

        ViewHolderGroupHeader(@NonNull View v)
        {
            super(v, true);

            m_ImageViewAddRecipeItemToGroup = v.findViewById(R.id.button_addRecipeItemToGroup);
            m_ImageViewDeleteGroup = v.findViewById(R.id.button_deleteGroup);
        }
    }

    RecipeItemsAdapter(@NonNull CoordinatorLayout coordLayout, @NonNull RecyclerView recyclerView, @NonNull Recipes.Recipe recipe, @NonNull Ingredients ingredients)
    {
        m_iActiveElement = -1;
        m_RecyclerView = recyclerView;
        m_Recipe = recipe;
        m_Ingredients = ingredients;
        m_CoordLayout = coordLayout;
    }

    @Override
    public int getItemViewType(int position)
    {
        if(position < m_Recipe.getAllRecipeItems().size())
        {
            if (m_iActiveElement == position)
            {
                return TYPE_ACTIVE;
            } else
            {
                return TYPE_INACTIVE;
            }
        }
        else
        {
            if(getRecipeItemsList().get(position).startsWith(GROUP_PREFIX))
            {
                return TYPE_GROUP_HEADER;
            }
            else if(m_iActiveElement == position)
            {
                return TYPE_GROUP_ELEMENT_ACTIVE;
            } else
            {
                return TYPE_GROUP_ELEMENT_INACTIVE;
            }
        }
    }

    @Override @NonNull
    public RecipeItemsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                            int viewType)
    {
        switch(viewType)
        {
            case TYPE_INACTIVE:
            {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_item_edit_recipieitem_inactive, parent, false);

                return new RecipeItemsAdapter.ViewHolderInactive(v, false);
            }

            case TYPE_GROUP_ELEMENT_INACTIVE:
            {
                View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_item_edit_recipieitem_inactive, parent, false);

                return new RecipeItemsAdapter.ViewHolderInactive(v, true);
            }

            case TYPE_ACTIVE:
            {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_item_edit_recipieitem_active, parent, false);

                return new RecipeItemsAdapter.ViewHolderActive(v, false);
            }

            case TYPE_GROUP_ELEMENT_ACTIVE:
            {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_item_edit_recipieitem_active, parent, false);

                return new RecipeItemsAdapter.ViewHolderActive(v, true);
            }

            case TYPE_GROUP_HEADER:
            default:
            {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_item_edit_recipieitem_group_header, parent, false);

                return new RecipeItemsAdapter.ViewHolderGroupHeader(v);
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeItemsAdapter.ViewHolder holder, int position)
    {
        String strRecipeItem = getRecipeItemsList().get(position);
        boolean isGroupHeader = strRecipeItem.startsWith(GROUP_PREFIX);
        holder.m_id = strRecipeItem;
        if(isGroupHeader)
        {
            holder.m_TextView.setText(strRecipeItem.replace(GROUP_PREFIX, ""));
        }
        else if(holder.isPartOfGroup())
        {
            holder.m_TextView.setText(String.format(Locale.getDefault(), "\t\u2022 %s", strRecipeItem));
        }
        else
        {
            holder.m_TextView.setText(strRecipeItem);
        }

        if(isGroupHeader)
        {
            updateViewHolderGroupHeader(holder);
        }
        else if(m_iActiveElement == position)
        {
            updateViewHolderActive(holder);
        }
        else
        {
            updateViewHolderInactive(holder);
        }
    }

    @Override
    public int getItemCount()
    {
        return getRecipeItemsList().size();
    }

    String getActiveElement()
    {
        if(m_iActiveElement == -1)
        {
            return "";
        }
        return getRecipeItemsList().get(m_iActiveElement);
    }

    void setActiveElement(String strElement)
    {
        if(m_iActiveElement != -1)
        {
            notifyItemChanged(m_iActiveElement);
        }

        if(strElement.equals(""))
        {
            m_iActiveElement = -1;
        }
        else
        {
            m_iActiveElement = getRecipeItemsList().indexOf(strElement);
            notifyItemChanged(m_iActiveElement);
        }
    }

    private void updateViewHolderGroupHeader(@NonNull RecipeItemsAdapter.ViewHolder holder)
    {
        RecipeItemsAdapter.ViewHolderGroupHeader vh = (RecipeItemsAdapter.ViewHolderGroupHeader)holder;
        vh.m_TextView.setTextColor(Color.BLACK);

        String groupName = vh.getID().replace(GROUP_PREFIX, "");

        vh.m_ImageViewAddRecipeItemToGroup.setOnClickListener((View view) -> onAddItemToGroup(groupName));
        vh.m_ImageViewDeleteGroup.setOnClickListener((View view) -> onDeleteGroup(groupName));

        vh.m_TextView.setOnLongClickListener((View view) ->
        {
            onRenameGroup(groupName);
            return true;
        });
    }

    private void updateViewHolderInactive(@NonNull RecipeItemsAdapter.ViewHolder holder)
    {
        RecipeItemsAdapter.ViewHolderInactive vh = (RecipeItemsAdapter.ViewHolderInactive)holder;
        Optional<RecipeItem> item = getRecipeItem(vh.m_id);
        if(!item.isPresent())
        {
            return;
        }
        vh.setDescription(holder.itemView.getContext(), item.get());

        if(item.get().isOptional())
        {
            vh.m_TextView.setTextColor(Color.GRAY);
            vh.m_TextView.setTypeface(vh.m_TextView.getTypeface(), Typeface.ITALIC);

            vh.m_TextViewDesc.setTextColor(Color.GRAY);
            vh.m_TextViewDesc.setTypeface(vh.m_TextViewDesc.getTypeface(), Typeface.ITALIC);
        }
        else
        {
            vh.m_TextView.setTextColor(Color.BLACK);
            vh.m_TextViewDesc.setTypeface(null, Typeface.NORMAL);
            if(vh.isPartOfGroup())
            {
                vh.m_TextView.setTypeface(null, Typeface.NORMAL);
                vh.m_TextViewDesc.setTextColor(Color.GRAY);
            }
            else
            {
                vh.m_TextView.setTypeface(vh.m_TextView.getTypeface(), Typeface.BOLD);
                vh.m_TextViewDesc.setTextColor(Color.BLACK);
            }
        }

        vh.m_TextViewDesc.setVisibility(View.VISIBLE);
        vh.m_View.setBackgroundColor(Color.TRANSPARENT);
    }

    private void updateViewHolderActive(@NonNull RecipeItemsAdapter.ViewHolder holder)
    {
        RecipeItemsAdapter.ViewHolderActive vh = (RecipeItemsAdapter.ViewHolderActive)holder;

        Optional<RecipeItem> item = getRecipeItem(vh.m_id);
        if(!item.isPresent())
        {
            return;
        }

        vh.updateAppearance(item.get());
        vh.m_View.setBackgroundColor(ContextCompat.getColor(vh.m_View.getContext(), R.color.colorHighlightedBackground));

        ArrayAdapter<CharSequence> adapterAmount = new ArrayAdapter<>(vh.m_View.getContext(), R.layout.spinner_item);
        for(Unit u : Unit.values())
        {
            adapterAmount.add(Unit.toUIString(vh.itemView.getContext(), u));
        }
        adapterAmount.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vh.m_SpinnerAmount.setAdapter(adapterAmount);
        vh.m_SpinnerAmount.setOnItemSelectedListener(this);
        vh.m_SpinnerAmount.setSelection(item.get().getAmount().getUnit().ordinal());

        ArrayAdapter<CharSequence> adapterSize = new ArrayAdapter<>(vh.m_View.getContext(), R.layout.spinner_item);
        for(RecipeItem.Size size : RecipeItem.Size.values())
        {
            adapterSize.add(RecipeItem.toUIString(vh.itemView.getContext(), size));
        }
        adapterSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vh.m_SpinnerSize.setAdapter(adapterSize);
        vh.m_SpinnerSize.setOnItemSelectedListener(this);
        vh.m_SpinnerSize.setSelection(item.get().getSize().ordinal());

        if(m_RecyclerView.getLayoutManager() == null)
        {
            return;
        }

        vh.m_CheckBoxOptional.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) ->
        {
            View child = m_RecyclerView.getLayoutManager().findViewByPosition(m_iActiveElement);
            if(child == null)
            {
                return;
            }
            RecipeItemsAdapter.ViewHolder viewHolder = (RecipeItemsAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(child);
            Optional<RecipeItem> recipeItem = getRecipeItem(viewHolder.getID());
            if(!recipeItem.isPresent())
            {
                return;
            }

            recipeItem.get().setIsOptional(isChecked);
            vh.updateAppearance(recipeItem.get());
        });
        vh.m_CheckBoxOptional.setChecked(item.get().isOptional());

        vh.m_AdditionalInfo.setText(item.get().getAdditionalInfo());
        vh.m_AdditionalInfo.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                View child = m_RecyclerView.getLayoutManager().findViewByPosition(m_iActiveElement);
                if(child == null)
                {
                    // Item not visible (yet) -> nothing to do
                    return;
                }

                RecipeItemsAdapter.ViewHolder vh = (RecipeItemsAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(child);
                Optional<RecipeItem> item = getRecipeItem(vh.getID());
                if(!item.isPresent())
                {
                    return;
                }
                item.get().setAdditionInfo(s.toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        vh.m_EditTextAmount.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                View child = m_RecyclerView.getLayoutManager().findViewByPosition(m_iActiveElement);
                if(child == null)
                {
                    // Item not visible (yet) -> nothing to do
                    return;
                }

                RecipeItemsAdapter.ViewHolder vh = (RecipeItemsAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(child);
                Optional<RecipeItem> item = getRecipeItem(vh.getID());
                if(!item.isPresent())
                {
                    return;
                }
                if(s.toString().isEmpty())
                {
                    item.get().getAmount().setQuantityMin(0.0f);
                }
                else
                {
                    item.get().getAmount().setQuantityMin(Float.valueOf(s.toString()));
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        vh.m_EditTextAmountMax.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                View child = m_RecyclerView.getLayoutManager().findViewByPosition(m_iActiveElement);
                if(child == null)
                {
                    // Item not visible (yet) -> nothing to do
                    return;
                }

                RecipeItemsAdapter.ViewHolder vh = (RecipeItemsAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(child);
                Optional<RecipeItem> item = getRecipeItem(vh.getID());
                if(!item.isPresent() || !item.get().getAmount().isRange())
                {
                    return;
                }

                if(s.toString().isEmpty())
                {
                    item.get().getAmount().setQuantityMax(0.0f);
                }
                else
                {
                    item.get().getAmount().setQuantityMax(Float.valueOf(s.toString()));
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        vh.m_CheckBoxAmountRange.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) ->
        {
            View child = m_RecyclerView.getLayoutManager().findViewByPosition(m_iActiveElement);
            if(child == null)
            {
                return;
            }
            RecipeItemsAdapter.ViewHolder viewHolder = (RecipeItemsAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(child);
            Optional<RecipeItem> recipeItem = getRecipeItem(viewHolder.getID());
            if(!recipeItem.isPresent())
            {
                return;
            }

            vh.m_TableRowAmountRange.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            recipeItem.get().getAmount().setIsRange(isChecked);

            adjustEditTextAmount(vh, item.get());
        });
        vh.m_CheckBoxAmountRange.setChecked(item.get().getAmount().isRange());

        adjustEditTextAmount(vh, item.get());
    }

    void onChangeAmount(boolean bChangeMax, boolean bIncrease)
    {
        if(m_RecyclerView.getLayoutManager() == null)
        {
            return;
        }

        View child = m_RecyclerView.getLayoutManager().findViewByPosition(m_iActiveElement);
        if(child == null)
        {
            return;
        }
        RecipeItemsAdapter.ViewHolderActive vh = (RecipeItemsAdapter.ViewHolderActive)m_RecyclerView.getChildViewHolder(child);
        Optional<RecipeItem> item = getRecipeItem(vh.getID());
        if(!item.isPresent())
        {
            return;
        }

        if(bIncrease)
        {
            if(bChangeMax)
            {
                item.get().getAmount().increaseAmountMax();
            }
            else
            {
                item.get().getAmount().increaseAmountMin();
            }
        }
        else
        {
            if(bChangeMax)
            {
                item.get().getAmount().decreaseAmountMax();
            }
            else
            {
                item.get().getAmount().decreaseAmountMin();
            }
        }

        if(bChangeMax)
        {
            vh.m_EditTextAmountMax.setText(String.format(Locale.getDefault(), "%s", Helper.formatNumber(item.get().getAmount().getQuantityMax())));
        }
        else
        {
            vh.m_EditTextAmount.setText(String.format(Locale.getDefault(), "%s", Helper.formatNumber(item.get().getAmount().getQuantityMin())));
        }
    }

    private void onAddItemToGroup(@NonNull final String strGroup)
    {
        if(m_RecyclerView.getLayoutManager() == null)
        {
            return;
        }

        ArrayList<String> inputList = new ArrayList<>();
        ArrayList<String> ingredientsUsed = getRecipeItemsList();
        for(Ingredients.Ingredient ingredient : m_Ingredients.getAllIngredients())
        {
            if(ingredientsUsed.contains(ingredient.getName()))
            {
                continue;
            }
            inputList.add(ingredient.getName());
        }

        InputStringDialogFragment newFragment = InputStringDialogFragment.newInstance(m_RecyclerView.getContext().getResources().getString(R.string.text_add_ingredient));
        newFragment.setAdditionalInformation(strGroup);
        newFragment.setListOnlyAllowed(inputList);
        newFragment.show(((AppCompatActivity) m_RecyclerView.getContext()).getSupportFragmentManager(), "addRecipeItemToGroup");
    }

    private void onRenameGroup(@NonNull final String strGroup)
    {
        InputStringDialogFragment newFragment = InputStringDialogFragment.newInstance(m_RecyclerView.getContext().getResources().getString(R.string.text_rename_group, strGroup));
        newFragment.setDefaultValue(strGroup);
        newFragment.setAdditionalInformation(strGroup);
        newFragment.setListExcludedInputs(m_Recipe.getAllGroupNames());
        newFragment.show(((AppCompatActivity) m_RecyclerView.getContext()).getSupportFragmentManager(), "renameAlternativesGroup");
    }

    private void onDeleteGroup(@NonNull final String strGroup)
    {
        if(m_RecyclerView.getLayoutManager() == null)
        {
            return;
        }

        m_GroupOfRecentlyDeletedItem = strGroup;
        m_ItemsOfRecentlyDeletedGroup = m_Recipe.getAllRecipeItemsInGroup(strGroup);
        if(m_ItemsOfRecentlyDeletedGroup == null)
        {
            return;
        }
        m_Recipe.removeGroup(strGroup);

        notifyDataSetChanged();
        setActiveElement("");

        // Allow undo

        Snackbar snackbar = Snackbar.make(m_CoordLayout, R.string.text_item_deleted, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.text_undo, (View view) ->
        {
            m_Recipe.addGroup(m_GroupOfRecentlyDeletedItem);
            for(RecipeItem item : m_ItemsOfRecentlyDeletedGroup)
            {
                m_Recipe.addRecipeItemToGroup(m_GroupOfRecentlyDeletedItem, item);
            }

            notifyDataSetChanged();

            m_GroupOfRecentlyDeletedItem = "";
            m_ItemsOfRecentlyDeletedGroup = null;

            Snackbar snackbar1 = Snackbar.make(m_CoordLayout, R.string.text_item_restored, Snackbar.LENGTH_SHORT);
            snackbar1.show();
        });
        snackbar.show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        if(m_iActiveElement == -1 || m_RecyclerView.getLayoutManager() == null)
        {
            return;
        }

        View child = m_RecyclerView.getLayoutManager().findViewByPosition(m_iActiveElement);
        if(child == null)
        {
            return;
        }
        RecipeItemsAdapter.ViewHolderActive vh = (RecipeItemsAdapter.ViewHolderActive)m_RecyclerView.getChildViewHolder(child);
        Optional<RecipeItem> item = getRecipeItem(vh.getID());
        if(!item.isPresent())
        {
            return;
        }

        if(parent == vh.m_SpinnerAmount)
        {
            item.get().getAmount().setUnit(Unit.values()[vh.m_SpinnerAmount.getSelectedItemPosition()]);
            adjustEditTextAmount(vh, item.get());
        }
        else if(parent == vh.m_SpinnerSize)
        {
            item.get().setSize(RecipeItem.Size.values()[vh.m_SpinnerSize.getSelectedItemPosition()]);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
    }

    @Override
    public void reactToSwipe(int position)
    {
        // Remove element

        if(m_RecyclerView.getLayoutManager() == null)
        {
            return;
        }

        View activeItem = m_RecyclerView.getLayoutManager().findViewByPosition(position);
        if(activeItem == null)
        {
            return;
        }
        RecipeItemsAdapter.ViewHolder holder = (RecipeItemsAdapter.ViewHolder)m_RecyclerView.getChildViewHolder(activeItem);
        Optional<RecipeItem> itemToDelete = getRecipeItem(holder.m_id);
        if(!itemToDelete.isPresent())
        {
            return;
        }
        m_RecentlyDeleted = itemToDelete.get();
        if(holder.isPartOfGroup())
        {
            m_GroupOfRecentlyDeletedItem = getGroup(holder.m_id);
            ArrayList<RecipeItem> group = m_Recipe.getAllRecipeItemsInGroup(m_GroupOfRecentlyDeletedItem);
            if(group == null)
            {
                return;
            }
            m_RecentlyDeletedIndex = group.indexOf(m_RecentlyDeleted);
            m_Recipe.removeRecipeItemFromGroup(m_GroupOfRecentlyDeletedItem, m_RecentlyDeleted);
        }
        else
        {
            m_RecentlyDeletedIndex = position;
            m_GroupOfRecentlyDeletedItem = "";
            m_Recipe.removeRecipeItem(m_RecentlyDeleted);
        }


        notifyDataSetChanged();
        setActiveElement("");

        // Allow undo

        Snackbar snackbar = Snackbar.make(m_CoordLayout, R.string.text_item_deleted, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.text_undo, (View view) ->
        {
            if(m_GroupOfRecentlyDeletedItem.isEmpty())
            {
                m_Recipe.addRecipeItem(m_RecentlyDeletedIndex, m_RecentlyDeleted);
            }
            else
            {
                ArrayList<RecipeItem> group = m_Recipe.getAllRecipeItemsInGroup(m_GroupOfRecentlyDeletedItem);
                if(group == null)
                {
                    return;
                }
                m_Recipe.addRecipeItemToGroup(m_GroupOfRecentlyDeletedItem, m_RecentlyDeleted);
            }

            notifyDataSetChanged();

            m_RecentlyDeleted = null;
            m_RecentlyDeletedIndex = -1;
            m_GroupOfRecentlyDeletedItem = "";

            Snackbar snackbar1 = Snackbar.make(m_CoordLayout, R.string.text_item_restored, Snackbar.LENGTH_SHORT);
            snackbar1.show();
        });
        snackbar.show();
    }

    @Override
    public boolean swipeAllowed(RecyclerView.ViewHolder vh)
    {
        return !(vh instanceof RecipeItemsAdapter.ViewHolderGroupHeader);
    }

    @Override
    public boolean reactToDrag(RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target)
    {
        return false;
    }

    boolean containsItem(@NonNull String strName)
    {
        return getRecipeItemsList().contains(strName);
    }

    private ArrayList<String> getRecipeItemsList()
    {
        ArrayList<String> vec = new ArrayList<>();
        for(RecipeItem item : m_Recipe.getAllRecipeItems())
        {
            vec.add(item.getIngredient());
        }

        for(String strGroup : m_Recipe.getAllGroupNames())
        {
            vec.add(GROUP_PREFIX + strGroup);
            for(RecipeItem item : m_Recipe.getAllRecipeItemsInGroup(strGroup))
            {
                vec.add(item.getIngredient());
            }
        }

        return vec;
    }

    private Optional<RecipeItem> getRecipeItem(@NonNull String strName)
    {
        for(RecipeItem r : m_Recipe.getAllRecipeItems())
        {
            if(r.getIngredient().equals(strName))
            {
                return Optional.of(r);
            }
        }

        for(String strGroup : m_Recipe.getAllGroupNames())
        {
            for(RecipeItem r : m_Recipe.getAllRecipeItemsInGroup(strGroup))
            {
                if(r.getIngredient().equals(strName))
                {
                    return Optional.of(r);
                }
            }
        }

        return Optional.empty();
    }

    private String getGroup(@NonNull String strIngredient)
    {
        for(String strGroup : m_Recipe.getAllGroupNames())
        {
            for(RecipeItem r : m_Recipe.getAllRecipeItemsInGroup(strGroup))
            {
                if(r.getIngredient().equals(strIngredient))
                {
                    return strGroup;
                }
            }
        }
        return "";
    }

    private void adjustEditTextAmount(@NonNull RecipeItemsAdapter.ViewHolderActive vh, @NonNull RecipeItem item)
    {
        if(item.getAmount().getUnit() == Unit.Unitless)
        {
            vh.m_EditTextAmount.setText("");
            vh.m_EditTextAmountMax.setText("");
            vh.m_CheckBoxAmountRange.setChecked(false);
            vh.m_CheckBoxAmountRange.setVisibility(View.GONE);
            vh.m_TableRowAmount.setVisibility(View.GONE);
            vh.m_TableRowAmountRange.setVisibility(View.GONE);
        }
        else
        {
            vh.m_EditTextAmount.setText(String.format(Locale.getDefault(), "%s", Helper.formatNumber(item.getAmount().getQuantityMin())));
            vh.m_CheckBoxAmountRange.setVisibility(View.VISIBLE);
            vh.m_TableRowAmount.setVisibility(View.VISIBLE);

            if(item.getAmount().isRange())
            {
                vh.m_EditTextAmountMax.setText(String.format(Locale.getDefault(), "%s", Helper.formatNumber(item.getAmount().getQuantityMax())));
                vh.m_TableRowAmountRange.setVisibility(View.VISIBLE);
                vh.m_TextViewAmountMin.setVisibility(View.VISIBLE);
            }
            else
            {
                vh.m_EditTextAmountMax.setText("");
                vh.m_TableRowAmountRange.setVisibility(View.GONE);
                vh.m_TextViewAmountMin.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void clearViewBackground(RecyclerView.ViewHolder vh)
    {
        if(m_iActiveElement == -1)
        {
            vh.itemView.setBackgroundColor(0);
            return;
        }

        if(m_RecyclerView.getLayoutManager() == null)
        {
            return;
        }

        View v = m_RecyclerView.getLayoutManager().findViewByPosition(m_iActiveElement);

        if(v != null && m_RecyclerView.getChildViewHolder(v) == vh)
        {
            vh.itemView.setBackgroundColor(ContextCompat.getColor(vh.itemView.getContext(), R.color.colorHighlightedBackground));
        }
        else
        {
            vh.itemView.setBackgroundColor(0);
        }
    }
}
