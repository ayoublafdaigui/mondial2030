package com.mondial2030.dao;

import com.mondial2030.model.Stadium;
import com.mondial2030.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Stadium entity.
 * Handles all database operations for stadiums.
 */
public class StadiumDAO {

    private static final Logger LOGGER = Logger.getLogger(StadiumDAO.class.getName());

    /**
     * Save a new stadium to the database.
     */
    public void saveStadium(Stadium stadium) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(stadium);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error saving stadium: " + stadium.getName(), e);
        }
    }

    /**
     * Find a stadium by its ID.
     */
    public Optional<Stadium> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Stadium stadium = session.get(Stadium.class, id);
            return Optional.ofNullable(stadium);
        }
    }

    /**
     * Find a stadium by its name.
     */
    public Optional<Stadium> findByName(String name) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Stadium s WHERE s.name = :name";
            Stadium stadium = session.createQuery(hql, Stadium.class)
                    .setParameter("name", name)
                    .uniqueResult();
            return Optional.ofNullable(stadium);
        }
    }

    /**
     * Get all stadiums from the database.
     */
    public List<Stadium> getAllStadiums() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Stadium ORDER BY capacity DESC", Stadium.class).list();
        }
    }

    /**
     * Get stadiums by country.
     */
    public List<Stadium> getStadiumsByCountry(String country) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Stadium s WHERE s.country = :country ORDER BY s.capacity DESC";
            return session.createQuery(hql, Stadium.class)
                    .setParameter("country", country)
                    .list();
        }
    }

    /**
     * Get stadiums by city.
     */
    public List<Stadium> getStadiumsByCity(String city) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Stadium s WHERE s.city = :city ORDER BY s.capacity DESC";
            return session.createQuery(hql, Stadium.class)
                    .setParameter("city", city)
                    .list();
        }
    }

    /**
     * Get main venues (for finals, semi-finals).
     */
    public List<Stadium> getMainVenues() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Stadium s WHERE s.isMainVenue = true ORDER BY s.capacity DESC";
            return session.createQuery(hql, Stadium.class).list();
        }
    }

    /**
     * Get the count of all stadiums.
     */
    public long getStadiumCount() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT COUNT(s) FROM Stadium s", Long.class).uniqueResult();
        }
    }

    /**
     * Update an existing stadium.
     */
    public void updateStadium(Stadium stadium) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(stadium);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error updating stadium: " + stadium.getName(), e);
        }
    }

    /**
     * Delete a stadium by its ID.
     */
    public void deleteStadium(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Stadium stadium = session.get(Stadium.class, id);
            if (stadium != null) {
                session.remove(stadium);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error deleting stadium with id: " + id, e);
        }
    }
}
