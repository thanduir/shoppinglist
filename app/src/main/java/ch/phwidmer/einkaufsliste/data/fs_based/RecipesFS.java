package ch.phwidmer.einkaufsliste.data.fs_based;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;

import ch.phwidmer.einkaufsliste.data.Ingredients;
import ch.phwidmer.einkaufsliste.data.RecipeItem;
import ch.phwidmer.einkaufsliste.data.Recipes;
import ch.phwidmer.einkaufsliste.helper.Helper;

public class RecipesFS extends Recipes
{
    public static class RecipeFS extends Recipes.Recipe
    {
        String m_Name;
        private int m_NumberOfPersons = 0;
        private LinkedList<RecipeItemFS> m_Items = new LinkedList<>();
        private TreeMap<String, LinkedList<RecipeItemFS>> m_Groups = new TreeMap<>(new Helper.SortStringIgnoreCase());

        RecipeFS(@NonNull String strName)
        {
            m_Name = strName;
        }

        @Override
        public @NonNull String getName()
        {
            return m_Name;
        }

        @Override
        public int getNumberOfPersons()
        {
            return m_NumberOfPersons;
        }
        @Override
        public void setNumberOfPersons(int number)
        {
            m_NumberOfPersons = number;
        }

        @Override
        public Optional<RecipeItem> addRecipeItem(@NonNull String strIngredient)
        {
            for(RecipeItemFS item : m_Items)
            {
                if(item.getIngredient().equals(strIngredient))
                {
                    return Optional.empty();
                }
            }

            RecipeItemFS r = new RecipeItemFS(strIngredient);
            m_Items.add(r);
            return Optional.of(r);
        }
        @Override
        public void removeRecipeItem(@NonNull RecipeItem item)
        {
            RecipeItemFS itemFS = (RecipeItemFS)item;
            m_Items.remove(itemFS);
        }
        @Override
        public ArrayList<RecipeItem> getAllRecipeItems()
        {
            return new ArrayList<>(m_Items);
        }

        // Groups

        @Override
        public void addGroup(@NonNull String strName)
        {
            if(m_Groups.containsKey(strName))
            {
                return;
            }
            m_Groups.put(strName, new LinkedList<>());
        }
        @Override
        public void removeGroup(@NonNull String strName)
        {
            m_Groups.remove(strName);
        }
        @Override
        public void renameGroup(@NonNull String strOldName, @NonNull String strNewName)
        {
            if(!m_Groups.containsKey(strOldName) || m_Groups.containsKey(strNewName))
            {
                return;
            }
            LinkedList<RecipeItemFS> data = m_Groups.get(strOldName);
            m_Groups.remove(strOldName);
            m_Groups.put(strNewName, data);
        }
        @Override
        public ArrayList<String> getAllGroupNames()
        {
            return new ArrayList<>(m_Groups.keySet());
        }

        @Override
        public Optional<RecipeItem> addRecipeItemToGroup(@NonNull String strGroup, @NonNull String strIngredient)
        {
            LinkedList<RecipeItemFS> group = m_Groups.get(strGroup);
            if(group == null)
            {
                return Optional.empty();
            }

            for(RecipeItemFS item : group)
            {
                if(item.getIngredient().equals(strIngredient))
                {
                    return Optional.empty();
                }
            }

            RecipeItemFS r = new RecipeItemFS(strIngredient);
            group.add(r);
            return Optional.of(r);
        }
        @Override
        public void removeRecipeItemFromGroup(@NonNull String strGroup, @NonNull RecipeItem r)
        {
            LinkedList<RecipeItemFS> group = m_Groups.get(strGroup);
            if(group == null)
            {
                return;
            }

            RecipeItemFS rFS = (RecipeItemFS)r;
            group.remove(rFS);
        }
        @Override
        public ArrayList<RecipeItem> getAllRecipeItemsInGroup(@NonNull String strGroup)
        {
            LinkedList<RecipeItemFS> group = m_Groups.get(strGroup);
            if(group == null)
            {
                return new ArrayList<>();
            }

            return new ArrayList<>(group);
        }
    }

    private TreeSet<RecipeFS> m_Recipies;

