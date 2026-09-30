package com.example.smartpantrymanager;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Small helper that reads and writes the user's settings.
 * Settings are stored with SharedPreferences, so they survive
 * closing and reopening the app.
 */
public class AppSettings {

    private static final String PREFS_NAME = "smart_pantry_settings";
    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts_enabled";
    private static final String KEY_DEFAULT_UNIT = "default_unit";

    // An item counts as "expiring soon" when it expires within this many days.
    public static final int EXPIRY_ALERT_DAYS = 3;

    // Utility class: not meant to be instantiated.
    private AppSettings() { }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Expiring-soon alerts are ON until the user turns them off. */
    public static boolean isExpiryAlertsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_EXPIRY_ALERTS, true);
    }

    public static void setExpiryAlertsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_EXPIRY_ALERTS, enabled).apply();
    }

    /** Returns the preferred unit, or an empty string if none is set. */
    public static String getDefaultUnit(Context context) {
        return prefs(context).getString(KEY_DEFAULT_UNIT, "");
    }

    public static void setDefaultUnit(Context context, String unit) {
        prefs(context).edit().putString(KEY_DEFAULT_UNIT, unit).apply();
    }
}