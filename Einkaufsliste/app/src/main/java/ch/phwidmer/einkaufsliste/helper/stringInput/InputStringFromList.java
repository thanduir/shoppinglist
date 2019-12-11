package ch.phwidmer.einkaufsliste.helper.stringInput;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import ch.phwidmer.einkaufsliste.R;

// TODO: Improve this class (and simplify if possible): list with filter instead of textbox with autocomplete?
public class InputStringFromList extends DialogFragment {
    private static final String m_keyTitle = "title";
    private static final String m_keyAdditionalInformation = "additionalInfo";
    private static final String m_keyListOnlyAllowed = "listOnlyAllowed";

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

    private View setupInputFromStringList(@NonNull LayoutInflater inflater, @NonNull ViewGroup container)
    {
        final View mainView = inflater.inflate(R.layout.overlay_input_from_list, container, false);

        AutoCompleteTextView inputView = mainView.findViewById(R.id.inputListControl);
        if(getContext() == null)
        {
            return null;
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, m_ListAllowedInputs);
        inputView.setAdapter(adapter);
        inputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                Button button = mainView.findViewById(R.id.ButtonOk);
                String strInput = s.toString();
                button.setEnabled(StringInputHelper.arrayListContainsIgnoreCase(m_ListAllowedInputs, strInput));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        inputView.setOnFocusChangeListener((View v, boolean hasFocus) ->
        {
            if (hasFocus) {
                AutoCompleteTextView view = mainView.findViewById(R.id.inputListControl);
                if (view.getText().toString().length() == 0) {
                    // We want to trigger the drop down, replace the text.
                    view.setText("");
                }
            }
        });

        return mainView;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if(getActivity() == null)
        {
            return null;
        }

        View view = setupInputFromStringList(inflater, container);
        if(view == null)
        {
            return null;
        }

        TextView textView = view.findViewById(R.id.textViewTitle);
        textView.setText(m_Title);

        Button buttonCancel = view.findViewById(R.id.ButtonCancel);
        buttonCancel.setOnClickListener((View v) -> dismiss());

        final View mainView = view;
        Button buttonOk = mainView.findViewById(R.id.ButtonOk);
        buttonOk.setEnabled(false);
        buttonOk.setOnClickListener((View v) ->
        {
            AutoCompleteTextView inputView = mainView.findViewById(R.id.inputListControl);
            String strInput = inputView.getText().toString();

            String tag = getTag();
            if(tag == null)
            {
                tag = "";
            }
            ((InputStringResponder) getActivity()).onStringInput(tag, strInput, m_AdditionalInformation);
            dismiss();
        });

        return mainView;
    }
}
