package com.mondial2030.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * TicketTransfer Entity - Tracks the ownership history of tickets.
 * Used by the Fraud Detection service to calculate fraud scores.
 */
@Entity
@Table(name = "ticket_transfers")
public class TicketTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id")
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser;

    @Column(name = "transfer_date", nullable = false)
    private LocalDateTime transferDate;

    @Column(name = "transfer_price")
    private Double transferPrice;

    @Column(name = "blockchain_tx_hash")
    private String blockchainTxHash;

    @Column(name = "notes")
    private String notes;

    public TicketTransfer() {
        this.transferDate = LocalDateTime.now();
    }

    public TicketTransfer(Ticket ticket, User fromUser, User toUser, Double transferPrice) {
        this();
        this.ticket = ticket;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.transferPrice = transferPrice;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }

    public User getFromUser() { return fromUser; }
    public void setFromUser(User fromUser) { this.fromUser = fromUser; }

    public User getToUser() { return toUser; }
    public void setToUser(User toUser) { this.toUser = toUser; }

    public LocalDateTime getTransferDate() { return transferDate; }
    public void setTransferDate(LocalDateTime transferDate) { this.transferDate = transferDate; }

    public Double getTransferPrice() { return transferPrice; }
    public void setTransferPrice(Double transferPrice) { this.transferPrice = transferPrice; }

    public String getBlockchainTxHash() { return blockchainTxHash; }
    public void setBlockchainTxHash(String blockchainTxHash) { this.blockchainTxHash = blockchainTxHash; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return "TicketTransfer{ticket=" + ticket.getId() + 
               ", from=" + (fromUser != null ? fromUser.getUsername() : "ORIGINAL_PURCHASE") +
               ", to=" + toUser.getUsername() + 
               ", date=" + transferDate + "}";
    }
}
