package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

/**
 * Shows the full ingredient list and method for ONE recipe.
 * The recipe is found using the id passed in through the Intent.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    // Key for the recipe id passed in through the Intent.
    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";

    private TextView tvDetailName;
    private TextView tvDetailIngredients;
    private TextView tvDetailSteps;
    private Button btnDetailBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        tvDetailName = findViewById(R.id.tvDetailName);
        tvDetailIngredients = findViewById(R.id.tvDetailIngredients);
        tvDetailSteps = findViewById(R.id.tvDetailSteps);
        btnDetailBack = findViewById(R.id.btnDetailBack);

        btnDetailBack.setOnClickListener(v -> finish());

        // Read the recipe id sent by SuggestedRecipesActivity.
        int recipeId = getIntent().getIntExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = findRecipeById(recipeId);

        if (recipe == null) {
            // Defensive: never show a blank screen if something went wrong.
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showRecipe(recipe);
    }

    /**
     * Looks the recipe up in the database. There are only about twenty
     * recipes, so loading them all and picking one by id is simple and fast.
     */
    private Recipe findRecipeById(int recipeId) {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        List<Recipe> recipes = databaseHelper.getAllRecipes();

        for (Recipe recipe : recipes) {
            if (recipe.getId() == recipeId) {
                return recipe;
            }
        }
        return null;
    }

    /** Fills the screen with the recipe's name, ingredients and method. */
    private void showRecipe(Recipe recipe) {

        tvDetailName.setText(recipe.getName());

        // Build a bulleted list such as "• egg - 3 pieces".
        StringBuilder ingredientText = new StringBuilder();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            ingredientText.append("\u2022 ")
                    .append(ingredient.getName())
                    .append(" - ")
                    .append(ingredient.getQuantityText())
                    .append("\n");
        }
        tvDetailIngredients.setText(ingredientText.toString().trim());

        // The steps are stored as numbered lines separated by new lines.
        tvDetailSteps.setText(recipe.getSteps());
    }
}