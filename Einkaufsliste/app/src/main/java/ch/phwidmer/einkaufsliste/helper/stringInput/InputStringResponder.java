package ch.phwidmer.einkaufsliste.helper.stringInput;

import android.support.annotation.NonNull;

public interface InputStringResponder {
    void onStringInput(@NonNull String tag, @NonNull String strInput, @NonNull String strAdditonalInformation);
}
