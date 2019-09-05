package ch.phwidmer.einkaufsliste.data.fs_based;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;

import ch.phwidmer.einkaufsliste.data.RecipeItem;
import ch.phwidmer.einkaufsliste.data.Recipes;
import ch.phwidmer.einkaufsliste.helper.Helper;

public class RecipesFS extends Recipes
{
    public class RecipeFS extends Recipes.Recipe
    {
        String m_Name;
        private int m_NumberOfPersons = 0;
        private LinkedList<RecipeItemFS> m_Items = new LinkedList<>();
        private TreeMap<String, LinkedList<RecipeItemFS>> m_Groups = new TreeMap<>(new Helper.SortStringIgnoreCase());

        RecipeFS(String strName)
        {
            m_Name = strName;
        }

        @Override
        public String getName()
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
        public RecipeItem addRecipeItem(String strIngredient)
        {
            for(RecipeItemFS item : m_Items)
            {
                if(item.getIngredient().equals(strIngredient))
                {
                    return null;
                }
            }

            RecipeItemFS r = new RecipeItemFS(strIngredient);
            m_Items.add(r);
            return r;
        }
        @Override
        public void addRecipeItem(int position, final RecipeItem item)
        {
            m_Items.add(position, (RecipeItemFS)item);
        }
        @Override
        public void removeRecipeItem(RecipeItem item)
        {
            m_Items.remove((RecipeItemFS)item);
        }
        @Override
        public ArrayList<RecipeItem> getAllRecipeItems()
        {
            return new ArrayList<>(m_Items);
        }

        // Groups

        @Override
        public void addGroup(String strName)
        {
            if(m_Groups.containsKey(strName))
            {
                return;
            }
            m_Groups.put(strName, new LinkedList<>());
        }
        @Override
        public void removeGroup(String strName)
        {
            m_Groups.remove(strName);
        }
        @Override
        public void renameGroup(String strOldName, String strNewName)
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
        public RecipeItem addRecipeItemToGroup(String strGroup, String strIngredient)
        {
            if(!m_Groups.containsKey(strGroup))
            {
                return null;
            }

            for(RecipeItemFS item : m_Groups.get(strGroup))
            {
                if(item.getIngredient().equals(strIngredient))
                {
                    return null;
                }
            }

            RecipeItemFS r = new RecipeItemFS(strIngredient);
            m_Groups.get(strGroup).add(r);
            return r;
        }
        @Override
        public void addRecipeItemToGroup(String strGroup, final RecipeItem r)
        {
            if(!m_Groups.containsKey(strGroup))
            {
                return;
            }

            for(RecipeItemFS item : m_Groups.get(strGroup))
            {
                if(item.getIngredient().equals(r.getIngredient()))
                {
                    return;
                }
            }

            m_Groups.get(strGroup).add((RecipeItemFS)r);
        }
        @Override
        public void removeRecipeItemFromGroup(String strGroup, RecipeItem r)
        {
            if(!m_Groups.containsKey(strGroup))
            {
                return;
            }

            m_Groups.get(strGroup).remove((RecipeItemFS)r);
        }
        @Override
        public ArrayList<RecipeItem> getAllRecipeItemsInGroup(String strGroup)
        {
            if(!m_Groups.containsKey(strGroup))
            {
                return null;
            }

            return new ArrayList<RecipeItem>(m_Groups.get(strGroup));
        }
    }

    private TreeSet<RecipeFS> m_Recipies;

    RecipesFS()
    {
        m_Recipies = new TreeSet<>(new Helper.SortNamedIgnoreCase());
    }

    @Override
    public Recipe addRecipe(String strName, int iNrPersons)
    {
        if(getRecipe(strName) != null)
        {
            return null;
        }
        RecipeFS r = new RecipeFS(strName);
        r.m_NumberOfPersons = iNrPersons;
        m_Recipies.add(r);
        return r;
    }

    @Override
    public void addRecipe(String strName, final Recipe r)
    {
        if(getRecipe(strName) != null)
        {
            return;
        }
        m_Recipies.add((RecipeFS)r);
    }

    @Override
    public void removeRecipe(Recipe r)
    {
        m_Recipies.remove((RecipeFS) r);
    }

    @Override
    public void renameRecipe(Recipe recipe, String strNewName)
    {
        if(getRecipe(strNewName) != null)
        {
            return;
        }

        RecipeFS r = (RecipeFS)recipe;
        r.m_Name = strNewName;
    }

    @Override
    public void copyRecipe(Recipe r, String strNewName)
    {
        if(getRecipe(strNewName) != null)
        {
            return;
        }

        RecipeFS oldRecipe = (RecipeFS)r;
        if(oldRecipe == null)
        {
            return;
        }

        RecipeFS recipe = new RecipeFS(strNewName);
        recipe.m_NumberOfPersons = oldRecipe.m_NumberOfPersons;
        for(RecipeItemFS item : oldRecipe.m_Items)
        {
            recipe.m_Items.add(new RecipeItemFS(item));
        }
        // TODO: ALSO COPY GROUPS!
        m_Recipies.add(recipe);
    }

    @Override
    public Recipe getRecipe(String strName)
    {
        for(RecipeFS recipe : m_Recipies)
        {
            if(recipe.getName().equals(strName))
            {
                return recipe;
            }
        }

        return null;
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
}
