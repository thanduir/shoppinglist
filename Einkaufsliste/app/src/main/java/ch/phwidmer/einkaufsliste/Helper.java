package ch.phwidmer.einkaufsliste;

import java.util.Comparator;

class Helper {

    public static class SortIgnoreCase implements Comparator<Object>
    {
        public int compare(Object o1, Object o2) {
            String s1 = (String) o1;
            String s2 = (String) o2;
            return s1.toLowerCase().compareTo(s2.toLowerCase());
        }
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
            return String.format("%.1f", f);
        }
        else
        {
            return String.format("%.2f", f);
        }
    }
}
