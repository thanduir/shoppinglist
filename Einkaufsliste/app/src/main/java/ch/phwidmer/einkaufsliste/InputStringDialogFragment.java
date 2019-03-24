package ch.phwidmer.einkaufsliste;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class InputStringDialogFragment extends DialogFragment {

    public static interface InputStringResponder
    {
        public void onStringInput(String tag, String strInput, String strAdditonalInformation);
    }

    private String m_Title;
    private String m_AdditonalInformation;
    private String m_DefaultValue;
    private int m_InputType;
    private Boolean m_InputFromList;
    private ArrayList<String> m_ListOfPossibleInputs;

    static InputStringDialogFragment newInstance(String strTitle, String strAdditonalInformation)
    {
        return newInstance(strTitle, strAdditonalInformation, strAdditonalInformation, InputType.TYPE_CLASS_TEXT);
    }

    static InputStringDialogFragment newInstance(String strTitle, String strAdditonalInformation, String defaultValue, int inputType)
    {
        InputStringDialogFragment f = new InputStringDialogFragment();

        Bundle args = new Bundle();
        args.putString("title", strTitle);
        args.putString("additionalInfo", strAdditonalInformation);
        args.putString("defaultValue", defaultValue);
        args.putInt("inputType", inputType);
        args.putBoolean("inputFromList", false);
        f.setArguments(args);

        return f;
    }

    static InputStringDialogFragment newInstance(String strTitle, String strAdditonalInformation, ArrayList<String> listOfPossibleInputs)
    {
        InputStringDialogFragment f = new InputStringDialogFragment();

        Bundle args = new Bundle();
        args.putString("title", strTitle);
        args.putString("additionalInfo", strAdditonalInformation);
        args.putString("defaultValue", strAdditonalInformation);
        args.putInt("inputType", InputType.TYPE_CLASS_TEXT);
        args.putBoolean("inputFromList", true);
        args.putStringArrayList("listOfPossibleInputs", listOfPossibleInputs);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_Title = getArguments().getString("title");
        m_AdditonalInformation = getArguments().getString("additionalInfo");
        m_DefaultValue = getArguments().getString("defaultValue");
        m_InputType = getArguments().getInt("inputType");
        m_InputFromList = getArguments().getBoolean("inputFromList");
        if(m_InputFromList)
        {
            m_ListOfPossibleInputs = getArguments().getStringArrayList("listOfPossibleInputs");
        }
    }

    private View setupStringInput(LayoutInflater inflater, ViewGroup container)
    {
        final View mainView = inflater.inflate(R.layout.overlay_input_string, container, false);

        EditText editText = mainView.findViewById(R.id.editTextInput);
        editText.setInputType(m_InputType);
        if(!m_DefaultValue.isEmpty())
        {
            editText.setText(m_DefaultValue);
        }
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                Button button = mainView.findViewById(R.id.ButtonOk);
                button.setEnabled(!s.toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return mainView;
    }

    private View setupInputFromStringList(LayoutInflater inflater, ViewGroup container)
    {
        final View mainView = inflater.inflate(R.layout.overlay_input_from_list, container, false);

        AutoCompleteTextView inputView = mainView.findViewById(R.id.inputListControl);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_dropdown_item_1line, m_ListOfPossibleInputs);
        inputView.setAdapter(adapter);
        inputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                Button button = mainView.findViewById(R.id.ButtonOk);
                button.setEnabled(m_ListOfPossibleInputs.contains(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        inputView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    AutoCompleteTextView view = mainView.findViewById(R.id.inputListControl);
                    if (view.getText().toString().length() == 0) {
                        // We want to trigger the drop down, replace the text.
                        view.setText("");
                    }
                }
            }
        });

        return mainView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = null;
        if(m_InputFromList)
        {
            view = setupInputFromStringList(inflater, container);
        }
        else
        {
            view = setupStringInput(inflater, container);
        }

        TextView textView = view.findViewById(R.id.textViewTitle);
        textView.setText(m_Title);

        Button buttonCancel = view.findViewById(R.id.ButtonCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });

        final View mainView = view;
        Button buttonOk = mainView.findViewById(R.id.ButtonOk);
        buttonOk.setEnabled(!m_AdditonalInformation.isEmpty());
        buttonOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!m_InputFromList)
                {
                    EditText editText = mainView.findViewById(R.id.editTextInput);
                    ((InputStringResponder) getActivity()).onStringInput(getTag(), editText.getText().toString(), m_AdditonalInformation);
                }
                else
                {
                    AutoCompleteTextView view = mainView.findViewById(R.id.inputListControl);
                    ((InputStringResponder) getActivity()).onStringInput(getTag(), view.getText().toString(), m_AdditonalInformation);
                }
                dismiss();
            }
        });

        return mainView;
    }
}