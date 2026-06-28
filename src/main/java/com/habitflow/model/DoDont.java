package com.habitflow.model;

import java.time.LocalDateTime;

/**
 * DoDont — represents one item in the
 * Dos or Don'ts list.
 *
 * type = DO   → something you SHOULD do daily
 * type = DONT → something you should AVOID daily
 *
 * Each item has a daily log — did you do it
 * or not today? Saves to stats over time.
 */
public class DoDont {

    private int    id;
    private String title;
    private String description;
    private Type   type;
    private String colorHex;
    private int    sortOrder;
    private boolean isArchived;
    private LocalDateTime createdAt;

    // Today's status — loaded separately
    private DailyStatus todayStatus;

    // ── Enums ───────────────────────────────────

    public enum Type {
        DO,     // Something to DO
        DONT    // Something to AVOID
    }

    public enum DailyStatus {
        PENDING,    // Not marked yet today
        SUCCESS,    // Did it (DO) / Avoided (DONT)
        FAILED      // Didn't do it / Did it anyway
    }

    // ── Constructors ─────────────────────────────

    public DoDont() {
        this.type        = Type.DO;
        this.colorHex    = "#10B981";
        this.todayStatus = DailyStatus.PENDING;
    }

    public DoDont(String title, Type type) {
        this();
        this.title = title;
        this.type  = type;
        // Don'ts default to red colour
        if (type == Type.DONT) {
            this.colorHex = "#EF4444";
        }
    }

    // ── Getters and Setters ──────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String t) {
        this.title = t;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String d) {
        this.description = d;
    }

    public Type getType() { return type; }
    public void setType(Type t) {
        this.type = t;
    }

    public String getColorHex() {
        return colorHex;
    }
    public void setColorHex(String c) {
        this.colorHex = c;
    }

    public int getSortOrder() {
        return sortOrder;
    }
    public void setSortOrder(int s) {
        this.sortOrder = s;
    }

    public boolean isArchived() {
        return isArchived;
    }
    public void setArchived(boolean a) {
        this.isArchived = a;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(
            LocalDateTime c) {
        this.createdAt = c;
    }

    public DailyStatus getTodayStatus() {
        return todayStatus;
    }
    public void setTodayStatus(
            DailyStatus s) {
        this.todayStatus = s;
    }

    /**
     * Returns the action label for the
     * success button based on type.
     *
     * DO   → "✓ Did it"
     * DONT → "✓ Avoided"
     */
    public String getSuccessLabel() {
        return type == Type.DO ?
            "✓  Did it" : "✓  Avoided";
    }

    /**
     * Returns the action label for the
     * failed button based on type.
     *
     * DO   → "✗ Skipped"
     * DONT → "✗ Did it"
     */
    public String getFailedLabel() {
        return type == Type.DO ?
            "✗  Skipped" : "✗  Did it";
    }

    @Override
    public String toString() {
        return "DoDont{id=" + id +
            ", title='" + title + '\'' +
            ", type=" + type + '}';
    }
}