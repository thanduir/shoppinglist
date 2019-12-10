package ch.phwidmer.einkaufsliste.data.db_based;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ch.phwidmer.einkaufsliste.data.Categories;
import ch.phwidmer.einkaufsliste.data.db_based.entities.category.CategoryRow;
import ch.phwidmer.einkaufsliste.data.db_based.entities.category.CategorySortOrderRow;
import ch.phwidmer.einkaufsliste.data.db_based.entities.category.SortOrderRow;

public class CategoriesDB extends Categories {
    private AppDatabase database;

    public class CategoryDB extends Categories.Category
    {
        private long   m_Id;

        private CategoryDB(long id)
        {
            m_Id = id;
        }

        public long getId()
        {
            return m_Id;
        }

        @Override
        public String getName()
        {
            return database.categoriesDao().getCategoryName(m_Id);
        }

        @Override
        public boolean equals(Object other)
        {
            if(!(other instanceof CategoryDB))
            {
                return false;
            }
            CategoryDB c = (CategoryDB)other;
            return this.m_Id == c.m_Id;
        }
    }

    CategoriesDB(AppDatabase database)
    {
        this.database = database;
    }

    @Override
    public void addCategory(@NonNull String strName)
    {
        if(database.categoriesDao().getCategoryIds(strName).length > 0)
        {
            return;
        }

        CategoryRow row = new CategoryRow();
        row.name = strName;
        long id = database.categoriesDao().insertCategory(row);

        int nextPosition = getCategoriesCount() - 1;

        long[] sortOrders = database.categoriesDao().getAllSortOrderIds();
        ArrayList<CategorySortOrderRow> categorySortOrders = new ArrayList<>();
        for(long sortOrderId : sortOrders)
        {
            CategorySortOrderRow categorySortOrder = new CategorySortOrderRow();
            categorySortOrder.position = nextPosition;
            categorySortOrder.categoryId = id;
            categorySortOrder.sortOrderId = sortOrderId;
            categorySortOrders.add(categorySortOrder);
        }
        database.categoriesDao().insertCategorySortOrders(categorySortOrders);
    }
    @Override
    public void renameCategory(@NonNull Category category, @NonNull String strNewName)
    {
        CategoryDB catDB = (CategoryDB)category;
        CategoryRow row = database.categoriesDao().getCategory(catDB.getId());
        row.name = strNewName;
        database.categoriesDao().updateCategories(row);
    }
    @Override
    public void removeCategory(@NonNull Category category)
    {
        CategoryDB catDB = (CategoryDB)category;
        CategoryRow row = database.categoriesDao().getCategory(catDB.getId());
        database.categoriesDao().deleteCategory(row);
    }
    @Override
    public Optional<Category> getCategory(@NonNull String category)
    {
        long[] ids = database.categoriesDao().getCategoryIds(category);
        if(ids.length == 0)
        {
            return Optional.empty();
        }
        return Optional.of(new CategoryDB(ids[0]));
    }

    @Override
    public int getCategoriesCount()
    {
        return (int)database.categoriesDao().getCategoriesCount();
    }
    @Override
    public ArrayList<Category> getAllCategories()
    {
        long[] ids = database.categoriesDao().getAllCategoryIds();
        ArrayList<Category> vec = new ArrayList<>();
        for(long id : ids)
        {
            vec.add(new CategoryDB(id));
        }
        return vec;
    }
    @Override
    public ArrayList<String> getAllCategorieNames()
    {
        List<String> names = database.categoriesDao().getAllCategoryNames();
        return new ArrayList<>(names);
    }
    @Override
    public Optional<Category> getDefaultCategory()
    {
        long[] ids = database.categoriesDao().getAllCategoryIds();
        if(ids.length > 0)
        {
            return Optional.of(new CategoryDB(ids[0]));
        }
        return Optional.empty();
    }

    // SortOrder

    public class SortOrderDB implements Categories.SortOrder
    {
        private long m_Id;

        SortOrderDB(long id)
        {
            m_Id = id;
        }

        public long getId()
        {
            return m_Id;
        }

        @Override
        public String getName()
        {
            return database.categoriesDao().getSortOrderName(m_Id);
        }

