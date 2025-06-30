package com.focusbuddy.utils.validation;

import java.util.regex.Pattern;
import java.time.LocalDate;
import java.time.LocalTime;

public class ValidationUtils {
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    // Password requirements:
    // - At least 8 characters
    // - Contains at least one digit
    // - Contains at least one lowercase letter
    // - Contains at least one uppercase letter
    // - Contains at least one special character
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
    );

    // Username requirements:
    // - 3-20 characters
    // - Letters, numbers, underscores, hyphens
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[A-Za-z0-9_-]{3,20}$"
    );

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username).matches();
    }

    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty() && name.length() >= 2 && name.length() <= 50;
    }

    public static boolean isValidTaskTitle(String title) {
        return title != null && !title.trim().isEmpty() && title.length() <= 100;
    }

    public static boolean isValidTaskDescription(String description) {
        return description == null || description.length() <= 500;
    }

    public static boolean isValidDueDate(LocalDate dueDate) {
        if (dueDate == null) {
            return false;
        }
        return !dueDate.isBefore(LocalDate.now());
    }

    public static boolean isValidPomodoroSettings(int focusDuration, int breakDuration) {
        return focusDuration >= 1 && focusDuration <= 60 &&
               breakDuration >= 1 && breakDuration <= 30;
    }

    public static boolean isValidTime(LocalTime time) {
        return time != null;
    }

    public static boolean isValidSubjectName(String name) {
        return name != null && !name.trim().isEmpty() && name.length() <= 50;
    }

    public static boolean isValidNoteTitle(String title) {
        return title != null && !title.trim().isEmpty() && title.length() <= 100;
    }

    public static boolean isValidNoteContent(String content) {
        return content != null && content.length() <= 10000; // 10KB limit
    }

    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        // Remove any HTML tags
        input = input.replaceAll("<[^>]*>", "");
        // Remove any script tags and their contents
        input = input.replaceAll("<script\\b[^<]*(?:(?!</script>)<[^<]*)*</script>", "");
        // Remove any potentially dangerous characters
        input = input.replaceAll("[\\\\\"'`]", "");
        return input.trim();
    }

    public static String getPasswordRequirements() {
        return "Password harus memenuhi kriteria berikut:\n" +
               "- Minimal 8 karakter\n" +
               "- Mengandung minimal 1 angka\n" +
               "- Mengandung minimal 1 huruf kecil\n" +
               "- Mengandung minimal 1 huruf besar\n" +
               "- Mengandung minimal 1 karakter spesial (@#$%^&+=!)";
    }

    public static String getUsernameRequirements() {
        return "Username harus memenuhi kriteria berikut:\n" +
               "- Panjang 3-20 karakter\n" +
               "- Hanya boleh mengandung huruf, angka, underscore, dan hyphen";
    }

    public static String getErrorMessage(String fieldName, String requirement) {
        return String.format("%s tidak valid. %s", fieldName, requirement);
    }
}
