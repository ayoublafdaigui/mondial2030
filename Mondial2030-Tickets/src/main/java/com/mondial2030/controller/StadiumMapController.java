package com.mondial2030.controller;

import com.mondial2030.model.Match;
import com.mondial2030.model.Seat;
import com.mondial2030.model.SeatStatus;
import com.mondial2030.service.MatchService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.*;

/**
 * Controller for the Interactive Stadium Map.
 * Displays a grid of seats with different statuses and allows seat selection.
 */
public class StadiumMapController implements Initializable {

    @FXML private GridPane seatGrid;
    @FXML private ComboBox<String> sectionComboBox;
    @FXML private ComboBox<Match> matchComboBox;
    @FXML private Label selectedSeatsLabel;
    @FXML private Label totalPriceLabel;
    @FXML private Label statusLabel;

    private static final int ROWS = 10;
    private static final int COLS = 10;
    
    private final Map<String, Seat> seats = new HashMap<>();
    private final Set<String> selectedSeatIds = new LinkedHashSet<>();
    private double ticketPrice = 100.0;
    
    private final MatchService matchService = new MatchService();

    // Color constants for seat statuses
    private static final Color AVAILABLE_COLOR = Color.rgb(76, 175, 80);    // Green
    private static final Color SOLD_COLOR = Color.rgb(244, 67, 54);          // Red
    private static final Color SELECTED_COLOR = Color.rgb(33, 150, 243);     // Blue
    private static final Color RESERVED_COLOR = Color.rgb(255, 193, 7);      // Amber
    private static final Color HOVER_COLOR = Color.rgb(129, 199, 132);       // Light Green

    private static final String[] SECTIONS = {
        "Section A - VIP", "Section B - Premium", "Section C - Standard",
        "Section D - Standard", "Section E - Economy", "Section F - Economy"
    };

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize sections
        sectionComboBox.setItems(FXCollections.observableArrayList(SECTIONS));
        sectionComboBox.getSelectionModel().selectFirst();
        sectionComboBox.setOnAction(e -> refreshSeatGrid());
        
        // Initialize matches (mock data for now)
        loadMatches();
        matchComboBox.setOnAction(e -> {
            Match selected = matchComboBox.getValue();
            if (selected != null) {
                ticketPrice = selected.getTicketPrice();
                refreshSeatGrid();
            }
        });
        
