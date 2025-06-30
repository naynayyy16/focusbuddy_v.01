package com.focusbuddy.controllers.pomodoro;

import com.focusbuddy.models.PomodoroSession;
import com.focusbuddy.services.PomodoroSessionService;
import com.focusbuddy.utils.NotificationManager;
import com.focusbuddy.utils.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDateTime;
import java.util.List;

public class PomodoroSessionController {

    @FXML private ListView<PomodoroSession> sessionListView;
    @FXML private Button startSessionButton;
    @FXML private Button pauseSessionButton;
    @FXML private Button endSessionButton;

    private PomodoroSessionService sessionService;
    private ObservableList<PomodoroSession> sessions;
    private PomodoroSession currentSession;

    @FXML
    private void initialize() {
        sessionService = new PomodoroSessionService();
        sessions = FXCollections.observableArrayList();

        sessionListView.setItems(sessions);
        sessionListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(PomodoroSession session, boolean empty) {
                super.updateItem(session, empty);
                if (empty || session == null) {
                    setText(null);
                } else {
                    setText("Session: " + session.getType() + ", Duration: " + session.getDuration() + " mins, Completed: " + (session.getCompletedAt() != null ? session.getCompletedAt() : "No"));
                }
            }
        });

        loadSessions();

        startSessionButton.setOnAction(e -> startSession());
        pauseSessionButton.setOnAction(e -> pauseSession());
        endSessionButton.setOnAction(e -> endSession());
    }

    private void loadSessions() {
        int userId = UserSession.getInstance().getCurrentUser().getId();
        List<PomodoroSession> sessionList = sessionService.getSessionsForUser(userId);
        sessions.setAll(sessionList);
    }

    private void startSession() {
        if (currentSession == null) {
            currentSession = new PomodoroSession();
            currentSession.setUserId(UserSession.getInstance().getCurrentUser().getId());
            currentSession.setType(PomodoroSession.Type.FOCUS);
            currentSession.setDuration(25);
        }
        currentSession.startSession();
        NotificationManager.getInstance().showNotification("Pomodoro", "Session started", NotificationManager.NotificationType.INFO);
    }

    private void pauseSession() {
        if (currentSession != null) {
            currentSession.pauseSession();
            NotificationManager.getInstance().showNotification("Pomodoro", "Session paused", NotificationManager.NotificationType.INFO);
        }
    }

    private void endSession() {
        if (currentSession != null) {
            currentSession.endSession();
            sessionService.addSession(currentSession);
            NotificationManager.getInstance().showNotification("Pomodoro", "Session ended", NotificationManager.NotificationType.SUCCESS);
            currentSession = null;
            loadSessions();
        }
    }
}
