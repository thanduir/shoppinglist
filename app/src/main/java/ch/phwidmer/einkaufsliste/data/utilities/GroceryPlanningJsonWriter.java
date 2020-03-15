package ch.phwidmer.einkaufsliste.data.utilities;

import android.support.annotation.NonNull;
import android.util.JsonWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ch.phwidmer.einkaufsliste.data.Categories;
import ch.phwidmer.einkaufsliste.data.GroceryPlanning;
import ch.phwidmer.einkaufsliste.data.Ingredients;
import ch.phwidmer.einkaufsliste.data.RecipeItem;
import ch.phwidmer.einkaufsliste.data.Recipes;
import ch.phwidmer.einkaufsliste.data.ShoppingList;
import ch.phwidmer.einkaufsliste.data.ShoppingListItem;

class GroceryPlanningJsonWriter {
    private GroceryPlanning m_GroceryPlanning;

    GroceryPlanningJsonWriter(GroceryPlanning groceryPlanning)
    {
        m_GroceryPlanning = groceryPlanning;
    }

    void write(@NonNull File fileToBeCreated,  String uniqueID) throws IOException
    {
        boolean moveFilesFirst = fileToBeCreated.exists();

        // Concept if the file already exists:
        //      * write to filename.new
        //      * delete filename.old if it already exists
        //      * move existing to filename.old
        //      * move filename.new to filename

        File fileNew;
        if(moveFilesFirst)
        {
            fileNew = new File(fileToBeCreated.getAbsolutePath() + ".new");
        }
        else
        {
            fileNew = fileToBeCreated;
        }

        try
        {
            FileWriter fw = new FileWriter(fileNew, false);
            JsonWriter jw = new JsonWriter(fw);
            jw.setIndent("  ");

            jw.beginArray();

            jw.beginObject();
            jw.name("id").value("ch.phwidmer.einkaufsliste");

            jw.name("origin");
            jw.beginArray();
            jw.value("android");
            jw.value(uniqueID);
            jw.endArray();

            jw.name("version").value(JsonSerializer.SERIALIZING_VERSION);
            jw.endObject();

            writeCategories(jw);
            writeIngredients(jw);
            writeRecipes(jw);
            writeShoppingList(jw);

            jw.endArray();

            jw.close();
            fw.close();
        }
        catch(IOException e)
        {
            throw new IOException(e.getMessage());
        }

        if(moveFilesFirst)
        {
            File fileOld = new File(fileToBeCreated.getAbsolutePath() + ".old");
            if(fileOld.exists())
            {
                //noinspection ResultOfMethodCallIgnored
                fileOld.delete();
            }
            if(!fileToBeCreated.renameTo(fileOld))
            {
                throw new IOException("Saving backup file failed");
            }
            if(!fileNew.renameTo(fileToBeCreated))
            {
                //noinspection ResultOfMethodCallIgnored
                fileOld.renameTo(fileToBeCreated);

                throw new IOException("Couldn't move new file");
            }
        }
    }

    private void writeCategories(@NonNull JsonWriter writer) throws IOException
    {
        Categories categories = m_GroceryPlanning.categories();

        writer.beginObject();
        writer.name("id").value("Categories");

        writer.name("all categories");
        writer.beginArray();
        for(Categories.Category c : categories.getAllCategories())
        {
            writer.value(c.getName());
        }
        writer.endArray();

        writer.name("sortOrders");
        writer.beginObject();
        for(Categories.SortOrder order : categories.getAllSortOrders())
        {
            writer.name(order.getName());
            writer.beginArray();
            for(Categories.Category c : order.getOrder())
            {
                writer.value(c.getName());
            }
            writer.endArray();
        }
        writer.endObject();

        writer.endObject();
    }

    private void writeIngredients(@NonNull JsonWriter writer) throws IOException
    {
        Ingredients ingredients = m_GroceryPlanning.ingredients();

        writer.beginObject();
        writer.name("id").value("Ingredients");

        for(Ingredients.Ingredient ingredient : ingredients.getAllIngredients())
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

    private void writeRecipes(@NonNull JsonWriter writer) throws IOException
    {
        writer.beginObject();
        writer.name("id").value("Recipes");

        for(Recipes.Recipe recipe : m_GroceryPlanning.recipes().getAllRecipes())
        {
            writer.name(recipe.getName());
            writer.beginObject();
            writer.name("NrPersons").value(recipe.getNumberOfPersons());
            for(RecipeItem ri : recipe.getAllRecipeItems())
            {
                writeRecipeItem(writer, ri);
            }

            for(String strGroup : recipe.getAllGroupNames())
            {
                writer.name("group");
                writer.beginObject();
                writer.name("groupName").value(strGroup);
                for(RecipeItem ri : recipe.getAllRecipeItemsInGroup(strGroup))
                {
                    writeRecipeItem(writer, ri);
                }
                writer.endObject();
            }

            writer.endObject();
        }
        writer.endObject();
    }

    private void writeRecipeItem(@NonNull JsonWriter writer, @NonNull RecipeItem ri) throws IOException
    {
        writer.name(ri.getIngredient());
        writer.beginObject();

        writer.name("amountMinMax");
        writer.beginArray();
        writer.value(ri.getAmount().getQuantityMin());
        writer.value(ri.getAmount().getQuantityMax());
        writer.value(ri.getAmount().getUnit().toString());
        writer.endArray();

        writer.name("size").value(ri.getSize().toString());
        writer.name("optional").value(ri.isOptional());
        writer.name("additionalInfo").value(ri.getAdditionalInfo());

        writer.endObject();
    }

    private void writeShoppingList(@NonNull JsonWriter writer) throws IOException
    {
        writer.beginObject();
        writer.name("id").value("Shoppinglist");

        for(ShoppingList.ShoppingRecipe recipe : m_GroceryPlanning.shoppingList().getAllShoppingRecipes())
        {
            writer.name(recipe.getName());
            writer.beginObject();
            writer.name("ScalingFactor").value(recipe.getScalingFactor());
            writer.name("DueDate").value(recipe.getDueDate().toString());
            for(ShoppingListItem si : recipe.getAllItems())
            {
                writer.name(si.getIngredient());
                writer.beginObject();

                writer.name("status").value(si.getStatus().toString());

                writer.name("amountMinMax");
                writer.beginArray();
                writer.value(si.getAmount().getQuantityMin());
                writer.value(si.getAmount().getQuantityMax());
                writer.value(si.getAmount().getUnit().toString());
                writer.endArray();

                writer.name("size").value(si.getSize().toString());
                writer.name("optional").value(si.isOptional());

                writer.name("additionalInfo").value(si.getAdditionalInfo());

                writer.endObject();
            }
            writer.endObject();
        }

        writer.endObject();
    }
}
