package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings screen: turn expiring-soon alerts on or off and choose
 * the unit that is pre-selected when adding an ingredient.
 */
public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat switchExpiryAlerts;
    private Spinner spinnerDefaultUnit;
    private Button btnSaveSettings;
    private Button btnCancelSettings;

    // Spinner options: "No default" followed by every real unit.
    private final List<String> unitOptions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        switchExpiryAlerts = findViewById(R.id.switchExpiryAlerts);
        spinnerDefaultUnit = findViewById(R.id.spinnerDefaultUnit);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);
        btnCancelSettings = findViewById(R.id.btnCancelSettings);

        setUpUnitSpinner();
        loadSavedSettings();

        btnSaveSettings.setOnClickListener(v -> saveSettings());
        btnCancelSettings.setOnClickListener(v -> finish());
    }

    /**
     * Builds the spinner from the same unit list used on the Add
     * Ingredient screen, skipping its "Select unit" placeholder.
     */
    private void setUpUnitSpinner() {
        String[] units = getResources().getStringArray(R.array.unit_options);

        unitOptions.add("No default");
        for (int i = 1; i < units.length; i++) {
            unitOptions.add(units[i]);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, unitOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDefaultUnit.setAdapter(adapter);
    }

    /** Shows the values that were saved the last time. */
    private void loadSavedSettings() {
        switchExpiryAlerts.setChecked(AppSettings.isExpiryAlertsEnabled(this));

        int position = unitOptions.indexOf(AppSettings.getDefaultUnit(this));
        spinnerDefaultUnit.setSelection(position >= 0 ? position : 0);
    }

    /** Writes the chosen values to SharedPreferences. */
    private void saveSettings() {
        AppSettings.setExpiryAlertsEnabled(this, switchExpiryAlerts.isChecked());

        int position = spinnerDefaultUnit.getSelectedItemPosition();
        // Position 0 is "No default", which is stored as an empty string.
        String unit = position <= 0 ? "" : unitOptions.get(position);
        AppSettings.setDefaultUnit(this, unit);

        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}