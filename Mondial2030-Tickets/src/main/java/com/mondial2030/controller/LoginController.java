package com.mondial2030.controller;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mondial2030.model.User;
import com.mondial2030.service.AuthenticationService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Login Controller - Handles user authentication and navigation.
 * Routes users to appropriate views based on their role.
 */
public class LoginController {
    
    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private VBox loginContainer;

    private final AuthenticationService authService;

    public LoginController() {
        this.authService = AuthenticationService.getInstance();
    }

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        
        // Allow login on Enter key
        passwordField.setOnAction(e -> handleLogin());
        
        // Style the login button
        loginButton.setDefaultButton(true);
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        if (authService.login(username, password)) {
            navigateToMainView();
        } else {
            showError("Nom d'utilisateur ou mot de passe incorrect.");
            passwordField.clear();
        }
    }

    private void navigateToMainView() {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Parent root;

            if (authService.isAdmin()) {
                // Admin goes to the dashboard
                root = FXMLLoader.load(getClass().getResource("/fxml/AdminDashboard.fxml"));
                stage.setTitle("Mondial 2030 - Tableau de Bord Administrateur");
            } else {
                // Regular user goes to user view
                root = FXMLLoader.load(getClass().getResource("/fxml/UserView.fxml"));
                stage.setTitle("Mondial 2030 - Mes Billets");
            }

            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading view", e);
            showError("Erreur lors du chargement de l'interface.");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");
    }

    @FXML
    private void handleRegister() {
        // Show registration dialog
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Inscription");
        dialog.setHeaderText("Créer un nouveau compte");

        ButtonType registerButtonType = new ButtonType("S'inscrire", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Nom complet");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField newUsernameField = new TextField();
        newUsernameField.setPromptText("Nom d'utilisateur");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Mot de passe");

        VBox content = new VBox(10);
        content.getChildren().addAll(
            new Label("Nom:"), nameField,
            new Label("Email:"), emailField,
            new Label("Nom d'utilisateur:"), newUsernameField,
            new Label("Mot de passe:"), newPasswordField
        );
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registerButtonType) {
                if (authService.register(
                        newUsernameField.getText(),
                        newPasswordField.getText(),
                        nameField.getText(),
                        emailField.getText())) {
                    
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Succès");
                    alert.setContentText("Compte créé avec succès! Vous pouvez maintenant vous connecter.");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setContentText("Le nom d'utilisateur existe déjà.");
                    alert.showAndWait();
                }
            }
            return null;
        });

        dialog.showAndWait();
    }
}
