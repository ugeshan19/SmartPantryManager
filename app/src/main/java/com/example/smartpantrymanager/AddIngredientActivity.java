package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Screen used for BOTH adding a new pantry item and editing an existing one.
 * If the launching Intent carries EXTRA_ITEM_ID we are in edit mode.
 */
public class AddIngredientActivity extends AppCompatActivity {

    // Keys for the data passed in through the Intent (edit mode).
    public static final String EXTRA_ITEM_ID = "extra_item_id";
    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_QUANTITY = "extra_quantity";
    public static final String EXTRA_UNIT = "extra_unit";
    public static final String EXTRA_EXPIRY = "extra_expiry";

    private EditText etIngredientName;
    private EditText etQuantity;
    private EditText etExpiryDate;
    private Spinner spinnerUnit;
    private Button btnSaveIngredient;
    private Button btnCancel;
    private TextView tvFormTitle;
    private TextView tvFormSubtitle;

    private DatabaseHelper databaseHelper;

    // -1 means "add mode"; any other value is the row id being edited.
    private int editItemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ingredient);

        databaseHelper = new DatabaseHelper(this);

        etIngredientName = findViewById(R.id.etIngredientName);
        etQuantity = findViewById(R.id.etQuantity);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        btnSaveIngredient = findViewById(R.id.btnSaveIngredient);
        btnCancel = findViewById(R.id.btnCancel);
        tvFormTitle = findViewById(R.id.tvFormTitle);
        tvFormSubtitle = findViewById(R.id.tvFormSubtitle);

        // If an item id was passed in, switch the screen to edit mode.
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_ITEM_ID)) {
            editItemId = intent.getIntExtra(EXTRA_ITEM_ID, -1);
            fillFormForEditing(intent);
        }

        btnSaveIngredient.setOnClickListener(v -> saveIngredient());
        btnCancel.setOnClickListener(v -> finish());
    }

    /**
     * Pre-fills the form with the existing item's values and changes
     * the texts so the user can tell they are editing.
     */
    private void fillFormForEditing(Intent intent) {
        tvFormTitle.setText("Edit Ingredient");
        tvFormSubtitle.setText("Update the details of this ingredient");
        btnSaveIngredient.setText("Update Ingredient");

        etIngredientName.setText(intent.getStringExtra(EXTRA_NAME));
        etQuantity.setText(
                PantryItem.formatQuantity(intent.getDoubleExtra(EXTRA_QUANTITY, 0)));

        String expiry = intent.getStringExtra(EXTRA_EXPIRY);
        etExpiryDate.setText(expiry == null ? "" : expiry);

        // Select the matching unit in the spinner.
        String unit = intent.getStringExtra(EXTRA_UNIT);
        for (int i = 0; i < spinnerUnit.getCount(); i++) {
            if (spinnerUnit.getItemAtPosition(i).toString()
                    .equalsIgnoreCase(unit)) {
                spinnerUnit.setSelection(i);
                break;
            }
        }
    }

    /**
     * Validates the input, then either inserts a new row (add mode)
     * or updates the existing row (edit mode).
     */
    private void saveIngredient() {

        String name = etIngredientName.getText().toString().trim();
        String quantityText = etQuantity.getText().toString().trim();
        String expiryDate = etExpiryDate.getText().toString().trim();
        String unit = spinnerUnit.getSelectedItem().toString();

        // -------------------------
        // INPUT VALIDATION
        // -------------------------

        if (name.isEmpty()) {
            etIngredientName.setError("Please enter an ingredient name");
            etIngredientName.requestFocus();
            return;
        }

        if (quantityText.isEmpty()) {
            etQuantity.setError("Please enter a quantity");
            etQuantity.requestFocus();
            return;
        }

        if (unit.equalsIgnoreCase("Select unit")) {
            Toast.makeText(this, "Please select a unit",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException e) {
            etQuantity.setError("Enter a valid number");
            etQuantity.requestFocus();
            return;
        }

        if (quantity <= 0) {
            etQuantity.setError("Quantity must be greater than zero");
            etQuantity.requestFocus();
            return;
        }

        // The expiry date is optional, but if typed it must be a real date.
        if (!expiryDate.isEmpty() && !isValidDate(expiryDate)) {
            etExpiryDate.setError("Use the format YYYY-MM-DD, e.g. 2026-10-31");
            etExpiryDate.requestFocus();
            return;
        }

        // -------------------------
        // SAVE TO SQLITE
        // -------------------------

        if (editItemId == -1) {
            // ADD MODE: insert a new row.
            long result = databaseHelper.addPantryItem(
                    name, quantity, unit, expiryDate);

            if (result == -1) {
                Toast.makeText(this, "Failed to save ingredient",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Ingredient saved successfully",
                    Toast.LENGTH_SHORT).show();

        } else {
            // EDIT MODE: update the existing row.
            int rows = databaseHelper.updatePantryItem(
                    editItemId, name, quantity, unit, expiryDate);

            if (rows == 0) {
                Toast.makeText(this, "Failed to update ingredient",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Ingredient updated successfully",
                    Toast.LENGTH_SHORT).show();
        }

        // Return to MainActivity; its onResume() reloads the list.
        finish();
    }

    /**
     * Checks the text is a real calendar date in yyyy-MM-dd format.
     * setLenient(false) rejects impossible dates such as 2026-02-31.
     */
    private boolean isValidDate(String text) {
        if (!text.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return false;
        }
        SimpleDateFormat format =
                new SimpleDateFormat(PantryItem.DATE_FORMAT, Locale.US);
        format.setLenient(false);
        try {
            format.parse(text);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}