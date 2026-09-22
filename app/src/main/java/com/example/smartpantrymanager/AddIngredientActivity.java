package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddIngredientActivity extends AppCompatActivity {

    // Input fields from the Add Ingredient screen.
    private EditText etIngredientName;
    private EditText etQuantity;
    private EditText etExpiryDate;

    // Spinner used to select the measurement unit.
    private Spinner spinnerUnit;

    // Buttons used to save or cancel.
    private Button btnSaveIngredient;
    private Button btnCancel;

    // Database helper used to store the pantry item in SQLite.
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the Add Ingredient layout.
        setContentView(R.layout.activity_add_ingredient);

        // Create the database helper.
        databaseHelper = new DatabaseHelper(this);

        // Connect Java variables to the XML views.
        etIngredientName = findViewById(R.id.etIngredientName);
        etQuantity = findViewById(R.id.etQuantity);
        etExpiryDate = findViewById(R.id.etExpiryDate);

        spinnerUnit = findViewById(R.id.spinnerUnit);

        btnSaveIngredient = findViewById(R.id.btnSaveIngredient);
        btnCancel = findViewById(R.id.btnCancel);

        // Save the ingredient when the user presses Save Ingredient.
        btnSaveIngredient.setOnClickListener(v -> saveIngredient());

        // Return to the previous screen when Cancel is pressed.
        btnCancel.setOnClickListener(v -> finish());
    }

    /**
     * Validates the user's input and saves the ingredient
     * into the SQLite pantry table.
     */
    private void saveIngredient() {

        // Get the values entered by the user.
        String name = etIngredientName.getText().toString().trim();
        String quantityText = etQuantity.getText().toString().trim();
        String expiryDate = etExpiryDate.getText().toString().trim();

        // Get the selected measurement unit.
        String unit = spinnerUnit.getSelectedItem().toString();

        // -------------------------
        // INPUT VALIDATION
        // -------------------------

        // Ingredient name cannot be empty.
        if (name.isEmpty()) {
            etIngredientName.setError("Please enter an ingredient name");
            etIngredientName.requestFocus();
            return;
        }

        // Quantity cannot be empty.
        if (quantityText.isEmpty()) {
            etQuantity.setError("Please enter a quantity");
            etQuantity.requestFocus();
            return;
        }

        // Make sure the selected unit is valid.
        if (unit.equalsIgnoreCase("Select unit")) {
            Toast.makeText(
                    this,
                    "Please select a unit",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // Convert the quantity from text into a number.
        double quantity;

        try {
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException e) {

            // Show an error if the user enters something
            // that is not a valid number.
            etQuantity.setError("Enter a valid number");
            etQuantity.requestFocus();
            return;
        }

        // Quantity must be greater than zero.
        if (quantity <= 0) {
            etQuantity.setError("Quantity must be greater than zero");
            etQuantity.requestFocus();
            return;
        }

        // -------------------------
        // SAVE TO SQLITE
        // -------------------------

        // Insert the ingredient into the pantry_items table.
        long result = databaseHelper.addPantryItem(
                name,
                quantity,
                unit,
                expiryDate
        );

        // SQLite returns -1 if the insert failed.
        if (result == -1) {

            Toast.makeText(
                    this,
                    "Failed to save ingredient",
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            // Tell the user that the ingredient was successfully saved.
            Toast.makeText(
                    this,
                    "Ingredient saved successfully",
                    Toast.LENGTH_SHORT
            ).show();

            // Close this Activity and return to MainActivity.
            // MainActivity's onResume() will reload the pantry list.
            finish();
        }
    }
}