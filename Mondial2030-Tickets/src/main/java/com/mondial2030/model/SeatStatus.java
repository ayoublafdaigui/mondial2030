package com.mondial2030.model;

/**
 * Enum representing the status of a seat in the stadium.
 */
public enum SeatStatus {
    AVAILABLE("Available"),
    SOLD("Sold"),
    RESERVED("Reserved"),
    BLOCKED("Blocked");
    
    private final String displayName;
    
    SeatStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
