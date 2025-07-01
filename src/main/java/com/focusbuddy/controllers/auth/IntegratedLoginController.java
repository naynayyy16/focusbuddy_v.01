package com.focusbuddy.controllers.auth;

import com.focusbuddy.database.DatabaseManager;
import com.focusbuddy.models.settings.User;
import com.focusbuddy.utils.ThemeManager;
import com.focusbuddy.utils.session.UserSession;
import com.focusbuddy.utils.validation.ValidationUtils;
import com.focusbuddy.utils.security.PasswordUtils;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IntegratedLoginController {
    @FXML private VBox mainContainer;
    @FXML private VBox loginForm;
    @FXML private VBox registerForm;
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label loginStatusLabel;
    
    @FXML private TextField regUsernameField;
    @FXML private PasswordField regPasswordField;
    @FXML private TextField regEmailField;
    @FXML private Label registerStatusLabel;
    
    @FXML private ToggleButton themeToggle;

    @FXML
    private void initialize() {
        // Set up theme toggle
        themeToggle.setOnAction(e -> toggleTheme());
        
        // Initially show login form
        loginForm.setVisible(true);
        loginForm.setManaged(true);
        registerForm.setVisible(false);
        registerForm.setManaged(false);
        
        // Add Enter key listeners
        passwordField.setOnAction(e -> handleLogin());
        usernameField.setOnAction(e -> passwordField.requestFocus());
        
        regPasswordField.setOnAction(e -> handleRegister());
        regUsernameField.setOnAction(e -> regPasswordField.requestFocus());
        regEmailField.setOnAction(e -> handleRegister());
    }

    private void toggleTheme() {
        Scene scene = mainContainer.getScene();
        ThemeManager.getInstance().toggleTheme(scene);
        themeToggle.setText(ThemeManager.getInstance().getCurrentTheme() == ThemeManager.Theme.DARK ? "🌙" : "☀️");
    }

    @FXML
    private void showRegisterForm() {
        fadeTransition(loginForm, false, () -> {
            loginForm.setVisible(false);
            loginForm.setManaged(false);
            registerForm.setVisible(true);
            registerForm.setManaged(true);
            fadeTransition(registerForm, true, null);
        });
    }

    @FXML
    private void showLoginForm() {
        fadeTransition(registerForm, false, () -> {
            registerForm.setVisible(false);
            registerForm.setManaged(false);
            loginForm.setVisible(true);
            loginForm.setManaged(true);
            fadeTransition(loginForm, true, null);
        });
    }

    private void fadeTransition(VBox form, boolean in, Runnable onFinished) {
        FadeTransition fade = new FadeTransition(Duration.millis(300), form);
        fade.setFromValue(in ? 0 : 1);
        fade.setToValue(in ? 1 : 0);
        if (onFinished != null) {
            fade.setOnFinished(e -> onFinished.run());
        }
        fade.play();
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validasi input
        if (!ValidationUtils.isNotEmpty(username) || !ValidationUtils.isNotEmpty(password)) {
            showLoginStatus("Mohon isi semua field", false);
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
                if (salt != null) {
                    // Verifikasi dengan salt
                    passwordValid = PasswordUtils.verifyPassword(password, storedPassword, salt);
                } else {
                    // Verifikasi plain text (untuk kompatibilitas)
                    passwordValid = password.equals(storedPassword);
                    
                    // Upgrade ke password dengan hash jika valid
                    if (passwordValid) {
                        String newSalt = PasswordUtils.generateSalt();
                        String hashedPassword = PasswordUtils.hashPassword(password, newSalt);
                        
                        String updateQuery = "UPDATE users SET password = ?, salt = ? WHERE id = ?";
                        PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                        updateStmt.setString(1, hashedPassword);
                        updateStmt.setString(2, newSalt);
                        updateStmt.setInt(3, rs.getInt("id"));
                        updateStmt.executeUpdate();
                    }
                }

                if (passwordValid) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setLevel(rs.getInt("level"));
                    user.setTotalXp(rs.getInt("total_xp"));
                    
                    UserSession.getInstance().setCurrentUser(user);
                    navigateToMainDashboard();
                } else {
                    showLoginStatus("Username atau password salah", false);
                }
            } else {
                showLoginStatus("Username atau password salah", false);
            }
        } catch (Exception e) {
            showLoginStatus("Login gagal: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister() {
        String username = regUsernameField.getText().trim();
        String password = regPasswordField.getText();
        String email = regEmailField.getText().trim();

        // Validasi input
        if (!ValidationUtils.isValidUsername(username)) {
            showRegisterStatus("Username harus 3-20 karakter, huruf, angka, dan underscore", false);
            return;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            showRegisterStatus("Password minimal 6 karakter", false);
            return;
        }

        if (!email.isEmpty() && !ValidationUtils.isValidEmail(email)) {
            showRegisterStatus("Format email tidak valid", false);
            return;
        }

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            // Generate salt dan hash password
            String salt = PasswordUtils.generateSalt();
            String hashedPassword = PasswordUtils.hashPassword(password, salt);

            String query = "INSERT INTO users (username, password, salt, email) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, salt);
            stmt.setString(4, email);

            int result = stmt.executeUpdate();
            if (result > 0) {
                showRegisterStatus("Registrasi berhasil! Silakan login.", true);
                // Reset form dan kembali ke login
                regUsernameField.clear();
                regPasswordField.clear();
                regEmailField.clear();
                showLoginForm();
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                showRegisterStatus("Username sudah digunakan. Silakan pilih username lain.", false);
            } else {
                showRegisterStatus("Registrasi gagal: " + e.getMessage(), false);
            }
            e.printStackTrace();
        }
    }

    private void navigateToMainDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/new_dashboard.fxml"));
            Stage stage = (Stage) mainContainer.getScene().getWindow();
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(loader.load());
            
            ThemeManager.getInstance().applyTheme(scene, ThemeManager.getInstance().getCurrentTheme());
            
            stage.setScene(scene);
            stage.setTitle("FocusBuddy");
            stage.setMaximized(true);
            stage.setResizable(true);
            stage.setMinWidth(1200);
            stage.setMinHeight(800);
            
            if (!stage.isMaximized()) {
                stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
                stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
            }
        } catch (Exception e) {
            showLoginStatus("Gagal memuat dashboard: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }

    private void showLoginStatus(String message, boolean isSuccess) {
        loginStatusLabel.setText(message);
        loginStatusLabel.setStyle(isSuccess ? "-fx-text-fill: #4CAF50;" : "-fx-text-fill: #F44336;");
    }

    private void showRegisterStatus(String message, boolean isSuccess) {
        registerStatusLabel.setText(message);
        registerStatusLabel.setStyle(isSuccess ? "-fx-text-fill: #4CAF50;" : "-fx-text-fill: #F44336;");
    }
}
