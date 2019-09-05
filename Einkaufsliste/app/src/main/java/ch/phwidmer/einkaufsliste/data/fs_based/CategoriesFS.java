package ch.phwidmer.einkaufsliste.data.fs_based;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;

import ch.phwidmer.einkaufsliste.data.Categories;
import ch.phwidmer.einkaufsliste.helper.Helper;

public class CategoriesFS extends Categories
{
    public class CategoryFS extends Category
    {
        private String m_Category;

        private CategoryFS(String strName)
        {
            m_Category = strName;
        }

        @Override
        public String getName()
        {
            return m_Category;
        }

        @Override
        public int hashCode()
        {
            return m_Category.hashCode();
        }
    }

    public class SortOrderFS implements Categories.SortOrder
    {
        private String m_Name;
        ArrayList<Category> m_CategoriesOrder = new ArrayList<>();

        SortOrderFS(String name)
        {
            m_Name = name;
        }

        @Override
        public String getName()
        {
            return m_Name;
        }

        @Override
        public ArrayList<Category> getOrder()
        {
            return m_CategoriesOrder;
        }

        @Override
        public void setOrder(ArrayList<Category> order)
        {
            m_CategoriesOrder = order;
        }

        @Override
        public void moveCategory(Category category, int newPos)
        {
            m_CategoriesOrder.remove(category);
            m_CategoriesOrder.add(newPos, category);
        }
    }

    private LinkedHashSet<String>       m_Categories;
    private LinkedHashSet<SortOrderFS>  m_SortOrders;

    CategoriesFS()
    {
        m_Categories = new LinkedHashSet<>();
        m_SortOrders = new LinkedHashSet<>();
    }

    // Categories

    @Override
    public void addCategory(String strName)
    {
        if(m_Categories.contains(strName))
        {
            return;
        }
        m_Categories.add(strName);

        for(SortOrderFS sortOrder : m_SortOrders)
        {
            sortOrder.m_CategoriesOrder.add(new CategoryFS(strName));
        }
    }

    @Override
    public void renameCategory(Category category, String strNewName)
    {
        if(!m_Categories.contains(category.getName()))
        {
            return;
        }

        m_Categories.remove(category.getName());
        m_Categories.add(strNewName);

        Category newCategory = new CategoryFS(strNewName);
        for(SortOrderFS sortOrder : m_SortOrders)
        {
            ArrayList<Category> vec = sortOrder.m_CategoriesOrder;

            int index = vec.indexOf(category);
            vec.remove(index);
            vec.add(index, newCategory);
        }
    }

    @Override
    public void removeCategory(Category category)
    {
        m_Categories.remove(category.getName());

        for(SortOrderFS sortOrder : m_SortOrders)
        {
            sortOrder.m_CategoriesOrder.remove(new CategoryFS(category.getName()));
        }
    }

    @Override
    public Category getCategory(String category)
    {
        if(m_Categories.contains(category))
        {
            return new CategoryFS(category);
        }

        return null;
    }

    @Override
    public int getCategoriesCount()
    {
        return m_Categories.size();
    }

    @Override
    public ArrayList<Category> getAllCategories()
    {
        ArrayList<Category> vec = new ArrayList<>();
        for(String str : m_Categories)
        {
            vec.add(new CategoryFS(str));
        }
        return vec;
    }

    @Override
    public ArrayList<String> getAllCategorieNames()
    {
        ArrayList<String> vec = new ArrayList<>(m_Categories);
        Collections.sort(vec, new Helper.SortStringIgnoreCase());
        return vec;
    }

    @Override
    public Category getDefaultCategory()
    {
        if(m_Categories.size() > 0)
        {
            return new CategoryFS((String)m_Categories.toArray()[0]);
        }
        return null;
    }

    // SortOrder

    @Override
    public SortOrder addSortOrder(String strName)
    {
        for(SortOrderFS sortOrder : m_SortOrders)
        {
            if(sortOrder.getName().equals(strName))
            {
                return sortOrder;
            }
        }

        SortOrderFS order = new SortOrderFS(strName);
        for(String str : m_Categories)
        {
            order.m_CategoriesOrder.add(new CategoryFS(str));
        }
        m_SortOrders.add(order);
        return order;
    }

    @Override
    public void renameSortOrder(SortOrder order, String strNewName)
    {
        for(SortOrderFS sortOrder : m_SortOrders)
        {
            if(sortOrder.getName().equals(strNewName))
            {
                return;
            }
        }

        SortOrderFS sortOrder = (SortOrderFS)order;
        sortOrder.m_Name = strNewName;
    }

    @Override
    public void removeSortOrder(String strName)
    {
        for(SortOrderFS sortOrder : m_SortOrders)
        {
            if(sortOrder.getName().equals(strName))
            {
                m_SortOrders.remove(sortOrder);
            }
        }
    }

    @Override
    public SortOrder getSortOrder(String strName)
    {
        for(SortOrderFS sortOrder : m_SortOrders)
        {
            if(sortOrder.getName().equals(strName))
            {
                return sortOrder;
            }
        }

        return null;
    }

    @Override
    public int getSortOrdersCount()
    {
        return m_SortOrders.size();
    }

    @Override
    public ArrayList<SortOrder> getAllSortOrders()
    {
        return new ArrayList<>(m_SortOrders);
    }

    @Override
    public ArrayList<String> getAllSortOrderNames()
    {
        ArrayList<String> vec = new ArrayList<>();
        for(SortOrderFS sortOrder : m_SortOrders)
        {
            vec.add(sortOrder.getName());
        }
        Collections.sort(vec, new Helper.SortStringIgnoreCase());
        return vec;
    }
}
