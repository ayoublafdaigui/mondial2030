package com.mondial2030.controller;

import com.mondial2030.service.AuthenticationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the Signup Modal dialog.
 * Handles user registration in a modal window.
 */
public class SignupModalController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button signupButton;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    private Stage stage;
    private Runnable onSignupSuccess;
    private final AuthenticationService authService = AuthenticationService.getInstance();

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
        
        // Allow signup on Enter key in last field
        confirmPasswordField.setOnAction(e -> handleSignup());
        signupButton.setDefaultButton(true);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setOnSignupSuccess(Runnable callback) {
        this.onSignupSuccess = callback;
    }

    @FXML
    private void handleSignup() {
        clearMessages();
        
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Veuillez entrer un email valide.");
            return;
        }

        if (username.length() < 3) {
            showError("Le nom d'utilisateur doit contenir au moins 3 caractères.");
            return;
        }

        if (password.length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas.");
            return;
        }

        // Attempt registration
        if (authService.register(username, password, name, email)) {
            showSuccess("Compte créé avec succès!");
            
            // Clear form
            nameField.clear();
            emailField.clear();
            usernameField.clear();
            passwordField.clear();
            confirmPasswordField.clear();
            
            // Notify success
            if (onSignupSuccess != null) {
                // Small delay to show success message
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
                pause.setOnFinished(e -> onSignupSuccess.run());
                pause.play();
            }
        } else {
            showError("Ce nom d'utilisateur existe déjà. Veuillez en choisir un autre.");
        }
    }

    @FXML
    private void handleSwitchToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginModal.fxml"));
            Parent loginContent = loader.load();
            
            LoginModalController loginController = loader.getController();
            loginController.setStage(stage);
            
            Scene scene = new Scene(loginContent, 400, 500);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Connexion - Mondial 2030");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClose() {
        if (stage != null) {
            stage.close();
        }
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
