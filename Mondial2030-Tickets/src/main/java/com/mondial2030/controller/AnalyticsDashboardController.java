package com.mondial2030.controller;

import com.mondial2030.model.Match;
import com.mondial2030.service.MatchService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controller for the Real-Time Analytics Dashboard.
 * Displays charts for ticket sales, revenue, and match statistics.
 */
public class AnalyticsDashboardController implements Initializable {

    @FXML private PieChart ticketsPieChart;
    @FXML private LineChart<String, Number> revenueLineChart;
    @FXML private BarChart<String, Number> stadiumBarChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private CategoryAxis stadiumXAxis;
    @FXML private NumberAxis stadiumYAxis;
    
    @FXML private Label totalRevenueLabel;
    @FXML private Label totalTicketsSoldLabel;
    @FXML private Label availableSeatsLabel;
    @FXML private Label totalMatchesLabel;
    @FXML private Label lastUpdatedLabel;
    @FXML private ComboBox<String> timeRangeComboBox;

    private final MatchService matchService = new MatchService();
    private List<Match> currentMatches = new ArrayList<>();

    private static final String[] TIME_RANGES = {
        "Last 7 Days", "Last 30 Days", "Last 3 Months", "All Time"
    };

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize time range combo box
        timeRangeComboBox.setItems(FXCollections.observableArrayList(TIME_RANGES));
        timeRangeComboBox.getSelectionModel().selectLast();
        timeRangeComboBox.setOnAction(e -> handleRefresh());
        
