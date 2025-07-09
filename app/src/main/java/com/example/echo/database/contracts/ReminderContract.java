package com.example.echo.database.contracts;

public final class ReminderContract {
    private ReminderContract() {}

    public static class ReminderEntry {
        public static final String TABLE_NAME = "reminders";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_LOCATION_NAME = "location_name";
        public static final String COLUMN_RADIUS = "radius";
        public static final String COLUMN_SCHEDULED_TIME = "scheduled_time";
        public static final String COLUMN_TYPE = "type";
    }
}