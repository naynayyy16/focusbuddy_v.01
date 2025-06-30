package com.focusbuddy.models.settings;

import java.sql.Timestamp;

public class User {
    private Integer id;
    private String name;
    private String username;
    private String email;
    private String password;
    private String avatarUrl;
    private int level;
    private int totalXp;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // User Preferences
    private boolean taskNotificationsEnabled = true;
    private boolean pomodoroNotificationsEnabled = true;
    private boolean reminderNotificationsEnabled = true;
    private int focusDuration = 25;
    private int breakDuration = 5;
    private boolean darkModeEnabled = false;
    private boolean soundEnabled = true;

    public User() {
        this.level = 1;
        this.totalXp = 0;
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public User(String name, String username, String email, String password) {
        this();
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Basic getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        updateTimestamp();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        updateTimestamp();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        updateTimestamp();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        updateTimestamp();
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        updateTimestamp();
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
        updateTimestamp();
    }

    public int getTotalXp() {
        return totalXp;
    }

    public void setTotalXp(int totalXp) {
        this.totalXp = totalXp;
        updateTimestamp();
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Notification preferences
    public boolean isTaskNotificationsEnabled() {
        return taskNotificationsEnabled;
    }

    public void setTaskNotificationsEnabled(boolean enabled) {
        this.taskNotificationsEnabled = enabled;
        updateTimestamp();
    }

    public boolean isPomodoroNotificationsEnabled() {
        return pomodoroNotificationsEnabled;
    }

    public void setPomodoroNotificationsEnabled(boolean enabled) {
        this.pomodoroNotificationsEnabled = enabled;
        updateTimestamp();
    }

    public boolean isReminderNotificationsEnabled() {
        return reminderNotificationsEnabled;
    }

    public void setReminderNotificationsEnabled(boolean enabled) {
        this.reminderNotificationsEnabled = enabled;
        updateTimestamp();
    }

    // Pomodoro settings
    public int getFocusDuration() {
        return focusDuration;
    }

    public void setFocusDuration(int duration) {
        this.focusDuration = duration;
        updateTimestamp();
    }

    public int getBreakDuration() {
        return breakDuration;
    }

    public void setBreakDuration(int duration) {
        this.breakDuration = duration;
        updateTimestamp();
    }

    // App preferences
    public boolean isDarkModeEnabled() {
        return darkModeEnabled;
    }

    public void setDarkModeEnabled(boolean enabled) {
        this.darkModeEnabled = enabled;
        updateTimestamp();
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        updateTimestamp();
    }

    // Utility methods
    private void updateTimestamp() {
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public String getDisplayName() {
        if (name != null && !name.trim().isEmpty()) {
            return name;
        }
        return username;
    }

    public String getInitials() {
        String displayName = getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            return "U";
        }

        String[] parts = displayName.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
        } else {
            return displayName.substring(0, Math.min(2, displayName.length())).toUpperCase();
        }
    }

    public boolean hasAvatar() {
        return avatarUrl != null && !avatarUrl.trim().isEmpty();
    }

    public int getXpForNextLevel() {
        return level * 100; // Each level requires level * 100 XP
    }

    public int getXpProgress() {
        int xpForCurrentLevel = (level - 1) * 100;
        int xpForNextLevel = level * 100;
        return totalXp - xpForCurrentLevel;
    }

    public double getXpProgressPercentage() {
        int xpForCurrentLevel = (level - 1) * 100;
        int xpForNextLevel = level * 100;
        int currentLevelXp = totalXp - xpForCurrentLevel;
        int requiredXp = xpForNextLevel - xpForCurrentLevel;
        return (double) currentLevelXp / requiredXp * 100;
    }

    public void addXp(int xp) {
        this.totalXp += xp;
        checkLevelUp();
        updateTimestamp();
    }

    private void checkLevelUp() {
        int newLevel = (totalXp / 100) + 1;
        if (newLevel > this.level) {
            this.level = newLevel;
        }
    }

    @Override
    public String toString() {
        return String.format("User{id=%d, name='%s', username='%s', email='%s', level=%d, totalXp=%d}",
                id, name, username, email, level, totalXp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    // Business logic methods (these should probably be moved to service classes)
    public void login() {
        // Implementation for login - consider moving to UserService
    }

    public void register() {
        // Implementation for register - consider moving to UserService
    }

    public void updateProfile() {
        // Implementation for updating profile - consider moving to UserService
        updateTimestamp();
    }

    public void getTasks() {
        // Implementation for getting tasks - consider moving to TaskService
    }

    public void getSubjects() {
        // Implementation for getting subjects - consider moving to SubjectService
    }

    public void getNotes() {
        // Implementation for getting notes - consider moving to NoteService
    }

    public void getPomodoroSessions() {
        // Implementation for getting pomodoro sessions - consider moving to PomodoroService
    }
}