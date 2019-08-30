package ch.phwidmer.einkaufsliste.helper;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
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

import ch.phwidmer.einkaufsliste.R;

public class InputStringDialogFragment extends DialogFragment {

    public interface InputStringResponder
    {
        void onStringInput(String tag, String strInput, String strAdditonalInformation);
    }

    private static final String m_keyTitle = "title";
    private static final String m_keyAdditionalInformation = "additionalInfo";
    private static final String m_keyDefaultValue = "defaultValue";
    private static final String m_keyInputType = "inputType";

    private static final String m_keyListExcludedInputs = "listExcludedInputs";
    private static final String m_keyListInputsToConfirm = "listInputsToConfirm";
    private static final String m_keyListOnlyAllowed = "listOnlyAllowed";

    private String m_Title;
    private String m_AdditionalInformation;
    private String m_DefaultValue;
    private int m_InputType;

    private ArrayList<String> m_ListExcludedInputs;
    private ArrayList<String> m_ListInputsToConfirm;
    private ArrayList<String> m_ListOnlyAllowed;

    public static InputStringDialogFragment newInstance(String strTitle)
    {
        InputStringDialogFragment f = new InputStringDialogFragment();

        Bundle args = new Bundle();
        args.putString(m_keyTitle, strTitle);
        args.putString(m_keyAdditionalInformation, "");
        args.putString(m_keyDefaultValue, "");
        args.putInt(m_keyInputType, InputType.TYPE_CLASS_TEXT);
        f.setArguments(args);

        return f;
    }

    public void setAdditionalInformation(String strAdditonalInformation)
    {
        if(getArguments() == null)
        {
            return;
        }
        getArguments().putString(m_keyAdditionalInformation, strAdditonalInformation);
    }

    public void setDefaultValue(String strDefaultValue)
    {
        if(getArguments() == null)
        {
            return;
        }
        getArguments().putString(m_keyDefaultValue, strDefaultValue);
    }

    public void setInputType(int inputType)
    {
        if(getArguments() == null)
        {
            return;
        }
        getArguments().putInt(m_keyInputType, inputType);
    }

    public void setListExcludedInputs(ArrayList<String> listExcludedInputs)
    {
        if(getArguments() == null)
        {
            return;
        }
        getArguments().putStringArrayList(m_keyListExcludedInputs, listExcludedInputs);
    }

    public void setListInputsToConfirm(ArrayList<String> listInputsToConfirm)
    {
        if(getArguments() == null)
        {
            return;
        }
        getArguments().putStringArrayList(m_keyListInputsToConfirm, listInputsToConfirm);
    }

    public void setListOnlyAllowed(ArrayList<String> listOnlyAllowed)
    {
        if(getArguments() == null)
        {
            return;
        }
        getArguments().putStringArrayList(m_keyListOnlyAllowed, listOnlyAllowed);
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
        m_DefaultValue = getArguments().getString("defaultValue");
        m_InputType = getArguments().getInt("inputType");

        m_ListExcludedInputs = getArguments().getStringArrayList(m_keyListExcludedInputs);
        m_ListInputsToConfirm = getArguments().getStringArrayList(m_keyListInputsToConfirm);
        m_ListOnlyAllowed = getArguments().getStringArrayList(m_keyListOnlyAllowed);
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
                String strInput = s.toString();
                button.setEnabled(!strInput.isEmpty() && (m_ListExcludedInputs == null || !Helper.arrayListContainsIgnoreCase(m_ListExcludedInputs, strInput)));
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
        if(getContext() == null)
        {
            return null;
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, m_ListOnlyAllowed);
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
                button.setEnabled(Helper.arrayListContainsIgnoreCase(m_ListOnlyAllowed, strInput)
                                 && (m_ListExcludedInputs == null || !Helper.arrayListContainsIgnoreCase(m_ListExcludedInputs, strInput)));
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

        View view;
        if(m_ListOnlyAllowed != null && m_ListOnlyAllowed.size() > 0)
        {
            view = setupInputFromStringList(inflater, container);
        }
        else
        {
            view = setupStringInput(inflater, container);
        }

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
        buttonOk.setEnabled(!m_DefaultValue.isEmpty()
                            && (m_ListExcludedInputs == null || !Helper.arrayListContainsIgnoreCase(m_ListExcludedInputs, m_DefaultValue))
                            && (m_ListOnlyAllowed == null || Helper.arrayListContainsIgnoreCase(m_ListOnlyAllowed, m_DefaultValue)));
        buttonOk.setOnClickListener((View v) ->
        {
            String strInput;
            if(m_ListOnlyAllowed != null && m_ListOnlyAllowed.size() > 0)
            {
                AutoCompleteTextView inputView = mainView.findViewById(R.id.inputListControl);
                strInput = inputView.getText().toString();
            }
            else
            {
                EditText editText = mainView.findViewById(R.id.editTextInput);
                strInput = editText.getText().toString();
            }

            if(m_ListInputsToConfirm != null && Helper.arrayListContainsIgnoreCase(m_ListInputsToConfirm, strInput))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getActivity().getResources().getString(R.string.file_exists_overwrite_header));
                builder.setMessage(getActivity().getResources().getString(R.string.file_exists_overwrite, strInput));
                builder.setPositiveButton(android.R.string.ok, (DialogInterface dialog, int which) ->
                {
                    ((InputStringResponder) getActivity()).onStringInput(getTag(), strInput, m_AdditionalInformation);
                    dismiss();
                });
                builder.setNegativeButton(android.R.string.cancel, (DialogInterface dialog, int which) ->
                {
                });
                builder.show();
                return;
            }
            else
            {
                ((InputStringResponder) getActivity()).onStringInput(getTag(), strInput, m_AdditionalInformation);
            }
            dismiss();
        });

        return mainView;
    }
}