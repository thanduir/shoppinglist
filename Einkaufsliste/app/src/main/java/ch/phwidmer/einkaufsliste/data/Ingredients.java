package ch.phwidmer.einkaufsliste.data;

import android.support.annotation.NonNull;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;

import ch.phwidmer.einkaufsliste.helper.Helper;

public abstract class Ingredients
{
    public static final String c_strProvenanceEverywhere = "*EVERYWHERE*";

    public abstract class Ingredient implements Helper.NamedObject
    {
        public abstract String getCategory();
        public abstract void setCategory(@NonNull String cateogry);

        public abstract String getProvenance();
        public abstract void setProvenance(@NonNull String strProvenance);

        public abstract Amount.Unit getDefaultUnit();
        public abstract void setDefaultUnit(@NonNull Amount.Unit unit);
    }

    public abstract Optional<Ingredient> addIngredient(@NonNull String strName, @NonNull Amount.Unit defaultUnit, @NonNull Categories.Category category);
    protected abstract Optional<Ingredient> addIngredient(@NonNull String strName, @NonNull Amount.Unit defaultUnit, @NonNull String strCategory);
    public abstract void removeIngredient(@NonNull Ingredient ingredient);
    public abstract void renameIngredient(@NonNull Ingredient ingredient, @NonNull String strNewName);
    public abstract Optional<Ingredient> getIngredient(String strName);

    public abstract int getIngredientsCount();
    public abstract ArrayList<Ingredient> getAllIngredients();
    public abstract ArrayList<String> getAllIngredientNames();

    public void onCategoryRenamed(@NonNull String oldCategory, @NonNull String newCategory)
    {
        for(Ingredient i : getAllIngredients())
        {
            if(i.getCategory().equals(oldCategory))
            {
                i.setCategory(newCategory);
            }
        }
    }

    public boolean isCategoryInUse(@NonNull Categories.Category category, @NonNull ArrayList<String> ingredientsUsingCategory)
    {
        boolean stillInUse = false;
        for(Ingredient ingredient : getAllIngredients())
        {
            if(ingredient.getCategory().equals(category.getName()))
            {
                ingredientsUsingCategory.add(ingredient.getName());
                stillInUse = true;
            }
        }
        return stillInUse;
    }

    public void onSortOrderRenamed(@NonNull String oldSortOrder, @NonNull String newSortOrder)
    {
        for(Ingredient i : getAllIngredients())
        {
            if(i.getProvenance().equals(oldSortOrder))
            {
                i.setProvenance(newSortOrder);
            }
        }
    }

    public boolean isSortOrderInUse(@NonNull String strSortOrder, @NonNull ArrayList<String> ingredientsUsingSortOrder)
    {
        boolean stillInUse = false;
        for(Ingredient ingredient : getAllIngredients())
        {
            if(ingredient.getProvenance().equals(strSortOrder))
            {
                ingredientsUsingSortOrder.add(ingredient.getName());
                stillInUse = true;
            }
        }
        return stillInUse;
    }

    boolean checkDataConsistency(@NonNull Categories categories, @NonNull LinkedList<String> missingCategories, @NonNull LinkedList<String> missingSortOrders)
    {
        boolean dataConsistent = true;

        for(Ingredient ingredient : getAllIngredients())
        {
            String category = ingredient.getCategory();
            if(!categories.getCategory(category).isPresent())
            {
                if(!missingCategories.contains(category))
                {
                    missingCategories.add(category);
                }
                dataConsistent = false;
            }
            String sortOrder = ingredient.getProvenance();
            if(!categories.getSortOrder(sortOrder).isPresent() && !sortOrder.equals(c_strProvenanceEverywhere))
            {
                if(!missingSortOrders.contains(sortOrder))
                {
                    missingSortOrders.add(sortOrder);
                }
                dataConsistent = false;
            }
        }

        return dataConsistent;
    }

    // Serializing

    void saveToJson(@NonNull JsonWriter writer) throws IOException
    {
        writer.beginObject();
        writer.name("id").value("Ingredients");

        for(Ingredient ingredient : getAllIngredients())
        {
            writer.name(ingredient.getName());

            writer.beginObject();
            writer.name("category").value(ingredient.getCategory());
            writer.name("provenance").value(ingredient.getProvenance());
            writer.name("default-unit").value(ingredient.getDefaultUnit().toString());
            writer.endObject();
        }

        writer.endObject();
    }

    void readFromJson(@NonNull JsonReader reader) throws IOException
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
                String strCategory = "";
                String strProvenance = "";
                Amount.Unit unit = Amount.Unit.Unitless;

                reader.beginObject();
                while (reader.hasNext())
                {
                    String currentName = reader.nextName();
                    switch(currentName)
                    {
                        case "category":
                        {
                            strCategory = reader.nextString();
                            break;
                        }


                        case "provenance":
                        {
                            strProvenance = reader.nextString();
                            break;
                        }

                        case "default-unit":
                        {
                            String strUnit = reader.nextString();
                            unit = Amount.Unit.valueOf(strUnit);
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

                Optional<Ingredient> in = addIngredient(name, unit, strCategory);
                if(!in.isPresent())
                {
                    throw new IOException();
                }
                in.get().setProvenance(strProvenance);
            }
        }
        reader.endObject();
    }
}
