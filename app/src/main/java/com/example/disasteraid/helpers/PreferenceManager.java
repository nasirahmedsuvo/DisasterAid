
package com.example.disasteraid.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private static final String PREF_NAME = "DisasterAidPreferences";

    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_NOTIFICATIONS = "notificationsEnabled";

    private static final String KEY_DARK_MODE = "darkMode";
    private static final String KEY_EMERGENCY_CONTACT = "emergencyContact";
    private final SharedPreferences preferences;

    public PreferenceManager(Context context) {
        if (context == null) {
            throw new IllegalArgumentException(
                    "PreferenceManager requires a valid Context."
            );
        }

        preferences = context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );
    }

    public void saveUserName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "User name cannot be empty."
            );
        }

        preferences.edit()
                .putString(KEY_USER_NAME, name.trim())
                .apply();
    }

    public String getUserName() {
        return preferences.getString(KEY_USER_NAME, "");
    }

    public void saveNotificationPreference(boolean enabled) {
        preferences.edit()
                .putBoolean(KEY_NOTIFICATIONS, enabled)
                .apply();
    }

    public boolean areNotificationsEnabled() {
        return preferences.getBoolean(KEY_NOTIFICATIONS, true);
    }

    public void clearPreferences() {
        preferences.edit().clear().apply();
    }
    public void saveDarkModePreference(boolean enabled) {
        preferences.edit()
                .putBoolean(KEY_DARK_MODE, enabled)
                .apply();
    }

    public boolean isDarkModeEnabled() {
        return preferences.getBoolean(KEY_DARK_MODE, false);
    }

    public void saveEmergencyContact(String phone) {
        preferences.edit()
                .putString(KEY_EMERGENCY_CONTACT, phone)
                .apply();
    }

    public String getEmergencyContact() {
        return preferences.getString(KEY_EMERGENCY_CONTACT, "");
    }

    // Clear only the username when logging out.
// Preserve theme, notification and emergency-contact preferences.
    public void clearUserName() {
        preferences.edit()
                .remove(KEY_USER_NAME)
                .apply();
    }
}
