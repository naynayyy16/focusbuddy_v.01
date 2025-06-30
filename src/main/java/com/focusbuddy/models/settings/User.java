package com.focusbuddy.models.settings;

import java.sql.Timestamp;

public class User {
    private int id;
    private String username;
    private String email;
    private String password;
    private String avatarUrl;
    private int level;
    private int totalXp;
    private Timestamp createdAt;
    
    // User Preferences
    private boolean taskNotificationsEnabled = true;
    private boolean pomodoroNotificationsEnabled = true;
    private boolean reminderNotificationsEnabled = true;
    private int focusDuration = 25;
    private int breakDuration = 5;

    public User() {
        this.level = 1;
        this.totalXp = 0;
    }

    // New getters and setters for profile settings
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public boolean isTaskNotificationsEnabled() { return taskNotificationsEnabled; }
    public void setTaskNotificationsEnabled(boolean enabled) { this.taskNotificationsEnabled = enabled; }

    public boolean isPomodoroNotificationsEnabled() { return pomodoroNotificationsEnabled; }
    public void setPomodoroNotificationsEnabled(boolean enabled) { this.pomodoroNotificationsEnabled = enabled; }

    public boolean isReminderNotificationsEnabled() { return reminderNotificationsEnabled; }
    public void setReminderNotificationsEnabled(boolean enabled) { this.reminderNotificationsEnabled = enabled; }

    public int getFocusDuration() { return focusDuration; }
    public void setFocusDuration(int duration) { this.focusDuration = duration; }

    public int getBreakDuration() { return breakDuration; }
    public void setBreakDuration(int duration) { this.breakDuration = duration; }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getTotalXp() {
        return totalXp;
    }

    public void setTotalXp(int totalXp) {
        this.totalXp = totalXp;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void login() {
        // Implementation for login
    }

    public void register() {
        // Implementation for register
    }

    public void updateProfile() {
        // Implementation for updating profile
    }

    public void getTasks() {
        // Implementation for getting tasks
    }

    public void getSubjects() {
        // Implementation for getting subjects
    }

    public void getNotes() {
        // Implementation for getting notes
    }

    public void getPomodoroSessions() {
        // Implementation for getting pomodoro sessions
    }
}
