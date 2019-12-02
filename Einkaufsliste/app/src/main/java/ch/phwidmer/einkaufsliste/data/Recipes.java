package ch.phwidmer.einkaufsliste.data;

import android.support.annotation.NonNull;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;

import ch.phwidmer.einkaufsliste.helper.Helper;

public abstract class Recipes
{
    public abstract class Recipe implements Helper.NamedObject
    {
        public abstract int getNumberOfPersons();
        public abstract void setNumberOfPersons(int number);

        public abstract Optional<RecipeItem> addRecipeItem(@NonNull String strIngredient);
        public abstract void addRecipeItem(int position, @NonNull final RecipeItem item);
        public abstract void removeRecipeItem(@NonNull RecipeItem r);
        public abstract ArrayList<RecipeItem> getAllRecipeItems();

        // Groups

        public abstract void addGroup(@NonNull String strName);
        public abstract void removeGroup(@NonNull String strName);
        public abstract void renameGroup(@NonNull String strOldName, @NonNull String strNewName);
        public abstract ArrayList<String> getAllGroupNames();

        public abstract Optional<RecipeItem> addRecipeItemToGroup(@NonNull String strGroup, @NonNull String strIngredient);
        public abstract void addRecipeItemToGroup(@NonNull String strGroup, @NonNull final RecipeItem r);
        public abstract void removeRecipeItemFromGroup(@NonNull String strGroup, @NonNull RecipeItem r);
        public abstract ArrayList<RecipeItem> getAllRecipeItemsInGroup(@NonNull String strGroup);
    }

    public abstract Optional<Recipe> addRecipe(@NonNull String strName, int iNrPersons);
    public abstract void addRecipe(@NonNull String strName, @NonNull final Recipe r);
    public abstract void removeRecipe(@NonNull Recipe r);
    public abstract void renameRecipe(@NonNull Recipe recipe, @NonNull String strNewName);
    public abstract void copyRecipe(@NonNull Recipe recipe, @NonNull String strNewName);
    public abstract Optional<Recipe> getRecipe(@NonNull String strName);

    public abstract ArrayList<Recipe> getAllRecipes();
    public abstract ArrayList<String> getAllRecipeNames();

    public boolean isIngredientInUse(@NonNull String strIngredient, @NonNull ArrayList<String> recipesUsingIngredient)
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

    public void onIngredientRenamed(@NonNull String strIngredient, @NonNull String strNewName)
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

    boolean checkDataConsistency(@NonNull Ingredients ingredients, @NonNull LinkedList<String> missingIngredients)
    {
        boolean dataConsistent = true;
        for(Recipe recipe : getAllRecipes())
        {
            for(RecipeItem ri : recipe.getAllRecipeItems())
            {
                String ingredient = ri.getIngredient();
                if(!ingredients.getIngredient(ingredient).isPresent())
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
                    if(!ingredients.getIngredient(ingredient).isPresent())
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

    void saveToJson(@NonNull JsonWriter writer) throws IOException
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

    void readFromJson(@NonNull JsonReader reader) throws IOException
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
                Optional<Recipe> optRecipe = addRecipe(name, 0);
                if(!optRecipe.isPresent())
                {
                    throw new IOException();
                }
                Recipe recipe = optRecipe.get();

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
                                Optional<RecipeItem> item = recipe.addRecipeItemToGroup(groupName, groupCurrentName);
                                if(!item.isPresent())
                                {
                                    throw new IOException();
                                }
                                readRecipeItem(reader, item.get());
                            }
                        }

                        reader.endObject();

                    }
                    else
                    {
                        Optional<RecipeItem> item = recipe.addRecipeItem(currentName);
                        if(!item.isPresent())
                        {
                            throw new IOException();
                        }
                        readRecipeItem(reader, item.get());
                    }
                }

                reader.endObject();
            }
        }

        reader.endObject();
    }

    private void readRecipeItem(@NonNull JsonReader reader, @NonNull RecipeItem item) throws IOException
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
