package ch.phwidmer.einkaufsliste.helper;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

public class Helper {

    public interface NamedObject
    {
        String getName();
    }

    public static class SortStringIgnoreCase implements Comparator<String>
    {
        public int compare(String s1, String s2)
        {
            final Collator instance = Collator.getInstance();
            // This strategy mean it'll ignore the accents
            instance.setStrength(Collator.NO_DECOMPOSITION);
            return instance.compare(s1.toLowerCase(), s2.toLowerCase());
        }
    }

    public static class SortNamedIgnoreCase implements Comparator<NamedObject>
    {
        private SortStringIgnoreCase m_Sort = new SortStringIgnoreCase();

        public int compare(NamedObject o1, NamedObject o2) {
            return m_Sort.compare(o1.getName(), o2.getName());
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
    public static String formatNumber(Float f)
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
