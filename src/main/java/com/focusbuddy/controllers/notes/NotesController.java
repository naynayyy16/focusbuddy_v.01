package com.focusbuddy.controllers.notes;

import com.focusbuddy.models.Note;
import com.focusbuddy.models.Subject;
import com.focusbuddy.models.notes.*;
import com.focusbuddy.services.NotesService;
import com.focusbuddy.services.SubjectService;
import com.focusbuddy.utils.NotificationManager;
import com.focusbuddy.utils.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;

import java.util.List;

public class NotesController {
    
    @FXML private VBox notesContainer;
    @FXML private ListView<Note> notesList;
    @FXML private TextField noteTitleField;
    @FXML private HTMLEditor noteEditor;
    @FXML private Button saveNoteButton;
    @FXML private Button newNoteButton;
    @FXML private Button deleteNoteButton;
    @FXML private TextField searchField;
    @FXML private TextField tagsField;
    @FXML private ComboBox<String> subjectComboBox;
    
    // Formatting buttons
    @FXML private Button boldButton;
    @FXML private Button italicButton;
    @FXML private Button highlightButton;
    @FXML private ColorPicker highlightColorPicker;
    
    private NotesService notesService;
    private SubjectService subjectService;
    private Note currentNote;
    private ObservableList<String> subjectNames;
    
    @FXML
    private void initialize() {
        notesService = new NotesService();
        subjectService = new SubjectService();
        subjectNames = FXCollections.observableArrayList();
        
        setupNotesList();
        setupButtons();
        setupSearch();
        loadSubjects();
        loadNotes();
    }
    
    private String getSubjectNameById(int subjectId) {
        int userId = UserSession.getInstance().getCurrentUser().getId();
        List<Subject> subjects = subjectService.getSubjectsForUser(userId);
        for (Subject subject : subjects) {
            if (subject.getId() == subjectId) {
                return subject.getName();
            }
        }
        return "Unknown";
    }

    private void setupNotesList() {
        notesList.setCellFactory(listView -> new ListCell<Note>() {
            @Override
            protected void updateItem(Note note, boolean empty) {
                super.updateItem(note, empty);
                if (empty || note == null) {
                    setText(null);
                } else {
                    setText(note.getTitle() + " (" + getSubjectNameById(note.getSubjectId()) + ")");
                }
            }
        });
        
        notesList.getSelectionModel().selectedItemProperty().addListener((obs, oldNote, newNote) -> {
            if (newNote != null) {
                loadNoteContent(newNote);
            }
        });
    }
    