        // Build initial seat grid
        buildSeatGrid();
    }

    private void loadMatches() {
        // Try to load from service, fallback to mock data
        List<Match> matches = matchService.getAllMatches();
        if (matches.isEmpty()) {
            // Create mock matches for demonstration
            matches = createMockMatches();
        }
        matchComboBox.setItems(FXCollections.observableArrayList(matches));
        if (!matches.isEmpty()) {
            matchComboBox.getSelectionModel().selectFirst();
            ticketPrice = matches.get(0).getTicketPrice();
        }
    }

    private List<Match> createMockMatches() {
        List<Match> mockMatches = new ArrayList<>();
        
        Match match1 = new Match();
        match1.setId(1L);
        match1.setHomeTeam("Morocco");
        match1.setAwayTeam("Spain");
        match1.setTicketPrice(150.0);
        mockMatches.add(match1);
        
        Match match2 = new Match();
        match2.setId(2L);
        match2.setHomeTeam("Portugal");
        match2.setAwayTeam("Brazil");
        match2.setTicketPrice(200.0);
        mockMatches.add(match2);
        
        Match match3 = new Match();
        match3.setId(3L);
        match3.setHomeTeam("France");
        match3.setAwayTeam("Argentina");
        match3.setTicketPrice(250.0);
        mockMatches.add(match3);
        
        return mockMatches;
    }

    private void buildSeatGrid() {
        seatGrid.getChildren().clear();
        seats.clear();
        
        String section = sectionComboBox.getValue();
        if (section == null) section = "A";
        
        // Add row labels
        for (int row = 0; row < ROWS; row++) {
            Label rowLabel = new Label(String.valueOf((char) ('A' + row)));
            rowLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 25;");
            seatGrid.add(rowLabel, 0, row + 1);
        }
        
        // Add column labels
        for (int col = 0; col < COLS; col++) {
            Label colLabel = new Label(String.valueOf(col + 1));
            colLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 35; -fx-alignment: center;");
            seatGrid.add(colLabel, col + 1, 0);
        }
        
        // Create seats
        Random random = new Random(section.hashCode()); // Consistent random for same section
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                String seatId = section.charAt(8) + "-" + (char) ('A' + row) + (col + 1);
                
                // Randomly assign status for demonstration
                SeatStatus status;
                int rand = random.nextInt(100);
                if (rand < 30) {
                    status = SeatStatus.SOLD;
                } else if (rand < 40) {
                    status = SeatStatus.RESERVED;
                } else {
                    status = SeatStatus.AVAILABLE;
                }
                
                Seat seat = new Seat(seatId, row, col, status);
                seats.put(seatId, seat);
                
                Rectangle seatRect = createSeatRectangle(seat);
                seatGrid.add(seatRect, col + 1, row + 1);
            }
        }
    }

    private Rectangle createSeatRectangle(Seat seat) {
        Rectangle rect = new Rectangle(35, 35);
        rect.setArcWidth(8);
        rect.setArcHeight(8);
        rect.setStroke(Color.DARKGRAY);
        rect.setStrokeWidth(1);
        
        updateSeatColor(rect, seat);
        
        // Store seat ID in user data
        rect.setUserData(seat.getSeatId());
        
        // Add tooltip
        Tooltip tooltip = new Tooltip(
            "Seat: " + seat.getSeatId() + 
            "\nStatus: " + seat.getStatus() +
            "\nPrice: $" + String.format("%.2f", ticketPrice)
        );
        Tooltip.install(rect, tooltip);
        
        // Add click handler
        rect.setOnMouseClicked(event -> handleSeatClick(seat, rect));
        
        // Add hover effects for available seats
        rect.setOnMouseEntered(event -> {
            if (seat.getStatus() == SeatStatus.AVAILABLE && !selectedSeatIds.contains(seat.getSeatId())) {
                rect.setFill(HOVER_COLOR);
                rect.setCursor(javafx.scene.Cursor.HAND);
            }
        });
        
        rect.setOnMouseExited(event -> {
            if (!selectedSeatIds.contains(seat.getSeatId())) {
                updateSeatColor(rect, seat);
            }
            rect.setCursor(javafx.scene.Cursor.DEFAULT);
        });
        
        return rect;
    }

    private void updateSeatColor(Rectangle rect, Seat seat) {
        if (selectedSeatIds.contains(seat.getSeatId())) {
            rect.setFill(SELECTED_COLOR);
        } else {
            switch (seat.getStatus()) {
                case AVAILABLE -> rect.setFill(AVAILABLE_COLOR);
                case SOLD -> rect.setFill(SOLD_COLOR);
                case RESERVED -> rect.setFill(RESERVED_COLOR);
                default -> rect.setFill(Color.GRAY);
            }
        }
    }

    private void handleSeatClick(Seat seat, Rectangle rect) {
        String seatId = seat.getSeatId();
        
        if (seat.getStatus() == SeatStatus.SOLD) {
            showStatus("Seat " + seatId + " is already sold.", true);
            return;
        }
        
        if (seat.getStatus() == SeatStatus.RESERVED) {
            showStatus("Seat " + seatId + " is reserved.", true);
            return;
        }
        
        // Toggle selection for available seats
        if (selectedSeatIds.contains(seatId)) {
            selectedSeatIds.remove(seatId);
            updateSeatColor(rect, seat);
            showStatus("Deselected seat: " + seatId, false);
        } else {
            selectedSeatIds.add(seatId);
            rect.setFill(SELECTED_COLOR);
            showStatus("Selected seat: " + seatId, false);
        }
        
        updateSelectionInfo();
    }

    private void updateSelectionInfo() {
        if (selectedSeatIds.isEmpty()) {
            selectedSeatsLabel.setText("None");
            totalPriceLabel.setText("$0.00");
        } else {
            selectedSeatsLabel.setText(String.join(", ", selectedSeatIds));
            double total = selectedSeatIds.size() * ticketPrice;
            totalPriceLabel.setText(String.format("$%.2f", total));
        }
    }

    @FXML
    private void handleBookSeats() {
        if (selectedSeatIds.isEmpty()) {
            showStatus("Please select at least one seat to book.", true);
            return;
        }
        
        // Mark selected seats as sold
        for (String seatId : selectedSeatIds) {
            Seat seat = seats.get(seatId);
            if (seat != null) {
                seat.setStatus(SeatStatus.SOLD);
            }
        }
        
        int count = selectedSeatIds.size();
        double total = count * ticketPrice;
        
        showStatus(String.format("Successfully booked %d seat(s) for $%.2f!", count, total), false);
        
        selectedSeatIds.clear();
        refreshSeatGrid();
    }

    @FXML
    private void handleClearSelection() {
        selectedSeatIds.clear();
        updateSelectionInfo();
        refreshSeatGrid();
        showStatus("Selection cleared.", false);
    }

    private void refreshSeatGrid() {
        buildSeatGrid();
        
        // Restore selections
        for (String seatId : selectedSeatIds) {
            Seat seat = seats.get(seatId);
            if (seat != null && seat.getStatus() == SeatStatus.AVAILABLE) {
                // Find and update the rectangle
                seatGrid.getChildren().stream()
                    .filter(node -> node instanceof Rectangle && seatId.equals(node.getUserData()))
                    .findFirst()
                    .ifPresent(node -> ((Rectangle) node).setFill(SELECTED_COLOR));
            }
        }
        
        updateSelectionInfo();
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle(isError ? "-fx-text-fill: #f44336;" : "-fx-text-fill: #4caf50;");
    }
}
