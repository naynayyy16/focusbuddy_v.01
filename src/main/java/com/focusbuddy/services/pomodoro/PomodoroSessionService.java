package com.focusbuddy.services.pomodoro;

import com.focusbuddy.database.DatabaseManager;
import com.focusbuddy.models.pomodoro.PomodoroSession;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PomodoroSessionService {

    public List<PomodoroSession> getSessionsForUser(int userId) {
        List<PomodoroSession> sessions = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String query = "SELECT * FROM pomodoro_sessions WHERE user_id = ? ORDER BY completed_at DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                PomodoroSession session = new PomodoroSession();
                session.setId(rs.getInt("id"));
                session.setUserId(rs.getInt("user_id"));
                session.setTaskId(rs.getInt("task_id"));
                session.setSubjectId(rs.getInt("subject_id"));
                session.setType(PomodoroSession.Type.valueOf(rs.getString("session_type")));
                session.setDuration(rs.getInt("duration_minutes"));
                Timestamp completedAt = rs.getTimestamp("completed_at");
                if (completedAt != null) {
                    session.setCompletedAt(completedAt.toLocalDateTime());
                }
                sessions.add(session);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sessions;
    }

    public boolean addSession(PomodoroSession session) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String query = "INSERT INTO pomodoro_sessions (user_id, task_id, subject_id, session_type, duration_minutes, completed_at) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);

            stmt.setInt(1, session.getUserId());
            stmt.setInt(2, session.getTaskId());
            stmt.setInt(3, session.getSubjectId());
            stmt.setString(4, session.getType().name());
            stmt.setInt(5, session.getDuration());
            if (session.getCompletedAt() != null) {
                stmt.setTimestamp(6, Timestamp.valueOf(session.getCompletedAt()));
            } else {
                stmt.setNull(6, Types.TIMESTAMP);
            }

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateSession(PomodoroSession session) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String query = "UPDATE pomodoro_sessions SET task_id = ?, subject_id = ?, session_type = ?, duration_minutes = ?, completed_at = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);

            stmt.setInt(1, session.getTaskId());
            stmt.setInt(2, session.getSubjectId());
            stmt.setString(3, session.getType().name());
            stmt.setInt(4, session.getDuration());
            if (session.getCompletedAt() != null) {
                stmt.setTimestamp(5, Timestamp.valueOf(session.getCompletedAt()));
            } else {
                stmt.setNull(5, Types.TIMESTAMP);
            }
            stmt.setInt(6, session.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSession(int sessionId) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            String query = "DELETE FROM pomodoro_sessions WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, sessionId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
