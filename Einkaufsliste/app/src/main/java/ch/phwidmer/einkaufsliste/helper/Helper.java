package ch.phwidmer.einkaufsliste.helper;

import android.support.annotation.NonNull;

import java.text.Collator;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Pattern;

public class Helper {

    public interface NamedObject
    {
        @NonNull String getName();
    }

    public static class SortStringIgnoreCase implements Comparator<String>
    {
        public int compare(@NonNull String s1, @NonNull String s2)
        {
            final Collator instance = Collator.getInstance();
            // This strategy means it'll ignore the accents
            instance.setStrength(Collator.NO_DECOMPOSITION);
            return instance.compare(s1.toLowerCase(), s2.toLowerCase());
        }
    }

    public static class SortNamedIgnoreCase implements Comparator<NamedObject>
    {
        private SortStringIgnoreCase m_Sort = new SortStringIgnoreCase();

        public int compare(@NonNull NamedObject o1, @NonNull NamedObject o2) {
            return m_Sort.compare(o1.getName(), o2.getName());
        }
    }

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

    // Format numbers: Write as integer without decimals if applicable, otherwise restrict decimal digits.
    public static String formatNumber(@NonNull Float f)
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

    public static String stripAccents(String str)
    {
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }
}