    private void setupButtons() {
        newNoteButton.setOnAction(e -> createNewNote());
        saveNoteButton.setOnAction(e -> saveCurrentNote());
        deleteNoteButton.setOnAction(e -> deleteCurrentNote());
        
        // Formatting buttons
        boldButton.setOnAction(e -> applyBoldFormatting());
        italicButton.setOnAction(e -> applyItalicFormatting());
        highlightButton.setOnAction(e -> applyHighlightFormatting());
    }
    
    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            searchNotes(newText);
        });
    }
    
    private void loadNotes() {
        int userId = UserSession.getInstance().getCurrentUser().getId();
        List<Note> notes = notesService.getNotesForUser(userId);
        notesList.getItems().setAll(notes);
    }
    
    private void loadSubjects() {
        int userId = UserSession.getInstance().getCurrentUser().getId();
        List<Subject> subjects = subjectService.getSubjectsForUser(userId);
        subjectNames.clear();
        for (Subject subject : subjects) {
            subjectNames.add(subject.getName());
        }
        subjectComboBox.setItems(subjectNames);
        if (!subjectNames.isEmpty()) {
            subjectComboBox.setValue(subjectNames.get(0));
        }
    }
    
    private void loadNoteContent(Note note) {
        currentNote = note;
        noteTitleField.setText(note.getTitle());
        noteEditor.setHtmlText(note.getContent());
        tagsField.setText(note.getTags() != null ? note.getTags() : "");
        subjectComboBox.setValue(getSubjectNameById(note.getSubjectId()));
    }
    
    private void createNewNote() {
        currentNote = new Note();
        currentNote.setUserId(UserSession.getInstance().getCurrentUser().getId());
        currentNote.setTitle("New Note");
        currentNote.setContent("");
        
        noteTitleField.setText("New Note");
        noteEditor.setHtmlText("");
        tagsField.setText("");
        
        notesList.getSelectionModel().clearSelection();
    }
    
    private void saveCurrentNote() {
        if (currentNote == null) {
            createNewNote();
        }
        
        String title = noteTitleField.getText().trim();
        String content = noteEditor.getHtmlText();
        String tags = tagsField.getText().trim();
        String subjectName = subjectComboBox.getValue();
        
        if (title.isEmpty()) {
            NotificationManager.getInstance().showNotification(
                "Validation Error", 
                "Note title cannot be empty", 
                NotificationManager.NotificationType.WARNING
            );
            return;
        }
        
        int userId = UserSession.getInstance().getCurrentUser().getId();
        int subjectId = subjectService.getSubjectIdByName(userId, subjectName);
        
        currentNote.setTitle(title);
        currentNote.setContent(content);
        currentNote.setTags(tags);
        currentNote.setSubjectId(subjectId);
        
        boolean success;
        if (currentNote.getId() == 0) {
            success = notesService.createNote(currentNote);
        } else {
            success = notesService.updateNote(currentNote);
        }
        
        if (success) {
            NotificationManager.getInstance().showNotification(
                "Note Saved", 
                "Your note has been saved successfully!", 
                NotificationManager.NotificationType.SUCCESS
            );
            loadNotes();
        } else {
            NotificationManager.getInstance().showNotification(
                "Error", 
                "Failed to save note", 
                NotificationManager.NotificationType.ERROR
            );
        }
    }
    
    private void deleteCurrentNote() {
        if (currentNote == null || currentNote.getId() == 0) {
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Note");
        alert.setHeaderText("Are you sure you want to delete this note?");
        alert.setContentText("This action cannot be undone.");
        
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                if (notesService.deleteNote(currentNote.getId())) {
                    NotificationManager.getInstance().showNotification(
                        "Note Deleted", 
                        "Note has been deleted successfully", 
                        NotificationManager.NotificationType.SUCCESS
                    );
                    loadNotes();
                    createNewNote();
                }
            }
        });
    }
    
    private void searchNotes(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            loadNotes();
            return;
        }
        
        int userId = UserSession.getInstance().getCurrentUser().getId();
        List<Note> searchResults = notesService.searchNotes(userId, searchText);
        notesList.getItems().setAll(searchResults);
    }
    
    private void applyBoldFormatting() {
        String selectedText = getSelectedText();
        if (!selectedText.isEmpty()) {
            NoteComponent note = new BasicNote(selectedText);
            NoteComponent boldNote = new BoldDecorator(note);
            replaceSelectedText(boldNote.getFormattedContent());
        }
    }
    
    private void applyItalicFormatting() {
        String selectedText = getSelectedText();
        if (!selectedText.isEmpty()) {
            NoteComponent note = new BasicNote(selectedText);
            NoteComponent italicNote = new ItalicDecorator(note);
            replaceSelectedText(italicNote.getFormattedContent());
        }
    }
    
    private void applyHighlightFormatting() {
        String selectedText = getSelectedText();
        if (!selectedText.isEmpty()) {
            String color = highlightColorPicker.getValue().toString().replace("0x", "#");
            NoteComponent note = new BasicNote(selectedText);
            NoteComponent highlightNote = new HighlightDecorator(note, color);
            replaceSelectedText(highlightNote.getFormattedContent());
        }
    }
    
    private String getSelectedText() {
        // This is a simplified implementation
        // In a real application, you'd need to get the selected text from the HTMLEditor
        return "Selected Text"; // Placeholder
    }
    
    private void replaceSelectedText(String newText) {
        // This is a simplified implementation
        // In a real application, you'd replace the selected text in the HTMLEditor
        String currentContent = noteEditor.getHtmlText();
        noteEditor.setHtmlText(currentContent + newText);
    }
}
