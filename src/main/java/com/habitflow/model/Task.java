package com.habitflow.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Task — represents a single task in HabitFlow.
 *
 * Think of this like a form with fields.
 * Every task in the database becomes a Task object
 * in Java when we load it.
 */
public class Task {

    // ── Fields — match the database columns ─────────
    private int    id;
    private int    parentTaskId;   // 0 = no parent
    private String title;
    private String description;
    private int    categoryId;

    // The 3-checkbox system
    private TaskStatus taskStatus;

    private Priority   priority;
    private Section    section;
    private LocalDate  dueDate;
    private String     dueTime;
    private int        sortOrder;
    private boolean    isDeleted;
    private boolean    isRolledOver;
    private int        rolloverCount;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Enums — fixed set of allowed values ─────────

    /**
     * The 3-Checkbox System.
     * Every task is exactly one of these three states.
     */
    public enum TaskStatus {
        PENDING,    // Not Done checkbox
        DONE,       // Done checkbox
        DELAYED     // Delayed checkbox
    }

    public enum Priority {
        LOW, NORMAL, HIGH, CRITICAL
    }

    public enum Section {
        MY_DAY, IMPORTANT, DO, DONT,
        HABITS, NOTIFY_LATER, COMPLETED,
        DELAYED, DELETED
    }

    // ── Constructors ─────────────────────────────────

    /** Empty constructor — used when loading from DB */
    public Task() {
        this.taskStatus = TaskStatus.PENDING;
        this.priority   = Priority.NORMAL;
        this.section    = Section.MY_DAY;
    }

    /**
     * Quick constructor — used for fast task creation
     * from the quick-add field.
     * Only needs a title — everything else is default.
     */
    public Task(String title) {
        this();
        this.title = title;
    }

    // ── Getters and Setters ──────────────────────────
    // These let other classes read and change the fields

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getParentTaskId() { return parentTaskId; }
    public void setParentTaskId(int parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
    }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public TaskStatus getTaskStatus() { return taskStatus; }
    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Section getSection() { return section; }
    public void setSection(Section section) {
        this.section = section;
    }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getDueTime() { return dueTime; }
    public void setDueTime(String dueTime) {
        this.dueTime = dueTime;
    }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public boolean isRolledOver() { return isRolledOver; }
    public void setRolledOver(boolean rolledOver) {
        isRolledOver = rolledOver;
    }

    public int getRolloverCount() { return rolloverCount; }
    public void setRolloverCount(int rolloverCount) {
        this.rolloverCount = rolloverCount;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Returns a simple text representation of this task.
     * Used for debugging — you'll see this in the terminal.
     */
    @Override
    public String toString() {
        return "Task{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", status=" + taskStatus +
            ", section=" + section +
            '}';
    }
}