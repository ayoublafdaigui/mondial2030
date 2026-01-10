package com.mondial2030.dao;

import com.mondial2030.model.Ticket;
import com.mondial2030.model.Ticket.TicketStatus;
import com.mondial2030.model.User;
import com.mondial2030.model.TicketTransfer;
import com.mondial2030.util.HibernateUtil;
import com.mondial2030.service.FraudDetectionService;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Ticket entity.
 * Handles all database operations for tickets including purchase and transfer.
 */
public class TicketDAO {

    private static final Logger LOGGER = Logger.getLogger(TicketDAO.class.getName());
    private final FraudDetectionService fraudService = new FraudDetectionService();

    /**
     * Save a new ticket to the database.
     * Automatically generates blockchain hash and initial fraud score.
     */
    public void saveTicket(Ticket ticket) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // Generate blockchain hash
            ticket.setBlockchainHash(generateBlockchainHash());
            
            // Calculate initial fraud score (should be 0 for new tickets)
            ticket.setFraudScore(fraudService.calculateFraudScore(ticket));
            
            session.persist(ticket);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error saving ticket", e);
        }
    }

    /**
     * Find a ticket by its ID.
     */
    public Optional<Ticket> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Ticket ticket = session.get(Ticket.class, id);
            return Optional.ofNullable(ticket);
        }
    }

    /**
     * Get all tickets from the database with eager loading of owner and match.
     */
    public List<Ticket> getAllTickets() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT t FROM Ticket t " +
                         "LEFT JOIN FETCH t.owner " +
                         "LEFT JOIN FETCH t.match " +
                         "ORDER BY t.purchaseDate DESC";
            return session.createQuery(hql, Ticket.class).list();
        }
    }

    /**
     * Get all tickets owned by a specific user with eager loading.
     */
    public List<Ticket> getTicketsByOwner(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT t FROM Ticket t " +
                         "LEFT JOIN FETCH t.owner " +
                         "LEFT JOIN FETCH t.match " +
                         "WHERE t.owner.id = :userId ORDER BY t.purchaseDate DESC";
            return session.createQuery(hql, Ticket.class)
                    .setParameter("userId", userId)
                    .list();
        }
    }

    /**
     * Get all tickets for a specific match with eager loading.
     */
    public List<Ticket> getTicketsByMatch(Long matchId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT t FROM Ticket t " +
                         "LEFT JOIN FETCH t.owner " +
                         "LEFT JOIN FETCH t.match " +
                         "WHERE t.match.id = :matchId";
            return session.createQuery(hql, Ticket.class)
                    .setParameter("matchId", matchId)
                    .list();
        }
    }

    /**
     * Get all high-risk tickets (fraud score > 0.7) with eager loading.
     */
    public List<Ticket> getHighRiskTickets() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT t FROM Ticket t " +
                         "LEFT JOIN FETCH t.owner " +
                         "LEFT JOIN FETCH t.match " +
                         "WHERE t.fraudScore > 0.7 ORDER BY t.fraudScore DESC";
            return session.createQuery(hql, Ticket.class).list();
        }
    }

    /**
     * Transfer a ticket to a new owner.
     * Updates fraud score based on transfer history.
     */
    public void transferTicket(Long ticketId, User newOwner, Double transferPrice) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            Ticket ticket = session.get(Ticket.class, ticketId);
            if (ticket != null && ticket.getStatus() == TicketStatus.ACTIVE) {
                User previousOwner = ticket.getOwner();
                
                // Create transfer record
                TicketTransfer transfer = new TicketTransfer(ticket, previousOwner, newOwner, transferPrice);
                transfer.setBlockchainTxHash(generateBlockchainHash());
                session.persist(transfer);
                
                // Update ticket
                ticket.setOwner(newOwner);
                ticket.incrementTransferCount();
                ticket.setBlockchainHash(generateBlockchainHash()); // New hash for transfer
                
                // Recalculate fraud score based on transfer count
                ticket.setFraudScore(fraudService.calculateFraudScore(ticket));
                
                session.merge(ticket);
            }
            
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error transferring ticket: " + ticketId, e);
        }
    }

    /**
     * Legacy method: Update ticket owner by name (for backward compatibility).
     */
    public void updateTicketOwner(Long ticketId, String newOwnerName) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Ticket ticket = session.get(Ticket.class, ticketId);
            if (ticket != null) {
                ticket.setOwnerName(newOwnerName);
                ticket.incrementTransferCount();
                ticket.setBlockchainHash(generateBlockchainHash());
                ticket.setFraudScore(fraudService.calculateFraudScore(ticket));
                session.merge(ticket);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error updating ticket owner: " + ticketId, e);
        }
    }

    /**
     * Update ticket status (ACTIVE, USED, CANCELLED, SUSPENDED).
     */
    public void updateTicketStatus(Long ticketId, TicketStatus status) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Ticket ticket = session.get(Ticket.class, ticketId);
            if (ticket != null) {
                ticket.setStatus(status);
                session.merge(ticket);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error updating ticket status: " + ticketId, e);
        }
    }

    /**
     * Update an existing ticket.
     */
    public void updateTicket(Ticket ticket) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(ticket);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error updating ticket", e);
        }
    }

    /**
     * Delete a ticket by its ID.
     */
    public void deleteTicket(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Ticket ticket = session.get(Ticket.class, id);
            if (ticket != null) {
                session.remove(ticket);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error deleting ticket: " + id, e);
        }
    }

    /**
     * Get the count of all tickets.
     */
    public long getTicketCount() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT COUNT(t) FROM Ticket t", Long.class).uniqueResult();
        }
    }

    /**
     * Get tickets by status.
     */
    public List<Ticket> getTicketsByStatus(TicketStatus status) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Ticket t WHERE t.status = :status";
            return session.createQuery(hql, Ticket.class)
                    .setParameter("status", status)
                    .list();
        }
    }

    /**
     * Find ticket by blockchain hash.
     */
    public Optional<Ticket> findByBlockchainHash(String hash) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Ticket t WHERE t.blockchainHash = :hash";
            Ticket ticket = session.createQuery(hql, Ticket.class)
                    .setParameter("hash", hash)
                    .uniqueResult();
            return Optional.ofNullable(ticket);
        }
    }

    /**
     * Generate a unique blockchain hash for security.
     */
    private String generateBlockchainHash() {
        return "BLK-" + UUID.randomUUID().toString().substring(0, 18).toUpperCase();
    }
}
