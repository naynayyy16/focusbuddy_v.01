package com.focusbuddy.models;

import java.time.LocalDateTime;

public class PomodoroSession {
    public enum Type {
        FOCUS, BREAK
    }

    private int id;
    private int userId;
    private int taskId;
    private int subjectId;
    private Type type;
    private int duration; // in minutes
    private LocalDateTime completedAt;

    public PomodoroSession() {}

    public PomodoroSession(int userId, int taskId, int subjectId, Type type, int duration) {
        this.userId = userId;
        this.taskId = taskId;
        this.subjectId = subjectId;
        this.type = type;
        this.duration = duration;
        this.completedAt = null;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    // Session control methods
    public void startSession() {
        // Implementation for starting session
    }

    public void pauseSession() {
        // Implementation for pausing session
    }

    public void endSession() {
        this.completedAt = LocalDateTime.now();
    }
}
