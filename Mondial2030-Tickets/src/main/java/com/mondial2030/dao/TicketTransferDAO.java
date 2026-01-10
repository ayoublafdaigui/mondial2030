package com.mondial2030.dao;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.mondial2030.model.TicketTransfer;
import com.mondial2030.util.HibernateUtil;

/**
 * Data Access Object for TicketTransfer entity.
 * Handles tracking of ticket ownership changes for fraud detection.
 */
public class TicketTransferDAO {
    
    private static final Logger LOGGER = Logger.getLogger(TicketTransferDAO.class.getName());

    /**
     * Save a new ticket transfer to the database.
     */
    public void saveTransfer(TicketTransfer transfer) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(transfer);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            LOGGER.log(Level.SEVERE, "Error saving transfer", e);
        }
    }

    /**
     * Get all transfers for a specific ticket.
     */
    public List<TicketTransfer> getTransfersByTicket(Long ticketId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM TicketTransfer t WHERE t.ticket.id = :ticketId ORDER BY t.transferDate";
            return session.createQuery(hql, TicketTransfer.class)
                    .setParameter("ticketId", ticketId)
                    .list();
        }
    }

    /**
     * Get the count of transfers for a specific ticket.
     */
    public long getTransferCount(Long ticketId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(t) FROM TicketTransfer t WHERE t.ticket.id = :ticketId";
            Long count = session.createQuery(hql, Long.class)
                    .setParameter("ticketId", ticketId)
                    .uniqueResult();
            return count != null ? count : 0;
        }
    }

    /**
     * Get all transfers where a user was the recipient.
     */
    public List<TicketTransfer> getTransfersToUser(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM TicketTransfer t WHERE t.toUser.id = :userId ORDER BY t.transferDate DESC";
            return session.createQuery(hql, TicketTransfer.class)
                    .setParameter("userId", userId)
                    .list();
        }
    }

    /**
     * Get all transfers where a user was the sender.
     */
    public List<TicketTransfer> getTransfersFromUser(Long userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM TicketTransfer t WHERE t.fromUser.id = :userId ORDER BY t.transferDate DESC";
            return session.createQuery(hql, TicketTransfer.class)
                    .setParameter("userId", userId)
                    .list();
        }
    }

    /**
     * Get all recent transfers (for admin dashboard).
     */
    public List<TicketTransfer> getRecentTransfers(int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM TicketTransfer t ORDER BY t.transferDate DESC";
            return session.createQuery(hql, TicketTransfer.class)
                    .setMaxResults(limit)
                    .list();
        }
    }

    /**
     * Get all transfers (for reporting).
     */
    public List<TicketTransfer> getAllTransfers() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM TicketTransfer ORDER BY transferDate DESC", TicketTransfer.class).list();
        }
    }

    /**
     * Find transfer by ID.
     */
    public Optional<TicketTransfer> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            TicketTransfer transfer = session.get(TicketTransfer.class, id);
            return Optional.ofNullable(transfer);
        }
    }

    /**
     * Get total transfer count (for statistics).
     */
    public long getTotalTransferCount() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT COUNT(t) FROM TicketTransfer t", Long.class).uniqueResult();
        }
    }
}
