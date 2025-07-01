package com.focusbuddy.utils;

import com.focusbuddy.database.DatabaseManager;
import com.focusbuddy.utils.security.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseMigration {
    
    public static void migratePasswords() {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            // 1. Ambil semua user yang masih menggunakan MD5
            String selectQuery = "SELECT id, username, password FROM users WHERE salt = 'dummy_salt'";
            PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
            ResultSet rs = selectStmt.executeQuery();

            while (rs.next()) {
                int userId = rs.getInt("id");
                String username = rs.getString("username");
                
                // 2. Generate salt baru
                String newSalt = PasswordUtils.generateSalt();
                
                // 3. Hash password default (123456) dengan salt baru
                String newHashedPassword = PasswordUtils.hashPassword("123456", newSalt);
                
                // 4. Update password dan salt
                String updateQuery = "UPDATE users SET password = ?, salt = ? WHERE id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setString(1, newHashedPassword);
                updateStmt.setString(2, newSalt);
                updateStmt.setInt(3, userId);
                updateStmt.executeUpdate();
                
                System.out.println("Migrated password for user: " + username);
            }
            
            System.out.println("Password migration completed successfully!");
            
        } catch (SQLException e) {
            System.err.println("Error during password migration: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
