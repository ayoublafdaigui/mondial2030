package com.mondial2030.util;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * Validation Utility - Centralized data validation for the application.
 * Provides common validation methods for user inputs and business logic.
 */
public class ValidationUtil {

    // Email validation pattern (RFC 5322 simplified)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

    // Password requirements: At least 8 characters, with at least one letter and
    // one number
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$");

    /**
     * Validates an email address format.
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates a password strength.
     * Requirements: At least 8 characters, with at least one letter and one number.
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Validates that a string is not null or empty.
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Validates that a number is positive.
     */
    public static boolean isPositive(Number value) {
        return value != null && value.doubleValue() > 0;
    }

    /**
     * Validates that a number is non-negative (zero or positive).
     */
    public static boolean isNonNegative(Number value) {
        return value != null && value.doubleValue() >= 0;
    }

    /**
     * Validates that a price is valid (positive and has at most 2 decimal places).
     */
    public static boolean isValidPrice(Double price) {
        if (price == null || price <= 0) {
            return false;
        }
        // Check for at most 2 decimal places
        return Math.round(price * 100) == (price * 100);
    }

    /**
     * Validates that a date is in the future.
     */
    public static boolean isFutureDate(LocalDateTime date) {
        return date != null && date.isAfter(LocalDateTime.now());
    }

    /**
     * Validates that a date is in the past.
     */
    public static boolean isPastDate(LocalDateTime date) {
        return date != null && date.isBefore(LocalDateTime.now());
    }

    /**
     * Validates username format.
     * Requirements: 3-50 characters, alphanumeric and underscores only.
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return username.matches("^[a-zA-Z0-9_]{3,50}$");
    }

    /**
     * Validates a seat number format.
     * Expected format: Letter(s) followed by number(s), e.g., "A12", "VIP-101"
     */
    public static boolean isValidSeatNumber(String seatNumber) {
        if (seatNumber == null || seatNumber.trim().isEmpty()) {
            return false;
        }
        return seatNumber.matches("^[A-Z]{1,5}[-]?\\d{1,4}$");
    }

    /**
     * Validates a capacity value for stadiums.
     * Must be between 10,000 and 200,000.
     */
    public static boolean isValidStadiumCapacity(Integer capacity) {
        return capacity != null && capacity >= 10000 && capacity <= 200000;
    }

    /**
     * Sanitizes user input by removing potentially dangerous characters.
     * Basic XSS prevention.
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("[<>\"']", "").trim();
    }

    /**
     * Validates that a string length is within bounds.
     */
    public static boolean isValidLength(String value, int minLength, int maxLength) {
        if (value == null) {
            return false;
        }
        int length = value.trim().length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * Validates a phone number format (international).
     * Accepts formats like: +1234567890, +12 345 678 90, etc.
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        // Remove spaces and dashes for validation
        String cleaned = phoneNumber.replaceAll("[\\s-]", "");
        return cleaned.matches("^\\+?[1-9]\\d{6,14}$");
    }

    /**
     * Validates that a value is within a range.
     */
    public static boolean isInRange(Number value, Number min, Number max) {
        if (value == null || min == null || max == null) {
            return false;
        }
        double val = value.doubleValue();
        return val >= min.doubleValue() && val <= max.doubleValue();
    }

    /**
     * Gets a user-friendly error message for password validation.
     */
    public static String getPasswordRequirements() {
        return "Password must be at least 8 characters long and contain at least one letter and one number.";
    }

    /**
     * Gets a user-friendly error message for email validation.
     */
    public static String getEmailRequirements() {
        return "Please enter a valid email address (e.g., user@example.com).";
    }

    /**
     * Gets a user-friendly error message for username validation.
     */
    public static String getUsernameRequirements() {
        return "Username must be 3-50 characters long and contain only letters, numbers, and underscores.";
    }
}
