package ch.phwidmer.einkaufsliste.helper.stringInput;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ch.phwidmer.einkaufsliste.R;
import ch.phwidmer.einkaufsliste.helper.ItemClickSupport;

public class InputStringFromList extends DialogFragment
{
    private static final String m_keyTitle                  = "title";
    private static final String m_keyAdditionalInformation  = "additionalInfo";
    private static final String m_keyListOnlyAllowed        = "listOnlyAllowed";

    private String              m_Title;
    private String              m_AdditionalInformation;
    private ArrayList<String>   m_ListAllowedInputs;

    public static InputStringFromList newInstance(@NonNull String strTitle, @NonNull ArrayList<String> listAllowedInputs, @NonNull String strAdditonalInformation)
    {
        InputStringFromList f = new InputStringFromList();

        Bundle args = new Bundle();
        args.putString(m_keyTitle, strTitle);
        args.putStringArrayList(m_keyListOnlyAllowed, listAllowedInputs);
        args.putString(m_keyAdditionalInformation, strAdditonalInformation);
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
        m_Title = getArguments().getString("title");
        m_AdditionalInformation = getArguments().getString("additionalInfo");
        m_ListAllowedInputs = getArguments().getStringArrayList(m_keyListOnlyAllowed);
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
        RecyclerView recyclerViewStringInput = mainView.findViewById(R.id.recyclerViewInputFromList);
        SearchView searchView = mainView.findViewById(R.id.searchViewInputFromList);
        ImageView closeView = mainView.findViewById(R.id.close_overlay);

        textViewTitle.setText(m_Title);

        buttonCancel.setOnClickListener((View v) -> dismiss());

        closeView.setOnClickListener((View v) -> dismiss());

        buttonOk.setEnabled(false);
        buttonOk.setOnClickListener((View v) ->
        {
            InputStringFromListAdapter stringInputAdapter = (InputStringFromListAdapter)recyclerViewStringInput.getAdapter();
            if(stringInputAdapter == null)
            {
                return;
            }
            String strInput = stringInputAdapter.getActiveElement();

            String tag = getTag();
            if(tag == null)
            {
                tag = "";
            }
            ((InputStringResponder) getActivity()).onStringInput(tag, strInput, m_AdditionalInformation);
            dismiss();
        });

        recyclerViewStringInput.setHasFixedSize(true);
        recyclerViewStringInput.setLayoutManager(new LinearLayoutManager(getContext()));
        InputStringFromListAdapter adapter = new InputStringFromListAdapter(m_ListAllowedInputs);
        recyclerViewStringInput.setAdapter(adapter);
        ItemClickSupport.addTo(recyclerViewStringInput).setOnItemClickListener((RecyclerView recyclerView, int position, View v) ->
        {
            InputStringFromListAdapter stringInputAdapter = (InputStringFromListAdapter)recyclerView.getAdapter();
            if(stringInputAdapter == null)
            {
                return;
            }
            boolean elementExists = stringInputAdapter.setActiveElement(position);
            buttonOk.setEnabled(elementExists);
            searchView.clearFocus();
        });

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
}
