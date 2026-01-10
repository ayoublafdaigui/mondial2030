package com.mondial2030.dao;

import com.mondial2030.model.Match;
import com.mondial2030.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Match entity.
 * Handles all database operations for football matches.
 */
public class MatchDAO {

    private static final Logger LOGGER = Logger.getLogger(MatchDAO.class.getName());

    /**
     * Save a new match to the database.
     */
    public void saveMatch(Match match) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(match);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error saving match", e);
        }
    }

    /**
     * Find a match by its ID.
     */
    public Optional<Match> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Match match = session.get(Match.class, id);
            return Optional.ofNullable(match);
        }
    }

    /**
     * Get all matches from the database.
     */
    public List<Match> getAllMatches() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Match ORDER BY matchDate", Match.class).list();
        }
    }

    /**
     * Get all upcoming matches (matches with date after now).
     */
    public List<Match> getUpcomingMatches() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Match m WHERE m.matchDate > :now ORDER BY m.matchDate";
            return session.createQuery(hql, Match.class)
                    .setParameter("now", LocalDateTime.now())
                    .list();
        }
    }

    /**
     * Get matches by phase (GROUP_STAGE, FINAL, etc.).
     */
    public List<Match> getMatchesByPhase(Match.MatchPhase phase) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Match m WHERE m.phase = :phase ORDER BY m.matchDate";
            return session.createQuery(hql, Match.class)
                    .setParameter("phase", phase)
                    .list();
        }
    }

    /**
     * Get matches by city.
     */
    public List<Match> getMatchesByCity(String city) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Match m WHERE m.city = :city ORDER BY m.matchDate";
            return session.createQuery(hql, Match.class)
                    .setParameter("city", city)
                    .list();
        }
    }

    /**
     * Get matches involving a specific team.
     */
    public List<Match> getMatchesByTeam(String team) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Match m WHERE m.homeTeam = :team OR m.awayTeam = :team ORDER BY m.matchDate";
            return session.createQuery(hql, Match.class)
                    .setParameter("team", team)
                    .list();
        }
    }

    /**
     * Update an existing match.
     */
    public void updateMatch(Match match) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(match);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error updating match", e);
        }
    }

    /**
     * Delete a match by its ID.
     */
    public void deleteMatch(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Match match = session.get(Match.class, id);
            if (match != null) {
                session.remove(match);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error deleting match: " + id, e);
        }
    }

    /**
     * Check if seats are available for a match.
     */
    public boolean hasAvailableSeats(Long matchId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Match match = session.get(Match.class, matchId);
            return match != null && match.getAvailableSeats() > 0;
        }
    }

    /**
     * Decrement available seats for a match.
     */
    public void decrementAvailableSeats(Long matchId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Match match = session.get(Match.class, matchId);
            if (match != null && match.getAvailableSeats() > 0) {
                match.setAvailableSeats(match.getAvailableSeats() - 1);
                session.merge(match);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error decrementing available seats for match: " + matchId, e);
        }
    }

    /**
     * Get the count of all matches.
     */
    public long getMatchCount() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT COUNT(m) FROM Match m", Long.class).uniqueResult();
        }
    }

    /**
     * Get all distinct cities hosting matches.
     */
    public List<String> getAllHostCities() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT DISTINCT m.city FROM Match m ORDER BY m.city", String.class).list();
        }
    }
}
