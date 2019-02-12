package ch.phwidmer.einkaufsliste;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity
{
    // TODO: Code-Doku!
    // TODO: DesignDokuemnt.txt aktualisieren!

    public static final String EXTRA_CATEGORIES = "ch.phwidmer.einkaufsliste.CATEGORIES";
    public static final String EXTRA_INGREDIENTS = "ch.phwidmer.einkaufsliste.INGREDIENTS";
    private final int REQUEST_CODE_ManageCategories = 1;
    private final int REQUEST_CODE_ManageIngredients = 1;

    private GroceryPlanning m_GroceryPlanning;

    // TODO: Serialization etc.

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_GroceryPlanning = new GroceryPlanning();
    }

    public void manageCategories(View view)
    {
        Intent intent = new Intent(this, ManageCategories.class);
        intent.putExtra(EXTRA_CATEGORIES, m_GroceryPlanning.m_Categories);
        startActivityForResult(intent, REQUEST_CODE_ManageCategories);
    }

    public void manageIngredients(View view)
    {
        Intent intent = new Intent(this, ManageIngredients.class);
        intent.putExtra(EXTRA_CATEGORIES, m_GroceryPlanning.m_Categories);
        intent.putExtra(EXTRA_INGREDIENTS, m_GroceryPlanning.m_Ingredients);
        startActivityForResult(intent, REQUEST_CODE_ManageIngredients);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode != RESULT_OK)
        {
            return;
        }

        if(requestCode == REQUEST_CODE_ManageCategories)
        {
            m_GroceryPlanning.m_Categories = data.getParcelableExtra(EXTRA_CATEGORIES);
        }
        else if(requestCode == REQUEST_CODE_ManageIngredients)
        {
            m_GroceryPlanning.m_Ingredients = data.getParcelableExtra(EXTRA_INGREDIENTS);
        }
    }
}
