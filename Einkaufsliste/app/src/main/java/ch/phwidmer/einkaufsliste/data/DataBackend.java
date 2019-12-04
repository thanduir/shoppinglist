package ch.phwidmer.einkaufsliste.data;

import android.content.Context;
import android.support.annotation.NonNull;

import ch.phwidmer.einkaufsliste.R;

public enum DataBackend {
    fs_based {
        public String toUIString(@NonNull Context context) {
            return context.getResources().getString(R.string.backend_fs_based);
        }
    },
    db_based {
        public String toUIString(@NonNull Context context) {
            return context.getResources().getString(R.string.backend_db_based);
        }
    };

    public abstract String toUIString(@NonNull Context context);
}
