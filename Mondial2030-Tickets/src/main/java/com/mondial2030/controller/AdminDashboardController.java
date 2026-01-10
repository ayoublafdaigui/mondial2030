package com.mondial2030.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mondial2030.dao.MatchDAO;
import com.mondial2030.dao.TicketDAO;
import com.mondial2030.dao.UserDAO;
import com.mondial2030.model.Match;
import com.mondial2030.model.Ticket;
import com.mondial2030.model.User;
import com.mondial2030.service.AuthenticationService;
import com.mondial2030.service.BlockchainService;
import com.mondial2030.service.FraudDetectionService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Admin Dashboard Controller - Main interface for administrators.
 * Provides ticket management, fraud detection, and transfer operations.
 */
public class AdminDashboardController {
    
    private static final Logger LOGGER = Logger.getLogger(AdminDashboardController.class.getName());

    // FXML Components - Spectator Flow Table
    @FXML private TableView<Ticket> ticketTable;
    @FXML private TableColumn<Ticket, String> colId;
    @FXML private TableColumn<Ticket, String> colOwner;
    @FXML private TableColumn<Ticket, String> colMatch;
    @FXML private TableColumn<Ticket, String> colSeat;
    @FXML private TableColumn<Ticket, String> colFraudScore;
    @FXML private TableColumn<Ticket, String> colBlockchain;
    @FXML private TableColumn<Ticket, String> colStatus;

    // FXML Components - Ticket Generation Form
    @FXML private ComboBox<Match> matchComboBox;
    @FXML private ComboBox<User> userComboBox;
    @FXML private TextField seatField;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private Button generateButton;

    // FXML Components - Transfer Section
    @FXML private ComboBox<User> transferToComboBox;
    @FXML private TextField transferPriceField;
    @FXML private Button transferButton;

    // FXML Components - Status Bar
    @FXML private Label aiStatusLabel;
    @FXML private Label blockchainStatusLabel;
    @FXML private Label userInfoLabel;
    @FXML private Label statsLabel;

    // Services and DAOs
    private final TicketDAO ticketDAO;
    private final MatchDAO matchDAO;
    private final UserDAO userDAO;
    private final FraudDetectionService fraudService;
    private final BlockchainService blockchainService;
    private final AuthenticationService authService;

    private ObservableList<Ticket> ticketList;

    public AdminDashboardController() {
        this.ticketDAO = new TicketDAO();
        this.matchDAO = new MatchDAO();
        this.userDAO = new UserDAO();
        this.fraudService = new FraudDetectionService();
        this.blockchainService = new BlockchainService();
        this.authService = AuthenticationService.getInstance();
    }

    @FXML
    public void initialize() {
        setupTable();
        loadComboBoxes();
        refreshData();
        updateStatusBar();
        
        // Setup category combo box
        if (categoryComboBox != null) {
            categoryComboBox.setItems(FXCollections.observableArrayList(
                "STANDARD", "VIP", "PREMIUM", "HOSPITALITY"
            ));
            categoryComboBox.setValue("STANDARD");
        }

        // Display current user info
        if (userInfoLabel != null && authService.getCurrentUser() != null) {
            userInfoLabel.setText("Connecté: " + authService.getCurrentUser().getName() + " (Admin)");
        }
    }

