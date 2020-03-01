package ch.phwidmer.einkaufsliste.UI;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.TreeMap;

import ch.phwidmer.einkaufsliste.data.Amount;
import ch.phwidmer.einkaufsliste.data.Categories;
import ch.phwidmer.einkaufsliste.data.Ingredients;
import ch.phwidmer.einkaufsliste.data.RecipeItem;
import ch.phwidmer.einkaufsliste.data.Recipes;
import ch.phwidmer.einkaufsliste.data.ShoppingList;
import ch.phwidmer.einkaufsliste.data.ShoppingListItem;
import ch.phwidmer.einkaufsliste.data.Unit;

class UndoData
{
    static class SortOrderUndoData
    {
        SortOrderUndoData(Categories.SortOrder sortOrder)
        {
            m_Name = sortOrder.getName();
            m_Order = sortOrder.getOrder();
        }

        String getName()
        {
            return m_Name;
        }

        ArrayList<Categories.Category> getOrder()
        {
            return m_Order;
        }

        private String                         m_Name;
        private ArrayList<Categories.Category> m_Order;
    }

    static class IngredientUndoData
    {
        IngredientUndoData(Ingredients.Ingredient item)
        {
            m_Name = item.getName();
            m_Category = item.getCategory();
            m_Provenance = item.getProvenance();
            m_DefaultUnit = item.getDefaultUnit();
        }

        String getName()
        {
            return m_Name;
        }

        String getCategory()
        {
            return m_Category;
        }

        Unit getDefaultUnit()
        {
            return m_DefaultUnit;
        }

        String getProvenance()
        {
            return m_Provenance;
        }

        private String  m_Name;
        private String  m_Category;
        private String  m_Provenance;
        private Unit    m_DefaultUnit;
    }

    static class RecipeUndoData
    {
        RecipeUndoData(Recipes.Recipe recipe)
        {
            m_Name = recipe.getName();
            m_NumberOfPersons = recipe.getNumberOfPersons();
            m_Items = new LinkedList<>();
            m_Groups = new TreeMap<>();

            ArrayList<RecipeItem> items = recipe.getAllRecipeItems();
            for(RecipeItem item : items)
            {
                m_Items.add(new RecipeItemUndoData(item));
            }

            ArrayList<String> groupNames = recipe.getAllGroupNames();
            for(String group : groupNames)
            {
                LinkedList<RecipeItemUndoData> undoItems = new LinkedList<>();
                ArrayList<RecipeItem> itemsInGroup = recipe.getAllRecipeItemsInGroup(group);
                for(RecipeItem item : itemsInGroup)
                {
                    undoItems.add(new RecipeItemUndoData(item));
                }
                m_Groups.put(group, undoItems);
            }
        }

        String getName()
        {
            return m_Name;
        }

        int getNumberOfPersons()
        {
            return m_NumberOfPersons;
        }

        void initializeRecipe(Recipes.Recipe recipe)
        {
            for(RecipeItemUndoData item : m_Items)
            {
                Optional<RecipeItem> newItem = recipe.addRecipeItem(item.getIngredient());
                if(!newItem.isPresent())
                {
                    continue;
                }
                item.initializeItem(newItem.get());
            }

            for(TreeMap.Entry<String, LinkedList<RecipeItemUndoData>> group : m_Groups.entrySet())
            {
                recipe.addGroup(group.getKey());

                for(RecipeItemUndoData item : group.getValue())
                {
                    Optional<RecipeItem> newItem = recipe.addRecipeItemToGroup(group.getKey(), item.getIngredient());
                    if(!newItem.isPresent())
                    {
                        continue;
                    }
                    item.initializeItem(newItem.get());
                }
            }
        }

        private String                                  m_Name;
        private int                                     m_NumberOfPersons;
        private LinkedList<RecipeItemUndoData>          m_Items;
        private TreeMap<String, LinkedList<RecipeItemUndoData>> m_Groups;
    }

