package com.mondial2030.service;

import com.mondial2030.model.Ticket;
import com.mondial2030.model.TicketTransfer;
import com.mondial2030.dao.TicketTransferDAO;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Fraud Detection Service - AI-based fraud risk analysis.
 * Calculates fraud scores based on ticket transfer patterns and ownership history.
 */
public class FraudDetectionService {

    // Thresholds for fraud detection
    private static final int HIGH_TRANSFER_COUNT = 3;
    private static final int VERY_HIGH_TRANSFER_COUNT = 5;
    private static final int RAPID_TRANSFER_HOURS = 24;
    
    // Score weights
    private static final double TRANSFER_COUNT_WEIGHT = 0.4;
    private static final double RAPID_TRANSFER_WEIGHT = 0.3;
    private static final double PRICE_ANOMALY_WEIGHT = 0.2;
    private static final double PATTERN_WEIGHT = 0.1;

    private final TicketTransferDAO transferDAO;

    public FraudDetectionService() {
        this.transferDAO = new TicketTransferDAO();
    }

    /**
     * Calculate the fraud risk score for a ticket.
     * Score ranges from 0.0 (safe) to 1.0 (high risk).
     */
    public double calculateFraudScore(Ticket ticket) {
        if (ticket == null) {
            return 0.0;
        }

        double score = 0.0;

        // Factor 1: Transfer count analysis (40% weight)
        score += calculateTransferCountScore(ticket) * TRANSFER_COUNT_WEIGHT;

        // Factor 2: Rapid transfer detection (30% weight)
        score += calculateRapidTransferScore(ticket) * RAPID_TRANSFER_WEIGHT;

        // Factor 3: Price anomaly detection (20% weight)
        score += calculatePriceAnomalyScore(ticket) * PRICE_ANOMALY_WEIGHT;

        // Factor 4: Suspicious pattern detection (10% weight)
        score += calculatePatternScore(ticket) * PATTERN_WEIGHT;

        // Ensure score is between 0.0 and 1.0
        return Math.min(1.0, Math.max(0.0, score));
    }

    /**
     * Calculate score based on number of ownership transfers.
     * More transfers = higher fraud risk.
     */
    private double calculateTransferCountScore(Ticket ticket) {
        Integer transferCountObj = ticket.getTransferCount();
        int transferCount = transferCountObj != null ? transferCountObj.intValue() : 0;

        if (transferCount == 0) {
            return 0.0;
        } else if (transferCount <= 2) {
            return 0.2;
        } else if (transferCount <= HIGH_TRANSFER_COUNT) {
            return 0.5;
        } else if (transferCount <= VERY_HIGH_TRANSFER_COUNT) {
            return 0.8;
        } else {
            return 1.0;
        }
    }

    /**
     * Calculate score based on how quickly a ticket was transferred after purchase.
     * Rapid transfers indicate potential scalping or fraud.
     */
    private double calculateRapidTransferScore(Ticket ticket) {
        if (ticket.getPurchaseDate() == null || ticket.getLastTransferDate() == null) {
            return 0.0;
        }

        // Check if ticket was transferred within 24 hours of purchase
        long hoursBetween = ChronoUnit.HOURS.between(ticket.getPurchaseDate(), ticket.getLastTransferDate());

        if (hoursBetween <= 1) {
            return 1.0; // Very suspicious - transferred within 1 hour
        } else if (hoursBetween <= 6) {
            return 0.8;
        } else if (hoursBetween <= RAPID_TRANSFER_HOURS) {
            return 0.5;
        } else if (hoursBetween <= 72) {
            return 0.2;
        } else {
            return 0.0;
        }
    }

