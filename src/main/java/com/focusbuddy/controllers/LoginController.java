package com.focusbuddy.controllers;

import com.focusbuddy.database.DatabaseManager;
import com.focusbuddy.models.settings.User;
import com.focusbuddy.utils.ThemeManager;
import com.focusbuddy.utils.session.UserSession;
import com.focusbuddy.utils.validation.ValidationUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.focusbuddy.utils.security.*;

public class LoginController {

    // Main containers - adjusted to match FXML
    @FXML private VBox mainContainer;
    @FXML private VBox loginForm;
    @FXML private VBox registerForm;
    @FXML private ToggleButton themeToggle;

    // Login form fields - adjusted to match FXML
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label loginStatusLabel;
    @FXML private Hyperlink showRegisterLink;

    // Register form fields - adjusted to match FXML
    @FXML private TextField regUsernameField;
    @FXML private PasswordField regPasswordField;
    @FXML private TextField regEmailField;
    @FXML private Button registerButton;
    @FXML private Label registerStatusLabel;
    @FXML private Hyperlink showLoginLink;

    @FXML
    private void initialize() {
        setupThemeToggle();
        setupEventHandlers();
        setupValidation();
    }

    private void setupThemeToggle() {
        if (themeToggle != null) {
            // Set initial state
            if (ThemeManager.getInstance().getCurrentTheme() == ThemeManager.Theme.DARK) {
                themeToggle.setText("🌙");
                themeToggle.setSelected(true);
            } else {
                themeToggle.setText("☀️");
                themeToggle.setSelected(false);
            }

            themeToggle.setOnAction(e -> toggleTheme());
        }
    }

    private void setupEventHandlers() {
        // Login button
        if (loginButton != null) {
            loginButton.setOnAction(e -> handleLogin());
        }

        // Register button
        if (registerButton != null) {
            registerButton.setOnAction(e -> handleRegister());
        }

        // Enter key handlers
        if (passwordField != null) {
            passwordField.setOnAction(e -> handleLogin());
        }

        if (usernameField != null) {
            usernameField.setOnAction(e -> {
                if (passwordField != null) {
                    passwordField.requestFocus();
                }
            });
        }

        // Register form enter keys
        if (regPasswordField != null) {
            regPasswordField.setOnAction(e -> handleRegister());
        }

        if (regEmailField != null) {
            regEmailField.setOnAction(e -> handleRegister());
        }
    }

    private void setupValidation() {
        // Login form validation
        if (usernameField != null && passwordField != null && loginButton != null) {
            usernameField.textProperty().addListener((obs, oldText, newText) -> validateLoginForm());
            passwordField.textProperty().addListener((obs, oldText, newText) -> validateLoginForm());
            validateLoginForm(); // Initial validation
        }

        // Register form validation with real-time feedback
        if (regUsernameField != null && regPasswordField != null && regEmailField != null && registerButton != null) {
            regUsernameField.textProperty().addListener((obs, oldText, newText) -> {
                validateRegisterForm();
                showRegisterValidationFeedback();
            });
            regPasswordField.textProperty().addListener((obs, oldText, newText) -> {
                validateRegisterForm();
                showRegisterValidationFeedback();
            });
            regEmailField.textProperty().addListener((obs, oldText, newText) -> {
                validateRegisterForm();
                showRegisterValidationFeedback();
            });
            validateRegisterForm(); // Initial validation
        }
    }

    private void showRegisterValidationFeedback() {
        if (regUsernameField == null || regPasswordField == null || regEmailField == null || registerStatusLabel == null) return;

        String username = regUsernameField.getText().trim();
        String password = regPasswordField.getText();
        String email = regEmailField.getText().trim();

        // Clear status if all fields are empty
        if (username.isEmpty() && password.isEmpty() && email.isEmpty()) {
            registerStatusLabel.setText("");
            return;
        }

        // Show specific validation messages
        if (!username.isEmpty() && username.length() < 3) {
            registerStatusLabel.setText("Username minimal 3 karakter");
            registerStatusLabel.setStyle("-fx-text-fill: #FF9800;"); // Orange for warning
            return;
        }

        if (!password.isEmpty() && password.length() < 6) {
            registerStatusLabel.setText("Password minimal 6 karakter (saat ini: " + password.length() + ")");
            registerStatusLabel.setStyle("-fx-text-fill: #FF9800;"); // Orange for warning
            return;
        }

        if (!email.isEmpty() && (!email.contains("@") || !email.contains("."))) {
            registerStatusLabel.setText("Format email tidak valid");
            registerStatusLabel.setStyle("-fx-text-fill: #FF9800;"); // Orange for warning
            return;
        }

        // Show success if all validations pass
        if (username.length() >= 3 && password.length() >= 6 && email.contains("@") && email.contains(".")) {
            registerStatusLabel.setText("Siap untuk registrasi!");
            registerStatusLabel.setStyle("-fx-text-fill: #4CAF50;"); // Green for success
        } else {
            registerStatusLabel.setText("");
        }
    }

