module com.focusbuddy {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.prefs;
    requires java.desktop;

    // Open packages to JavaFX
    opens com.focusbuddy to javafx.fxml;
    opens com.focusbuddy.controllers to javafx.fxml;
    opens com.focusbuddy.models to javafx.base;
    
    // Export packages
    exports com.focusbuddy;
    exports com.focusbuddy.controllers;
    exports com.focusbuddy.models;
    exports com.focusbuddy.services;
    exports com.focusbuddy.utils;
    exports com.focusbuddy.database;
    exports com.focusbuddy.models.notes;
    exports com.focusbuddy.observers;

    // Open controller packages for FXML
    opens com.focusbuddy.controllers.tasks to javafx.fxml;
    opens com.focusbuddy.controllers.notes to javafx.fxml;
    opens com.focusbuddy.controllers.pomodoro to javafx.fxml;
    opens com.focusbuddy.controllers.subjects to javafx.fxml;
    opens com.focusbuddy.controllers.settings to javafx.fxml;

    // Open model packages for JavaFX properties
    opens com.focusbuddy.models.tasks to javafx.base;
    opens com.focusbuddy.models.notes to javafx.base;
    opens com.focusbuddy.models.pomodoro to javafx.base;
    opens com.focusbuddy.models.subjects to javafx.base;
    opens com.focusbuddy.models.settings to javafx.base;

    // Export utility packages
    exports com.focusbuddy.utils.theme;
    exports com.focusbuddy.utils.validation;
    exports com.focusbuddy.utils.notification;
    exports com.focusbuddy.utils.config;
    exports com.focusbuddy.utils.error;
    exports com.focusbuddy.utils.session;
    exports com.focusbuddy.utils.icon;

    // Export service packages
    exports com.focusbuddy.services.tasks;
    exports com.focusbuddy.services.notes;
    exports com.focusbuddy.services.pomodoro;
    exports com.focusbuddy.services.subjects;
    exports com.focusbuddy.services.settings;

    // Export database packages
    exports com.focusbuddy.database.connection;
    exports com.focusbuddy.database.dao;
    exports com.focusbuddy.database.migration;

    // Export observer packages
    exports com.focusbuddy.observers.timer;
    exports com.focusbuddy.observers.task;
    exports com.focusbuddy.observers.notification;
}
