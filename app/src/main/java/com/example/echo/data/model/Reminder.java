package com.example.echo.data.model;

import java.util.Date;

public class Reminder {
    public Reminder() {

    }

    public enum ReminderType {
        TIME_BASED,
        LOCATION_BASED
    }

    private String id;
    private String title;
    private String description;
    private ReminderType type;
    private Date createdAt;

    // Time-based reminder fields
    private Date scheduledTime;

    // Location-based reminder fields
    private Double latitude; // Changed to Double for nullability
    private Double longitude; // Changed to Double for nullability
    private String locationName;
    private Integer radiusInMeters; // Changed to Integer for nullability

    // Basic constructor for ReminderDao
    public Reminder(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.createdAt = new Date();
    }

    // Constructor for time-based reminder
    public Reminder(String id, String title, String description, Date scheduledTime) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = ReminderType.TIME_BASED;
        this.scheduledTime = scheduledTime;
        this.createdAt = new Date();
    }

    // Constructor for location-based reminder
    public Reminder(String id, String title, String description,
                    Double latitude, Double longitude, String locationName, Integer radiusInMeters) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = ReminderType.LOCATION_BASED;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = locationName;
        this.radiusInMeters = radiusInMeters;
        this.createdAt = new Date();
    }

    // Convenience method to set location fields
    public void setLocation(Double latitude, Double longitude, String locationName, Integer radiusInMeters) {
        this.type = ReminderType.LOCATION_BASED;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = locationName;
        this.radiusInMeters = radiusInMeters;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ReminderType getType() { return type; }
    public void setType(ReminderType type) { this.type = type; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(Date scheduledTime) { this.scheduledTime = scheduledTime; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public Integer getRadiusInMeters() { return radiusInMeters; }
    public void setRadiusInMeters(Integer radiusInMeters) { this.radiusInMeters = radiusInMeters; }
}