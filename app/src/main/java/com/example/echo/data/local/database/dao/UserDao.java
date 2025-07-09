package com.example.echo.data.local.database.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.echo.database.EchoDbHelper;
import com.example.echo.database.contracts.UserContract;
import com.example.echo.data.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User operations
 * Handles local SQLite database operations for Firebase authenticated users
 * Provides offline data access and caching for user information
 */
public class UserDao {
    private static final String TAG = "UserDao";
    private final EchoDbHelper dbHelper;

    public UserDao(EchoDbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Insert or update user in local database
     * @param user User object to save
     * @return row ID of inserted user, or number of updated rows
     */
    public long saveUser(User user) {
        if (user == null || user.getUid() == null) {
            Log.e(TAG, "Cannot save null user or user without UID");
            return -1;
        }

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = createContentValues(user);
            long result = db.insertWithOnConflict(
                    UserContract.UserEntry.TABLE_NAME,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
            );
            Log.d(TAG, "User saved with result: " + result + " for UID: " + user.getUid());
            return result;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error saving user: " + e.getMessage(), e);
            return -1;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    /**
     * Get user by Firebase UID
     * @param uid Firebase user UID
     * @return User object if found, null otherwise
     */
    public User getUserByUid(String uid) {
        if (uid == null || uid.isEmpty()) {
            Log.e(TAG, "UID cannot be null or empty");
            return null;
        }

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            String selection = UserContract.UserEntry.COLUMN_UID + " = ?";
            String[] selectionArgs = {uid};

            cursor = db.query(
                    UserContract.UserEntry.TABLE_NAME,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                User user = cursorToUser(cursor);
                Log.d(TAG, "User found with UID: " + uid);
                return user;
            }

            Log.d(TAG, "User not found with UID: " + uid);
            return null;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error getting user by UID: " + e.getMessage(), e);
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    /**
     * Get user by email address
     * @param email User's email
     * @return User object if found, null otherwise
     */
    public User getUserByEmail(String email) {
        if (email == null || email.isEmpty()) {
            Log.e(TAG, "Email cannot be null or empty");
            return null;
        }

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            String selection = UserContract.UserEntry.COLUMN_EMAIL + " = ?";
            String[] selectionArgs = {email};

            cursor = db.query(
                    UserContract.UserEntry.TABLE_NAME,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                User user = cursorToUser(cursor);
                Log.d(TAG, "User found with email: " + email);
                return user;
            }

            Log.d(TAG, "User not found with email: " + email);
            return null;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error getting user by email: " + e.getMessage(), e);
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    /**
     * Update user's last login timestamp
     * @param uid Firebase user UID
     * @return number of rows updated
     */
    public int updateLastLogin(String uid) {
        if (uid == null || uid.isEmpty()) {
            Log.e(TAG, "UID cannot be null or empty");
            return 0;
        }

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(UserContract.UserEntry.COLUMN_LAST_LOGIN_AT, System.currentTimeMillis());
            values.put(UserContract.UserEntry.COLUMN_UPDATED_AT, System.currentTimeMillis());

            String whereClause = UserContract.UserEntry.COLUMN_UID + " = ?";
            String[] whereArgs = {uid};

            int rowsUpdated = db.update(
                    UserContract.UserEntry.TABLE_NAME,
                    values,
                    whereClause,
                    whereArgs
            );

            Log.d(TAG, "Updated last login for " + rowsUpdated + " users with UID: " + uid);
            return rowsUpdated;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error updating last login: " + e.getMessage(), e);
            return 0;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    /**
     * Update user's device token for push notifications
     * @param uid Firebase user UID
     * @param deviceToken FCM device token
     * @return number of rows updated
     */
    public int updateDeviceToken(String uid, String deviceToken) {
        if (uid == null || uid.isEmpty()) {
            Log.e(TAG, "UID cannot be null or empty");
            return 0;
        }

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(UserContract.UserEntry.COLUMN_DEVICE_TOKEN, deviceToken);
            values.put(UserContract.UserEntry.COLUMN_UPDATED_AT, System.currentTimeMillis());

            String whereClause = UserContract.UserEntry.COLUMN_UID + " = ?";
            String[] whereArgs = {uid};

            int rowsUpdated = db.update(
                    UserContract.UserEntry.TABLE_NAME,
                    values,
                    whereClause,
                    whereArgs
            );

            Log.d(TAG, "Updated device token for " + rowsUpdated + " users with UID: " + uid);
            return rowsUpdated;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error updating device token: " + e.getMessage(), e);
            return 0;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    /**
     * Delete user from local database
     * @param uid Firebase user UID
     * @return number of rows deleted
     */
    public int deleteUser(String uid) {
        if (uid == null || uid.isEmpty()) {
            Log.e(TAG, "UID cannot be null or empty");
            return 0;
        }

        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            String whereClause = UserContract.UserEntry.COLUMN_UID + " = ?";
            String[] whereArgs = {uid};

            int rowsDeleted = db.delete(
                    UserContract.UserEntry.TABLE_NAME,
                    whereClause,
                    whereArgs
            );

            Log.d(TAG, "Deleted " + rowsDeleted + " user records with UID: " + uid);
            return rowsDeleted;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error deleting user: " + e.getMessage(), e);
            return 0;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    /**
     * Get all active users
     * @return List of active users
     */
    public List<User> getActiveUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            String selection = UserContract.UserEntry.COLUMN_IS_ACTIVE + " = ?";
            String[] selectionArgs = {"1"};

            cursor = db.query(
                    UserContract.UserEntry.TABLE_NAME,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    UserContract.UserEntry.COLUMN_LAST_LOGIN_AT + " DESC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    User user = cursorToUser(cursor);
                    if (user != null) {
                        users.add(user);
                    }
                } while (cursor.moveToNext());
            }

            Log.d(TAG, "Retrieved " + users.size() + " active users");
            return users;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error getting active users: " + e.getMessage(), e);
            return users;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    /**
     * Check if user exists locally
     * @param uid Firebase user UID
     * @return true if user exists locally
     */
    public boolean userExists(String uid) {
        if (uid == null || uid.isEmpty()) {
            return false;
        }

        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            String selection = UserContract.UserEntry.COLUMN_UID + " = ?";
            String[] selectionArgs = {uid};

            cursor = db.query(
                    UserContract.UserEntry.TABLE_NAME,
                    new String[]{UserContract.UserEntry.COLUMN_UID},
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            boolean exists = cursor != null && cursor.getCount() > 0;
            Log.d(TAG, "User exists check for " + uid + ": " + exists);
            return exists;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error checking if user exists: " + e.getMessage(), e);
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    /**
     * Clear all user data (for logout/app reset)
     * @return number of rows deleted
     */
    public int clearAllUsers() {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            int rowsDeleted = db.delete(UserContract.UserEntry.TABLE_NAME, null, null);
            Log.d(TAG, "Cleared all user data: " + rowsDeleted + " records");
            return rowsDeleted;
        } catch (SQLiteException e) {
            Log.e(TAG, "Error clearing all users: " + e.getMessage(), e);
            return 0;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    /**
     * Create ContentValues from User object
     * @param user User object
     * @return ContentValues for database operations
     */
    private ContentValues createContentValues(User user) {
        ContentValues values = new ContentValues();
        values.put(UserContract.UserEntry.COLUMN_UID, user.getUid());
        values.put(UserContract.UserEntry.COLUMN_EMAIL, user.getEmail());
        values.put(UserContract.UserEntry.COLUMN_DISPLAY_NAME, user.getDisplayName());
        values.put(UserContract.UserEntry.COLUMN_PHOTO_URL, user.getPhotoUrl());
        values.put(UserContract.UserEntry.COLUMN_PHONE_NUMBER, user.getPhoneNumber());
        values.put(UserContract.UserEntry.COLUMN_EMAIL_VERIFIED, user.isEmailVerified() ? 1 : 0);
        values.put(UserContract.UserEntry.COLUMN_PROVIDER, user.getProvider());
        values.put(UserContract.UserEntry.COLUMN_CREATED_AT, user.getCreatedAt());
        values.put(UserContract.UserEntry.COLUMN_LAST_LOGIN_AT, user.getLastLoginAt());
        values.put(UserContract.UserEntry.COLUMN_UPDATED_AT, user.getUpdatedAt());
        values.put(UserContract.UserEntry.COLUMN_DEVICE_TOKEN, user.getDeviceToken());
        values.put(UserContract.UserEntry.COLUMN_IS_ACTIVE, user.isActive() ? 1 : 0);
        values.put(UserContract.UserEntry.COLUMN_PREFERRED_LANGUAGE, user.getPreferredLanguage());
        values.put(UserContract.UserEntry.COLUMN_NOTIFICATIONS_ENABLED, user.isNotificationsEnabled() ? 1 : 0);
        return values;
    }

    /**
     * Convert cursor to User object
     * @param cursor Database cursor
     * @return User object
     */
    private User cursorToUser(Cursor cursor) {
        try {
            User user = new User();
            user.setUid(cursor.getString(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_UID)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_EMAIL)));
            user.setDisplayName(cursor.getString(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_DISPLAY_NAME)));
            user.setPhotoUrl(cursor.getString(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_PHOTO_URL)));
            user.setPhoneNumber(cursor.getString(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_PHONE_NUMBER)));
            user.setEmailVerified(cursor.getInt(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_EMAIL_VERIFIED)) == 1);
            user.setProvider(cursor.getString(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_PROVIDER)));
            user.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_CREATED_AT)));
            user.setLastLoginAt(cursor.getLong(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_LAST_LOGIN_AT)));
            user.setUpdatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_UPDATED_AT)));
            user.setDeviceToken(cursor.getString(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_DEVICE_TOKEN)));
            user.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_IS_ACTIVE)) == 1);
            user.setPreferredLanguage(cursor.getString(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_PREFERRED_LANGUAGE)));
            user.setNotificationsEnabled(cursor.getInt(cursor.getColumnIndexOrThrow(UserContract.UserEntry.COLUMN_NOTIFICATIONS_ENABLED)) == 1);
            return user;
        } catch (Exception e) {
            Log.e(TAG, "Error converting cursor to User: " + e.getMessage(), e);
            return null;
        }
    }
}