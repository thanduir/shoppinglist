package ch.phwidmer.einkaufsliste;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
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
        public Provenance m_Provenance = Provenance.Everywhere;;
        // TODO: Std-Wert entfernen (Config?) und in ManageIngredients einbauen!
        public Amount.Unit m_DefaultUnit = Amount.Unit.Count;
    }
    private LinkedHashMap<String, Ingredient>  m_Ingredients;

    public Ingredients(Categories categories)
    {
        m_Categories = categories;
        m_Ingredients = new LinkedHashMap<String, Ingredient>();
    }

    public void addIngredient(String strName)
    {
        if(m_Ingredients.containsKey(strName))
        {
            return;
        }
        Ingredient i = new Ingredient();
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

    // Serializing

    public void saveToJson(JsonWriter writer) throws IOException
    {
        writer.beginObject();
        writer.name("id").value("Ingredients");

        for(LinkedHashMap.Entry<String, Ingredient> e : m_Ingredients.entrySet())
        {
            writer.name(e.getKey());
            writer.beginObject();
            writer.name("category").value(e.getValue().m_Category.getName());
            writer.name("provenance").value(e.getValue().m_Provenance.toString());
            writer.name("default-unit").value(e.getValue().m_DefaultUnit.toString());

            writer.endObject();
        }

        writer.endObject();
    }

    public void readFromJson(JsonReader reader, int iVersion) throws IOException
    {
        reader.beginObject();
        while (reader.hasNext())
        {
            String name = reader.nextName();
            if (name.equals("id"))
            {
                String id = reader.nextString();
                if(!id.equals("Ingredients"))
                {
                    throw new IOException();
                }
            }
            else
            {
                Ingredient in = new Ingredient();

                reader.beginObject();
                while (reader.hasNext())
                {
                    String currentName = reader.nextName();
                    if (currentName.equals("category"))
                    {
                        in.m_Category = m_Categories.getCategory(reader.nextString());
                    }
                    else if(currentName.equals("provenance"))
                    {
                        String provenance = reader.nextString();
                        in.m_Provenance = Provenance.valueOf(provenance);
                    }
                    else if(currentName.equals("default-unit"))
                    {
                        String unit = reader.nextString();
                        in.m_DefaultUnit = Amount.Unit.valueOf(unit);
                    }
                    else
                    {
                        reader.skipValue();
                    }
                }
                reader.endObject();

                m_Ingredients.put(name, in);
            }
        }
        reader.endObject();
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
            out.writeInt(e.getValue().m_DefaultUnit.ordinal());
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
            order.m_DefaultUnit = Amount.Unit.values()[in.readInt()];
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
