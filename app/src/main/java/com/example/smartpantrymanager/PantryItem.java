package com.example.smartpantrymanager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Model class representing one row of the pantry_items table.
 */
public class PantryItem {

    // Expiry dates are stored as text in this format, e.g. 2026-10-31.
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    private final int id;
    private final String name;
    private final double quantity;
    private final String unit;
    private final String expiryDate;

    public PantryItem(int id, String name, double quantity, String unit, String expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    /** Quantity as display text: 2.0 becomes "2", 0.50 becomes "0.5". */
    public String getQuantityText() {
        return formatQuantity(quantity);
    }

    public static String formatQuantity(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        String text = String.format(Locale.US, "%.2f", value);
        // Remove trailing zeros (and a dangling decimal point).
        return text.replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    /**
     * Number of days from today until the expiry date.
     * Negative means already expired. Returns Long.MAX_VALUE when the
     * item has no (valid) expiry date.
     */
    public long getDaysUntilExpiry() {
        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            return Long.MAX_VALUE;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.US);
            format.setLenient(false);
            Date expiry = format.parse(expiryDate.trim());

            // Today at midnight, so the result is a whole number of days.
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            long diffMillis = expiry.getTime() - today.getTimeInMillis();
            return Math.round(diffMillis / 86400000.0);
        } catch (ParseException e) {
            return Long.MAX_VALUE;
        }
    }

    /** True if the item has an expiry date within the given number of days (or already passed). */
    public boolean isExpiringWithin(int days) {
        long remaining = getDaysUntilExpiry();
        return remaining != Long.MAX_VALUE && remaining <= days;
    }
}