    private void setupTable() {
        // Configure table columns
        colId.setCellValueFactory(cell -> 
            new SimpleStringProperty(String.valueOf(cell.getValue().getId())));
        
        colOwner.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getOwnerName()));
        
        colMatch.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getMatchDetails()));
        
        colSeat.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getSeatNumber()));
        
        colFraudScore.setCellValueFactory(cell -> {
            double score = cell.getValue().getFraudRiskScore();
            return new SimpleStringProperty(String.format("%.2f", score));
        });

        // Color code fraud scores
        colFraudScore.setCellFactory(column -> new TableCell<Ticket, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                if (!empty && item != null) {
                    try {
                        double val = Double.parseDouble(item.replace(",", "."));
                        if (val > 0.7) {
                            setTextFill(Color.RED);
                            setStyle("-fx-font-weight: bold;");
                        } else if (val > 0.4) {
                            setTextFill(Color.ORANGE);
                            setStyle("-fx-font-weight: bold;");
                        } else {
                            setTextFill(Color.GREEN);
                            setStyle("");
                        }
                    } catch (NumberFormatException e) {
                        setTextFill(Color.BLACK);
                    }
                }
            }
        });

        colBlockchain.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getBlockchainHash()));

        colStatus.setCellValueFactory(cell -> {
            Ticket.TicketStatus status = cell.getValue().getStatus();
            return new SimpleStringProperty(status != null ? status.toString() : "ACTIVE");
        });

        // Initialize observable list
        ticketList = FXCollections.observableArrayList();
        ticketTable.setItems(ticketList);

        // Add selection listener for transfer functionality
        ticketTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (transferButton != null) {
                transferButton.setDisable(newVal == null);
            }
        });
    }

    private void loadComboBoxes() {
        // Load matches into combo box
        if (matchComboBox != null) {
            List<Match> matches = matchDAO.getAllMatches();
            matchComboBox.setItems(FXCollections.observableArrayList(matches));
            matchComboBox.setConverter(new javafx.util.StringConverter<Match>() {
                @Override
                public String toString(Match match) {
                    if (match == null) return "";
                    return match.getHomeTeam() + " vs " + match.getAwayTeam() + 
                           " (" + match.getCity() + ")";
                }
                @Override
                public Match fromString(String string) { return null; }
            });
        }

        // Load users into combo boxes
        if (userComboBox != null) {
            List<User> users = userDAO.getAllUsers();
            userComboBox.setItems(FXCollections.observableArrayList(users));
            setupUserComboBox(userComboBox);
        }

        if (transferToComboBox != null) {
            List<User> users = userDAO.getAllUsers();
            transferToComboBox.setItems(FXCollections.observableArrayList(users));
            setupUserComboBox(transferToComboBox);
        }
    }

    private void setupUserComboBox(ComboBox<User> comboBox) {
        comboBox.setConverter(new javafx.util.StringConverter<User>() {
            @Override
            public String toString(User user) {
                if (user == null) return "";
                return user.getName() + " (" + user.getUsername() + ")";
            }
            @Override
            public User fromString(String string) { return null; }
        });
    }

    @FXML
    private void handleGenerateTicket() {
        Match selectedMatch = matchComboBox.getValue();
        User selectedUser = userComboBox.getValue();
        String seat = seatField.getText().trim();
        String priceText = priceField.getText().trim();

        if (selectedMatch == null || selectedUser == null || seat.isEmpty() || priceText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", 
                "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            
            Ticket ticket = new Ticket(seat, price, selectedMatch, selectedUser);
            ticket.setCategory(Ticket.TicketCategory.valueOf(categoryComboBox.getValue()));
            
            ticketDAO.saveTicket(ticket);
            
            refreshData();
            clearForm();
            
            showAlert(Alert.AlertType.INFORMATION, "Succès", 
                "Billet généré avec succès!\nHash: " + ticket.getBlockchainHash());

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Prix invalide.");
        }
    }

    @FXML
    private void handleTransferTicket() {
        Ticket selectedTicket = ticketTable.getSelectionModel().getSelectedItem();
        User newOwner = transferToComboBox.getValue();

        if (selectedTicket == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", 
                "Veuillez sélectionner un billet à transférer.");
            return;
        }

        if (newOwner == null) {
            showAlert(Alert.AlertType.WARNING, "Destinataire requis", 
                "Veuillez sélectionner le nouveau propriétaire.");
            return;
        }

        Double transferPrice = null;
        if (!transferPriceField.getText().isEmpty()) {
            try {
                transferPrice = Double.parseDouble(transferPriceField.getText());
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Prix de transfert invalide.");
                return;
            }
        }

        // Confirm transfer
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer le transfert");
        confirm.setContentText(String.format(
            "Transférer le billet #%d à %s?", 
            selectedTicket.getId(), 
            newOwner.getName()));
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            ticketDAO.transferTicket(selectedTicket.getId(), newOwner, transferPrice);
            refreshData();
            transferPriceField.clear();
            
            showAlert(Alert.AlertType.INFORMATION, "Succès", 
                "Billet transféré avec succès!");
        }
    }

    @FXML
    private void handleRefresh() {
        refreshData();
        updateStatusBar();
    }

    @FXML
    private void handleLogout() {
        authService.logout();
        navigateToHome();
    }

    @FXML
    private void handleBackToHome() {
        navigateToHome();
    }

    private void navigateToHome() {
        try {
            Stage stage = (Stage) ticketTable.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Home.fxml"));
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Mondial 2030 - FIFA World Cup Tickets");
            stage.centerOnScreen();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error navigating to home", e);
        }
    }

    @FXML
    private void handleAnalyzeFraud() {
        Ticket selectedTicket = ticketTable.getSelectionModel().getSelectedItem();
        if (selectedTicket == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", 
                "Veuillez sélectionner un billet à analyser.");
            return;
        }

        FraudDetectionService.FraudReport report = fraudService.analyzeTicket(selectedTicket);
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Analyse de Fraude IA");
        alert.setHeaderText("Rapport pour le Billet #" + selectedTicket.getId());
        alert.setContentText(String.format(
            "Score de Fraude: %.2f\n" +
            "Niveau de Risque: %s\n\n" +
            "Détails:\n%s",
            report.getScore(),
            report.getRiskLevel(),
            report.getDetails()
        ));
        alert.showAndWait();
    }

    private void refreshData() {
        List<Ticket> tickets = ticketDAO.getAllTickets();
        ticketList.clear();
        ticketList.addAll(tickets);
        
        // Update stats
        if (statsLabel != null) {
            long totalTickets = ticketDAO.getTicketCount();
            List<Ticket> highRisk = ticketDAO.getHighRiskTickets();
            statsLabel.setText(String.format(
                "Total: %d billets | À risque: %d", 
                totalTickets, 
                highRisk.size()
            ));
        }
    }

    private void updateStatusBar() {
        if (aiStatusLabel != null) {
            aiStatusLabel.setText("Système IA: Actif");
            aiStatusLabel.setTextFill(Color.GREEN);
        }

        if (blockchainStatusLabel != null) {
            BlockchainService.BlockchainStatus status = blockchainService.getNetworkStatus();
            blockchainStatusLabel.setText(status.toString());
            blockchainStatusLabel.setTextFill(Color.DARKBLUE);
        }
    }

    private void clearForm() {
        if (seatField != null) seatField.clear();
        if (priceField != null) priceField.clear();
        if (matchComboBox != null) matchComboBox.getSelectionModel().clearSelection();
        if (userComboBox != null) userComboBox.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