    static class RecipeItemGroupUndoData
    {
        RecipeItemGroupUndoData(String strName, ArrayList<RecipeItem> recipeItems)
        {
            m_Name = strName;
            m_Items = new LinkedList<>();

            for(RecipeItem item : recipeItems)
            {
                m_Items.add(new RecipeItemUndoData(item));
            }
        }

        String getName()
        {
            return m_Name;
        }

        void addToRecipe(Recipes.Recipe recipe)
        {
            recipe.addGroup(m_Name);
            for(RecipeItemUndoData item : m_Items)
            {
                Optional<RecipeItem> newItem = recipe.addRecipeItemToGroup(m_Name, item.getIngredient());
                if(!newItem.isPresent())
                {
                    continue;
                }
                item.initializeItem(newItem.get());
            }
        }

        private String                            m_Name;
        private LinkedList<RecipeItemUndoData>    m_Items;
    }

    static class RecipeItemUndoData
    {
        RecipeItemUndoData(RecipeItem item)
        {
            m_strGroup = "";

            m_Ingredient = item.getIngredient();
            m_Amount = item.getAmount();
            m_AdditionalInfo = item.getAdditionalInfo();
            m_Size = item.getSize();
            m_Optional = item.isOptional();
        }

        void setGroup(String strGroup)
        {
            m_strGroup = strGroup;
        }
        String getGroup()
        {
            return m_strGroup;
        }

        String getIngredient()
        {
            return m_Ingredient;
        }

        void initializeItem(RecipeItem item)
        {
            item.setAmount(m_Amount);
            item.setAdditionInfo(m_AdditionalInfo);
            item.setSize(m_Size);
            item.setIsOptional(m_Optional);
        }

        private String                  m_strGroup;

        private String                  m_Ingredient;
        private Amount                  m_Amount;
        private String                  m_AdditionalInfo;
        private RecipeItem.Size         m_Size;
        private boolean                 m_Optional;
    }

    static class ShoppingRecipeUndoData
    {
        ShoppingRecipeUndoData(ShoppingList.ShoppingRecipe recipe)
        {
            m_Name = recipe.getName();
            m_ScalingFactor = recipe.getScalingFactor();
            m_Items = new LinkedList<>();

            ArrayList<ShoppingListItem> items = recipe.getAllItems();
            for(ShoppingListItem item : items)
            {
                m_Items.add(new ShoppingListItemUndoData(item));
            }
        }

        String getName()
        {
            return m_Name;
        }

        void initializeRecipe(ShoppingList.ShoppingRecipe recipe)
        {
            recipe.setScalingFactor(m_ScalingFactor);
            for(ShoppingListItemUndoData item : m_Items)
            {
                Optional<ShoppingListItem> newItem = recipe.addItem(item.getIngredient());
                if(!newItem.isPresent())
                {
                    continue;
                }
                item.initializeItem(newItem.get());
            }
        }

        private String                                  m_Name;
        private float                                   m_ScalingFactor;
        private LinkedList<ShoppingListItemUndoData>    m_Items;
    }

    static class ShoppingListItemUndoData
    {
        ShoppingListItemUndoData(ShoppingListItem item)
        {
            m_Status = item.getStatus();
            m_Ingredient = item.getIngredient();
            m_Amount = item.getAmount();
            m_AdditionalInfo = item.getAdditionalInfo();
            m_Size = item.getSize();
            m_Optional = item.isOptional();
        }

        String getIngredient()
        {
            return m_Ingredient;
        }

        void initializeItem(ShoppingListItem item)
        {
            item.setStatus(m_Status);
            item.setAmount(m_Amount);
            item.setAdditionInfo(m_AdditionalInfo);
            item.setSize(m_Size);
            item.setIsOptional(m_Optional);
        }

        private ShoppingListItem.Status m_Status;
        private String                  m_Ingredient;
        private Amount                  m_Amount;
        private String                  m_AdditionalInfo;
        private RecipeItem.Size         m_Size;
        private boolean                 m_Optional;
    }
}
