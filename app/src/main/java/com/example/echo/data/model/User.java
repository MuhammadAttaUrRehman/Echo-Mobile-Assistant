package com.example.echo.data.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

/**
 * User model class for Firebase Authentication and Realtime Database
 * Compatible with Firebase serialization/deserialization
 */
@IgnoreExtraProperties
public class User {
    private String uid;                    // Firebase Auth UID
    private String email;
    private String displayName;
    private String photoUrl;               // Firebase profile photo URL
    private String phoneNumber;
    private boolean emailVerified;
    private String provider;               // "email", "google.com", etc.
    private long createdAt;
    private long lastLoginAt;
    private long updatedAt;
    private String deviceToken;            // FCM token for notifications
    private boolean isActive;              // Account status
    private String preferredLanguage;      // User language preference
    private boolean notificationsEnabled;

    /**
     * Default constructor required for Firebase
     */
    public User() {
        this.isActive = true;
        this.notificationsEnabled = true;
        this.preferredLanguage = "en";
        this.emailVerified = false;
    }

    /**
     * Constructor for creating a new user with Firebase Auth data
     * @param uid Firebase Auth UID
     * @param email User's email
     * @param displayName User's display name
     */
    public User(String uid, String email, String displayName) {
        this();
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.provider = "email";
        long currentTime = System.currentTimeMillis();
        this.createdAt = currentTime;
        this.lastLoginAt = currentTime;
        this.updatedAt = currentTime;
    }

    /**
     * Constructor for Google Sign-In users
     * @param uid Firebase Auth UID
     * @param email User's email
     * @param displayName User's display name
     * @param photoUrl User's photo URL
     * @param provider Authentication provider
     */
    public User(String uid, String email, String displayName, String photoUrl, String provider) {
        this(uid, email, displayName);
        this.photoUrl = photoUrl;
        this.provider = provider;
        this.emailVerified = true; // Google accounts are pre-verified
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        updateTimestamp();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        updateTimestamp();
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
        updateTimestamp();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        updateTimestamp();
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
        updateTimestamp();
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(long lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
        updateTimestamp();
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
        updateTimestamp();
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
        updateTimestamp();
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
        updateTimestamp();
    }

    @Exclude
    public void updateTimestamp() {
        this.updatedAt = System.currentTimeMillis();
    }

    @Exclude
    public boolean hasProfilePhoto() {
        return photoUrl != null && !photoUrl.isEmpty();
    }

    @Exclude
    public String getInitials() {
        if (displayName == null || displayName.trim().isEmpty()) {
            if (email != null && !email.isEmpty()) {
                return email.substring(0, 1).toUpperCase();
            }
            return "U";
        }

        String[] nameParts = displayName.trim().split("\\s+");
        if (nameParts.length == 1) {
            return nameParts[0].substring(0, Math.min(2, nameParts[0].length())).toUpperCase();
        } else {
            return (nameParts[0].substring(0, 1) +
                    nameParts[nameParts.length - 1].substring(0, 1)).toUpperCase();
        }
    }

    @Exclude
    public boolean isGoogleUser() {
        return "google.com".equals(provider);
    }

    @Exclude
    public boolean needsEmailVerification() {
        return "email".equals(provider) && !emailVerified;
    }

    @Exclude
    public boolean isNewUser() {
        long twentyFourHours = 24 * 60 * 60 * 1000;
        return (System.currentTimeMillis() - createdAt) < twentyFourHours;
    }

    @Exclude
    public String getDisplayNameOrEmail() {
        if (displayName != null && !displayName.trim().isEmpty()) {
            return displayName;
        }
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf("@"));
        }
        return "User";
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", email='" + email + '\'' +
                ", displayName='" + displayName + '\'' +
                ", provider='" + provider + '\'' +
                ", emailVerified=" + emailVerified +
                ", isActive=" + isActive +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        User user = (User) obj;
        return uid != null ? uid.equals(user.uid) : user.uid == null;
    }

    @Override
    public int hashCode() {
        return uid != null ? uid.hashCode() : 0;
    }
}