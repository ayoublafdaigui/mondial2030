package com.mondial2030.app;

import com.mondial2030.dao.TicketDAO;
import com.mondial2030.dao.MatchDAO;
import com.mondial2030.model.Ticket;
import com.mondial2030.model.Match;
import com.mondial2030.util.HibernateUtil;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;

/**
 * Legacy Dashboard - Fallback programmatic UI when FXML loading fails.
 * Provides basic ticket management functionality.
 */
public class LegacyDashboard extends Application {

    private final TicketDAO ticketDAO = new TicketDAO();
    private final MatchDAO matchDAO = new MatchDAO();
    private final TableView<Ticket> table = new TableView<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Mondial 2030 - Gestion Intelligente des Tickets");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f0f4f8;");

        // --- Header ---
        VBox headerBox = createHeader();
        root.setTop(headerBox);

        // --- Center: Ticket Table ---
        setupTable();
        VBox centerBox = new VBox(10);
        Label tableTitle = new Label("📊 Flux des Spectateurs & Billets en temps réel");
        tableTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        tableTitle.setTextFill(Color.web("#1a5f7a"));
        centerBox.getChildren().addAll(tableTitle, table);
        centerBox.setPadding(new Insets(10));
        centerBox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        root.setCenter(centerBox);

        // --- Right: Forms ---
        VBox rightBox = createFormPanel();
        root.setRight(rightBox);

        // --- Bottom: Status Bar ---
        HBox bottomBox = createStatusFooter();
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 1200, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setStyle("-fx-background-color: linear-gradient(to right, #1a5f7a, #159895); -fx-background-radius: 10;");

        Label logo = new Label("🏟️");
        logo.setFont(Font.font(32));

        Label title = new Label("Mondial 2030 - Tableau de Bord Administrateur");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.setStyle("-fx-background-color: #57c5b6; -fx-text-fill: white; -fx-font-weight: bold;");
        refreshBtn.setOnAction(e -> refreshTable());

        header.getChildren().addAll(logo, title, spacer, refreshBtn);

        VBox headerContainer = new VBox(header);
        headerContainer.setPadding(new Insets(0, 0, 20, 0));
        return headerContainer;
    }

    private void setupTable() {
        table.setPrefHeight(400);
        table.setStyle("-fx-background-radius: 5;");

        TableColumn<Ticket, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getId())));
        idCol.setPrefWidth(60);

        TableColumn<Ticket, String> ownerCol = new TableColumn<>("Propriétaire");
        ownerCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getOwnerName()));
        ownerCol.setPrefWidth(150);

        TableColumn<Ticket, String> matchCol = new TableColumn<>("Match");
        matchCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMatchDetails()));
        matchCol.setPrefWidth(200);

        TableColumn<Ticket, String> seatCol = new TableColumn<>("Place");
        seatCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSeatNumber()));
        seatCol.setPrefWidth(120);

        TableColumn<Ticket, String> fraudCol = new TableColumn<>("Score Fraude IA");
        fraudCol.setCellValueFactory(cell -> {
            double score = cell.getValue().getFraudRiskScore();
            return new SimpleStringProperty(String.format("%.2f", score));
        });
        fraudCol.setPrefWidth(120);

        // Color code fraud scores
        fraudCol.setCellFactory(column -> new TableCell<Ticket, String>() {
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

        TableColumn<Ticket, String> hashCol = new TableColumn<>("Hash Blockchain");
        hashCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getBlockchainHash()));
        hashCol.setPrefWidth(180);

        table.getColumns().addAll(idCol, ownerCol, matchCol, seatCol, fraudCol, hashCol);
        refreshTable();
    }

    private VBox createFormPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(0, 0, 0, 20));
        panel.setPrefWidth(320);

        // Generate Ticket Section
        VBox generateBox = new VBox(10);
        generateBox.setPadding(new Insets(15));
        generateBox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        Label generateTitle = new Label("🎫 Générer Billet");
        generateTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        generateTitle.setTextFill(Color.web("#1a5f7a"));

        TextField ownerField = new TextField();
        ownerField.setPromptText("Nom du spectateur");

        ComboBox<String> matchBox = new ComboBox<>();
        List<Match> matches = matchDAO.getAllMatches();
        for (Match m : matches) {
            matchBox.getItems().add(m.getHomeTeam() + " vs " + m.getAwayTeam());
        }
        if (matchBox.getItems().isEmpty()) {
            matchBox.getItems().addAll("Maroc vs Espagne", "Portugal vs Argentine", "France vs Brésil", "Finale - Casablanca");
        }
        matchBox.setMaxWidth(Double.MAX_VALUE);

        TextField seatField = new TextField();
        seatField.setPromptText("Place (ex: Zone A, Rang 12)");

        TextField priceField = new TextField();
        priceField.setPromptText("Prix (€)");

        Button buyBtn = new Button("✨ Générer Billet (Secured)");
        buyBtn.setStyle("-fx-background-color: #1a5f7a; -fx-text-fill: white; -fx-font-weight: bold;");
        buyBtn.setMaxWidth(Double.MAX_VALUE);

        buyBtn.setOnAction(e -> {
            if (ownerField.getText().isEmpty() || matchBox.getValue() == null || seatField.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir tous les champs.");
                return;
            }
            Ticket t = new Ticket(ownerField.getText(), matchBox.getValue(), seatField.getText());
            ticketDAO.saveTicket(t);
            refreshTable();
            ownerField.clear();
            seatField.clear();
            priceField.clear();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Billet généré et sécurisé sur la Blockchain!\nHash: " + t.getBlockchainHash());
        });

        generateBox.getChildren().addAll(
            generateTitle,
            new Label("Nom"), ownerField,
            new Label("Match"), matchBox,
            new Label("Place"), seatField,
            new Label("Prix (€)"), priceField,
            buyBtn
        );

        // Transfer Section
        VBox transferBox = new VBox(10);
        transferBox.setPadding(new Insets(15));
        transferBox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        Label transferTitle = new Label("🔄 Transfert de Billet");
        transferTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        transferTitle.setTextFill(Color.web("#1a5f7a"));

        Label hint = new Label("Sélectionnez un billet dans la table");
        hint.setStyle("-fx-text-fill: #888; -fx-font-style: italic;");

        TextField newOwnerField = new TextField();
        newOwnerField.setPromptText("Nouveau propriétaire");

        Button transferBtn = new Button("📤 Transférer Sélection");
        transferBtn.setStyle("-fx-background-color: #57c5b6; -fx-text-fill: white; -fx-font-weight: bold;");
        transferBtn.setMaxWidth(Double.MAX_VALUE);

        transferBtn.setOnAction(e -> {
            Ticket selected = table.getSelectionModel().getSelectedItem();
            if (selected != null && !newOwnerField.getText().isEmpty()) {
                ticketDAO.updateTicketOwner(selected.getId(), newOwnerField.getText());
                refreshTable();
                newOwnerField.clear();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Billet transféré avec succès!");
            } else {
                showAlert(Alert.AlertType.WARNING, "Erreur", "Sélectionnez un billet et entrez le nouveau propriétaire.");
            }
        });

        transferBox.getChildren().addAll(transferTitle, hint, new Label("Nouveau propriétaire"), newOwnerField, transferBtn);

        panel.getChildren().addAll(generateBox, transferBox);
        return panel;
    }

    private HBox createStatusFooter() {
        HBox box = new HBox(30);
        box.setPadding(new Insets(15, 20, 10, 20));
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle("-fx-background-color: #002b36; -fx-background-radius: 10;");

        Label sysStatus = new Label("🤖 Système IA: Actif");
        sysStatus.setTextFill(Color.web("#2ecc71"));
        sysStatus.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

        Separator sep = new Separator();
        sep.setOrientation(javafx.geometry.Orientation.VERTICAL);

        Label chainStatus = new Label("⛓️ Blockchain Node: Connecté (Bloc #49201)");
        chainStatus.setTextFill(Color.web("#3498db"));
        chainStatus.setFont(Font.font("Segoe UI", 12));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label version = new Label("Mondial 2030 - v1.0");
        version.setTextFill(Color.web("#666"));
        version.setFont(Font.font("Segoe UI", 11));

        box.getChildren().addAll(sysStatus, sep, chainStatus, spacer, version);

        VBox container = new VBox(box);
        container.setPadding(new Insets(15, 0, 0, 0));
        return box;
    }

    private void refreshTable() {
        List<Ticket> tickets = ticketDAO.getAllTickets();
        table.setItems(FXCollections.observableArrayList(tickets));
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public void stop() throws Exception {
        HibernateUtil.shutdown();
        super.stop();
    }
}
