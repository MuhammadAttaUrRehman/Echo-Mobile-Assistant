package com.example.echo.database.contracts;

public final class NoteContract {
    private NoteContract() {}

    public static class NoteEntry {
        public static final String TABLE_NAME = "notes";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_CREATED_AT = "created_at";
    }
}