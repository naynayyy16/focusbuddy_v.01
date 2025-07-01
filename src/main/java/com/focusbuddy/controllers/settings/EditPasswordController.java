package com.focusbuddy.controllers.settings;

import com.focusbuddy.database.DatabaseManager;
import com.focusbuddy.utils.security.PasswordUtils;
import com.focusbuddy.utils.session.UserSession;
import com.focusbuddy.utils.validation.ValidationUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EditPasswordController {
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        // Tambahkan listener untuk validasi real-time
        newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!ValidationUtils.isValidPassword(newVal)) {
                showStatus("Password minimal 6 karakter", "warning");
            } else {
                statusLabel.setText("");
            }
        });

        // Validasi konfirmasi password
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.equals(newPasswordField.getText())) {
                showStatus("Password tidak cocok", "warning");
            } else {
                statusLabel.setText("");
            }
        });
    }

    @FXML
    private void handleSave() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validasi input
        if (!ValidationUtils.isNotEmpty(currentPassword) || 
            !ValidationUtils.isNotEmpty(newPassword) || 
            !ValidationUtils.isNotEmpty(confirmPassword)) {
            showStatus("Mohon isi semua field", "error");
            return;
        }

        if (!ValidationUtils.isValidPassword(newPassword)) {
            showStatus("Password baru minimal 6 karakter", "error");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showStatus("Password baru tidak cocok", "error");
            return;
        }

        // Verifikasi password saat ini
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            int userId = UserSession.getInstance().getCurrentUser().getId();
            String query = "SELECT password, salt FROM users WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                String storedSalt = rs.getString("salt");

                boolean isCurrentPasswordValid;
                if (storedSalt != null && !storedSalt.equals("dummy_salt")) {
                    // Verifikasi dengan salt
                    isCurrentPasswordValid = PasswordUtils.verifyPassword(currentPassword, storedPassword, storedSalt);
                } else {
                    // Fallback untuk password lama (MD5)
                    isCurrentPasswordValid = currentPassword.equals(storedPassword);
                }

                if (!isCurrentPasswordValid) {
                    showStatus("Password saat ini salah", "error");
                    return;
                }

                // Generate salt baru dan hash password baru
                String newSalt = PasswordUtils.generateSalt();
                String hashedNewPassword = PasswordUtils.hashPassword(newPassword, newSalt);

                // Update password di database
                String updateQuery = "UPDATE users SET password = ?, salt = ? WHERE id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setString(1, hashedNewPassword);
                updateStmt.setString(2, newSalt);
                updateStmt.setInt(3, userId);
                updateStmt.executeUpdate();

                showStatus("Password berhasil diubah!", "success");
                
                // Tunggu sebentar sebelum menutup window
                new Thread(() -> {
                    try {
                        Thread.sleep(1500);
                        javafx.application.Platform.runLater(this::closeWindow);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();

            } else {
                showStatus("Terjadi kesalahan: User tidak ditemukan", "error");
            }

        } catch (Exception e) {
            showStatus("Terjadi kesalahan: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void showStatus(String message, String type) {
        statusLabel.setText(message);
        switch (type) {
            case "success":
                statusLabel.setTextFill(Color.valueOf("#00B894"));
                break;
            case "error":
                statusLabel.setTextFill(Color.valueOf("#D63031"));
                break;
            case "warning":
                statusLabel.setTextFill(Color.valueOf("#FDCB6E"));
                break;
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) currentPasswordField.getScene().getWindow();
        stage.close();
    }
}
