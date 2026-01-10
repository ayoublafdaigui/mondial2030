package com.mondial2030.controller;

import com.mondial2030.model.Match;
import com.mondial2030.service.MatchService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ResourceBundle;

/**
 * Controller for the Add Match form.
 * Handles match creation with validation and database persistence.
 */
public class AddMatchController implements Initializable {

    @FXML private ComboBox<String> homeTeamComboBox;
    @FXML private ComboBox<String> awayTeamComboBox;
    @FXML private DatePicker matchDatePicker;
    @FXML private Spinner<Integer> hourSpinner;
    @FXML private Spinner<Integer> minuteSpinner;
    @FXML private ComboBox<String> stadiumComboBox;
    @FXML private TextField ticketPriceField;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    private final MatchService matchService = new MatchService();

    // World Cup 2030 host countries teams and other qualified teams
    private static final String[] TEAMS = {
        "Morocco", "Spain", "Portugal", "Argentina", "Brazil", "France",
        "Germany", "England", "Italy", "Netherlands", "Belgium", "Croatia",
        "Uruguay", "Mexico", "USA", "Japan", "South Korea", "Australia",
        "Saudi Arabia", "Qatar", "Senegal", "Ghana", "Cameroon", "Nigeria"
    };

    // World Cup 2030 stadiums across Morocco, Spain, and Portugal
    private static final String[] STADIUMS = {
        "Grand Stade de Casablanca (Morocco)",
        "Stade Mohammed V (Casablanca, Morocco)",
        "Grand Stade de Marrakech (Morocco)",
        "Stade Ibn Batouta (Tangier, Morocco)",
        "Stade de Fès (Morocco)",
        "Stade d'Agadir (Morocco)",
        "Santiago Bernabéu (Madrid, Spain)",
        "Camp Nou (Barcelona, Spain)",
        "Estadio Metropolitano (Madrid, Spain)",
        "San Mamés (Bilbao, Spain)",
        "La Cartuja (Seville, Spain)",
        "RCDE Stadium (Barcelona, Spain)",
        "Estádio da Luz (Lisbon, Portugal)",
        "Estádio do Dragão (Porto, Portugal)",
        "Estádio José Alvalade (Lisbon, Portugal)"
    };

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize team ComboBoxes
        homeTeamComboBox.setItems(FXCollections.observableArrayList(TEAMS));
        awayTeamComboBox.setItems(FXCollections.observableArrayList(TEAMS));
        
        // Initialize stadium ComboBox
        stadiumComboBox.setItems(FXCollections.observableArrayList(STADIUMS));
        
        // Initialize time spinners (0-23 hours, 0-59 minutes)
        SpinnerValueFactory<Integer> hourFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 18);
        SpinnerValueFactory<Integer> minuteFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 15);
        
        hourSpinner.setValueFactory(hourFactory);
        minuteSpinner.setValueFactory(minuteFactory);
        
        // Set default date to today
        matchDatePicker.setValue(LocalDate.now().plusDays(30));
        
        // Add numeric validation to price field
        ticketPriceField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                ticketPriceField.setText(oldVal);
            }
        });
    }

    @FXML
    private void handleSaveMatch() {
        clearMessages();
        
        // Validate input
        String validationError = validateInput();
        if (validationError != null) {
            showError(validationError);
            return;
        }
        
        try {
            // Create Match object
            Match match = createMatchFromForm();
            
            // Save to database using service
            boolean saved = matchService.saveMatch(match);
            
            if (saved) {
                showSuccess("Match saved successfully! ID: " + match.getId());
                handleClear();
            } else {
                showError("Failed to save match. Please try again.");
            }
        } catch (Exception e) {
            showError("Error saving match: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClear() {
        homeTeamComboBox.getSelectionModel().clearSelection();
        awayTeamComboBox.getSelectionModel().clearSelection();
        matchDatePicker.setValue(LocalDate.now().plusDays(30));
        hourSpinner.getValueFactory().setValue(18);
        minuteSpinner.getValueFactory().setValue(0);
        stadiumComboBox.getSelectionModel().clearSelection();
        ticketPriceField.clear();
        clearMessages();
    }

    private String validateInput() {
        if (homeTeamComboBox.getValue() == null) {
            return "Please select a home team.";
        }
        if (awayTeamComboBox.getValue() == null) {
            return "Please select an away team.";
        }
        if (homeTeamComboBox.getValue().equals(awayTeamComboBox.getValue())) {
            return "Home team and away team cannot be the same.";
        }
        if (matchDatePicker.getValue() == null) {
            return "Please select a match date.";
        }
        if (matchDatePicker.getValue().isBefore(LocalDate.now())) {
            return "Match date cannot be in the past.";
        }
        if (stadiumComboBox.getValue() == null) {
            return "Please select a stadium.";
        }
        if (ticketPriceField.getText().isEmpty()) {
            return "Please enter a ticket price.";
        }
        try {
            double price = Double.parseDouble(ticketPriceField.getText());
            if (price <= 0) {
                return "Ticket price must be greater than zero.";
            }
        } catch (NumberFormatException e) {
            return "Invalid ticket price format.";
        }
        return null;
    }

    private Match createMatchFromForm() {
        Match match = new Match();
        match.setHomeTeam(homeTeamComboBox.getValue());
        match.setAwayTeam(awayTeamComboBox.getValue());
        
        LocalDate date = matchDatePicker.getValue();
        LocalTime time = LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue());
        match.setMatchDateTime(LocalDateTime.of(date, time));
        
        match.setStadium(stadiumComboBox.getValue());
        match.setTicketPrice(Double.parseDouble(ticketPriceField.getText()));
        match.setTotalSeats(50000); // Default stadium capacity
        match.setAvailableSeats(50000);
        
        return match;
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        successLabel.setVisible(false);
    }

    private void showSuccess(String message) {
        successLabel.setText(message);
        successLabel.setVisible(true);
        errorLabel.setVisible(false);
    }

    private void clearMessages() {
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
    }
}
