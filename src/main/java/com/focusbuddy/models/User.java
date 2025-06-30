package com.focusbuddy.models;

import java.sql.Timestamp;

public class User {
    private int id;
    private String username;
    private String email;
    private String password;
    private int level;
    private int totalXp;
    private Timestamp createdAt;

    public User() {
        this.level = 1;
        this.totalXp = 0;
    }

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
