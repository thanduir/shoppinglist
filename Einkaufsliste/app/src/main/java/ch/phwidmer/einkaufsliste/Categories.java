package ch.phwidmer.einkaufsliste;

import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Vector;

public class Categories
{
    public class Category
    {
        private CharSequence m_Category;

        public Category(String strName)
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
    public class SortOrder
    {
        public Vector<Category> m_CategoriesOrder = new Vector<Category>();
    }
    private LinkedHashMap<String, SortOrder>  m_SortOrders;

    public Categories()
    {
        m_Categories = new LinkedHashSet<String>();
        m_SortOrders = new LinkedHashMap<String, SortOrder>();
    }

    // Categories methods

    public void addCategory(String strName)
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

    public Category getCategory(String category)
    {
        if(m_Categories.contains(category))
        {
            return new Category(category);
        }

        return null;
    }

    public Vector<String> getAllCategories()
    {
        Vector<String> vec = new Vector<String>();
        for(Object obj : m_Categories.toArray())
        {
            vec.add((String)obj);
        }
        return vec;
    }

    public void removeCategory(String strName)
    {
        m_Categories.remove(strName);

        for(LinkedHashMap.Entry<String, SortOrder> e : m_SortOrders.entrySet())
        {
            e.getValue().m_CategoriesOrder.remove(new Category(strName));
        }
    }

    // SortOrder methods

    public void addSortOrder(String strName)
    {
        SortOrder order = new SortOrder();
        for(Object obj : m_Categories.toArray())
        {
            order.m_CategoriesOrder.add(new Category((String)obj));
        }
        m_SortOrders.put(strName, order);
    }

    public void addSortOrder(String strName, SortOrder order)
    {
        if(order.m_CategoriesOrder.size() != m_Categories.size())
        {
            return;
        }
        m_SortOrders.put(strName, order);
    }

    public SortOrder getSortOrder(String strName)
    {
        return m_SortOrders.get(strName);
    }

    public Vector<String> getAllSortOrders()
    {
        Vector<String> vec = new Vector<String>();

        for(String str : m_SortOrders.keySet())
        {
            vec.add(str);
        }
        return vec;
    }

    public void removeSortOrder(String strName)
    {
        m_SortOrders.remove(strName);
    }

    // Serializing

    public void saveToJson(JsonWriter writer) throws IOException
    {
        writer.beginObject();
        writer.name("id").value("Categories");

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

    public void readFromJson(JsonReader reader, int iVersion) throws IOException
    {
        reader.beginObject();
        while (reader.hasNext())
        {
            String name = reader.nextName();
            if (name.equals("id"))
            {
                String id = reader.nextString();
                if(!id.equals("Categories"))
                {
                    throw new IOException();
                }
            }
            else if (name.equals("all categories"))
            {
                reader.beginArray();
                while (reader.hasNext())
                {
                    addCategory(reader.nextString());
                }
                reader.endArray();
            }
            else if (name.equals("sortOrders"))
            {
                reader.beginObject();
                while (reader.hasNext())
                {
                    String orderName = reader.nextName();
                    SortOrder order = new SortOrder();

                    reader.beginArray();
                    while (reader.hasNext())
                    {
                        order.m_CategoriesOrder.add(getCategory(reader.nextString()));
                    }
                    reader.endArray();
                    m_SortOrders.put(orderName, order);
                }
                reader.endObject();
            }
            else
            {
                reader.skipValue();
            }
        }


        reader.endObject();
    }
}
