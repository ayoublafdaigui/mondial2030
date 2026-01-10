package com.mondial2030.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mondial2030.dao.TicketDAO;
import com.mondial2030.dao.UserDAO;
import com.mondial2030.model.Ticket;
import com.mondial2030.model.User;
import com.mondial2030.service.AuthenticationService;

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
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * User View Controller - Interface for regular users to view and transfer their tickets.
 */
public class UserViewController {
    
    private static final Logger LOGGER = Logger.getLogger(UserViewController.class.getName());

    @FXML private TableView<Ticket> myTicketsTable;
    @FXML private TableColumn<Ticket, String> colId;
    @FXML private TableColumn<Ticket, String> colMatch;
    @FXML private TableColumn<Ticket, String> colSeat;
    @FXML private TableColumn<Ticket, String> colDate;
    @FXML private TableColumn<Ticket, String> colStatus;
    @FXML private TableColumn<Ticket, String> colBlockchain;

    @FXML private Label welcomeLabel;
    @FXML private Label ticketCountLabel;

    @FXML private ComboBox<User> transferToComboBox;
    @FXML private Button transferButton;

    private final TicketDAO ticketDAO;
    private UserDAO userDAO;
    private AuthenticationService authService;
    private ObservableList<Ticket> ticketList;

    public UserViewController() {
        this.ticketDAO = new TicketDAO();
        this.userDAO = new UserDAO();
        this.authService = AuthenticationService.getInstance();
    }

    @FXML
    public void initialize() {
        setupTable();
        loadUserData();
        loadTransferOptions();
        
        // Display welcome message
        User currentUser = authService.getCurrentUser();
        if (currentUser != null && welcomeLabel != null) {
            welcomeLabel.setText("Bienvenue, " + currentUser.getName() + "!");
        }
    }

    private void setupTable() {
        colId.setCellValueFactory(cell -> 
            new SimpleStringProperty(String.valueOf(cell.getValue().getId())));
        
        colMatch.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getMatchDetails()));
        
        colSeat.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getSeatNumber()));
        
        colDate.setCellValueFactory(cell -> {
            if (cell.getValue().getPurchaseDate() != null) {
                return new SimpleStringProperty(
                    cell.getValue().getPurchaseDate().toLocalDate().toString());
            }
            return new SimpleStringProperty("");
        });

        colStatus.setCellValueFactory(cell -> {
            Ticket.TicketStatus status = cell.getValue().getStatus();
            return new SimpleStringProperty(status != null ? status.toString() : "ACTIVE");
        });

        // Color code status
        colStatus.setCellFactory(column -> new TableCell<Ticket, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item);
                if (!empty && item != null) {
                    switch (item) {
                        case "ACTIVE" -> setTextFill(Color.GREEN);
                        case "USED" -> setTextFill(Color.GRAY);
                        case "CANCELLED" -> setTextFill(Color.RED);
                        case "SUSPENDED" -> setTextFill(Color.ORANGE);
                        default -> setTextFill(Color.BLACK);
                    }
                }
            }
        });

        colBlockchain.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getBlockchainHash()));

        ticketList = FXCollections.observableArrayList();
        myTicketsTable.setItems(ticketList);

        // Enable/disable transfer button based on selection
        myTicketsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (transferButton != null) {
                transferButton.setDisable(newVal == null);
            }
        });
    }

    private void loadUserData() {
        User currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            List<Ticket> userTickets = ticketDAO.getTicketsByOwner(currentUser.getId());
            ticketList.clear();
            ticketList.addAll(userTickets);

            if (ticketCountLabel != null) {
                ticketCountLabel.setText("Vous avez " + userTickets.size() + " billet(s)");
            }
        }
    }

    private void loadTransferOptions() {
        if (transferToComboBox != null) {
            List<User> users = userDAO.getUsersByRole(User.Role.USER);
            // Remove current user from list
            User currentUser = authService.getCurrentUser();
            users.removeIf(u -> u.getId().equals(currentUser.getId()));
            
            transferToComboBox.setItems(FXCollections.observableArrayList(users));
            transferToComboBox.setConverter(new javafx.util.StringConverter<User>() {
                @Override
                public String toString(User user) {
                    if (user == null) return "";
                    return user.getName() + " (" + user.getUsername() + ")";
                }
                @Override
                public User fromString(String string) { return null; }
            });
        }
    }

    @FXML
    private void handleTransfer() {
        Ticket selectedTicket = myTicketsTable.getSelectionModel().getSelectedItem();
        User newOwner = transferToComboBox.getValue();

        if (selectedTicket == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", 
                "Veuillez sélectionner un billet à transférer.");
            return;
        }

        if (newOwner == null) {
            showAlert(Alert.AlertType.WARNING, "Destinataire requis", 
                "Veuillez sélectionner le destinataire.");
            return;
        }

        // Confirm transfer
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer le transfert");
        confirm.setHeaderText("Transfert de billet");
        confirm.setContentText(String.format(
            "Êtes-vous sûr de vouloir transférer le billet pour '%s' à %s?", 
            selectedTicket.getMatchDetails(),
            newOwner.getName()));
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            ticketDAO.transferTicket(selectedTicket.getId(), newOwner, null);
            loadUserData();
            
            showAlert(Alert.AlertType.INFORMATION, "Succès", 
                "Billet transféré avec succès à " + newOwner.getName() + "!");
        }
    }

    @FXML
    private void handleRefresh() {
        loadUserData();
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
            Stage stage = (Stage) myTicketsTable.getScene().getWindow();
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
    private void handleViewDetails() {
        Ticket selectedTicket = myTicketsTable.getSelectionModel().getSelectedItem();
        if (selectedTicket == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", 
                "Veuillez sélectionner un billet.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du Billet");
        alert.setHeaderText("Billet #" + selectedTicket.getId());
        alert.setContentText(String.format(
            "Match: %s\n" +
            "Place: %s\n" +
            "Catégorie: %s\n" +
            "Statut: %s\n" +
            "Date d'achat: %s\n\n" +
            "Sécurité Blockchain:\n%s",
            selectedTicket.getMatchDetails(),
            selectedTicket.getSeatNumber(),
            selectedTicket.getCategory(),
            selectedTicket.getStatus(),
            selectedTicket.getPurchaseDate().toLocalDate(),
            selectedTicket.getBlockchainHash()
        ));
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
