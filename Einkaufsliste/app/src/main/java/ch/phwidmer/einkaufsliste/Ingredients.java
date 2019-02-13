package ch.phwidmer.einkaufsliste;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.LinkedHashMap;
import java.util.Vector;

public class Ingredients implements Parcelable
{
    private Categories m_Categories;

    // TODO: Diese Liste sollte nicht statisch, sondern konfigurierbar sein (-> zusätzliche Activity nötig?)
    public enum Provenance
    {
        Everywhere,
        Migros,
        Coop;
    }

    public class Ingredient
    {
        public Categories.Category m_Category;
        public Provenance m_Provenance;
    }
    private LinkedHashMap<String, Ingredient>  m_Ingredients;

    public Ingredients(Categories categories)
    {
        m_Categories = categories;
        m_Ingredients = new LinkedHashMap<String, Ingredient>();

        // TODO: Test-Code hier löschen, nachdem alles soweit fertig ist (und v.a. gespeichert wird!)

        Ingredient in0 = new Ingredient();
        in0.m_Category = m_Categories.getCategory("Fruits and vegetables");
        in0.m_Provenance = Provenance.Everywhere;
        m_Ingredients.put("Pepper", in0);

        Ingredient in1 = new Ingredient();
        in1.m_Category = m_Categories.getCategory("Bread");
        in1.m_Provenance = Provenance.Everywhere;
        m_Ingredients.put("Zopf", in1);

        Ingredient in2 = new Ingredient();
        in2.m_Category = m_Categories.getCategory("Fruits and vegetables");
        in2.m_Provenance = Provenance.Coop;
        m_Ingredients.put("Apple", in2);

        Ingredient in3 = new Ingredient();
        in3.m_Category = m_Categories.getCategory("Dairy products");
        in3.m_Provenance = Provenance.Migros;
        m_Ingredients.put("Milk", in3);
    }

    public void addIngredient(String strName)
    {
        if(m_Ingredients.containsKey(strName))
        {
            return;
        }
        Ingredient i = new Ingredient();
        i.m_Provenance = Provenance.Everywhere;
        i.m_Category = m_Categories.getCategory(m_Categories.getAllCategories().firstElement());
        m_Ingredients.put(strName, i);
    }

    public Ingredient getIngredient(String strName)
    {
        return m_Ingredients.get(strName);
    }

    public Vector<String> getAllIngredients()
    {
        Vector<String> vec = new Vector<String>();
        for(Object obj : m_Ingredients.keySet())
        {
            String str = (String)obj;
            vec.add(str);
        }
        return vec;
    }

    public void removeIngredient(String strName)
    {
        m_Ingredients.remove(strName);
    }

    // Parceling

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        m_Categories.writeToParcel(out, flags);

        out.writeInt(m_Ingredients.size());
        for(LinkedHashMap.Entry<String, Ingredient> e : m_Ingredients.entrySet())
        {
            out.writeString(e.getKey());
            out.writeString(e.getValue().m_Category.getName());
            out.writeInt(e.getValue().m_Provenance.ordinal());
        }
    }

    private Ingredients(Parcel in)
    {
        m_Categories = Categories.CREATOR.createFromParcel(in);

        int size = in.readInt();
        m_Ingredients = new LinkedHashMap<String, Ingredient>(size);
        for(int i = 0; i < size; i++)
        {
            String strName = in.readString();
            Ingredient order = new Ingredient();
            order.m_Category = m_Categories.getCategory(in.readString());
            order.m_Provenance = Provenance.values()[in.readInt()];
            m_Ingredients.put(strName, order);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Ingredients> CREATOR
            = new Parcelable.Creator<Ingredients>()
    {
        @Override
        public Ingredients createFromParcel(Parcel in) {
            return new Ingredients(in);
        }

        @Override
        public Ingredients[] newArray(int size) {
            return new Ingredients[size];
        }
    };
}
