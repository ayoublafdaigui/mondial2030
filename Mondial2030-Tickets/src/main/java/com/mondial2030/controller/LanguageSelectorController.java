package com.mondial2030.controller;

import com.mondial2030.util.LanguageManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for the Language Selector component.
 * Demonstrates how to use LanguageManager for I18n.
 */
public class LanguageSelectorController implements Initializable {

    @FXML private ComboBox<String> languageComboBox;
    @FXML private Label welcomeLabel;
    @FXML private Label buyTicketLabel;
    @FXML private Label stadiumLabel;

    private final LanguageManager languageManager = LanguageManager.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Populate language combo box
        languageComboBox.setItems(FXCollections.observableArrayList(
            languageManager.getSupportedLocaleNames()
        ));
        
        // Select current language
        String currentLangName = getCurrentLanguageName();
        languageComboBox.getSelectionModel().select(currentLangName);
        
        // Add listener to preview language changes
        languageComboBox.setOnAction(e -> previewLanguage());
        
        // Register for locale change notifications
        languageManager.addLocaleChangeListener(locale -> updateLabels());
        
        // Initial update
        updateLabels();
    }

    private String getCurrentLanguageName() {
        Locale current = languageManager.getCurrentLocale();
        Map<String, Locale> locales = languageManager.getSupportedLocales();
        
        for (Map.Entry<String, Locale> entry : locales.entrySet()) {
            if (entry.getValue().getLanguage().equals(current.getLanguage())) {
                return entry.getKey();
            }
        }
        return "English";
    }

    private void previewLanguage() {
        String selectedName = languageComboBox.getValue();
        if (selectedName != null) {
            Map<String, Locale> locales = languageManager.getSupportedLocales();
            Locale locale = locales.get(selectedName);
            if (locale != null) {
                languageManager.setLocale(locale);
            }
        }
    }

    private void updateLabels() {
        welcomeLabel.setText(languageManager.getString("welcome"));
        buyTicketLabel.setText(languageManager.getString("buy_ticket"));
        stadiumLabel.setText(languageManager.getString("stadium"));
    }

    @FXML
    private void handleApply() {
        String selectedName = languageComboBox.getValue();
        if (selectedName != null) {
            Map<String, Locale> locales = languageManager.getSupportedLocales();
            Locale locale = locales.get(selectedName);
            if (locale != null) {
                languageManager.setLocale(locale);
                
                // Update node orientation for RTL languages
                if (welcomeLabel.getScene() != null) {
                    welcomeLabel.getScene().getRoot().setNodeOrientation(
                        languageManager.getNodeOrientation()
                    );
                }
                
                System.out.println("Language applied: " + selectedName);
            }
        }
    }
}
