package ch.phwidmer.einkaufsliste;

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

public class InputStringDialogFragment extends DialogFragment {

    public interface InputStringResponder
    {
        void onStringInput(String tag, String strInput, String strAdditonalInformation);
    }

    private String m_Title;
    private String m_AdditionalInformation;
    private String m_DefaultValue;
    private int m_InputType;
    private Boolean m_InputFromList;
    private Boolean m_ConfirmElementsInList;
    private ArrayList<String> m_ListOfSpecialInputs;

    /* TODO: REFACTOR THIS, list should have different possibilites: excludeOnList, onlyThoseFromList, confirmThoseOnList! Should it be possible to combine these Lists?
          FIRST LIST ALL CURRENT (and usefull) VARIANTS, THEN DECIDE ON NEW CODE-STRUCTURE
          ALSO LOOK AT OTHER TODO ITEM: "Lange (d.h. mehrzeilige) Nachrichten sind abgeschnitten!"
     */

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
        args.putBoolean("confirmElementsInList", false);
        f.setArguments(args);

        return f;
    }

    static InputStringDialogFragment newInstance(String strTitle, String strAdditonalInformation, String defaultValue, ArrayList<String> listOfInputsToConfirm)
    {
        InputStringDialogFragment f = new InputStringDialogFragment();

        Bundle args = new Bundle();
        args.putString("title", strTitle);
        args.putString("additionalInfo", strAdditonalInformation);
        args.putString("defaultValue", defaultValue);
        args.putInt("inputType", InputType.TYPE_CLASS_TEXT);
        args.putBoolean("inputFromList", false);
        args.putBoolean("confirmElementsInList", true);
        args.putStringArrayList("listOfSpecialInputs", listOfInputsToConfirm);
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
        args.putBoolean("confirmElementsInList", false);
        args.putStringArrayList("listOfSpecialInputs", listOfPossibleInputs);
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
        m_DefaultValue = getArguments().getString("defaultValue");
        m_InputType = getArguments().getInt("inputType");
        m_InputFromList = getArguments().getBoolean("inputFromList");
        m_ConfirmElementsInList = getArguments().getBoolean("confirmElementsInList");
        if(m_InputFromList || m_ConfirmElementsInList)
        {
            m_ListOfSpecialInputs = getArguments().getStringArrayList("listOfSpecialInputs");
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
        if(getContext() == null)
        {
            return null;
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, m_ListOfSpecialInputs);
        inputView.setAdapter(adapter);
        inputView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                Button button = mainView.findViewById(R.id.ButtonOk);
                button.setEnabled(m_ListOfSpecialInputs.contains(s.toString()));
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
        if(m_InputFromList)
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
        buttonOk.setEnabled(!m_AdditionalInformation.isEmpty());
        buttonOk.setOnClickListener((View v) ->
        {
            if(!m_InputFromList && !m_ConfirmElementsInList)
            {
                EditText editText = mainView.findViewById(R.id.editTextInput);
                InputStringResponder activity = (InputStringResponder) getActivity();
                if(activity == null)
                {
                    return;
                }
                activity.onStringInput(getTag(), editText.getText().toString(), m_AdditionalInformation);
            }
            else if(m_ConfirmElementsInList && !m_InputFromList)
            {
                EditText editText = mainView.findViewById(R.id.editTextInput);
                if(m_ListOfSpecialInputs.contains(editText.getText().toString()))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getActivity().getResources().getString(R.string.file_exists_overwrite_header));
                    builder.setMessage(getActivity().getResources().getString(R.string.file_exists_overwrite, editText.getText().toString()));
                    builder.setPositiveButton(android.R.string.ok, (DialogInterface dialog, int which) ->
                    {
                        ((InputStringResponder) getActivity()).onStringInput(getTag(), editText.getText().toString(), m_AdditionalInformation);
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
                    ((InputStringResponder) getActivity()).onStringInput(getTag(), editText.getText().toString(), m_AdditionalInformation);
                }
            }
            else
            {
                AutoCompleteTextView inputView = mainView.findViewById(R.id.inputListControl);
                InputStringResponder activity = (InputStringResponder) getActivity();
                if(activity == null)
                {
                    return;
                }
                activity.onStringInput(getTag(), inputView.getText().toString(), m_AdditionalInformation);
            }
            dismiss();
        });

        return mainView;
    }
}