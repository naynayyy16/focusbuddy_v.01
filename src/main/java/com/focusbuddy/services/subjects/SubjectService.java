package com.focusbuddy.services.subjects;

import com.focusbuddy.database.DatabaseManager;
import com.focusbuddy.models.Subject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SubjectService {
    
    public List<Subject> getSubjectsForUser(int userId) {
        List<Subject> subjects = new ArrayList<>();
        String query = "SELECT * FROM subjects WHERE user_id = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Subject subject = new Subject();
                subject.setId(rs.getInt("id"));
                subject.setUserId(rs.getInt("user_id"));
                subject.setName(rs.getString("name"));
                subject.setColor(rs.getString("color"));
                subjects.add(subject);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return subjects;
    }
    
    public int getSubjectIdByName(int userId, String name) {
        String query = "SELECT id FROM subjects WHERE user_id = ? AND name = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            stmt.setString(2, name);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
    
    public boolean addSubject(Subject subject) {
        String query = "INSERT INTO subjects (user_id, name, color) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, subject.getUserId());
            stmt.setString(2, subject.getName());
            stmt.setString(3, subject.getColor());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updateSubject(Subject subject) {
        String query = "UPDATE subjects SET name = ?, color = ? WHERE id = ? AND user_id = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, subject.getName());
            stmt.setString(2, subject.getColor());
            stmt.setInt(3, subject.getId());
            stmt.setInt(4, subject.getUserId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean deleteSubject(int subjectId, int userId) {
        String query = "DELETE FROM subjects WHERE id = ? AND user_id = ?";
        
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, subjectId);
            stmt.setInt(2, userId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
