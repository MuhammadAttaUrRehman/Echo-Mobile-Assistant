package com.example.echo.data.local.preferences;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Utility class to manage application preferences using SharedPreferences
 */
public class AppPreferences {
    private static final String PREFS_NAME = "echo_preferences";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_NOTIFICATION_ENABLED = "notification_enabled";
    private static final String KEY_VOICE_ASSISTANT_ENABLED = "voice_assistant_enabled";

    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    public AppPreferences(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public void setUserId(String userId) {
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    public String getUserId() {
        return preferences.getString(KEY_USER_ID, null);
    }

    public void setUserEmail(String email) {
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    public String getUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, null);
    }

    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public boolean isFirstLaunch() {
        boolean isFirstLaunch = preferences.getBoolean(KEY_FIRST_LAUNCH, true);
        if (isFirstLaunch) {
            editor.putBoolean(KEY_FIRST_LAUNCH, false);
            editor.apply();
        }
        return isFirstLaunch;
    }

    public void setDarkMode(boolean isDarkMode) {
        editor.putBoolean(KEY_DARK_MODE, isDarkMode);
        editor.apply();
    }

    public boolean isDarkMode() {
        return preferences.getBoolean(KEY_DARK_MODE, false);
    }

    public void setNotificationsEnabled(boolean enabled) {
        editor.putBoolean(KEY_NOTIFICATION_ENABLED, enabled);
        editor.apply();
    }

    public boolean areNotificationsEnabled() {
        return preferences.getBoolean(KEY_NOTIFICATION_ENABLED, true);
    }

    public void setVoiceAssistantEnabled(boolean enabled) {
        editor.putBoolean(KEY_VOICE_ASSISTANT_ENABLED, enabled);
        editor.apply();
    }

    public boolean isVoiceAssistantEnabled() {
        return preferences.getBoolean(KEY_VOICE_ASSISTANT_ENABLED, true);
    }

    public void clearAllPreferences() {
        editor.clear();
        editor.apply();
    }
}