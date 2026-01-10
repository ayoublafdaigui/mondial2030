package com.mondial2030.util;

import com.mondial2030.dao.*;
import org.hibernate.Session;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database Health Check Utility - Monitors database connectivity and health.
 * Provides comprehensive health status reporting and diagnostics.
 */
public class DatabaseHealthCheck {

    private static final Logger LOGGER = Logger.getLogger(DatabaseHealthCheck.class.getName());

    /**
     * Performs a comprehensive database health check.
     */
    public static HealthStatus performHealthCheck() {
        LOGGER.info("Performing database health check...");

        HealthStatus status = new HealthStatus();

        // Check basic connectivity
        status.setConnected(checkConnection());

        if (status.isConnected()) {
            // Check table counts
            status.setTableCounts(getTableCounts());

            // Measure query response time
            status.setResponseTimeMs(measureResponseTime());

            // Overall health
            status.setHealthy(true);
        } else {
            status.setHealthy(false);
        }

        return status;
    }

    /**
     * Checks basic database connection.
     */
    private static boolean checkConnection() {
        try {
            return HibernateUtil.isHealthy();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Database connection check failed", e);
            return false;
        }
    }

    /**
     * Gets record counts for all main tables.
     */
    private static Map<String, Long> getTableCounts() {
        Map<String, Long> counts = new HashMap<>();

        try {
            UserDAO userDAO = new UserDAO();
            StadiumDAO stadiumDAO = new StadiumDAO();
            MatchDAO matchDAO = new MatchDAO();
            TicketDAO ticketDAO = new TicketDAO();

            counts.put("users", userDAO.getUserCount());
            counts.put("stadiums", stadiumDAO.getStadiumCount());
            counts.put("matches", matchDAO.getMatchCount());
            counts.put("tickets", ticketDAO.getTicketCount());

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to retrieve table counts", e);
        }

        return counts;
    }

    /**
     * Measures database query response time.
     */
    private static long measureResponseTime() {
        try {
            long startTime = System.currentTimeMillis();

            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                session.createQuery("SELECT 1", Integer.class).uniqueResult();
            }

            long endTime = System.currentTimeMillis();
            return endTime - startTime;

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to measure response time", e);
            return -1;
        }
    }

    /**
     * Prints a detailed health report to console.
     */
    public static void printHealthReport() {
        HealthStatus status = performHealthCheck();

        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║          DATABASE HEALTH REPORT                  ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println(status.isConnected() ? "✅ Database Connection: HEALTHY" : "❌ Database Connection: FAILED");

        if (status.isConnected()) {
            System.out.println("⏱️  Response Time: " + status.getResponseTimeMs() + " ms");
            System.out.println();
            System.out.println("📊 Table Counts:");

            status.getTableCounts().forEach((table, count) -> {
                System.out.printf("   %-20s: %,d records%n", table, count);
            });

            System.out.println();
            System.out.println(status.isHealthy() ? "✅ Overall Status: HEALTHY" : "⚠️  Overall Status: DEGRADED");
        }

        System.out.println("══════════════════════════════════════════════════");
        System.out.println();
    }

    /**
     * Health status data class.
     */
    public static class HealthStatus {
        private boolean connected;
        private boolean healthy;
        private Map<String, Long> tableCounts;
        private long responseTimeMs;

        public HealthStatus() {
            this.tableCounts = new HashMap<>();
        }

        public boolean isConnected() {
            return connected;
        }

        public void setConnected(boolean connected) {
            this.connected = connected;
        }

        public boolean isHealthy() {
            return healthy;
        }

        public void setHealthy(boolean healthy) {
            this.healthy = healthy;
        }

        public Map<String, Long> getTableCounts() {
            return tableCounts;
        }

        public void setTableCounts(Map<String, Long> tableCounts) {
            this.tableCounts = tableCounts;
        }

        public long getResponseTimeMs() {
            return responseTimeMs;
        }

        public void setResponseTimeMs(long responseTimeMs) {
            this.responseTimeMs = responseTimeMs;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Database Health Status:\n");
            sb.append("  Connected: ").append(connected).append("\n");
            sb.append("  Healthy: ").append(healthy).append("\n");
            sb.append("  Response Time: ").append(responseTimeMs).append(" ms\n");
            sb.append("  Table Counts: ").append(tableCounts);
            return sb.toString();
        }
    }
}
