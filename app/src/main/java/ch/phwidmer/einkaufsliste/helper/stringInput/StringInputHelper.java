package ch.phwidmer.einkaufsliste.helper.stringInput;

import android.support.annotation.NonNull;

import java.text.Collator;
import java.util.ArrayList;

class StringInputHelper {
    static boolean arrayListContainsIgnoreCase(@NonNull ArrayList<String> list, @NonNull String str)
    {
        final Collator instance = Collator.getInstance();
        instance.setStrength(Collator.NO_DECOMPOSITION);

        String toSearch = str.toLowerCase();
        for(String s : list)
        {
            if(instance.equals(s.toLowerCase(), toSearch))
            {
                return true;
            }
        }
        return false;
    }
}