        // Load initial data
        loadData();
    }

    private void loadData() {
        // Try to load from service, fallback to mock data
        currentMatches = matchService.getAllMatches();
        if (currentMatches.isEmpty()) {
            currentMatches = createMockMatches();
        }
        
        updateCharts(currentMatches);
        updateSummaryCards(currentMatches);
        updateLastUpdated();
    }

    /**
     * Updates all charts with the provided match data.
     * This method can be called externally to refresh the dashboard.
     *
     * @param matches List of matches to display in the charts
     */
    public void updateCharts(List<Match> matches) {
        updatePieChart(matches);
        updateRevenueLineChart(matches);
        updateStadiumBarChart(matches);
    }

    private void updatePieChart(List<Match> matches) {
        int totalSold = 0;
        int totalAvailable = 0;
        
        for (Match match : matches) {
            int sold = match.getTotalSeats() - match.getAvailableSeats();
            totalSold += sold;
            totalAvailable += match.getAvailableSeats();
        }

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Sold (" + totalSold + ")", totalSold),
            new PieChart.Data("Available (" + totalAvailable + ")", totalAvailable)
        );

        ticketsPieChart.setData(pieChartData);
        
        // Apply colors via CSS classes or inline styles
        if (pieChartData.size() >= 2) {
            pieChartData.get(0).getNode().setStyle("-fx-pie-color: #4CAF50;"); // Green for sold
            pieChartData.get(1).getNode().setStyle("-fx-pie-color: #2196F3;"); // Blue for available
        }
    }

    private void updateRevenueLineChart(List<Match> matches) {
        revenueLineChart.getData().clear();
        
        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenue");
        
        XYChart.Series<String, Number> ticketsSeries = new XYChart.Series<>();
        ticketsSeries.setName("Tickets Sold");

        for (Match match : matches) {
            String matchLabel = shortenTeamName(match.getHomeTeam()) + " vs " + 
                               shortenTeamName(match.getAwayTeam());
            
            int ticketsSold = match.getTotalSeats() - match.getAvailableSeats();
            double revenue = ticketsSold * match.getTicketPrice();
            
            revenueSeries.getData().add(new XYChart.Data<>(matchLabel, revenue));
            ticketsSeries.getData().add(new XYChart.Data<>(matchLabel, ticketsSold));
        }

        revenueLineChart.getData().add(revenueSeries);
    }

    private void updateStadiumBarChart(List<Match> matches) {
        stadiumBarChart.getData().clear();
        
        // Group by stadium
        Map<String, Integer> stadiumSales = new LinkedHashMap<>();
        
        for (Match match : matches) {
            String stadium = shortenStadiumName(match.getStadium());
            int ticketsSold = match.getTotalSeats() - match.getAvailableSeats();
            stadiumSales.merge(stadium, ticketsSold, Integer::sum);
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Tickets Sold");

        stadiumSales.forEach((stadium, sales) -> 
            series.getData().add(new XYChart.Data<>(stadium, sales))
        );

        stadiumBarChart.getData().add(series);
    }

    private void updateSummaryCards(List<Match> matches) {
        double totalRevenue = 0;
        int totalSold = 0;
        int totalAvailable = 0;
        
        for (Match match : matches) {
            int sold = match.getTotalSeats() - match.getAvailableSeats();
            totalSold += sold;
            totalAvailable += match.getAvailableSeats();
            totalRevenue += sold * match.getTicketPrice();
        }
        
        totalRevenueLabel.setText(String.format("$%,.2f", totalRevenue));
        totalTicketsSoldLabel.setText(String.format("%,d", totalSold));
        availableSeatsLabel.setText(String.format("%,d", totalAvailable));
        totalMatchesLabel.setText(String.valueOf(matches.size()));
    }

    private void updateLastUpdated() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        lastUpdatedLabel.setText("Last updated: " + LocalDateTime.now().format(formatter));
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    @FXML
    private void handleExport() {
        // Export functionality - could generate CSV or PDF report
        System.out.println("Exporting analytics report...");
        // TODO: Implement export functionality
    }

    private String shortenTeamName(String teamName) {
        if (teamName == null) return "TBD";
        return teamName.length() > 8 ? teamName.substring(0, 6) + ".." : teamName;
    }

    private String shortenStadiumName(String stadium) {
        if (stadium == null) return "Unknown";
        // Extract just the city or short name
        if (stadium.contains("(")) {
            int start = stadium.indexOf("(");
            int end = stadium.indexOf(",");
            if (end == -1) end = stadium.indexOf(")");
            return stadium.substring(start + 1, end);
        }
        return stadium.length() > 15 ? stadium.substring(0, 12) + "..." : stadium;
    }

    /**
     * Creates mock match data for demonstration purposes.
     */
    private List<Match> createMockMatches() {
        List<Match> matches = new ArrayList<>();
        
        String[][] matchData = {
            {"Morocco", "Spain", "Grand Stade de Casablanca (Morocco)", "150.0", "50000", "32000"},
            {"Portugal", "Brazil", "Estádio da Luz (Lisbon, Portugal)", "200.0", "65000", "48000"},
            {"France", "Argentina", "Santiago Bernabéu (Madrid, Spain)", "250.0", "81000", "75000"},
            {"Germany", "England", "Camp Nou (Barcelona, Spain)", "180.0", "99000", "85000"},
            {"Netherlands", "Italy", "Stade Mohammed V (Casablanca, Morocco)", "120.0", "45000", "28000"},
            {"Belgium", "Croatia", "Estádio do Dragão (Porto, Portugal)", "140.0", "50000", "35000"},
            {"Uruguay", "Mexico", "Grand Stade de Marrakech (Morocco)", "100.0", "45000", "22000"},
            {"USA", "Japan", "La Cartuja (Seville, Spain)", "130.0", "57000", "40000"}
        };
        
        long id = 1;
        for (String[] data : matchData) {
            Match match = new Match();
            match.setId(id++);
            match.setHomeTeam(data[0]);
            match.setAwayTeam(data[1]);
            match.setStadium(data[2]);
            match.setTicketPrice(Double.parseDouble(data[3]));
            match.setTotalSeats(Integer.parseInt(data[4]));
            match.setAvailableSeats(Integer.parseInt(data[5]));
            matches.add(match);
        }
        
        return matches;
    }

    /**
     * Gets the current total revenue from all matches.
     * Useful for external components needing analytics data.
     */
    public double getTotalRevenue() {
        return currentMatches.stream()
            .mapToDouble(m -> (m.getTotalSeats() - m.getAvailableSeats()) * m.getTicketPrice())
            .sum();
    }

    /**
     * Gets the total number of tickets sold across all matches.
     */
    public int getTotalTicketsSold() {
        return currentMatches.stream()
            .mapToInt(m -> m.getTotalSeats() - m.getAvailableSeats())
            .sum();
    }
}
