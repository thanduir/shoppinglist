package ch.phwidmer.einkaufsliste.data.fs_based;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;

import ch.phwidmer.einkaufsliste.data.Categories;
import ch.phwidmer.einkaufsliste.helper.Helper;

public class CategoriesFS extends Categories
{
    public static class CategoryFS extends Category
    {
        private String m_Category;

        private CategoryFS(@NonNull String strName)
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

    public static class SortOrderFS implements Categories.SortOrder
    {
        private String m_Name;
        ArrayList<Category> m_CategoriesOrder = new ArrayList<>();

        SortOrderFS(@NonNull String name)
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
        public void setOrder(@NonNull ArrayList<Category> order)
        {
            m_CategoriesOrder = order;
        }

        @Override
        public void moveCategory(@NonNull Category category, int newPos)
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
    public void addCategory(@NonNull String strName)
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
    public void renameCategory(@NonNull Category category, @NonNull String strNewName)
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
    public void removeCategory(@NonNull Category category)
    {
        m_Categories.remove(category.getName());

        for(SortOrderFS sortOrder : m_SortOrders)
        {
            sortOrder.m_CategoriesOrder.remove(new CategoryFS(category.getName()));
        }
    }

    @Override
    public Optional<Category> getCategory(@NonNull String category)
    {
        if(m_Categories.contains(category))
        {
            return Optional.of(new CategoryFS(category));
        }

        return Optional.empty();
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
    public Optional<Category> getDefaultCategory()
    {
        if(m_Categories.size() > 0)
        {
            return Optional.of(new CategoryFS((String)m_Categories.toArray()[0]));
        }
        return Optional.empty();
    }

    // SortOrder

    @Override
    public SortOrder addSortOrder(@NonNull String strName)
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
    public void renameSortOrder(@NonNull SortOrder order, @NonNull String strNewName)
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
    public void removeSortOrder(@NonNull SortOrder order)
    {
        SortOrderFS sortOrder = (SortOrderFS)order;
        m_SortOrders.remove(sortOrder);
    }

    @Override
    public Optional<SortOrder> getSortOrder(@NonNull String strName)
    {
        for(SortOrderFS sortOrder : m_SortOrders)
        {
            if(sortOrder.getName().equals(strName))
            {
                return Optional.of(sortOrder);
            }
        }

        return Optional.empty();
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
