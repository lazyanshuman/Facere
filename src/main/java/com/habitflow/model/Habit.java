package com.habitflow.model;

/**
 * Habit — represents a single habit in HabitFlow.
 */
public class Habit {

    private int    id;
    private String title;
    private String description;
    private String recurrenceDays;
    private String colorHex;
    private HabitType habitType;
    private int    currentStreak;
    private int    longestStreak;
    private boolean isArchived;
    private int    sortOrder;

    // Today's log status — loaded separately
    private LogStatus todayStatus;

    // ── Enums ───────────────────────────────────────

    public enum HabitType {
        BUILD,  // Positive habit to build
        BREAK   // Bad habit to break
    }

    public enum LogStatus {
        PENDING,  // Not marked yet today
        DONE,     // Marked as done today
        SKIPPED,  // Marked as skipped today
        FAILED    // Marked as failed today
    }

    // ── Constructor ──────────────────────────────────

    public Habit() {
        this.habitType     = HabitType.BUILD;
        this.colorHex      = "#6C63FF";
        this.recurrenceDays = "1,2,3,4,5,6,7";
        this.todayStatus   = LogStatus.PENDING;
    }

    public Habit(String title,
                 HabitType type) {
        this();
        this.title     = title;
        this.habitType = type;
    }

    // ── Getters and Setters ──────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String d) {
        this.description = d;
    }

    public String getRecurrenceDays() {
        return recurrenceDays;
    }
    public void setRecurrenceDays(String r) {
        this.recurrenceDays = r;
    }

    public String getColorHex() {
        return colorHex;
    }
    public void setColorHex(String c) {
        this.colorHex = c;
    }

    public HabitType getHabitType() {
        return habitType;
    }
    public void setHabitType(HabitType t) {
        this.habitType = t;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }
    public void setCurrentStreak(int s) {
        this.currentStreak = s;
    }

    public int getLongestStreak() {
        return longestStreak;
    }
    public void setLongestStreak(int s) {
        this.longestStreak = s;
    }

    public boolean isArchived() {
        return isArchived;
    }
    public void setArchived(boolean a) {
        this.isArchived = a;
    }

    public int getSortOrder() {
        return sortOrder;
    }
    public void setSortOrder(int s) {
        this.sortOrder = s;
    }

    public LogStatus getTodayStatus() {
        return todayStatus;
    }
    public void setTodayStatus(LogStatus s) {
        this.todayStatus = s;
    }

    public String getFrequencyLabel() {
        if ("1,2,3,4,5,6,7"
                .equals(recurrenceDays)) {
            return "Daily";
        } else if ("1,2,3,4,5"
                .equals(recurrenceDays)) {
            return "Weekdays";
        } else if ("6,7"
                .equals(recurrenceDays)) {
            return "Weekends";
        } else {
            return "Custom";
        }
    }

    @Override
    public String toString() {
        return "Habit{id=" + id +
            ", title='" + title + '\'' +
            ", type=" + habitType + '}';
    }
}