        @Override
        public ArrayList<Category> getOrder()
        {
            CategorySortOrderRow[] rows = database.categoriesDao().getCategoriesFromSortOrderIdSortedByPosition(m_Id);
            ArrayList<Category> vec = new ArrayList<>();
            for(CategorySortOrderRow row : rows)
            {
                vec.add(new CategoryDB(row.categoryId));
            }
            return vec;
        }
        @Override
        public void setOrder(ArrayList<Category> order)
        {
            CategorySortOrderRow[] rows = database.categoriesDao().getCategoriesFromSortOrderIdSortedByCategoryId(m_Id);
            int i = 0;
            for(Category category : order)
            {
                CategoryDB categoryDB = (CategoryDB)category;
                for(CategorySortOrderRow row : rows)
                {
                    if(row.categoryId == categoryDB.getId())
                    {
                        row.position = i;
                    }
                }
                ++i;
            }
            database.categoriesDao().updateCategorySortOrderss(rows);
        }

        @Override
        public void moveCategory(Category category, int newPos)
        {
            CategoryDB categoryDB = (CategoryDB)category;

            CategorySortOrderRow[] rows = database.categoriesDao().getCategoriesFromSortOrderIdSortedByPosition(m_Id);
            long oldPos = newPos;
            for(CategorySortOrderRow row : rows)
            {
                if(row.categoryId == categoryDB.getId())
                {
                    oldPos = row.position;
                    row.position = newPos;
                }
            }

            for(CategorySortOrderRow row : rows)
            {
                if(row.categoryId == categoryDB.getId())
                {
                    continue;
                }

                if(row.position > oldPos && row.position <= newPos)
                {
                    row.position -= 1;
                }
                else if(row.position < oldPos && row.position >= newPos)
                {
                    row.position += 1;
                }
            }
            database.categoriesDao().updateCategorySortOrderss(rows);
        }
    }

    @Override
    public SortOrder addSortOrder(@NonNull String strName)
    {
        Optional<SortOrder> order = getSortOrder(strName);
        if(order.isPresent())
        {
            return order.get();
        }

        SortOrderRow row = new SortOrderRow();
        row.name = strName;
        long id = database.categoriesDao().insertSortOrder(row);

        long[] categories = database.categoriesDao().getAllCategoryIds();
        if(categories.length > 0)
        {
            ArrayList<CategorySortOrderRow> categorySortOrders = new ArrayList<>();
            int i = 0;
            for(long categoryId : categories)
            {
                CategorySortOrderRow categorySortOrder = new CategorySortOrderRow();
                categorySortOrder.position = i++;
                categorySortOrder.categoryId = categoryId;
                categorySortOrder.sortOrderId = id;
                categorySortOrders.add(categorySortOrder);
            }
            database.categoriesDao().insertCategorySortOrders(categorySortOrders);
        }
        return new SortOrderDB(id);
    }
    @Override
    public void renameSortOrder(@NonNull SortOrder order, @NonNull String strNewName)
    {
        SortOrderDB sortOrderDB = (SortOrderDB)order;
        SortOrderRow row = database.categoriesDao().getSortOrder(sortOrderDB.getId());
        row.name = strNewName;
        database.categoriesDao().updateSortOrders(row);
    }
    @Override
    public void removeSortOrder(@NonNull SortOrder order)
    {
        SortOrderDB sortOrderDB = (SortOrderDB)order;
        SortOrderRow row = database.categoriesDao().getSortOrder(sortOrderDB.getId());
        database.categoriesDao().deleteSortOrder(row);
    }
    @Override
    public Optional<SortOrder> getSortOrder(@NonNull String strName)
    {
        long[] ids = database.categoriesDao().getSortOrderIds(strName);
        if(ids.length == 0)
        {
            return Optional.empty();
        }
        return Optional.of(new SortOrderDB(ids[0]));
    }

    @Override
    public ArrayList<SortOrder> getAllSortOrders()
    {
        long[] ids = database.categoriesDao().getAllSortOrderIds();
        ArrayList<SortOrder> vec = new ArrayList<>();
        for(long id : ids)
        {
            vec.add(new SortOrderDB(id));
        }
        return vec;
    }
    @Override
    public ArrayList<String> getAllSortOrderNames()
    {
        List<String> names = database.categoriesDao().getAllSortOrderNames();
        return new ArrayList<>(names);
    }
}
