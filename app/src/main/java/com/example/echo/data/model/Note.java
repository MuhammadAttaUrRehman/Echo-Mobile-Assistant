package com.example.echo.data.model;

public class Note {
    private String id;
    private String userId;
    private String title;
    private String content;
    private long timestamp;

    public Note() {
        // Required empty constructor for Firebase
    }

    public Note(String id, String userId, String title, String content, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
    }

    public Note(String number, String projectIdeas, String s) {
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public Object getCreatedAt() {
        return null;
    }
} 