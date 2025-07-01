package com.focusbuddy.utils;

import com.focusbuddy.database.DatabaseManager;
import com.focusbuddy.utils.security.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabasePasswordUpdater {
    public static void updatePasswords() {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            // Update password untuk user yang ada menjadi '123456' dengan SHA-256
            String hashedPassword = "jZae727K08KaOmKSgOaGzww/XVqGr/PKEgIMkjrcbJI="; // SHA-256 hash untuk '123456'
            
            String query = "UPDATE users SET password = ?, salt = NULL WHERE username IN (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, hashedPassword);
            stmt.setString(2, "john_doe");
            stmt.setString(3, "jane_smith");
            stmt.setString(4, "bob_wilson");
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Password updated for " + rowsAffected + " users");
            
        } catch (SQLException e) {
            System.err.println("Error updating passwords: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
