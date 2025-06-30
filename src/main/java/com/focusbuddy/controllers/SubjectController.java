package com.focusbuddy.controllers;

import com.focusbuddy.models.Subject;
import com.focusbuddy.services.SubjectService;
import com.focusbuddy.utils.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;

public class SubjectController {

    @FXML private VBox subjectContainer;
    @FXML private ListView<Subject> subjectList;
    @FXML private TextField subjectNameField;
    @FXML private ColorPicker subjectColorPicker;
    @FXML private Button saveSubjectButton;
    @FXML private Button newSubjectButton;
    @FXML private Button deleteSubjectButton;

    private SubjectService subjectService;
    private ObservableList<Subject> subjects;
    private Subject currentSubject;

    @FXML
    private void initialize() {
        subjectService = new SubjectService();
        subjects = FXCollections.observableArrayList();

        subjectList.setItems(subjects);
        subjectList.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Subject subject, boolean empty) {
                super.updateItem(subject, empty);
                if (empty || subject == null) {
                    setText(null);
                } else {
                    setText(subject.getName());
                }
            }
        });

        subjectList.getSelectionModel().selectedItemProperty().addListener((obs, oldSubject, newSubject) -> {
            if (newSubject != null) {
                loadSubjectDetails(newSubject);
            }
        });

        loadSubjects();

        newSubjectButton.setOnAction(e -> createNewSubject());
        saveSubjectButton.setOnAction(e -> saveCurrentSubject());
        deleteSubjectButton.setOnAction(e -> deleteCurrentSubject());
    }

    private void loadSubjects() {
        int userId = UserSession.getInstance().getCurrentUser().getId();
        List<Subject> subjectListData = subjectService.getSubjectsForUser(userId);
        subjects.setAll(subjectListData);
    }

    private void loadSubjectDetails(Subject subject) {
        currentSubject = subject;
        subjectNameField.setText(subject.getName());
        subjectColorPicker.setValue(javafx.scene.paint.Color.web(subject.getColor()));
    }

    private void createNewSubject() {
        currentSubject = new Subject();
        currentSubject.setUserId(UserSession.getInstance().getCurrentUser().getId());
        subjectNameField.clear();
        subjectColorPicker.setValue(javafx.scene.paint.Color.WHITE);
        subjectList.getSelectionModel().clearSelection();
    }

    private void saveCurrentSubject() {
        if (currentSubject == null) {
            createNewSubject();
        }

        String name = subjectNameField.getText().trim();
        String color = toHexString(subjectColorPicker.getValue());

        if (name.isEmpty()) {
            showAlert("Validation Error", "Subject name cannot be empty.");
            return;
        }

        currentSubject.setName(name);
        currentSubject.setColor(color);

        boolean success;
        if (currentSubject.getId() == 0) {
            success = subjectService.addSubject(currentSubject);
        } else {
            success = subjectService.updateSubject(currentSubject);
        }

        if (success) {
            showAlert("Success", "Subject saved successfully.");
            loadSubjects();
        } else {
            showAlert("Error", "Failed to save subject.");
        }
    }

    private void deleteCurrentSubject() {
        if (currentSubject == null || currentSubject.getId() == 0) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Subject");
        alert.setHeaderText("Are you sure you want to delete this subject?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                if (subjectService.deleteSubject(currentSubject.getId(), currentSubject.getUserId())) {
                    showAlert("Success", "Subject deleted successfully.");
                    loadSubjects();
                    createNewSubject();
                }
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String toHexString(javafx.scene.paint.Color color) {
        return String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }
}