    RecipesFS()
    {
        m_Recipies = new TreeSet<>(new Helper.SortNamedIgnoreCase());
    }

    @Override
    public Optional<Recipe> addRecipe(@NonNull String strName, int iNrPersons)
    {
        if(getRecipe(strName).isPresent())
        {
            return Optional.empty();
        }
        RecipeFS r = new RecipeFS(strName);
        r.m_NumberOfPersons = iNrPersons;
        m_Recipies.add(r);
        return Optional.of(r);
    }

    @Override
    public void removeRecipe(@NonNull Recipe r)
    {
        RecipeFS rFS = (RecipeFS)r;
        m_Recipies.remove(rFS);
    }

    @Override
    public void renameRecipe(@NonNull Recipe recipe, @NonNull String strNewName)
    {
        if(getRecipe(strNewName).isPresent())
        {
            return;
        }

        RecipeFS r = (RecipeFS)recipe;
        r.m_Name = strNewName;
    }

    @Override
    public void copyRecipe(@NonNull Recipe r, @NonNull String strNewName)
    {
        if(getRecipe(strNewName).isPresent())
        {
            return;
        }

        RecipeFS oldRecipe = (RecipeFS)r;
        RecipeFS recipe = new RecipeFS(strNewName);
        recipe.m_NumberOfPersons = oldRecipe.m_NumberOfPersons;
        for(RecipeItemFS item : oldRecipe.m_Items)
        {
            recipe.m_Items.add(new RecipeItemFS(item));
        }
        for(TreeMap.Entry<String, LinkedList<RecipeItemFS>> oldItem : oldRecipe.m_Groups.entrySet())
        {
            LinkedList<RecipeItemFS> items = new LinkedList<>();
            for(RecipeItemFS item : oldItem.getValue())
            {
                items.add(new RecipeItemFS(item));
            }
            recipe.m_Groups.put(oldItem.getKey(), items);
        }

        m_Recipies.add(recipe);
    }

    @Override
    public Optional<Recipe> getRecipe(@NonNull String strName)
    {
        for(RecipeFS recipe : m_Recipies)
        {
            if(recipe.getName().equals(strName))
            {
                return Optional.of(recipe);
            }
        }

        return Optional.empty();
    }

    @Override
    public ArrayList<Recipe> getAllRecipes()
    {
        return new ArrayList<>(m_Recipies);
    }

    @Override
    public ArrayList<String> getAllRecipeNames()
    {
        ArrayList<String> vec = new ArrayList<>();
        for(RecipeFS recipe : m_Recipies)
        {
            vec.add(recipe.getName());
        }
        return vec;
    }

    @Override
    public boolean isIngredientInUse(@NonNull Ingredients.Ingredient ingredient, @NonNull ArrayList<String> recipesUsingIngredient)
    {
        boolean stillInUse = false;
        for(RecipeFS recipe : m_Recipies)
        {
            for(RecipeItem ri : recipe.m_Items)
            {
                if(ri.getIngredient().equals(ingredient.getName()))
                {
                    if(!recipesUsingIngredient.contains(recipe.getName()))
                    {
                        recipesUsingIngredient.add(recipe.getName());
                    }
                    stillInUse = true;
                }
            }

            for(TreeMap.Entry<String, LinkedList<RecipeItemFS>> entry : recipe.m_Groups.entrySet())
            {
                for(RecipeItemFS ri : entry.getValue())
                {
                    if(ri.getIngredient().equals(ingredient.getName()))
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

    @Override
    public void onIngredientRenamed(@NonNull String strIngredient, @NonNull String strNewName)
    {
        for(RecipeFS r : m_Recipies)
        {
            for(RecipeItem ri : r.m_Items)
            {
                if (ri.getIngredient().equals(strIngredient))
                {
                    ri.setIngredient(strNewName);
                }
            }

            for(TreeMap.Entry<String, LinkedList<RecipeItemFS>> entry : r.m_Groups.entrySet())
            {
                for(RecipeItemFS ri : entry.getValue())
                {
                    if (ri.getIngredient().equals(strIngredient))
                    {
                        ri.setIngredient(strNewName);
                    }
                }
            }
        }
    }
}
