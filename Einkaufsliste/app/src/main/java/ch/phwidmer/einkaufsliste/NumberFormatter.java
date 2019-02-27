package ch.phwidmer.einkaufsliste;

public class NumberFormatter {

    static String format(Float f)
    {
        if(f == Math.round(f))
        {
            return String.valueOf(f.intValue());
        }
        else
        {
            return String.format("%.2f", f);
        }
    }
}
