package ch.phwidmer.einkaufsliste.data;

import android.support.annotation.NonNull;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

public abstract class Recipes
{
    public interface Recipe
    {
        String getName();

        int getNumberOfPersons();
        void setNumberOfPersons(int number);

        RecipeItem addRecipeItem(String strIngredient);
        void addRecipeItem(int position, final RecipeItem item);
        void removeRecipeItem(RecipeItem r);
        ArrayList<RecipeItem> getAllRecipeItems();

        // Groups

        void addGroup(String strName);
        void removeGroup(String strName);
        void renameGroup(String strOldName, String strNewName);
        ArrayList<String> getAllGroupNames();

        RecipeItem addRecipeItemToGroup(String strGroup, String strIngredient);
        void addRecipeItemToGroup(String strGroup, final RecipeItem r);
        void removeRecipeItemFromGroup(String strGroup, RecipeItem r);
        ArrayList<RecipeItem> getAllRecipeItemsInGroup(String strGroup);
    }

    public abstract Recipe addRecipe(String strName, int iNrPersons);
    public abstract void addRecipe(String strName, final Recipe r);
    public abstract void removeRecipe(Recipe r);
    public abstract void renameRecipe(Recipe recipe, String strNewName);
    public abstract void copyRecipe(Recipe recipe, String strNewName);
    public abstract Recipe getRecipe(String strName);

    public abstract ArrayList<Recipe> getAllRecipes();
    public abstract ArrayList<String> getAllRecipeNames();

    public boolean isIngredientInUse(String strIngredient, @NonNull ArrayList<String> recipesUsingIngredient)
    {
        boolean stillInUse = false;
        for(Recipe recipe : getAllRecipes())
        {
            for(RecipeItem ri : recipe.getAllRecipeItems())
            {
                if(ri.getIngredient().equals(strIngredient))
                {
                    if(!recipesUsingIngredient.contains(recipe.getName()))
                    {
                        recipesUsingIngredient.add(recipe.getName());
                    }
                    stillInUse = true;
                }
            }

            for(String strGroup : recipe.getAllGroupNames())
            {
                for(RecipeItem ri : recipe.getAllRecipeItemsInGroup(strGroup))
                {
                    if(ri.getIngredient().equals(strIngredient))
                    {
                        if(!recipesUsingIngredient.contains(recipe.getName()))
                        {
                            recipesUsingIngredient.add(recipe.getName());
                        }
                        stillInUse = true;
                    }
                }
            }
        }

        return stillInUse;
    }

    public void onIngredientRenamed(String strIngredient, String strNewName)
    {
        for(Recipe r : getAllRecipes())
        {
            for(RecipeItem ri : r.getAllRecipeItems())
            {
                if (ri.getIngredient().equals(strIngredient))
                {
                    ri.setIngredient(strNewName);
                }
            }

            for(String strGroup : r.getAllGroupNames())
            {
                for(RecipeItem ri : r.getAllRecipeItemsInGroup(strGroup))
                {
                    if (ri.getIngredient().equals(strIngredient))
                    {
                        ri.setIngredient(strNewName);
                    }
                }
            }
        }
    }

    boolean checkDataConsistency(Ingredients ingredients, LinkedList<String> missingIngredients)
    {
        boolean dataConsistent = true;
        for(Recipe recipe : getAllRecipes())
        {
            for(RecipeItem ri : recipe.getAllRecipeItems())
            {
                String ingredient = ri.getIngredient();
                if(ingredients.getIngredient(ingredient) == null)
                {
                    if(!missingIngredients.contains(ingredient))
                    {
                        missingIngredients.add(ingredient);
                    }
                    dataConsistent = false;
                }
            }

            for(String strGroup : recipe.getAllGroupNames())
            {
                for(RecipeItem ri : recipe.getAllRecipeItemsInGroup(strGroup))
                {
                    String ingredient = ri.getIngredient();
                    if(ingredients.getIngredient(ingredient) == null)
                    {
                        if(!missingIngredients.contains(ingredient))
                        {
                            missingIngredients.add(ingredient);
                        }
                        dataConsistent = false;
                    }
                }
            }
        }
        return dataConsistent;
    }

    // Serializing

    void saveToJson(JsonWriter writer) throws IOException
    {
        writer.beginObject();
        writer.name("id").value("Recipes");

        for(Recipe recipe : getAllRecipes())
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

    private void writeRecipeItem(JsonWriter writer, RecipeItem ri) throws IOException
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

    void readFromJson(JsonReader reader) throws IOException
    {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("id"))
            {
                String id = reader.nextString();
                if (!id.equals("Recipes"))
                {
                    throw new IOException();
                }
            }
            else if(name.equals("activeRecipe"))
            {
                reader.skipValue();
            }
            else
            {
                Recipe recipe = addRecipe(name, 0);

                reader.beginObject();
                while (reader.hasNext())
                {
                    String currentName = reader.nextName();
                    if (currentName.equals("NrPersons"))
                    {
                        recipe.setNumberOfPersons(reader.nextInt());
                    }
                    else if(currentName.equals("group"))
                    {
                        reader.beginObject();
                        String groupName = "";
                        while (reader.hasNext())
                        {
                            String groupCurrentName = reader.nextName();

                            if (groupCurrentName.equals("groupName"))
                            {
                                groupName = reader.nextString();
                                recipe.addGroup(groupName);
                            }
                            else
                            {
                                RecipeItem item = recipe.addRecipeItemToGroup(groupName, groupCurrentName);
                                readRecipeItem(reader, item);
                            }
                        }

                        reader.endObject();

                    }
                    else
                    {
                        RecipeItem item = recipe.addRecipeItem(currentName);
                        readRecipeItem(reader, item);
                    }
                }

                reader.endObject();
            }
        }

        reader.endObject();
    }

    private void readRecipeItem(JsonReader reader, RecipeItem item) throws IOException
    {
        reader.beginObject();
        while (reader.hasNext())
        {
            String itemName = reader.nextName();
            switch(itemName)
            {
                case "amount":
                {
                    // Old version of Amount without min / max.
                    reader.beginArray();

                    item.getAmount().setQuantityMin((float)reader.nextDouble());
                    String str = reader.nextString();
                    item.getAmount().setUnit(Amount.Unit.valueOf(str));

                    reader.endArray();
                    break;
                }

                case "amountMinMax":
                {
                    reader.beginArray();

                    item.getAmount().setQuantityMin((float)reader.nextDouble());
                    item.getAmount().setQuantityMax((float) reader.nextDouble());
                    String str = reader.nextString();
                    item.getAmount().setUnit(Amount.Unit.valueOf(str));

                    reader.endArray();
                    break;
                }

                case "size":
                {
                    String size = reader.nextString();
                    item.setSize(RecipeItem.Size.valueOf(size));
                    break;
                }

                case "optional":
                {
                    item.setIsOptional(reader.nextBoolean());
                    break;
                }

                case "additionalInfo":
                {
                    item.setAdditionInfo(reader.nextString());
                    break;
                }
            }
        }
        reader.endObject();
    }
}
