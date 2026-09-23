package com.example.smartpantrymanager;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);

        recyclerPantry = findViewById(R.id.recyclerPantry);
        btnAddIngredient = findViewById(R.id.btnAddIngredient);
        btnSuggestedRecipes = findViewById(R.id.btnSuggestedRecipes);

        recyclerPantry.setLayoutManager(new LinearLayoutManager(this));

        // Open the Add Ingredient screen (no extras = "add" mode).
        btnAddIngredient.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    AddIngredientActivity.class
            );
            startActivity(intent);
        });

        // Suggested Recipes button will be connected in a later part.
        btnSuggestedRecipes.setOnClickListener(v -> {
            // TODO: open SuggestedRecipesActivity
        });

        loadPantryItems();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reload the pantry whenever the user returns to this screen so
        // added / edited items appear immediately.
        loadPantryItems();
    }

    /**
     * Reads every pantry row from SQLite and shows it in the RecyclerView.
     */
    private void loadPantryItems() {

        ArrayList<PantryItem> pantryItems = new ArrayList<>();

        Cursor cursor = databaseHelper.getAllPantryItems();

        while (cursor.moveToNext()) {
            int id = cursor.getInt(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_ID));
            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_NAME));
            double quantity = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_QUANTITY));
            String unit = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_UNIT));
            String expiryDate = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PANTRY_EXPIRY));

            pantryItems.add(new PantryItem(id, name, quantity, unit, expiryDate));
        }

        // Always close the Cursor after reading the database.
        cursor.close();

        // The listener tells the adapter what to do on Edit / Delete.
        PantryAdapter adapter = new PantryAdapter(
                pantryItems,
                new PantryAdapter.OnItemActionListener() {
                    @Override
                    public void onEdit(PantryItem item) {
                        openEditScreen(item);
                    }

                    @Override
                    public void onDelete(PantryItem item) {
                        confirmDelete(item);
                    }
                }
        );

        recyclerPantry.setAdapter(adapter);
    }

    /**
     * Opens AddIngredientActivity in "edit" mode by passing the
     * selected item's data through Intent extras.
     */
    private void openEditScreen(PantryItem item) {
        Intent intent = new Intent(this, AddIngredientActivity.class);
        intent.putExtra(AddIngredientActivity.EXTRA_ITEM_ID, item.getId());
        intent.putExtra(AddIngredientActivity.EXTRA_NAME, item.getName());
        intent.putExtra(AddIngredientActivity.EXTRA_QUANTITY, item.getQuantity());
        intent.putExtra(AddIngredientActivity.EXTRA_UNIT, item.getUnit());
        intent.putExtra(AddIngredientActivity.EXTRA_EXPIRY, item.getExpiryDate());
        startActivity(intent);
    }

    /**
     * Asks the user to confirm before deleting, so items are not
     * removed by an accidental tap.
     */
    private void confirmDelete(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient")
                .setMessage("Remove " + item.getName() + " from your pantry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int rows = databaseHelper.deletePantryItem(item.getId());
                    if (rows > 0) {
                        Toast.makeText(this, "Ingredient deleted",
                                Toast.LENGTH_SHORT).show();
                        loadPantryItems();
                    } else {
                        Toast.makeText(this, "Could not delete ingredient",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}