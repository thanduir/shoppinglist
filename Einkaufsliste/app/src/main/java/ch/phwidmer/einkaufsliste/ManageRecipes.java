package ch.phwidmer.einkaufsliste;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class ManageRecipes extends AppCompatActivity {

    private Recipes m_Recipes;
    private Ingredients m_Ingredients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_recipes);

        Intent intent = getIntent();
        m_Recipes = (Recipes)intent.getParcelableExtra(MainActivity.EXTRA_RECIPES);
        m_Ingredients = (Ingredients)intent.getParcelableExtra(MainActivity.EXTRA_INGREDIENTS);
    }

    public void onConfirm(View v)
    {
        Intent data = new Intent();
        data.putExtra(MainActivity.EXTRA_RECIPES, m_Recipes);
        setResult(RESULT_OK, data);
        finish();
    }

    public void onCancel(View v)
    {
        finish();
    }
}
