package com.mondial2030.dao;

import com.mondial2030.model.User;
import com.mondial2030.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for User entity.
 * Handles all database operations for users including authentication.
 */
public class UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    /**
     * Save a new user to the database.
     * Password is hashed before storage.
     */
    public void saveUser(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // Hash the password before saving
            user.setPassword(hashPassword(user.getPassword()));
            
            session.persist(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error saving user: " + user.getUsername(), e);
        }
    }

    /**
     * Find a user by their username.
     */
    public Optional<User> findByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM User u WHERE u.username = :username";
            User user = session.createQuery(hql, User.class)
                    .setParameter("username", username)
                    .uniqueResult();
            return Optional.ofNullable(user);
        }
    }

    /**
     * Authenticate a user with username and password.
     * Returns the user if credentials are valid, empty otherwise.
     */
    public Optional<User> authenticate(String username, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hashedPassword = hashPassword(password);
            String hql = "FROM User u WHERE u.username = :username AND u.password = :password";
            User user = session.createQuery(hql, User.class)
                    .setParameter("username", username)
                    .setParameter("password", hashedPassword)
                    .uniqueResult();
            
            if (user != null) {
                // Update last login time
                updateLastLogin(user.getId());
            }
            
            return Optional.ofNullable(user);
        }
    }

    /**
     * Update the last login timestamp for a user.
     */
    public void updateLastLogin(Long userId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User user = session.get(User.class, userId);
            if (user != null) {
                user.setLastLogin(LocalDateTime.now());
                session.merge(user);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error updating last login for user: " + userId, e);
        }
    }

    /**
     * Find a user by their ID.
     */
    public Optional<User> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, id);
            return Optional.ofNullable(user);
        }
    }

    /**
     * Get all users from the database.
     */
    public List<User> getAllUsers() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM User", User.class).list();
        }
    }

    /**
     * Get all users with a specific role.
     */
    public List<User> getUsersByRole(User.Role role) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM User u WHERE u.role = :role";
            return session.createQuery(hql, User.class)
                    .setParameter("role", role)
                    .list();
        }
    }

    /**
     * Update an existing user.
     */
    public void updateUser(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error updating user", e);
        }
    }

    /**
     * Delete a user by their ID.
     */
    public void deleteUser(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User user = session.get(User.class, id);
            if (user != null) {
                session.remove(user);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            LOGGER.log(Level.SEVERE, "Error deleting user: " + id, e);
        }
    }

    /**
     * Check if a username already exists.
     */
    public boolean usernameExists(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(u) FROM User u WHERE u.username = :username";
            Long count = session.createQuery(hql, Long.class)
                    .setParameter("username", username)
                    .uniqueResult();
            return count != null && count > 0;
        }
    }

    /**
     * Get the count of all users.
     */
    public long getUserCount() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT COUNT(u) FROM User u", Long.class).uniqueResult();
        }
    }

    /**
     * Hash a password using SHA-256.
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
}
