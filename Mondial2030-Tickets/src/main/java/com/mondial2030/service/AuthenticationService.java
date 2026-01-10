package com.mondial2030.service;

import com.mondial2030.model.User;
import com.mondial2030.dao.UserDAO;

import java.util.Optional;

/**
 * Authentication Service - Handles user login and session management.
 */
public class AuthenticationService {

    private static AuthenticationService instance;
    private UserDAO userDAO;
    private User currentUser;

    private AuthenticationService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Get the singleton instance of AuthenticationService.
     */
    public static synchronized AuthenticationService getInstance() {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }

    /**
     * Authenticate a user with username and password.
     * @return true if authentication succeeds, false otherwise.
     */
    public boolean login(String username, String password) {
        Optional<User> user = userDAO.authenticate(username, password);
        if (user.isPresent()) {
            this.currentUser = user.get();
            return true;
        }
        return false;
    }

    /**
     * Log out the current user.
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Get the currently logged-in user.
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if a user is currently logged in.
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Check if the current user is an admin.
     */
    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    /**
     * Check if the current user is a regular user.
     */
    public boolean isRegularUser() {
        return currentUser != null && !currentUser.isAdmin();
    }

    /**
     * Get the current user's role as a string.
     */
    public String getCurrentUserRole() {
        if (currentUser == null) {
            return "GUEST";
        }
        return currentUser.getRole().toString();
    }

    /**
     * Register a new user.
     */
    public boolean register(String username, String password, String name, String email) {
        if (userDAO.usernameExists(username)) {
            return false;
        }

        User newUser = new User(username, password, name, email, User.Role.USER);
        userDAO.saveUser(newUser);
        return true;
    }
}
