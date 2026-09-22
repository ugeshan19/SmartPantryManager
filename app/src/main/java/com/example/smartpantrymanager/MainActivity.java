package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // Database helper used to access the SQLite database.
    private DatabaseHelper databaseHelper;

    // RecyclerView used to display the user's pantry items.
    private RecyclerView recyclerPantry;

    // Buttons used to navigate to other screens.
    private Button btnAddIngredient;
    private Button btnSuggestedRecipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the MainActivity layout.
        setContentView(R.layout.activity_main);

        // Create the database helper.
        databaseHelper = new DatabaseHelper(this);

        // Connect the Java variables to the XML views.
        recyclerPantry = findViewById(R.id.recyclerPantry);
        btnAddIngredient = findViewById(R.id.btnAddIngredient);
        btnSuggestedRecipes = findViewById(R.id.btnSuggestedRecipes);

        // Set the RecyclerView to display items vertically.
        recyclerPantry.setLayoutManager(new LinearLayoutManager(this));

        // Open the Add Ingredient screen when the button is clicked.
        btnAddIngredient.setOnClickListener(v -> {

            // Intent is used to navigate from MainActivity
            // to AddIngredientActivity.
            Intent intent = new Intent(
                    MainActivity.this,
                    AddIngredientActivity.class
            );

            startActivity(intent);
        });

        // Suggested Recipes button will be connected later
        // when we create the Suggested Recipes screen.
        btnSuggestedRecipes.setOnClickListener(v -> {

            // We will add the SuggestedRecipesActivity here
            // once that screen has been created.
        });

        // Load existing pantry items from SQLite.
        loadPantryItems();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reload the pantry whenever the user returns to this screen.
        // This means a newly added ingredient can appear immediately.
        loadPantryItems();
    }

    private void loadPantryItems() {

        // Create a list that will hold pantry items from the database.
        ArrayList<PantryItem> pantryItems = new ArrayList<>();

        // Get all pantry records from SQLite.
        // The Cursor contains the database results.
        android.database.Cursor cursor =
                databaseHelper.getAllPantryItems();

        // Move through every record returned by SQLite.
        while (cursor.moveToNext()) {

            // Read each column from the current database row.
            int id = cursor.getInt(
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.COL_PANTRY_ID
                    )
            );

            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.COL_PANTRY_NAME
                    )
            );

            double quantity = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.COL_PANTRY_QUANTITY
                    )
            );

            String unit = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.COL_PANTRY_UNIT
                    )
            );

            String expiryDate = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                            DatabaseHelper.COL_PANTRY_EXPIRY
                    )
            );

            // Create a PantryItem object from the database record.
            pantryItems.add(
                    new PantryItem(
                            id,
                            name,
                            String.valueOf(quantity),
                            unit,
                            expiryDate
                    )
            );
        }

        // Always close the Cursor after reading the database.
        cursor.close();

        // Create the RecyclerView adapter.
        PantryAdapter adapter = new PantryAdapter(pantryItems);

        // Connect the adapter to the RecyclerView.
        recyclerPantry.setAdapter(adapter);
    }
}