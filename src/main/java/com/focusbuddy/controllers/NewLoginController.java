package com.focusbuddy.controllers;

import com.focusbuddy.models.settings.User;
import com.focusbuddy.utils.*;
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
import com.focusbuddy.utils.error.*;
import com.focusbuddy.utils.icon.*;
import com.focusbuddy.utils.config.*;
import com.focusbuddy.utils.notification.*;
import com.focusbuddy.utils.session.UserSession;

public class NewLoginController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label statusLabel;
    @FXML private VBox loginContainer;
    @FXML private CheckBox rememberMeCheckbox;
    @FXML private ToggleButton themeToggle;
    
    @FXML
    private void initialize() {
        try {
            setupThemeToggle();
            setupFields();
            loadSavedCredentials();
            
            // Add enter key handler
            loginContainer.setOnKeyPressed(event -> {
                if (event.getCode().toString().equals("ENTER")) {
                    handleLogin();
                }
            });
            
        } catch (Exception e) {
            ErrorHandler.log("Error initializing login screen", e);
            showError("Terjadi kesalahan saat memuat halaman login");
        }
    }
    
    private void setupThemeToggle() {
        // Set initial theme toggle state
        if (ThemeManager.getInstance().getCurrentTheme() == ThemeManager.Theme.DARK) {
            themeToggle.setText("🌙");
            themeToggle.setSelected(true);
        } else {
            themeToggle.setText("☀️");
            themeToggle.setSelected(false);
        }

        themeToggle.setOnAction(e -> {
            ThemeManager.getInstance().toggleTheme(loginContainer.getScene());
            if (ThemeManager.getInstance().getCurrentTheme() == ThemeManager.Theme.DARK) {
                themeToggle.setText("🌙");
            } else {
                themeToggle.setText("☀️");
            }
        });
    }
    
    private void setupFields() {
        // Add icons to fields using IconManager
        IconManager iconManager = IconManager.getInstance();
        
        usernameField.setPromptText("Nama Pengguna");
        passwordField.setPromptText("Kata Sandi");
        
        // Add validation
        usernameField.textProperty().addListener((obs, oldText, newText) -> {
            validateInput();
        });
        
        passwordField.textProperty().addListener((obs, oldText, newText) -> {
            validateInput();
        });
    }
    
    private void validateInput() {
        boolean isValid = !usernameField.getText().trim().isEmpty() && 
                         !passwordField.getText().trim().isEmpty();
        loginButton.setDisable(!isValid);
    }
    
    private void loadSavedCredentials() {
        String savedUsername = ConfigManager.getInstance().getUserPreference("last_username", "");
        boolean rememberMe = ConfigManager.getInstance().getUserPreference("remember_me", "false").equals("true");
        
        if (rememberMe && !savedUsername.isEmpty()) {
            usernameField.setText(savedUsername);
            rememberMeCheckbox.setSelected(true);
            passwordField.requestFocus();
        } else {
            usernameField.requestFocus();
        }
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        
        // Basic validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("Nama pengguna dan kata sandi harus diisi");
            return;
        }
        
        try {
            // Attempt login
            if (UserSession.getInstance().login(username, password, rememberMeCheckbox.isSelected())) {
                // Login berhasil - UserSession sudah menangani remember me secara internal
                // Tidak perlu save/clear preferences manual karena sudah otomatis

                // Show success message
                showSuccess("Login berhasil!");

                // Navigate to dashboard with animation
                navigateToDashboard();

            } else {
                showError("Nama pengguna atau kata sandi salah");
                passwordField.clear();
                passwordField.requestFocus();
            }
            
        } catch (Exception e) {
            ErrorHandler.log("Error during login", e);
            showError("Terjadi kesalahan saat login");
        }
    }
    
    private void navigateToDashboard() {
        try {
            // Load new dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/new_dashboard.fxml"));
            Scene scene = new Scene(loader.load());
            
            // Apply current theme
            ThemeManager.getInstance().applyTheme(scene, ThemeManager.getInstance().getCurrentTheme());
            
            // Get current stage
            Stage stage = (Stage) loginButton.getScene().getWindow();
            
            // Get screen dimensions
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            
            // Calculate size (90% of screen)
            double width = screenBounds.getWidth() * 0.9;
            double height = screenBounds.getHeight() * 0.9;
            
            // Fade out current scene
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), loginContainer);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                // Update stage
                stage.setScene(scene);
                stage.setTitle("FocusBuddy");
                
                // Set size and position
                stage.setWidth(width);
                stage.setHeight(height);
                stage.setX((screenBounds.getWidth() - width) / 2);
                stage.setY((screenBounds.getHeight() - height) / 2);
                
                // Set minimum size
                stage.setMinWidth(1024);
                stage.setMinHeight(768);
                
                // Fade in new scene
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), scene.getRoot());
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();
            
        } catch (Exception e) {
            ErrorHandler.log("Error navigating to dashboard", e);
            showError("Gagal memuat dashboard");
        }
    }
    
    @FXML
    private void handleRegister() {
        NotificationManager.getInstance().showInfo("Fitur registrasi akan segera hadir!");
    }
    
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #F44336;");
        NotificationManager.getInstance().showError(message);
    }
    
    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #4CAF50;");
        NotificationManager.getInstance().showSuccess(message);
    }
}
