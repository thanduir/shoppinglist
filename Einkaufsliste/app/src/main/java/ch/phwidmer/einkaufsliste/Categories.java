package ch.phwidmer.einkaufsliste;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

class Categories implements Parcelable
{
    public class Category
    {
        private CharSequence m_Category;

        private Category(String strName)
        {
            m_Category = strName;
        }

        public String getName()
        {
            return (String)m_Category;
        }

        @Override
        public boolean equals(Object other)
        {
            if(!(other instanceof Category))
            {
                return false;
            }
            Category c = (Category)other;
            return m_Category.equals(c.m_Category);
        }

        @Override
        public int hashCode()
        {
            return m_Category.hashCode();
        }
    }
    // Categories
    private LinkedHashSet<String> m_Categories;

    // CategorySortOrder
    class SortOrder
    {
        ArrayList<Category> m_CategoriesOrder = new ArrayList<>();
    }
    private LinkedHashMap<String, SortOrder>  m_SortOrders;

    private String m_ActiveSortOrder;

    Categories()
    {
        m_Categories = new LinkedHashSet<>();
        m_SortOrders = new LinkedHashMap<>();
        m_ActiveSortOrder = "";
    }

    void setActiveSortOrder(String strOrder)
    {
        m_ActiveSortOrder = strOrder;
    }

    String getActiveSortOrder()
    {
        return m_ActiveSortOrder;
    }

    // Categories methods

    void addCategory(String strName)
    {
        if(m_Categories.contains(strName))
        {
            return;
        }
        m_Categories.add(strName);

        for(LinkedHashMap.Entry<String, SortOrder> e : m_SortOrders.entrySet())
        {
            e.getValue().m_CategoriesOrder.add(new Category(strName));
        }
    }

    Category getCategory(String category)
    {
        if(m_Categories.contains(category))
        {
            return new Category(category);
        }

        return null;
    }

    ArrayList<String> getAllCategories()
    {
        ArrayList<String> vec = new ArrayList<>();
        for(Object obj : m_Categories.toArray())
        {
            vec.add((String)obj);
        }
        Collections.sort(vec, new Helper.SortIgnoreCase());
        return vec;
    }

    void removeCategory(String strName)
    {
        m_Categories.remove(strName);

        for(LinkedHashMap.Entry<String, SortOrder> e : m_SortOrders.entrySet())
        {
            e.getValue().m_CategoriesOrder.remove(new Category(strName));
        }
    }

    void renameCategory(Category category, String strNewName)
    {
        if(!m_Categories.contains(category.getName()))
        {
            return;
        }

        m_Categories.remove(category.getName());
        m_Categories.add(strNewName);

        Category newCategory = new Category(strNewName);
        for(LinkedHashMap.Entry<String, SortOrder> e : m_SortOrders.entrySet())
        {
            ArrayList<Category> vec = e.getValue().m_CategoriesOrder;

            int index = vec.indexOf(category);
            vec.remove(index);
            vec.add(index, newCategory);
        }
    }

    // SortOrder methods

    void addSortOrder(String strName)
    {
        SortOrder order = new SortOrder();
        for(String str : m_Categories)
        {
            order.m_CategoriesOrder.add(new Category(str));
        }
        m_SortOrders.put(strName, order);
    }

    void addSortOrder(String strName, SortOrder order)
    {
        if(order.m_CategoriesOrder.size() != m_Categories.size())
        {
            return;
        }
        m_SortOrders.put(strName, order);
    }

    SortOrder getSortOrder(String strName)
    {
        return m_SortOrders.get(strName);
    }

    ArrayList<String> getAllSortOrders()
    {
        return new ArrayList<>(m_SortOrders.keySet());
    }

    void removeSortOrder(String strName)
    {
        m_SortOrders.remove(strName);
    }

    // Serializing

    void saveToJson(JsonWriter writer) throws IOException
    {
        writer.beginObject();
        writer.name("id").value("Categories");
        writer.name("activeSortOrder").value(m_ActiveSortOrder);

        writer.name("all categories");
        writer.beginArray();
        for(String str : m_Categories)
        {
            writer.value(str);
        }
        writer.endArray();

        writer.name("sortOrders");
        writer.beginObject();
        for(LinkedHashMap.Entry<String, SortOrder> e : m_SortOrders.entrySet())
        {
            writer.name(e.getKey());
            writer.beginArray();
            for(Category c : e.getValue().m_CategoriesOrder)
            {
                writer.value(c.getName());
            }
            writer.endArray();
        }
        writer.endObject();

        writer.endObject();
    }

    void readFromJson(JsonReader reader) throws IOException
    {
        reader.beginObject();
        while (reader.hasNext())
        {
            String name = reader.nextName();
            switch(name) {
                case "id":
                {
                    String id = reader.nextString();
                    if (!id.equals("Categories")) {
                        throw new IOException();
                    }
                    break;
                }

                case ("activeSortOrder"):
                {
                    m_ActiveSortOrder = reader.nextString();
                    break;
                }

                case ("all categories"):
                {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        addCategory(reader.nextString());
                    }
                    reader.endArray();
                    break;
                }

                case ("sortOrders"):
                {
                    reader.beginObject();
                    while (reader.hasNext()) {
                        String orderName = reader.nextName();
                        SortOrder order = new SortOrder();

                        reader.beginArray();
                        while (reader.hasNext()) {
                            order.m_CategoriesOrder.add(getCategory(reader.nextString()));
                        }
                        reader.endArray();
                        m_SortOrders.put(orderName, order);
                    }
                    reader.endObject();
                    break;
                }

                default:
                {
                    reader.skipValue();
                    break;
                }
            }
        }

        reader.endObject();
    }

    // Parcelable

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeInt(m_Categories.size());
        for(String str : m_Categories)
        {
            out.writeString(str);
        }

        out.writeInt(m_SortOrders.size());
        for(LinkedHashMap.Entry<String, SortOrder> e : m_SortOrders.entrySet())
        {
            out.writeString(e.getKey());

            out.writeInt(e.getValue().m_CategoriesOrder.size());
            for(Category cat : e.getValue().m_CategoriesOrder)
            {
                out.writeString(cat.getName());
            }
        }

        out.writeString(m_ActiveSortOrder);

    }

    private Categories(Parcel in)
    {
        int categoriesSize = in.readInt();
        m_Categories = new LinkedHashSet<>(categoriesSize);
        for(int i = 0; i <categoriesSize; ++i)
        {
            m_Categories.add(in.readString());
        }

        int sortOrdersSize = in.readInt();
        m_SortOrders = new LinkedHashMap<>(sortOrdersSize);
        for(int i = 0; i <sortOrdersSize; ++i)
        {
            String str = in.readString();

            int sortOrderSize = in.readInt();
            SortOrder s = new SortOrder();
            for(int j = 0; j < sortOrderSize; ++j)
            {
                s.m_CategoriesOrder.add(new Category(in.readString()));
            }
            m_SortOrders.put(str, s);
        }

        m_ActiveSortOrder = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Categories> CREATOR
            = new Parcelable.Creator<Categories>() {

        @Override
        public Categories createFromParcel(Parcel in) {
            return new Categories(in);
        }

        @Override
        public Categories[] newArray(int size) {
            return new Categories[size];
        }
    };
}
