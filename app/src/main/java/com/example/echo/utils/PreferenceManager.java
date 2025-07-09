package com.example.echo.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static final String PREF_NAME = "EchoPreferences";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_CURRENT_CONVERSATION_ID = "current_conversation_id";

    private final SharedPreferences preferences;

    public PreferenceManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserId(String userId) {
        preferences.edit().putString(KEY_USER_ID, userId).apply();
    }

    public String getUserId() {
        return preferences.getString(KEY_USER_ID, null);
    }

    public void setCurrentConversationId(String conversationId) {
        preferences.edit().putString(KEY_CURRENT_CONVERSATION_ID, conversationId).apply();
    }

    public String getCurrentConversationId() {
        return preferences.getString(KEY_CURRENT_CONVERSATION_ID, null);
    }
}