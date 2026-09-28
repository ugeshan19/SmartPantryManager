package com.example.smartpantrymanager;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

    // Banner warning about items that expire soon.
    private TextView tvExpiryBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Use our Toolbar as the app bar so it can show the overflow menu.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        databaseHelper = new DatabaseHelper(this);

        recyclerPantry = findViewById(R.id.recyclerPantry);
        btnAddIngredient = findViewById(R.id.btnAddIngredient);
        btnSuggestedRecipes = findViewById(R.id.btnSuggestedRecipes);
        tvExpiryBanner = findViewById(R.id.tvExpiryBanner);

        recyclerPantry.setLayoutManager(new LinearLayoutManager(this));

        // Open the Add Ingredient screen (no extras = "add" mode).
        btnAddIngredient.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    AddIngredientActivity.class
            );
            startActivity(intent);
        });

        // Open the Suggested Recipes screen, which runs the strict matching.
        btnSuggestedRecipes.setOnClickListener(v -> openSuggestedRecipes());

        // Tapping the banner jumps to recipes that can use those ingredients.
        tvExpiryBanner.setOnClickListener(v -> openSuggestedRecipes());

        loadPantryItems();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reload the pantry whenever the user returns to this screen so
        // added / edited items and changed settings appear immediately.
        loadPantryItems();
    }

    // ------------------------------------------------------------------
    // TOOLBAR MENU (navigation)
    // ------------------------------------------------------------------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Build the overflow menu from res/menu/main_menu.xml.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_suggested) {
            openSuggestedRecipes();
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openSuggestedRecipes() {
        startActivity(new Intent(this, SuggestedRecipesActivity.class));
    }

    // ------------------------------------------------------------------
    // PANTRY LIST
    // ------------------------------------------------------------------

    /**
     * Reads every pantry row from SQLite and shows it in the RecyclerView.
     */
    private void loadPantryItems() {

        ArrayList<PantryItem> pantryItems = new ArrayList<>();

        Cursor cursor = databaseHelper.getAllPantryItems();

        while (cursor.moveToNext()) {
            // PantryItem.fromCursor reads one database row into an object.
            pantryItems.add(PantryItem.fromCursor(cursor));
        }

        // Always close the Cursor after reading the database.
        cursor.close();

        // Read the alert setting fresh each time (it may have just changed).
        boolean alertsEnabled = AppSettings.isExpiryAlertsEnabled(this);

        updateExpiryBanner(pantryItems, alertsEnabled);

        // The listener tells the adapter what to do on Edit / Delete.
        PantryAdapter adapter = new PantryAdapter(
                pantryItems,
                alertsEnabled,
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
     * Shows a banner counting items that are expired or expire within
     * the alert window. Hidden when alerts are off or nothing is expiring.
     */
    private void updateExpiryBanner(ArrayList<PantryItem> items,
                                    boolean alertsEnabled) {

        int expiringCount = 0;
        if (alertsEnabled) {
            for (PantryItem item : items) {
                if (item.isExpiringWithin(AppSettings.EXPIRY_ALERT_DAYS)) {
                    expiringCount++;
                }
            }
        }

        if (expiringCount == 0) {
            tvExpiryBanner.setVisibility(View.GONE);
            return;
        }

        String noun = expiringCount == 1 ? "item is" : "items are";
        tvExpiryBanner.setText("\u26A0 " + expiringCount + " " + noun
                + " expired or expiring within "
                + AppSettings.EXPIRY_ALERT_DAYS
                + " days. Tap to see what you can cook.");
        tvExpiryBanner.setVisibility(View.VISIBLE);
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