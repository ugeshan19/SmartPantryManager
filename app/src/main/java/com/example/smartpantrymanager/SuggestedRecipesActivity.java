package com.example.smartpantrymanager;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows ONLY the recipes the user can make right now.
 * The filtering is done by RecipeMatcher (the strict-matching rule).
 */
public class SuggestedRecipesActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    private RecyclerView recyclerRecipes;
    private TextView tvEmptyMessage;
    private TextView tvResultCount;
    private Button btnBackToPantry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        databaseHelper = new DatabaseHelper(this);

        recyclerRecipes = findViewById(R.id.recyclerRecipes);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        tvResultCount = findViewById(R.id.tvResultCount);
        btnBackToPantry = findViewById(R.id.btnBackToPantry);

        recyclerRecipes.setLayoutManager(new LinearLayoutManager(this));

        // Go back to the pantry list.
        btnBackToPantry.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Re-run the matching every time the screen is shown so the
        // list always reflects the current pantry.
        loadSuggestedRecipes();
    }

    /**
     * Reads the pantry and all recipes from SQLite, applies the strict
     * matching rule, and shows the result (or an empty-state message).
     */
    private void loadSuggestedRecipes() {

        // 1. Read the current pantry from the database.
        List<PantryItem> pantry = new ArrayList<>();
        Cursor cursor = databaseHelper.getAllPantryItems();
        while (cursor.moveToNext()) {
            pantry.add(PantryItem.fromCursor(cursor));
        }
        cursor.close();

        // 2. Read every recipe (with its ingredients).
        List<Recipe> allRecipes = databaseHelper.getAllRecipes();

        // 3. Keep only the recipes where EVERY ingredient is in the pantry.
        List<Recipe> suggested = RecipeMatcher.findStrictMatches(allRecipes, pantry);

        tvResultCount.setText("You can make " + suggested.size()
                + " of " + allRecipes.size() + " recipes right now");

        // 4. Show the list, or a friendly message if nothing matches.
        if (suggested.isEmpty()) {
            recyclerRecipes.setVisibility(View.GONE);
            tvEmptyMessage.setVisibility(View.VISIBLE);
        } else {
            tvEmptyMessage.setVisibility(View.GONE);
            recyclerRecipes.setVisibility(View.VISIBLE);

            RecipeAdapter adapter = new RecipeAdapter(suggested, recipe -> {
                // Open the detail screen, passing only the recipe id.
                Intent intent = new Intent(
                        SuggestedRecipesActivity.this,
                        RecipeDetailActivity.class
                );
                intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
                startActivity(intent);
            });
            recyclerRecipes.setAdapter(adapter);
        }
    }
}