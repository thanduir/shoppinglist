package ch.phwidmer.einkaufsliste;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class InputStringDialogFragment extends DialogFragment {

    public static interface InputStringResponder
    {
        public void onStringInput(String tag, String strInput, String strAdditonalInformation);
    }

    private String m_Title;
    private String m_AdditonalInformation;
    private String m_DefaultValue;
    private int m_InputType;

    static InputStringDialogFragment newInstance(String strTitle, String strAdditonalInformation)
    {
        InputStringDialogFragment f = new InputStringDialogFragment();

        Bundle args = new Bundle();
        args.putString("title", strTitle);
        args.putString("additionalInfo", strAdditonalInformation);
        args.putString("defaultValue", strAdditonalInformation);
        args.putInt("inputType", InputType.TYPE_CLASS_TEXT);
        f.setArguments(args);

        return f;
    }

    static InputStringDialogFragment newInstance(String strTitle, String strAdditonalInformation, String defaultValue, int inputType)
    {
        InputStringDialogFragment f = new InputStringDialogFragment();

        Bundle args = new Bundle();
        args.putString("title", strTitle);
        args.putString("additionalInfo", strAdditonalInformation);
        args.putString("defaultValue", defaultValue);
        args.putInt("inputType", inputType);
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View mainView = inflater.inflate(R.layout.overlay_input_string, container, false);
        TextView textView = (TextView)mainView.findViewById(R.id.textViewTitle);
        textView.setInputType(m_InputType);
        textView.setText(m_Title);

        Button buttonCancel = (Button)mainView.findViewById(R.id.ButtonCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });

        Button buttonOk = (Button)mainView.findViewById(R.id.ButtonOk);
        buttonOk.setEnabled(!m_AdditonalInformation.isEmpty());
        buttonOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText editText = (EditText)mainView.findViewById(R.id.editTextInput);
                ((InputStringResponder)getActivity()).onStringInput(getTag(), editText.getText().toString(), m_AdditonalInformation);
                dismiss();
            }
        });

        EditText editText = (EditText)mainView.findViewById(R.id.editTextInput);
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
                Button button = (Button)mainView.findViewById(R.id.ButtonOk);
                button.setEnabled(!s.toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return mainView;
    }
}