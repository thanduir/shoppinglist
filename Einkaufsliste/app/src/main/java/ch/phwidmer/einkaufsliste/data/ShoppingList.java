package ch.phwidmer.einkaufsliste.data;

import android.support.annotation.NonNull;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;

public abstract class ShoppingList
{
    public abstract class ShoppingRecipe
    {
        public abstract String getName();

        // Current scaling factor used for the items in the list.
        public abstract float getScalingFactor();
        public abstract void setScalingFactor(float factor);

        public abstract Optional<ShoppingListItem> addItem(@NonNull String strIngredient);
        public abstract void addItem(@NonNull RecipeItem recipeItem);
        public abstract void addItem(int position, @NonNull ShoppingListItem item);
        public abstract void removeItem(@NonNull ShoppingListItem r);
        public abstract ArrayList<ShoppingListItem> getAllItems();

        public void changeScalingFactor(float f)
        {
            float fFactor = f / getScalingFactor();
            setScalingFactor(f);

            for(ShoppingListItem sli : getAllItems())
            {
                sli.getAmount().scaleAmount(fFactor);
            }
        }
    }

    protected abstract Optional<ShoppingRecipe> addRecipe(@NonNull String strName);

    public void addFromRecipe(@NonNull String strName, @NonNull Recipes.Recipe recipe)
    {
        if(getShoppingRecipe(strName).isPresent())
        {
            return;
        }

        Optional<ShoppingRecipe> item = addRecipe(strName);
        if(!item.isPresent())
        {
            return;
        }
        item.get().setScalingFactor((float)recipe.getNumberOfPersons());
        for(RecipeItem r : recipe.getAllRecipeItems())
        {
            item.get().addItem(r);
        }
    }
    public abstract void addExistingShoppingRecipe(@NonNull ShoppingRecipe recipe);

    public abstract Optional<ShoppingRecipe> getShoppingRecipe(@NonNull String strName);
    public abstract void renameShoppingRecipe(@NonNull ShoppingRecipe recipe, @NonNull String strNewName);
    public abstract void removeShoppingRecipe(@NonNull ShoppingRecipe recipe);

    public abstract ArrayList<ShoppingRecipe> getAllShoppingRecipes();
    public abstract ArrayList<String> getAllShoppingRecipeNames();

    public abstract void clearShoppingList();

    public boolean isIngredientInUse(@NonNull String strIngredient, @NonNull ArrayList<String> shoppingListItemUsingIngredient)
    {
        boolean stillInUse = false;
        for(ShoppingRecipe recipe : getAllShoppingRecipes())
        {
            for(ShoppingListItem sli : recipe.getAllItems())
            {
                if (sli.getIngredient().equals(strIngredient))
                {
                    if(!shoppingListItemUsingIngredient.contains(recipe.getName()))
                    {
                        shoppingListItemUsingIngredient.add(recipe.getName());
                    }
                    stillInUse = true;
                }
            }
        }
        return stillInUse;
    }

    public void onIngredientRenamed(@NonNull String strIngredient, @NonNull String strNewName)
    {
        for(ShoppingRecipe sr : getAllShoppingRecipes())
        {
            for(ShoppingListItem sli : sr.getAllItems())
            {
                if (sli.getIngredient().equals(strIngredient))
                {
                    sli.setIngredient(strNewName);
                }
            }
        }
    }

    boolean checkDataConsistency(@NonNull Ingredients ingredients, @NonNull LinkedList<String> missingIngredients)
    {
        boolean dataConsistent = true;
        for(ShoppingRecipe recipe : getAllShoppingRecipes())
        {
            for(ShoppingListItem li : recipe.getAllItems())
            {
                String ingredient = li.getIngredient();
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
        return dataConsistent;
    }

    // Serializing

    void saveToJson(@NonNull JsonWriter writer) throws IOException
    {
        writer.beginObject();
        writer.name("id").value("Shoppinglist");

        for(ShoppingRecipe recipe : getAllShoppingRecipes())
        {
            writer.name(recipe.getName());
            writer.beginObject();
            writer.name("ScalingFactor").value(recipe.getScalingFactor());
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

    void readFromJson(@NonNull JsonReader reader) throws IOException
    {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("id"))
            {
                String id = reader.nextString();
                if (!id.equals("Shoppinglist"))
                {
                    throw new IOException();
                }
            }
            else if(name.equals("currentSortOrder"))
            {
                // Legacy item -> nothing to do
                reader.skipValue();
            }
            else
            {
                Optional<ShoppingRecipe> optRecipe = addRecipe(name);
                if(!optRecipe.isPresent())
                {
                    throw new IOException();
                }
                ShoppingRecipe recipe = optRecipe.get();

                reader.beginObject();
                while (reader.hasNext())
                {
                    String currentName = reader.nextName();
                    if (currentName.equals("ScalingFactor"))
                    {
                        recipe.setScalingFactor((float)reader.nextDouble());
                    }
                    else
                    {
                        Optional<ShoppingListItem> optItem = recipe.addItem(currentName);
                        if(!optItem.isPresent())
                        {
                            throw new IOException();
                        }
                        ShoppingListItem item = optItem.get();

                        reader.beginObject();
                        while (reader.hasNext())
                        {
                            String itemName = reader.nextName();
                            switch(itemName)
                            {
                                case "status":
                                {
                                    String str = reader.nextString();
                                    item.setStatus(ShoppingListItem.Status.valueOf(str));
                                    break;
                                }

                                case "amount":
                                {
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

                reader.endObject();
            }
        }

        reader.endObject();
    }
}
