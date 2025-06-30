package com.focusbuddy.utils;

import com.focusbuddy.models.User;
import java.util.prefs.Preferences;
import java.time.LocalDateTime;
import java.time.Duration;

public class UserSession {
    private static UserSession instance;
    private User currentUser;
    private LocalDateTime loginTime;
    private final Preferences prefs;
    private static final String LAST_USERNAME_KEY = "last_username";
    private static final String REMEMBER_ME_KEY = "remember_me";
    private static final int SESSION_TIMEOUT_MINUTES = 60; // 1 hour session timeout

    private UserSession() {
        prefs = Preferences.userNodeForPackage(UserSession.class);
        loginTime = LocalDateTime.now();
    }

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void login(User user, boolean rememberMe) {
        this.currentUser = user;
        this.loginTime = LocalDateTime.now();
        
        if (rememberMe) {
            saveUserPreferences(user.getUsername());
        } else {
            clearUserPreferences();
        }

        // Log successful login
        ErrorHandler.logInfo("User logged in: " + user.getUsername());
        NotificationManager.getInstance().showSuccess("Selamat datang, " + user.getUsername() + "!");
    }

    public void logout() {
        if (currentUser != null) {
            ErrorHandler.logInfo("User logged out: " + currentUser.getUsername());
        }
        
        this.currentUser = null;
        this.loginTime = null;
    }

    public User getCurrentUser() {
        if (currentUser == null) {
            return null;
        }

        // Check session timeout
        if (isSessionExpired()) {
            logout();
            NotificationManager.getInstance().showWarning("Sesi Anda telah berakhir. Silakan login kembali.");
            return null;
        }

        return currentUser;
    }

    public boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    private boolean isSessionExpired() {
        if (loginTime == null) {
            return true;
        }

        Duration sessionDuration = Duration.between(loginTime, LocalDateTime.now());
        return sessionDuration.toMinutes() > SESSION_TIMEOUT_MINUTES;
    }

    public void refreshSession() {
        if (isLoggedIn()) {
            this.loginTime = LocalDateTime.now();
        }
    }

    public void saveUserPreferences(String username) {
        prefs.put(LAST_USERNAME_KEY, username);
        prefs.putBoolean(REMEMBER_ME_KEY, true);
    }

    public void clearUserPreferences() {
        prefs.remove(LAST_USERNAME_KEY);
        prefs.putBoolean(REMEMBER_ME_KEY, false);
    }

    public String getLastUsername() {
        return prefs.get(LAST_USERNAME_KEY, "");
    }

    public boolean isRememberMe() {
        return prefs.getBoolean(REMEMBER_ME_KEY, false);
    }

    public void updateUserProfile(User updatedUser) {
        if (currentUser != null && currentUser.getId().equals(updatedUser.getId())) {
            this.currentUser = updatedUser;
            ErrorHandler.logInfo("User profile updated: " + updatedUser.getUsername());
            NotificationManager.getInstance().showSuccess("Profil berhasil diperbarui!");
        }
    }

    public void updateUserPreferences(boolean darkMode, boolean notifications) {
        if (currentUser != null) {
            // Save user preferences (implement as needed)
            ErrorHandler.logInfo("User preferences updated for: " + currentUser.getUsername());
        }
    }

    public Duration getSessionDuration() {
        if (loginTime == null) {
            return Duration.ZERO;
        }
        return Duration.between(loginTime, LocalDateTime.now());
    }

    public String getSessionInfo() {
        if (!isLoggedIn()) {
            return "Tidak ada sesi aktif";
        }

        Duration duration = getSessionDuration();
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();

        return String.format("Sesi aktif: %d jam %d menit", hours, minutes);
    }
}