    private void validateLoginForm() {
        if (usernameField != null && passwordField != null && loginButton != null) {
            boolean isValid = !usernameField.getText().trim().isEmpty() &&
                    !passwordField.getText().trim().isEmpty();
            loginButton.setDisable(!isValid);
        }
    }

    private void validateRegisterForm() {
        if (regUsernameField != null && regPasswordField != null && regEmailField != null && registerButton != null) {
            String username = regUsernameField.getText().trim();
            String password = regPasswordField.getText();
            String email = regEmailField.getText().trim();

            boolean isValid = username.length() >= 3 &&
                    password.length() >= 6 &&
                    !email.isEmpty() &&
                    email.contains("@") && email.contains(".");
            registerButton.setDisable(!isValid);
        }
    }

    private void toggleTheme() {
        if (mainContainer != null && themeToggle != null) {
            Scene scene = mainContainer.getScene();
            ThemeManager.getInstance().toggleTheme(scene);

            // Update toggle button text
            if (ThemeManager.getInstance().getCurrentTheme() == ThemeManager.Theme.DARK) {
                themeToggle.setText("🌙");
            } else {
                themeToggle.setText("☀️");
            }
        }
    }

    @FXML
    private void handleLogin() {
        if (usernameField == null || passwordField == null) return;

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (!ValidationUtils.isNotEmpty(username) || !ValidationUtils.isNotEmpty(password)) {
            showLoginStatus("Please fill in all fields", false);
            return;
        }

        if (!ValidationUtils.isValidUsername(username)) {
            showLoginStatus("Invalid username format", false);
            return;
        }

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String query = "SELECT * FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                String salt = rs.getString("salt");

                boolean passwordValid;
                if (salt != null && !salt.isEmpty()) {
                    // Use hashed password verification
                    passwordValid = PasswordUtils.verifyPassword(password, storedPassword, salt);
                } else {
                    // Fallback to plain text (for migration)
                    passwordValid = password.equals(storedPassword);
                }

                if (passwordValid) {
                    // Create user object
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setLevel(rs.getInt("level"));
                    user.setTotalXp(rs.getInt("total_xp"));

                    // Set current user session
                    UserSession.getInstance().setCurrentUser(user);

                    showLoginStatus("Login successful!", true);

                    // Navigate to main dashboard
                    navigateToMainDashboard();
                } else {
                    showLoginStatus("Invalid username or password", false);
                    if (passwordField != null) {
                        passwordField.clear();
                        passwordField.requestFocus();
                    }
                }
            } else {
                showLoginStatus("Invalid username or password", false);
                if (passwordField != null) {
                    passwordField.clear();
                    passwordField.requestFocus();
                }
            }

        } catch (Exception e) {
            showLoginStatus("Login failed: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }

    @FXML
    private void showRegisterForm() {
        if (loginForm != null && registerForm != null) {
            loginForm.setVisible(false);
            loginForm.setManaged(false);
            registerForm.setVisible(true);
            registerForm.setManaged(true);

            // Clear register form
            if (regUsernameField != null) regUsernameField.clear();
            if (regPasswordField != null) regPasswordField.clear();
            if (regEmailField != null) regEmailField.clear();
            if (registerStatusLabel != null) registerStatusLabel.setText("");

            if (regUsernameField != null) {
                regUsernameField.requestFocus();
            }
        }
    }

    @FXML
    private void showLoginForm() {
        if (loginForm != null && registerForm != null) {
            registerForm.setVisible(false);
            registerForm.setManaged(false);
            loginForm.setVisible(true);
            loginForm.setManaged(true);

            // Clear login status
            if (loginStatusLabel != null) loginStatusLabel.setText("");

            if (usernameField != null) {
                usernameField.requestFocus();
            }
        }
    }

    @FXML
    private void handleRegister() {
        if (regUsernameField == null || regPasswordField == null || regEmailField == null) return;

        String username = regUsernameField.getText().trim();
        String password = regPasswordField.getText();
        String email = regEmailField.getText().trim();

        // Basic validation with specific messages
        if (username.length() < 3) {
            showRegisterStatus("Username minimal 3 karakter", false);
            return;
        }

        if (username.length() > 20) {
            showRegisterStatus("Username maksimal 20 karakter", false);
            return;
        }

        if (password.length() < 6) {
            showRegisterStatus("Password minimal 6 karakter", false);
            return;
        }

        if (email.isEmpty()) {
            showRegisterStatus("Email harus diisi", false);
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            showRegisterStatus("Format email tidak valid", false);
            return;
        }

        // Additional validation using ValidationUtils if available
        if (!ValidationUtils.isValidUsername(username)) {
            showRegisterStatus("Username hanya boleh huruf, angka, dan underscore", false);
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            showRegisterStatus("Format email tidak valid", false);
            return;
        }

        registerUser(username, password, email);
    }

    private void registerUser(String username, String password, String email) {
        // Final validation before database operation
        if (username.length() < 3 || username.length() > 20) {
            showRegisterStatus("Username harus 3-20 karakter", false);
            return;
        }

        if (password.length() < 6) {
            showRegisterStatus("Password minimal 6 karakter", false);
            return;
        }

        if (email.isEmpty() || !email.contains("@") || !email.contains(".")) {
            showRegisterStatus("Format email tidak valid", false);
            return;
        }

        // Sanitize inputs
        username = ValidationUtils.sanitizeInput(username);
        email = ValidationUtils.sanitizeInput(email);

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            // Check if username already exists
            String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, username);
            ResultSet checkRs = checkStmt.executeQuery();

            if (checkRs.next() && checkRs.getInt(1) > 0) {
                showRegisterStatus("Username sudah digunakan. Pilih username lain.", false);
                return;
            }

            // Generate salt and hash password
            String salt = PasswordUtils.generateSalt();
            String hashedPassword = PasswordUtils.hashPassword(password, salt);

            String query = "INSERT INTO users (username, password, salt, email, level, total_xp, created_at) VALUES (?, ?, ?, ?, 1, 0, NOW())";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, salt);
            stmt.setString(4, email);

            int result = stmt.executeUpdate();
            if (result > 0) {
                showRegisterStatus("Registrasi berhasil! Silakan login.", true);

                // Switch to login form and pre-fill username
                showLoginForm();
                if (usernameField != null) {
                    usernameField.setText(username);
                    if (passwordField != null) {
                        passwordField.requestFocus();
                    }
                }
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                showRegisterStatus("Username sudah digunakan. Pilih username lain.", false);
            } else {
                showRegisterStatus("Registrasi gagal: " + e.getMessage(), false);
            }
            e.printStackTrace();
        }
    }

    private void navigateToMainDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));

            Stage stage = (Stage) mainContainer.getScene().getWindow();

            // Get screen dimensions
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            // Create scene without fixed dimensions
            Scene scene = new Scene(loader.load());

            // Apply current theme
            ThemeManager.getInstance().applyTheme(scene, ThemeManager.getInstance().getCurrentTheme());

            // Set scene
            stage.setScene(scene);
            stage.setTitle("FocusBuddy");

            // Make window maximized and resizable
            stage.setMaximized(true);
            stage.setResizable(true);

            // Set minimum size
            stage.setMinWidth(1200);
            stage.setMinHeight(800);

            // Center window if not maximized
            if (!stage.isMaximized()) {
                stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
                stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
            }

        } catch (Exception e) {
            showLoginStatus("Failed to load dashboard: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }

    private void showLoginStatus(String message, boolean isSuccess) {
        if (loginStatusLabel != null) {
            loginStatusLabel.setText(message);
            loginStatusLabel.setStyle(isSuccess ? "-fx-text-fill: #4CAF50;" : "-fx-text-fill: #F44336;");
        }
    }

    private void showRegisterStatus(String message, boolean isSuccess) {
        if (registerStatusLabel != null) {
            registerStatusLabel.setText(message);
            registerStatusLabel.setStyle(isSuccess ? "-fx-text-fill: #4CAF50;" : "-fx-text-fill: #F44336;");
        }
    }
}