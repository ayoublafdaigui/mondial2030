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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Ticket Entity - Represents a ticket for a Mondial 2030 match.
 * Includes blockchain hash for security and fraud score for AI detection.
 */
@Entity
@Table(name = "tickets")
public class Ticket {

    public enum TicketStatus {
        ACTIVE, USED, CANCELLED, SUSPENDED
    }

    public enum TicketCategory {
        STANDARD, VIP, PREMIUM, HOSPITALITY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seat_number", nullable = false)
    private String seatNumber;

    @Column(name = "seat_zone")
    private String seatZone;

    @Column(nullable = false)
    private Double price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketCategory category = TicketCategory.STANDARD;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.ACTIVE;

    // Blockchain security: unique hash of the ticket
    @Column(name = "blockchain_hash", unique = true)
    private String blockchainHash;

    // AI fraud detection: score from 0.0 (safe) to 1.0 (high risk)
    @Column(name = "fraud_score")
    private Double fraudScore = 0.0;

    // Transfer tracking for fraud detection
    @Column(name = "transfer_count")
    private Integer transferCount = 0;

    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;

    @Column(name = "last_transfer_date")
    private LocalDateTime lastTransferDate;

    // Many Tickets belong to one User (owner)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    // Many Tickets belong to one Match
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    // Track transfer history
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketTransfer> transferHistory = new ArrayList<>();

    // Legacy field for backward compatibility
    @Transient
    private String ownerName;

    @Transient
    private String matchDetails;

    public Ticket() {
        this.purchaseDate = LocalDateTime.now();
    }

    public Ticket(String seatNumber, Double price, Match match, User owner) {
        this();
        this.seatNumber = seatNumber;
        this.price = price;
        this.match = match;
        this.owner = owner;
    }

    // Legacy constructor for backward compatibility
    public Ticket(String ownerName, String matchDetails, String seatNumber) {
        this();
        this.ownerName = ownerName;
        this.matchDetails = matchDetails;
        this.seatNumber = seatNumber;
        this.price = 100.0; // Default price
        this.fraudScore = 0.0;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public String getSeatZone() { return seatZone; }
    public void setSeatZone(String seatZone) { this.seatZone = seatZone; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public TicketCategory getCategory() { return category; }
    public void setCategory(TicketCategory category) { this.category = category; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }

    public String getBlockchainHash() { return blockchainHash; }
    public void setBlockchainHash(String blockchainHash) { this.blockchainHash = blockchainHash; }

    public Double getFraudScore() { return fraudScore != null ? fraudScore : Double.valueOf(0.0); }
    public void setFraudScore(Double fraudScore) { this.fraudScore = fraudScore; }

    // Legacy method for compatibility - handles null safely
    public double getFraudRiskScore() { return fraudScore != null ? fraudScore.doubleValue() : 0.0; }
    public void setFraudRiskScore(double score) { this.fraudScore = score; }

    public Integer getTransferCount() { return transferCount; }
    public void setTransferCount(Integer transferCount) { this.transferCount = transferCount; }

    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }

    public LocalDateTime getLastTransferDate() { return lastTransferDate; }
    public void setLastTransferDate(LocalDateTime lastTransferDate) { this.lastTransferDate = lastTransferDate; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public Match getMatch() { return match; }
    public void setMatch(Match match) { this.match = match; }

    public List<TicketTransfer> getTransferHistory() { return transferHistory; }
    public void setTransferHistory(List<TicketTransfer> transferHistory) { this.transferHistory = transferHistory; }

    // Legacy getters for backward compatibility - with null safety
    public String getOwnerName() {
        if (owner != null) {
            try {
                return owner.getName();
            } catch (Exception e) {
                // Handle LazyInitializationException
                return ownerName != null ? ownerName : "Available";
            }
        }
        return ownerName != null ? ownerName : "Available";
    }
    
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getMatchDetails() {
        if (match != null) {
            try {
                return match.getMatchDisplay();
            } catch (Exception e) {
                // Handle LazyInitializationException
                return matchDetails != null ? matchDetails : "Match Info";
            }
        }
        return matchDetails != null ? matchDetails : "Match Info";
    }

    public void setMatchDetails(String matchDetails) {
        this.matchDetails = matchDetails;
    }

    public void incrementTransferCount() {
        this.transferCount++;
        this.lastTransferDate = LocalDateTime.now();
    }

    public boolean isHighRisk() {
        return fraudScore != null && fraudScore > 0.7;
    }

    @Override
    public String toString() {
        String ownerDisplay = owner != null ? owner.getName() : ownerName;
        String matchDisplay = match != null ? match.getMatchDisplay() : matchDetails;
        return "Ticket #" + id + " - " + ownerDisplay + " (" + matchDisplay + ")";
    }
}
