package com.mondial2030.service;

import com.mondial2030.dao.MatchDAO;
import com.mondial2030.model.Match;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Match operations.
 * Handles business logic and delegates to DAO for persistence.
 */
public class MatchService {

    private final MatchDAO matchDAO;

    public MatchService() {
        this.matchDAO = new MatchDAO();
    }

    /**
     * Saves a new match to the database.
     * @param match The match to save
     * @return true if saved successfully, false otherwise
     */
    public boolean saveMatch(Match match) {
        try {
            // Business validation
            if (match.getHomeTeam().equals(match.getAwayTeam())) {
                throw new IllegalArgumentException("Home and away teams cannot be the same");
            }
            
            matchDAO.saveMatch(match);
            return match.getId() != null;
        } catch (Exception e) {
            System.err.println("Error saving match: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all matches from the database.
     * @return List of all matches
     */
    public List<Match> getAllMatches() {
        return matchDAO.getAllMatches();
    }

    /**
     * Finds a match by its ID.
     * @param id The match ID
     * @return Optional containing the match if found
     */
    public Optional<Match> findMatchById(Long id) {
        return matchDAO.findById(id);
    }

    /**
     * Updates an existing match.
     * @param match The match to update
     * @return true if updated successfully
     */
    public boolean updateMatch(Match match) {
        try {
            matchDAO.updateMatch(match);
            return true;
        } catch (Exception e) {
            System.err.println("Error updating match: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a match by ID.
     * @param id The match ID to delete
     * @return true if deleted successfully
     */
    public boolean deleteMatch(Long id) {
        try {
            Optional<Match> match = matchDAO.findById(id);
            if (match.isPresent()) {
                matchDAO.deleteMatch(id);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error deleting match: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets the total number of tickets sold for a match.
     * @param matchId The match ID
     * @return Number of tickets sold
     */
    public int getTicketsSold(Long matchId) {
        Optional<Match> matchOpt = matchDAO.findById(matchId);
        if (matchOpt.isPresent()) {
            Match match = matchOpt.get();
            return match.getTotalSeats() - match.getAvailableSeats();
        }
        return 0;
    }

    /**
     * Calculates revenue for a specific match.
     * @param matchId The match ID
     * @return Total revenue from ticket sales
     */
    public double getMatchRevenue(Long matchId) {
        Optional<Match> matchOpt = matchDAO.findById(matchId);
        if (matchOpt.isPresent()) {
            Match match = matchOpt.get();
            int ticketsSold = match.getTotalSeats() - match.getAvailableSeats();
            return ticketsSold * match.getTicketPrice();
        }
        return 0.0;
    }
}
