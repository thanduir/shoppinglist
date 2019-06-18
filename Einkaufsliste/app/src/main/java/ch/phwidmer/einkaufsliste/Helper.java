package ch.phwidmer.einkaufsliste;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

class Helper {

    public static class SortIgnoreCase implements Comparator<Object>
    {
        public int compare(Object o1, Object o2) {
            String s1 = (String) o1;
            String s2 = (String) o2;

            final Collator instance = Collator.getInstance();
            // This strategy mean it'll ignore the accents
            instance.setStrength(Collator.NO_DECOMPOSITION);
            return instance.compare(s1.toLowerCase(), s2.toLowerCase());
        }
    }

    static boolean arrayListContainsIgnoreCase(ArrayList<String> list, String str)
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

    // Format numbers: Write as integer without decimals if applicable, otherwise restrict decimal digits.
    static String formatNumber(Float f)
    {
        if(f == Math.round(f))
        {
            return String.valueOf(f.intValue());
        }
        else if(f * 10 == Math.round(f * 10))
        {
            return String.format(Locale.getDefault(), "%.1f", f);
        }
        else
        {
            return String.format(Locale.getDefault(), "%.2f", f);
        }
    }
}
