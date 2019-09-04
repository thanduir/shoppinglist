package ch.phwidmer.einkaufsliste.data;

import android.support.annotation.NonNull;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

public abstract class Ingredients
{
    public static final String c_strProvenanceEverywhere = "*EVERYWHERE*";

    public interface Ingredient
    {
        String getName();

        String getCategory();
        void setCategory(String cateogry);

        String getProvenance();
        void setProvenance(String strProvenance);

        Amount.Unit getDefaultUnit();
        void setDefaultUnit(Amount.Unit unit);
    }

    public abstract Ingredient addIngredient(String strName, Amount.Unit defaultUnit, Categories.Category category);
    protected abstract Ingredient addIngredient(String strName, Amount.Unit defaultUnit, String strCategory);
    public abstract void removeIngredient(Ingredient ingredient);
    public abstract void renameIngredient(Ingredient ingredient, String strNewName);
    public abstract Ingredient getIngredient(String strName);

    public abstract int getIngredientsCount();
    public abstract ArrayList<Ingredient> getAllIngredients();
    public abstract ArrayList<String> getAllIngredientNames();

    public void onCategoryRenamed(Categories.Category category, Categories.Category newCategory)
    {
        for(Ingredient i : getAllIngredients())
        {
            if(i.getCategory().equals(category.getName()))
            {
                i.setCategory(newCategory.getName());
            }
        }
    }

    public boolean isCategoryInUse(Categories.Category category, @NonNull ArrayList<Ingredient> ingredientsUsingCategory)
    {
        boolean stillInUse = false;
        for(Ingredient ingredient : getAllIngredients())
        {
            if(ingredient.getCategory().equals(category.getName()))
            {
                ingredientsUsingCategory.add(ingredient);
                stillInUse = true;
            }
        }
        return stillInUse;
    }

    public boolean isSortOrderInUse(String strSortOrder, @NonNull ArrayList<Ingredient> ingredientsUsingSortOrder)
    {
        boolean stillInUse = false;
        for(Ingredient ingredient : getAllIngredients())
        {
            if(ingredient.getProvenance().equals(strSortOrder))
            {
                ingredientsUsingSortOrder.add(ingredient);
                stillInUse = true;
            }
        }
        return stillInUse;
    }

    boolean checkDataConsistency(Categories categories, LinkedList<String> missingCategories, LinkedList<String> missingSortOrders)
    {
        boolean dataConsistent = true;

        for(Ingredient ingredient : getAllIngredients())
        {
            String category = ingredient.getCategory();
            if(categories.getCategory(category) == null)
            {
                if(!missingCategories.contains(category))
                {
                    missingCategories.add(category);
                }
                dataConsistent = false;
            }
            String sortOrder = ingredient.getProvenance();
            if(categories.getSortOrder(sortOrder) == null && !sortOrder.equals(c_strProvenanceEverywhere))
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

    void saveToJson(JsonWriter writer) throws IOException
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

    void readFromJson(JsonReader reader) throws IOException
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

                Ingredient in = addIngredient(name, unit, strCategory);
                in.setProvenance(strProvenance);
            }
        }
        reader.endObject();
    }
}
