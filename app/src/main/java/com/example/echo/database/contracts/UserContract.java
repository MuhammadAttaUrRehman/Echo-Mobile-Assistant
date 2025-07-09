package com.example.echo.database.contracts;

/**
 * Contract class for the User table schema
 */
public final class UserContract {
    // Prevent instantiation
    private UserContract() {}

    /**
     * Schema for the User table
     */
    public static final class UserEntry {
        public static final String TABLE_NAME = "users";

        // Column names
        public static final String COLUMN_UID = "uid";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_DISPLAY_NAME = "display_name";
        public static final String COLUMN_PHOTO_URL = "photo_url";
        public static final String COLUMN_PHONE_NUMBER = "phone_number";
        public static final String COLUMN_EMAIL_VERIFIED = "email_verified";
        public static final String COLUMN_PROVIDER = "provider";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_LAST_LOGIN_AT = "last_login_at";
        public static final String COLUMN_UPDATED_AT = "updated_at";
        public static final String COLUMN_DEVICE_TOKEN = "device_token";
        public static final String COLUMN_IS_ACTIVE = "is_active";
        public static final String COLUMN_PREFERRED_LANGUAGE = "preferred_language";
        public static final String COLUMN_NOTIFICATIONS_ENABLED = "notifications_enabled";
    }
}
