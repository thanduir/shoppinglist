package ch.phwidmer.einkaufsliste.data.db_based;

import android.content.Context;
import android.support.annotation.NonNull;

import androidx.room.Room;

import ch.phwidmer.einkaufsliste.data.GroceryPlanning;

public class GroceryPlanningDB extends GroceryPlanning
{
    private static GroceryPlanningDB m_Instance = null;

    private AppDatabase database;

    public static GroceryPlanningDB getInstance(@NonNull Context context)
    {
        if(m_Instance == null)
        {
            m_Instance = new GroceryPlanningDB(context);
        }
        return m_Instance;
    }

    private GroceryPlanningDB(@NonNull Context context) {
        database = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, AppDatabase.DB_NAME)
                       .allowMainThreadQueries()
                       .build();

        m_Categories = new CategoriesDB(database);
        m_Ingredients = new IngredientsDB(database);
        m_Recipes = new RecipesDB(database);
        m_ShoppingList = new ShoppingListDB(database);
    }

    @Override
    public void clearAll() {
        database.clearAllTables();
    }

    @Override
    public void flush() {
        // Nothing to do here as everything is immediately saved in the database
    }
}
