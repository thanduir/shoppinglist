package ch.phwidmer.einkaufsliste.data;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Optional;

public abstract class Categories
{
    // Categories

    public static abstract class Category
    {
        public abstract String getName();

        @Override
        public boolean equals(Object other)
        {
            if(!(other instanceof Category))
            {
                return false;
            }
            Category c = (Category)other;
            return this.getName().equals(c.getName());
        }
    }

    public abstract void addCategory(@NonNull String strName);
    public abstract void renameCategory(@NonNull Category category, @NonNull String strNewName);
    public abstract void removeCategory(@NonNull Category category);
    public abstract Optional<Category> getCategory(@NonNull String category);

    public abstract int getCategoriesCount();
    public abstract ArrayList<Category> getAllCategories();
    public abstract ArrayList<String> getAllCategorieNames();
    public abstract Optional<Category> getDefaultCategory();

    // SortOrder

    public interface SortOrder
    {
        String getName();

        ArrayList<Category> getOrder();
        void setOrder(ArrayList<Category> order);

        void moveCategory(Category category, int newPos);
    }

    public abstract SortOrder addSortOrder(@NonNull String strName);
    public abstract void renameSortOrder(@NonNull SortOrder order, @NonNull String strNewName);
    public abstract void removeSortOrder(@NonNull SortOrder order);
    public abstract Optional<SortOrder> getSortOrder(@NonNull String strName);

    public abstract ArrayList<SortOrder> getAllSortOrders();
    public abstract ArrayList<String> getAllSortOrderNames();
}
