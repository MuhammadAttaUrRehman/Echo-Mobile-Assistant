package com.example.echo.database.contracts;

public final class ConversationContract {
    private ConversationContract() {}

    public static final class ConversationEntry {
        public static final String TABLE_NAME = "conversations";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_CONVERSATION_ID = "conversation_id";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_IS_USER_MESSAGE = "is_user_message";
    }
}