    /**
     * Calculate score based on price anomalies in transfer history.
     * Large price differences from base price indicate potential fraud.
     */
    private double calculatePriceAnomalyScore(Ticket ticket) {
        if (ticket.getId() == null) {
            return 0.0;
        }

        try {
            List<TicketTransfer> transfers = transferDAO.getTransfersByTicket(ticket.getId());
            if (transfers.isEmpty()) {
                return 0.0;
            }

            Double basePrice = ticket.getPrice();
            if (basePrice == null || basePrice <= 0) {
                return 0.0;
            }

            // Check for price inflation in transfers
            for (TicketTransfer transfer : transfers) {
                if (transfer.getTransferPrice() != null) {
                    double ratio = transfer.getTransferPrice() / basePrice;
                    if (ratio > 3.0) {
                        return 1.0; // Price tripled - very suspicious
                    } else if (ratio > 2.0) {
                        return 0.7;
                    } else if (ratio > 1.5) {
                        return 0.4;
                    }
                }
            }
        } catch (Exception e) {
            // If we can't check transfers, return neutral score
            return 0.0;
        }

        return 0.0;
    }

    /**
     * Calculate score based on suspicious patterns.
     * Detects circular transfers and other fraud patterns.
     */
    private double calculatePatternScore(Ticket ticket) {
        if (ticket.getId() == null) {
            return 0.0;
        }

        try {
            List<TicketTransfer> transfers = transferDAO.getTransfersByTicket(ticket.getId());
            if (transfers.size() < 2) {
                return 0.0;
            }

            // Check for circular transfer patterns (ticket returns to previous owner)
            for (int i = 0; i < transfers.size() - 1; i++) {
                for (int j = i + 1; j < transfers.size(); j++) {
                    if (transfers.get(i).getToUser() != null && 
                        transfers.get(j).getToUser() != null &&
                        transfers.get(i).getToUser().getId().equals(transfers.get(j).getToUser().getId())) {
                        return 0.8; // Circular pattern detected
                    }
                }
            }

            // Check for rapid succession of transfers (multiple in same day)
            LocalDateTime prevDate = null;
            int rapidCount = 0;
            for (TicketTransfer transfer : transfers) {
                if (prevDate != null) {
                    long hoursBetween = ChronoUnit.HOURS.between(prevDate, transfer.getTransferDate());
                    if (hoursBetween < 24) {
                        rapidCount++;
                    }
                }
                prevDate = transfer.getTransferDate();
            }

            if (rapidCount >= 2) {
                return 0.6;
            }

        } catch (Exception e) {
            return 0.0;
        }

        return 0.0;
    }

    /**
     * Get a human-readable risk level from the fraud score.
     */
    public String getRiskLevel(double score) {
        if (score < 0.2) {
            return "LOW";
        } else if (score < 0.5) {
            return "MEDIUM";
        } else if (score < 0.7) {
            return "HIGH";
        } else {
            return "CRITICAL";
        }
    }

    /**
     * Get a detailed fraud analysis report for a ticket.
     */
    public FraudReport analyzeTicket(Ticket ticket) {
        double score = calculateFraudScore(ticket);
        String riskLevel = getRiskLevel(score);
        
        StringBuilder details = new StringBuilder();
        
        Integer transferCountObj = ticket.getTransferCount();
        int transferCount = transferCountObj != null ? transferCountObj.intValue() : 0;
        details.append("Transfer Count: ").append(transferCount).append("\n");
        
        if (ticket.getLastTransferDate() != null) {
            details.append("Last Transfer: ").append(ticket.getLastTransferDate()).append("\n");
        }
        
        if (score > 0.7) {
            details.append("WARNING: High fraud risk detected!\n");
            details.append("Recommendation: Manual review required.\n");
        }

        return new FraudReport(ticket.getId(), score, riskLevel, details.toString());
    }

    /**
     * Inner class to hold fraud analysis report.
     */
    public static class FraudReport {
        private final Long ticketId;
        private final double score;
        private final String riskLevel;
        private final String details;

        public FraudReport(Long ticketId, double score, String riskLevel, String details) {
            this.ticketId = ticketId;
            this.score = score;
            this.riskLevel = riskLevel;
            this.details = details;
        }

        public Long getTicketId() { return ticketId; }
        public double getScore() { return score; }
        public String getRiskLevel() { return riskLevel; }
        public String getDetails() { return details; }

        @Override
        public String toString() {
            return String.format("FraudReport{ticketId=%d, score=%.2f, risk=%s}", ticketId, score, riskLevel);
        }
    }
}
