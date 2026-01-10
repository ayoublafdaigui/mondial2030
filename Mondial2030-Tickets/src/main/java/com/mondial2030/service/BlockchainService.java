package com.mondial2030.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Blockchain Service - Simulates blockchain operations for ticket security.
 * Generates unique hashes and validates ticket authenticity.
 */
public class BlockchainService {

    private static final String HASH_PREFIX = "BLK-M2030-";
    private static long currentBlockNumber = 49201; // Simulated current block

    /**
     * Generate a unique blockchain hash for a new ticket.
     */
    public String generateTicketHash(Long ticketId, String seatNumber, LocalDateTime timestamp) {
        String data = String.format("%d:%s:%s:%s", 
            ticketId, 
            seatNumber, 
            timestamp.toString(), 
            UUID.randomUUID().toString()
        );
        return HASH_PREFIX + sha256(data).substring(0, 16).toUpperCase();
    }

    /**
     * Generate a blockchain transaction hash for a transfer.
     */
    public String generateTransferHash(Long ticketId, Long fromUserId, Long toUserId) {
        String data = String.format("TRANSFER:%d:%d:%d:%s:%d", 
            ticketId, 
            fromUserId, 
            toUserId, 
            LocalDateTime.now().toString(),
            getCurrentBlockNumber()
        );
        return "TX-" + sha256(data).substring(0, 20).toUpperCase();
    }

    /**
     * Verify if a blockchain hash is valid (format check).
     */
    public boolean verifyHash(String hash) {
        if (hash == null || hash.isEmpty()) {
            return false;
        }
        // Check if hash matches expected format
        return hash.startsWith(HASH_PREFIX) || hash.startsWith("TX-") || hash.startsWith("BLK-");
    }

    /**
     * Get the current simulated block number.
     */
    public long getCurrentBlockNumber() {
        return currentBlockNumber++;
    }

    /**
     * Get blockchain network status.
     */
    public BlockchainStatus getNetworkStatus() {
        return new BlockchainStatus(
            true, 
            currentBlockNumber, 
            "Mondial2030-MainNet",
            LocalDateTime.now()
        );
    }

    /**
     * SHA-256 hash function.
     */
    private String sha256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Inner class representing blockchain network status.
     */
    public static class BlockchainStatus {
        private final boolean connected;
        private final long currentBlock;
        private final String networkName;
        private final LocalDateTime lastSync;

        public BlockchainStatus(boolean connected, long currentBlock, String networkName, LocalDateTime lastSync) {
            this.connected = connected;
            this.currentBlock = currentBlock;
            this.networkName = networkName;
            this.lastSync = lastSync;
        }

        public boolean isConnected() { return connected; }
        public long getCurrentBlock() { return currentBlock; }
        public String getNetworkName() { return networkName; }
        public LocalDateTime getLastSync() { return lastSync; }

        @Override
        public String toString() {
            return String.format("Blockchain Node: %s (Bloc #%d)", 
                connected ? "Connecté" : "Déconnecté", currentBlock);
        }
    }
}
