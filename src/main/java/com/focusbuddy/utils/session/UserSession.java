package com.focusbuddy.utils.session;

import com.focusbuddy.models.settings.User;
import java.util.prefs.Preferences;
import java.time.LocalDateTime;
import java.time.Duration;
import com.focusbuddy.utils.error.*;
import com.focusbuddy.utils.notification.*;

public class UserSession {
    private static UserSession instance;
    private static User currentUser;
    private static LocalDateTime loginTime;
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

    /**
     * Authenticate and login user
     * @param username the username
     * @param password the password
     * @param rememberMe whether to remember the user
     * @return true if login successful, false otherwise
     */
    public boolean login(String username, String password, boolean rememberMe) {
        // Here you would typically validate credentials against database
        // For now, this is a placeholder - implement actual authentication logic
        User user = authenticateUser(username, password);

        if (user != null) {
            login(user, rememberMe);
            return true;
        }
        return false;
    }

    /**
     * Login with User object
     * @param user the user object
     * @param rememberMe whether to remember the user
     */
    public void login(User user, boolean rememberMe) {
        currentUser = user;
        loginTime = LocalDateTime.now();

        if (rememberMe) {
            saveUserPreferences(user.getUsername());
        } else {
            clearUserPreferences();
        }

        // Log successful login
        ErrorHandler.logInfo("User logged in: " + user.getUsername());
        NotificationManager.getInstance().showSuccess("Selamat datang, " + user.getUsername() + "!");
    }

    /**
     * Placeholder for user authentication
     * Implement this method with actual database/service call
     */
    private User authenticateUser(String username, String password) {
        // TODO: Implement actual authentication logic
        // This should validate credentials against your database
        // Return User object if valid, null if invalid
        return null;
    }

    public void logout() {
        if (currentUser != null) {
            ErrorHandler.logInfo("User logged out: " + currentUser.getUsername());
        }

        currentUser = null;
        loginTime = null;
    }

    /**
     * Set current user directly without full login process
     * @param user the user to set as current user (null to clear)
     */
    public void setCurrentUser(User user) {
        currentUser = user;
        if (user != null) {
            loginTime = LocalDateTime.now();
            ErrorHandler.logInfo("Current user set: " + user.getUsername());
        } else {
            loginTime = null;
            ErrorHandler.logInfo("Current user cleared");
        }
    }

    public static User getCurrentUser() {
        if (currentUser == null) {
            return null;
        }

        // Check session expiration before returning user
        if (isSessionExpired()) {
            performLogout();
            NotificationManager.getInstance().showWarning("Sesi Anda telah berakhir. Silakan login kembali.");
            return null;
        }

        return currentUser;
    }

    /**
     * Static method to perform logout
     */
    public static void performLogout() {
        getInstance().logout();
    }

    public boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    private static boolean isSessionExpired() {
        if (loginTime == null) {
            return true;
        }

        Duration sessionDuration = Duration.between(loginTime, LocalDateTime.now());
        return sessionDuration.toMinutes() > SESSION_TIMEOUT_MINUTES;
    }

    public void refreshSession() {
        if (isLoggedIn()) {
            loginTime = LocalDateTime.now();
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
            currentUser = updatedUser;
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