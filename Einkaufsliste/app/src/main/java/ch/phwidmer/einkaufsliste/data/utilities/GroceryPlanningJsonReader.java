package ch.phwidmer.einkaufsliste.data.utilities;

import android.support.annotation.NonNull;
import android.util.JsonReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import ch.phwidmer.einkaufsliste.data.Amount;
import ch.phwidmer.einkaufsliste.data.Categories;
import ch.phwidmer.einkaufsliste.data.GroceryPlanning;
import ch.phwidmer.einkaufsliste.data.Ingredients;
import ch.phwidmer.einkaufsliste.data.RecipeItem;
import ch.phwidmer.einkaufsliste.data.Recipes;
import ch.phwidmer.einkaufsliste.data.ShoppingList;
import ch.phwidmer.einkaufsliste.data.ShoppingListItem;
import ch.phwidmer.einkaufsliste.data.Unit;

class GroceryPlanningJsonReader {
    private GroceryPlanning m_GroceryPlanning;

    GroceryPlanningJsonReader(GroceryPlanning groceryPlanning)
    {
        m_GroceryPlanning = groceryPlanning;
    }

    void read(@NonNull File file) throws IOException
    {
        try
        {
            FileReader fw = new FileReader(file);
            JsonReader jr = new JsonReader(fw);

            jr.beginArray();

            jr.beginObject();
            boolean bIDFound = false;
            int iVersion = -1;
            while (jr.hasNext()) {
                String name = jr.nextName();
                if (name.equals("id")) {
                    String id = jr.nextString();
                    if (!id.equals("ch.phwidmer.einkaufsliste"))
                    {
                        throw new IOException("Invalid ID string");
                    }
                    bIDFound = true;
                }
                else if (name.equals("version")) {
                    iVersion = jr.nextInt();
                    if (iVersion > JsonSerializer.SERIALIZING_VERSION)
                    {
                        throw new IOException("Invalid version");
                    }
                }
            }
            jr.endObject();
            if(!bIDFound || iVersion == -1)
            {
                throw new IOException("Invalid ID");
            }

            m_GroceryPlanning.clearAll();

            readCategories(jr);
            readIngredients(jr);
            readRecipes(jr);
            readShoppongList(jr);

            jr.endArray();

            jr.close();
            fw.close();
        }
        catch(IOException e)
        {
            m_GroceryPlanning.clearAll();
            throw new IOException(e.getMessage());
        }
    }

    private void readCategories(JsonReader reader) throws IOException
    {
        Categories categories = m_GroceryPlanning.categories();

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
                        categories.addCategory(reader.nextString());
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
                        ArrayList<Categories.Category> newOrder = new ArrayList<>();
                        while (reader.hasNext()) {
                            Optional<Categories.Category> category = categories.getCategory(reader.nextString());
                            if(!category.isPresent())
                            {
                                throw new IOException();
                            }
                            newOrder.add(category.get());
                        }
                        reader.endArray();

                        Categories.SortOrder order = categories.addSortOrder(orderName);
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

    private void readIngredients(@NonNull JsonReader reader) throws IOException
    {
        Ingredients ingredients = m_GroceryPlanning.ingredients();

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
                Unit unit = Unit.Unitless;

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
                            unit = Unit.valueOf(strUnit);
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

                Optional<Categories.Category> category = m_GroceryPlanning.categories().getCategory(strCategory);
                if(!category.isPresent())
                {
                    throw new IOException();
                }
                Optional<Ingredients.Ingredient> in = ingredients.addIngredient(name, unit, category.get());
                if(!in.isPresent())
                {
                    throw new IOException();
                }
                in.get().setProvenance(strProvenance);
            }
        }
        reader.endObject();
    }

    private void readRecipes(@NonNull JsonReader reader) throws IOException
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
                Optional<Recipes.Recipe> optRecipe = m_GroceryPlanning.recipes().addRecipe(name, 0);
                if(!optRecipe.isPresent())
                {
                    throw new IOException();
                }
                Recipes.Recipe recipe = optRecipe.get();

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

                    Amount amount = item.getAmount();
                    amount.setQuantityMin((float)reader.nextDouble());
                    String str = reader.nextString();
                    amount.setUnit(Unit.valueOf(str));
                    item.setAmount(amount);

                    reader.endArray();
                    break;
                }

                case "amountMinMax":
                {
                    reader.beginArray();

                    Amount amount = item.getAmount();
                    amount.setQuantityMin((float)reader.nextDouble());
                    amount.setQuantityMax((float) reader.nextDouble());
                    String str = reader.nextString();
                    amount.setUnit(Unit.valueOf(str));
                    item.setAmount(amount);

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

    private void readShoppongList(@NonNull JsonReader reader) throws IOException
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
                Optional<ShoppingList.ShoppingRecipe> optRecipe = m_GroceryPlanning.shoppingList().addNewRecipe(name);
                if(!optRecipe.isPresent())
                {
                    throw new IOException();
                }
                ShoppingList.ShoppingRecipe recipe = optRecipe.get();

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

                                    Amount amount = item.getAmount();
                                    amount.setQuantityMin((float)reader.nextDouble());
                                    String str = reader.nextString();
                                    amount.setUnit(Unit.valueOf(str));
                                    item.setAmount(amount);

                                    reader.endArray();
                                    break;
                                }

                                case "amountMinMax":
                                {
                                    reader.beginArray();

                                    Amount amount = item.getAmount();
                                    amount.setQuantityMin((float)reader.nextDouble());
                                    amount.setQuantityMax((float) reader.nextDouble());
                                    String str = reader.nextString();
                                    amount.setUnit(Unit.valueOf(str));
                                    item.setAmount(amount);

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
