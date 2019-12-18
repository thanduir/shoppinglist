package ch.phwidmer.einkaufsliste.helper.stringInput;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import ch.phwidmer.einkaufsliste.R;
import ch.phwidmer.einkaufsliste.helper.ItemClickSupport;
import ch.phwidmer.einkaufsliste.helper.ReactToTouchActionsCallback;
import ch.phwidmer.einkaufsliste.helper.ReactToTouchActionsInterface;

public class InputStringFromListMultiSelect extends DialogFragment implements ReactToTouchActionsInterface
{
    public enum SelectionType
    {
        SingleSelect,
        MultiSelectDifferentElements,
        MultiSelect
    }

    private static final String m_keyTitle                  = "title";
    private static final String m_keyAdditionalInformation  = "additionalInfo";
    private static final String m_keyListOnlyAllowed        = "listOnlyAllowed";
    private static final String m_keySelectionType          = "selectionType";

    private String              m_Title;
    private String              m_AdditionalInformation;
    private ArrayList<String>   m_ListAllowedInputs;
    private SelectionType       m_SelectionType;

    private RecyclerView        m_RecyclerView;

    public static InputStringFromListMultiSelect newInstance(@NonNull String strTitle, @NonNull ArrayList<String> listAllowedInputs,
                                                             @NonNull String strAdditonalInformation, SelectionType selectionType)
    {
        InputStringFromListMultiSelect f = new InputStringFromListMultiSelect();

        Bundle args = new Bundle();
        args.putString(m_keyTitle, strTitle);
        args.putStringArrayList(m_keyListOnlyAllowed, listAllowedInputs);
        args.putString(m_keyAdditionalInformation, strAdditonalInformation);
        args.putInt(m_keySelectionType, selectionType.ordinal());
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() == null)
        {
            return;
        }
        m_Title = getArguments().getString(m_keyTitle);
        m_AdditionalInformation = getArguments().getString(m_keyAdditionalInformation);
        m_ListAllowedInputs = getArguments().getStringArrayList(m_keyListOnlyAllowed);
        m_SelectionType = SelectionType.values()[getArguments().getInt(m_keySelectionType)];
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if(getActivity() == null || getContext() == null)
        {
            return null;
        }

        final View mainView = inflater.inflate(R.layout.overlay_input_from_list, container, false);

        TextView textViewTitle = mainView.findViewById(R.id.textViewTitle);
        Button buttonCancel = mainView.findViewById(R.id.ButtonCancel);
        Button buttonOk = mainView.findViewById(R.id.ButtonOk);
        m_RecyclerView = mainView.findViewById(R.id.recyclerViewInputFromList);
        SearchView searchView = mainView.findViewById(R.id.searchViewInputFromList);

        textViewTitle.setText(m_Title);

        buttonCancel.setVisibility(View.INVISIBLE);

        buttonOk.setEnabled(true);
        buttonOk.setText(R.string.button_close);
        buttonOk.setOnClickListener((View v) -> dismiss());

        m_RecyclerView.setHasFixedSize(true);
        m_RecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        InputStringFromListAdapter adapter = new InputStringFromListAdapter(m_ListAllowedInputs);
        m_RecyclerView.setAdapter(adapter);
        ItemClickSupport.addTo(m_RecyclerView).setOnItemClickListener((RecyclerView recyclerView, int position, View v) -> searchView.clearFocus());
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ReactToTouchActionsCallback(this,
                getContext(),
                R.drawable.ic_add_black_24dp,
                false));
        itemTouchHelper.attachToRecyclerView(m_RecyclerView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String text) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                adapter.getFilter().filter(text);
                return true;
            }
        });

        searchView.requestFocus();
        return mainView;
    }

    @Override
    public void reactToSwipe(int position)
    {
        InputStringFromListAdapter stringInputAdapter = (InputStringFromListAdapter)m_RecyclerView.getAdapter();
        if(stringInputAdapter == null)
        {
            return;
        }
        String strInput = stringInputAdapter.getItemAtPosition(position);

        String tag = getTag();
        if(tag == null)
        {
            tag = "";
        }

        InputStringResponder responder = (InputStringResponder)getActivity();
        if(responder == null)
        {
            return;
        }
        responder.onStringInput(tag, strInput, m_AdditionalInformation);

        if(m_SelectionType == SelectionType.MultiSelect)
        {
            stringInputAdapter.notifyItemChanged(position);
        }
        else if(m_SelectionType == SelectionType.MultiSelectDifferentElements)
        {
            stringInputAdapter.removeItem(strInput);
        }
        else if(m_SelectionType == SelectionType.SingleSelect)
        {
            dismiss();
        }
    }

    @Override
    public boolean swipeAllowed(RecyclerView.ViewHolder vh)
    {
        return true;
    }

    @Override
    public boolean reactToDrag(RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target)
    {
        return false;
    }

    @Override
    public void clearViewBackground(@NonNull RecyclerView.ViewHolder vh)
    {
        vh.itemView.setBackgroundColor(0);
    }
}
