package com.habitflow.model;

/**
 * User — represents a profile in HabitFlow.
 * Each user has separate tasks, habits,
 * dos/don'ts, their own accent colour and
 * an optional PIN.
 */
public class User {

    private int    id;
    private String fullName;
    private String username;
    private String avatarColor;
    private String accentColor;
    private String pinHash;
    private boolean active = true;
    private int    sortOrder;

    public User() {}

    public User(int id, String fullName,
            String username,
            String avatarColor,
            String accentColor) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.avatarColor = avatarColor;
        this.accentColor = accentColor;
    }

    // ── Initials for the avatar circle ──
    public String getInitials() {
        if (fullName == null
                || fullName.isBlank()) {
            return "?";
        }
        String[] parts =
            fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0]
                .substring(0, 1)
                .toUpperCase();
        }
        return (parts[0].substring(0, 1) +
            parts[parts.length - 1]
                .substring(0, 1))
            .toUpperCase();
    }

    public boolean hasPin() {
        return pinHash != null
            && !pinHash.isBlank();
    }

    // ── Getters / setters ──
    public int getId() { return id; }
    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarColor() {
        return avatarColor;
    }
    public void setAvatarColor(
            String avatarColor) {
        this.avatarColor = avatarColor;
    }

    public String getAccentColor() {
        return accentColor;
    }
    public void setAccentColor(
            String accentColor) {
        this.accentColor = accentColor;
    }

    public String getPinHash() {
        return pinHash;
    }
    public void setPinHash(String pinHash) {
        this.pinHash = pinHash;
    }

    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }

    public int getSortOrder() {
        return sortOrder;
    }
    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}