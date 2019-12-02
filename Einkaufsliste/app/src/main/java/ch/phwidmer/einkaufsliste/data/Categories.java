package ch.phwidmer.einkaufsliste.data;

import android.support.annotation.NonNull;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public abstract class Categories
{
    // Categories

    public abstract class Category
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
    public abstract void removeSortOrder(@NonNull String strName);
    public abstract Optional<SortOrder> getSortOrder(@NonNull String strName);

    public abstract ArrayList<SortOrder> getAllSortOrders();
    public abstract ArrayList<String> getAllSortOrderNames();

    // Serializing

    void saveToJson(JsonWriter writer) throws IOException
    {
        writer.beginObject();
        writer.name("id").value("Categories");

        writer.name("all categories");
        writer.beginArray();
        for(Category c : getAllCategories())
        {
            writer.value(c.getName());
        }
        writer.endArray();

        writer.name("sortOrders");
        writer.beginObject();
        for(SortOrder order : getAllSortOrders())
        {
            writer.name(order.getName());
            writer.beginArray();
            for(Category c : order.getOrder())
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

                        reader.beginArray();
                        ArrayList<Category> newOrder = new ArrayList<>();
                        while (reader.hasNext()) {
                            Optional<Category> category = getCategory(reader.nextString());
                            if(!category.isPresent())
                            {
                                throw new IOException();
                            }
                            newOrder.add(category.get());
                        }
                        reader.endArray();

                        SortOrder order = addSortOrder(orderName);
                        order.setOrder(newOrder);
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
}
