package com.mondial2030.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Match Entity - Represents a football match in the Mondial 2030.
 * A Match has many Tickets.
 */
@Entity
@Table(name = "matches")
public class Match {

    public enum MatchPhase {
        GROUP_STAGE, ROUND_OF_16, QUARTER_FINAL, SEMI_FINAL, THIRD_PLACE, FINAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "home_team", nullable = false)
    private String homeTeam;

    @Column(name = "away_team", nullable = false)
    private String awayTeam;

    @Column(name = "match_date", nullable = false)
    private LocalDateTime matchDate;

    @Column(nullable = false)
    private String stadium;

    @Column(nullable = false)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchPhase phase = MatchPhase.GROUP_STAGE;

    @Column(name = "base_price")
    private Double basePrice;

    @Column(name = "total_seats")
    private Integer totalSeats;

    @Column(name = "available_seats")
    private Integer availableSeats;

    // One Match has many Tickets
    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ticket> tickets = new ArrayList<>();

    public Match() {}

    public Match(String homeTeam, String awayTeam, LocalDateTime matchDate, String stadium, String city, MatchPhase phase, Double basePrice, Integer totalSeats) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.matchDate = matchDate;
        this.stadium = stadium;
        this.city = city;
        this.phase = phase;
        this.basePrice = basePrice;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getHomeTeam() { return homeTeam; }
    public void setHomeTeam(String homeTeam) { this.homeTeam = homeTeam; }

    public String getAwayTeam() { return awayTeam; }
    public void setAwayTeam(String awayTeam) { this.awayTeam = awayTeam; }

    public LocalDateTime getMatchDate() { return matchDate; }
    public void setMatchDate(LocalDateTime matchDate) { this.matchDate = matchDate; }

    public String getStadium() { return stadium; }
    public void setStadium(String stadium) { this.stadium = stadium; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public MatchPhase getPhase() { return phase; }
    public void setPhase(MatchPhase phase) { this.phase = phase; }

    public Double getBasePrice() { return basePrice; }
    public void setBasePrice(Double basePrice) { this.basePrice = basePrice; }

    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }

    public Integer getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(Integer availableSeats) { this.availableSeats = availableSeats; }

    // Alias methods for compatibility with new controllers
    public LocalDateTime getMatchDateTime() { return matchDate; }
    public void setMatchDateTime(LocalDateTime matchDateTime) { this.matchDate = matchDateTime; }
    
    public Double getTicketPrice() { return basePrice != null ? basePrice : Double.valueOf(0.0); }
    public void setTicketPrice(Double ticketPrice) { this.basePrice = ticketPrice; }

    public List<Ticket> getTickets() { return tickets; }
    public void setTickets(List<Ticket> tickets) { this.tickets = tickets; }

    public void addTicket(Ticket ticket) {
        tickets.add(ticket);
        ticket.setMatch(this);
        if (availableSeats > 0) {
            availableSeats--;
        }
    }

    public String getMatchDisplay() {
        return homeTeam + " vs " + awayTeam;
    }

    public String getFullDescription() {
        return homeTeam + " vs " + awayTeam + " - " + stadium + ", " + city;
    }

    @Override
    public String toString() {
        return "Match{" + homeTeam + " vs " + awayTeam + " at " + stadium + " on " + matchDate + "}";
    }
}
