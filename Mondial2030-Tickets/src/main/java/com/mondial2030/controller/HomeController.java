package com.mondial2030.controller;

import com.mondial2030.dao.MatchDAO;
import com.mondial2030.dao.StadiumDAO;
import com.mondial2030.dao.TicketDAO;
import com.mondial2030.model.Match;
import com.mondial2030.model.Stadium;
import com.mondial2030.model.User;
import com.mondial2030.service.AuthenticationService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Home Controller - Landing page for all users (guests and authenticated).
 * Displays upcoming matches, stadiums, and available tickets.
 * Handles navigation and authentication state.
 */
public class HomeController {

    private static final Logger LOGGER = Logger.getLogger(HomeController.class.getName());

    @FXML private HBox authButtonsContainer;
    @FXML private FlowPane matchesContainer;
    @FXML private FlowPane stadiumsContainer;
    @FXML private Button buyTicketsHeroBtn;
    
    // Stats labels
    @FXML private Label totalMatchesLabel;
    @FXML private Label totalStadiumsLabel;
    @FXML private Label totalTeamsLabel;
    @FXML private Label availableTicketsLabel;

    private final MatchDAO matchDAO = new MatchDAO();
    private final TicketDAO ticketDAO = new TicketDAO();
    private final StadiumDAO stadiumDAO = new StadiumDAO();
    private final AuthenticationService authService = AuthenticationService.getInstance();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        updateAuthButtons();
        loadUpcomingMatches();
        loadStadiums();
        loadStats();
    }

    /**
     * Updates the authentication buttons based on login state.
     * - Guest: Shows "Login / Sign Up" button
     * - Logged in User: Shows "My Tickets" button and user info
     * - Admin: Shows "Dashboard" button
     */
    private void updateAuthButtons() {
        authButtonsContainer.getChildren().clear();

        if (authService.isLoggedIn()) {
            User currentUser = authService.getCurrentUser();
            
            // User info label
            Label userLabel = new Label("👤 " + currentUser.getName());
            userLabel.getStyleClass().add("user-label");
            authButtonsContainer.getChildren().add(userLabel);

            if (authService.isAdmin()) {
                // Admin: Show Dashboard button
                Button dashboardBtn = new Button("📊 Tableau de Bord");
                dashboardBtn.getStyleClass().add("nav-button-primary");
                dashboardBtn.setOnAction(e -> navigateToAdminDashboard());
                authButtonsContainer.getChildren().add(dashboardBtn);
            } else {
                // Regular User: Show My Tickets button
                Button myTicketsBtn = new Button("🎫 Mes Billets");
                myTicketsBtn.getStyleClass().add("nav-button-primary");
                myTicketsBtn.setOnAction(e -> navigateToUserView());
                authButtonsContainer.getChildren().add(myTicketsBtn);
            }

            // Logout button
            Button logoutBtn = new Button("🚪 Déconnexion");
            logoutBtn.getStyleClass().add("nav-button-secondary");
            logoutBtn.setOnAction(e -> handleLogout());
            authButtonsContainer.getChildren().add(logoutBtn);
            
            // Enable buy tickets for logged-in users
            buyTicketsHeroBtn.setDisable(false);
            
        } else {
            // Guest: Show Login / Sign Up buttons
            Button loginBtn = new Button("🔐 Connexion");
            loginBtn.getStyleClass().add("nav-button-primary");
            loginBtn.setOnAction(e -> showLoginModal());
            
            Button signupBtn = new Button("📝 Inscription");
            signupBtn.getStyleClass().add("nav-button-secondary");
            signupBtn.setOnAction(e -> showSignupModal());
            
            authButtonsContainer.getChildren().addAll(loginBtn, signupBtn);
            
            // Buy tickets button prompts login for guests
            buyTicketsHeroBtn.setOnAction(e -> {
                showLoginRequiredAlert();
            });
        }
    }

    /**
     * Loads and displays upcoming matches as cards.
     */
    private void loadUpcomingMatches() {
        matchesContainer.getChildren().clear();
        
        List<Match> upcomingMatches = matchDAO.getUpcomingMatches();
        
        // Show up to 6 matches on home page
        int count = Math.min(upcomingMatches.size(), 6);
        
        for (int i = 0; i < count; i++) {
            Match match = upcomingMatches.get(i);
            VBox matchCard = createMatchCard(match);
            matchesContainer.getChildren().add(matchCard);
        }
        
        if (upcomingMatches.isEmpty()) {
            Label noMatchesLabel = new Label("Aucun match à venir pour le moment.");
            noMatchesLabel.getStyleClass().add("no-data-label");
            matchesContainer.getChildren().add(noMatchesLabel);
        }
    }

    /**
     * Creates a match card UI component.
     */
    private VBox createMatchCard(Match match) {
        VBox card = new VBox(10);
        card.getStyleClass().add("match-card");
        card.setPrefWidth(280);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));

        // Match phase badge
        Label phaseBadge = new Label(getPhaseDisplayName(match.getPhase()));
        phaseBadge.getStyleClass().add("phase-badge");

        // Teams
        HBox teamsBox = new HBox(10);
        teamsBox.setAlignment(Pos.CENTER);
        
        Label homeTeam = new Label(match.getHomeTeam());
        homeTeam.getStyleClass().add("team-name");
        
        Label vs = new Label("vs");
        vs.getStyleClass().add("vs-label");
        
        Label awayTeam = new Label(match.getAwayTeam());
        awayTeam.getStyleClass().add("team-name");
        
        teamsBox.getChildren().addAll(homeTeam, vs, awayTeam);

        // Date & Time
        String dateStr = match.getMatchDate() != null 
            ? match.getMatchDate().format(DATE_FORMATTER) : "TBD";
        String timeStr = match.getMatchDate() != null 
            ? match.getMatchDate().format(TIME_FORMATTER) : "";
        
        Label dateLabel = new Label("📅 " + dateStr + " à " + timeStr);
        dateLabel.getStyleClass().add("match-date");

        // Stadium
        Label stadiumLabel = new Label("🏟️ " + match.getStadium());
        stadiumLabel.getStyleClass().add("match-stadium");
        stadiumLabel.setWrapText(true);

        // Available seats
        int available = match.getAvailableSeats() != null ? match.getAvailableSeats() : 0;
        Label seatsLabel = new Label("🎫 " + available + " places disponibles");
        seatsLabel.getStyleClass().add(available > 0 ? "seats-available" : "seats-soldout");

        // Price
        Double price = match.getBasePrice();
        Label priceLabel = new Label(price != null ? String.format("À partir de %.2f €", price) : "Prix à confirmer");
        priceLabel.getStyleClass().add("match-price");

        // Buy button
        Button buyBtn = new Button("Acheter");
        buyBtn.getStyleClass().add("buy-button");
        buyBtn.setDisable(available <= 0);
        
        buyBtn.setOnAction(e -> {
            if (authService.isLoggedIn()) {
                handleBuyTicketForMatch(match);
            } else {
                showLoginRequiredAlert();
            }
        });

        card.getChildren().addAll(phaseBadge, teamsBox, dateLabel, stadiumLabel, seatsLabel, priceLabel, buyBtn);
        
        return card;
    }

    /**
     * Loads statistics for the stats section.
     */
    private void loadStats() {
        List<Match> allMatches = matchDAO.getAllMatches();
        List<Stadium> allStadiums = stadiumDAO.getAllStadiums();
        
        int totalAvailableTickets = allMatches.stream()
            .mapToInt(m -> m.getAvailableSeats() != null ? m.getAvailableSeats() : 0)
            .sum();
        
        totalMatchesLabel.setText(String.valueOf(allMatches.size()));
        totalStadiumsLabel.setText(String.valueOf(allStadiums.size()));
        availableTicketsLabel.setText(String.format("%,d", totalAvailableTickets));
    }

    /**
     * Loads and displays stadiums as cards.
     */
    private void loadStadiums() {
        if (stadiumsContainer == null) {
            return; // Stadium container not present in FXML
        }
        stadiumsContainer.getChildren().clear();
        
        List<Stadium> stadiums = stadiumDAO.getAllStadiums();
        
        // Show up to 6 stadiums on home page
        int count = Math.min(stadiums.size(), 6);
        
        for (int i = 0; i < count; i++) {
            Stadium stadium = stadiums.get(i);
            VBox stadiumCard = createStadiumCard(stadium);
            stadiumsContainer.getChildren().add(stadiumCard);
        }
        
        if (stadiums.isEmpty()) {
            Label noStadiumsLabel = new Label("Aucun stade disponible pour le moment.");
            noStadiumsLabel.getStyleClass().add("no-data-label");
            stadiumsContainer.getChildren().add(noStadiumsLabel);
        }
    }

    /**
     * Creates a stadium card UI component.
     */
    private VBox createStadiumCard(Stadium stadium) {
        VBox card = new VBox(10);
        card.getStyleClass().add("stadium-card");
        card.setPrefWidth(280);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));

        // Main venue badge
        if (Boolean.TRUE.equals(stadium.getIsMainVenue())) {
            Label mainVenueBadge = new Label("⭐ Stade Principal");
            mainVenueBadge.getStyleClass().add("main-venue-badge");
            card.getChildren().add(mainVenueBadge);
        }

        // Stadium name
        Label nameLabel = new Label(stadium.getName());
        nameLabel.getStyleClass().add("stadium-name");
        nameLabel.setWrapText(true);

        // Location
        Label locationLabel = new Label("📍 " + stadium.getCity() + ", " + stadium.getCountry());
        locationLabel.getStyleClass().add("stadium-location");

        // Capacity
        Label capacityLabel = new Label("🏟️ " + stadium.getCapacityFormatted() + " places");
        capacityLabel.getStyleClass().add("stadium-capacity");

        // Year built
        if (stadium.getYearBuilt() != null) {
            Label yearLabel = new Label("Construit en " + stadium.getYearBuilt());
            yearLabel.getStyleClass().add("stadium-year");
            card.getChildren().addAll(nameLabel, locationLabel, capacityLabel, yearLabel);
        } else {
            card.getChildren().addAll(nameLabel, locationLabel, capacityLabel);
        }

        // View matches button
        Button viewMatchesBtn = new Button("Voir les matchs");
        viewMatchesBtn.getStyleClass().add("view-matches-button");
        viewMatchesBtn.setOnAction(e -> showMatchesAtStadium(stadium));
        card.getChildren().add(viewMatchesBtn);

        return card;
    }

    /**
     * Shows matches at a specific stadium.
     */
    private void showMatchesAtStadium(Stadium stadium) {
        showInfo("Matchs au " + stadium.getName() + " - Fonctionnalité bientôt disponible!");
    }

    /**
     * Shows the login modal dialog.
     */
    private void showLoginModal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginModal.fxml"));
            Parent modalContent = loader.load();
            
            LoginModalController modalController = loader.getController();
            
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(authButtonsContainer.getScene().getWindow());
            modalStage.setTitle("Connexion - Mondial 2030");
            
            Scene scene = new Scene(modalContent, 400, 500);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            modalStage.setScene(scene);
            modalStage.setResizable(false);
            
            modalController.setStage(modalStage);
            modalController.setOnLoginSuccess(() -> {
                modalStage.close();
                handlePostLoginNavigation();
            });
            
            modalStage.showAndWait();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading login modal", e);
            showError("Erreur lors du chargement de la fenêtre de connexion.");
        }
    }

    /**
     * Shows the signup modal dialog.
     */
    private void showSignupModal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SignupModal.fxml"));
            Parent modalContent = loader.load();
            
            SignupModalController modalController = loader.getController();
            
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(authButtonsContainer.getScene().getWindow());
            modalStage.setTitle("Inscription - Mondial 2030");
            
            Scene scene = new Scene(modalContent, 400, 550);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            modalStage.setScene(scene);
            modalStage.setResizable(false);
            
            modalController.setStage(modalStage);
            modalController.setOnSignupSuccess(() -> {
                modalStage.close();
                showInfo("Compte créé avec succès! Vous pouvez maintenant vous connecter.");
            });
            
            modalStage.showAndWait();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading signup modal", e);
            showError("Erreur lors du chargement de la fenêtre d'inscription.");
        }
    }

    /**
     * Handles navigation after successful login based on user role.
     */
    private void handlePostLoginNavigation() {
        if (authService.isAdmin()) {
            navigateToAdminDashboard();
        } else {
            // Regular user stays on home but with updated UI
            updateAuthButtons();
            loadUpcomingMatches(); // Refresh to enable buy buttons
        }
    }

    /**
     * Navigates to the Admin Dashboard.
     */
    private void navigateToAdminDashboard() {
        try {
            Stage stage = (Stage) authButtonsContainer.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/AdminDashboard.fxml"));
            
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Mondial 2030 - Tableau de Bord Administrateur");
            stage.centerOnScreen();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading admin dashboard", e);
            showError("Erreur lors du chargement du tableau de bord.");
        }
    }

    /**
     * Navigates to the User View (My Tickets).
     */
    private void navigateToUserView() {
        try {
            Stage stage = (Stage) authButtonsContainer.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/UserView.fxml"));
            
            Scene scene = new Scene(root, 1000, 700);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Mondial 2030 - Mes Billets");
            stage.centerOnScreen();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading user view", e);
            showError("Erreur lors du chargement de vos billets.");
        }
    }

    /**
     * Handles user logout.
     */
    private void handleLogout() {
        authService.logout();
        updateAuthButtons();
        loadUpcomingMatches();
    }

    /**
     * Shows alert when login is required.
     */
    private void showLoginRequiredAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Connexion Requise");
        alert.setHeaderText("Veuillez vous connecter");
        alert.setContentText("Vous devez être connecté pour acheter des billets.");
        
        ButtonType loginButton = new ButtonType("Se connecter");
        ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        alert.getButtonTypes().setAll(loginButton, cancelButton);
        
        alert.showAndWait().ifPresent(response -> {
            if (response == loginButton) {
                showLoginModal();
            }
        });
    }

    /**
     * Handles buying a ticket for a specific match.
     */
    private void handleBuyTicketForMatch(Match match) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StadiumMap.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) authButtonsContainer.getScene().getWindow();
            Scene scene = new Scene(root, 900, 700);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Mondial 2030 - Sélection de Places");
            stage.centerOnScreen();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading stadium map", e);
            showError("Erreur lors du chargement de la carte du stade.");
        }
    }

    // Navigation handlers
    @FXML
    private void handleHome() {
        // Already on home, just refresh
        loadUpcomingMatches();
        loadStadiums();
        loadStats();
    }

    @FXML
    private void handleMatches() {
        // Navigate to matches view
        showInfo("La page des matchs sera bientôt disponible!");
    }

    @FXML
    private void handleTickets() {
        if (authService.isLoggedIn()) {
            navigateToUserView();
        } else {
            showLoginRequiredAlert();
        }
    }

    @FXML
    private void handleStadiums() {
        try {
            Stage stage = (Stage) authButtonsContainer.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/StadiumMap.fxml"));
            
            Scene scene = new Scene(root, 900, 700);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("Mondial 2030 - Carte des Stades");
            stage.centerOnScreen();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading stadiums view", e);
            showError("Erreur lors du chargement de la carte des stades.");
        }
    }

    @FXML
    private void handleBuyTickets() {
        if (authService.isLoggedIn()) {
            handleStadiums();
        } else {
            showLoginRequiredAlert();
        }
    }

    private String getPhaseDisplayName(Match.MatchPhase phase) {
        if (phase == null) return "Match";
        return switch (phase) {
            case GROUP_STAGE -> "Phase de Groupes";
            case ROUND_OF_16 -> "Huitièmes de Finale";
            case QUARTER_FINAL -> "Quarts de Finale";
            case SEMI_FINAL -> "Demi-Finales";
            case THIRD_PLACE -> "Match pour la 3ème Place";
            case FINAL -> "🏆 Finale";
        };
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
