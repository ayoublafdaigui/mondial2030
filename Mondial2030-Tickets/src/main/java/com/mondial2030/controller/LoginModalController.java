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
 * Controller for the Login Modal dialog.
 * Handles user authentication in a modal window.
 */
public class LoginModalController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    private Stage stage;
    private Runnable onLoginSuccess;
    private final AuthenticationService authService = AuthenticationService.getInstance();

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        
        // Allow login on Enter key
        passwordField.setOnAction(e -> handleLogin());
        loginButton.setDefaultButton(true);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
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
            if (onLoginSuccess != null) {
                onLoginSuccess.run();
            }
        } else {
            showError("Nom d'utilisateur ou mot de passe incorrect.");
            passwordField.clear();
        }
    }

    @FXML
    private void handleSwitchToSignup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SignupModal.fxml"));
            Parent signupContent = loader.load();
            
            SignupModalController signupController = loader.getController();
            signupController.setStage(stage);
            signupController.setOnSignupSuccess(() -> {
                // After signup, switch back to login
                try {
                    FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/fxml/LoginModal.fxml"));
                    Parent loginContent = loginLoader.load();
                    
                    LoginModalController loginController = loginLoader.getController();
                    loginController.setStage(stage);
                    loginController.setOnLoginSuccess(onLoginSuccess);
                    
                    Scene scene = new Scene(loginContent, 400, 500);
                    scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                    stage.setScene(scene);
                    
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            
            Scene scene = new Scene(signupContent, 400, 550);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Inscription - Mondial 2030");
            
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
    }
}
