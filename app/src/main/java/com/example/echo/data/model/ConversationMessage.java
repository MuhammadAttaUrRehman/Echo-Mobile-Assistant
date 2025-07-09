package com.example.echo.data.model;

public class ConversationMessage {
    private String id;
    private String conversationId;
    private String userId;
    private String content;
    private long timestamp;
    private boolean isUserMessage;

    // No-argument constructor required by Firebase
    public ConversationMessage() {
    }

    public ConversationMessage(String id, String conversationId, String userId, String content, long timestamp, boolean isUserMessage) {
        this.id = id;
        this.conversationId = conversationId;
        this.userId = userId;
        this.content = content;
        this.timestamp = timestamp;
        this.isUserMessage = isUserMessage;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public boolean isUserMessage() { return isUserMessage; }
    public void setUserMessage(boolean userMessage) { isUserMessage = userMessage; }
}