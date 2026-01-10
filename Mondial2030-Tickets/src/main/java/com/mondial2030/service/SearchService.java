package com.mondial2030.service;

import com.mondial2030.dao.*;
import com.mondial2030.model.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced Search Service - Provides comprehensive search capabilities across
 * all entities.
 * Supports multi-criteria searches, filtering, and complex queries.
 */
public class SearchService {

    private final MatchDAO matchDAO;
    private final TicketDAO ticketDAO;
    private final StadiumDAO stadiumDAO;
    private final UserDAO userDAO;

    public SearchService() {
        this.matchDAO = new MatchDAO();
        this.ticketDAO = new TicketDAO();
        this.stadiumDAO = new StadiumDAO();
        this.userDAO = new UserDAO();
    }

    // ===================================================================
    // Match Search Methods
    // ===================================================================

    /**
     * Search matches by multiple criteria.
     */
    public List<Match> searchMatches(MatchSearchCriteria criteria) {
        List<Match> results = matchDAO.getAllMatches();

        // Filter by team
        if (criteria.getTeamName() != null && !criteria.getTeamName().isEmpty()) {
            String team = criteria.getTeamName().toLowerCase();
            results = results.stream()
                    .filter(m -> m.getHomeTeam().toLowerCase().contains(team) ||
                            m.getAwayTeam().toLowerCase().contains(team))
                    .collect(Collectors.toList());
        }

        // Filter by city
        if (criteria.getCity() != null && !criteria.getCity().isEmpty()) {
            results = results.stream()
                    .filter(m -> m.getCity().equalsIgnoreCase(criteria.getCity()))
                    .collect(Collectors.toList());
        }

        // Filter by stadium
        if (criteria.getStadium() != null && !criteria.getStadium().isEmpty()) {
            results = results.stream()
                    .filter(m -> m.getStadium().toLowerCase().contains(criteria.getStadium().toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Filter by phase
        if (criteria.getPhase() != null) {
            results = results.stream()
                    .filter(m -> m.getPhase() == criteria.getPhase())
                    .collect(Collectors.toList());
        }

        // Filter by date range
        if (criteria.getStartDate() != null) {
            results = results.stream()
                    .filter(m -> m.getMatchDate().isAfter(criteria.getStartDate()) ||
                            m.getMatchDate().isEqual(criteria.getStartDate()))
                    .collect(Collectors.toList());
        }

        if (criteria.getEndDate() != null) {
            results = results.stream()
                    .filter(m -> m.getMatchDate().isBefore(criteria.getEndDate()) ||
                            m.getMatchDate().isEqual(criteria.getEndDate()))
                    .collect(Collectors.toList());
        }

        // Filter by price range
        if (criteria.getMinPrice() != null) {
            results = results.stream()
                    .filter(m -> m.getBasePrice() != null && m.getBasePrice() >= criteria.getMinPrice())
                    .collect(Collectors.toList());
        }

        if (criteria.getMaxPrice() != null) {
            results = results.stream()
                    .filter(m -> m.getBasePrice() != null && m.getBasePrice() <= criteria.getMaxPrice())
                    .collect(Collectors.toList());
        }

        // Filter by availability
        if (criteria.isOnlyAvailable()) {
            results = results.stream()
                    .filter(m -> m.getAvailableSeats() != null && m.getAvailableSeats() > 0)
                    .collect(Collectors.toList());
        }

        return results;
    }

    /**
     * Search matches by team name (home or away).
     */
    public List<Match> searchMatchesByTeam(String teamName) {
        MatchSearchCriteria criteria = new MatchSearchCriteria();
        criteria.setTeamName(teamName);
        return searchMatches(criteria);
    }

    /**
     * Search upcoming matches in a city.
     */
    public List<Match> searchUpcomingMatchesInCity(String city) {
        MatchSearchCriteria criteria = new MatchSearchCriteria();
        criteria.setCity(city);
        criteria.setStartDate(LocalDateTime.now());
        return searchMatches(criteria);
    }

    // ===================================================================
    // Ticket Search Methods
    // ===================================================================

    /**
     * Search tickets by multiple criteria.
     */
    public List<Ticket> searchTickets(TicketSearchCriteria criteria) {
        List<Ticket> results = ticketDAO.getAllTickets();

        // Filter by category
        if (criteria.getCategory() != null) {
            results = results.stream()
                    .filter(t -> t.getCategory() == criteria.getCategory())
                    .collect(Collectors.toList());
        }

        // Filter by status
        if (criteria.getStatus() != null) {
            results = results.stream()
                    .filter(t -> t.getStatus() == criteria.getStatus())
                    .collect(Collectors.toList());
        }

        // Filter by price range
        if (criteria.getMinPrice() != null) {
            results = results.stream()
                    .filter(t -> t.getPrice() >= criteria.getMinPrice())
                    .collect(Collectors.toList());
        }

        if (criteria.getMaxPrice() != null) {
            results = results.stream()
                    .filter(t -> t.getPrice() <= criteria.getMaxPrice())
                    .collect(Collectors.toList());
        }

        // Filter by owner
        if (criteria.getOwnerId() != null) {
            results = results.stream()
                    .filter(t -> t.getOwner() != null && t.getOwner().getId().equals(criteria.getOwnerId()))
                    .collect(Collectors.toList());
        }

        // Filter by match
        if (criteria.getMatchId() != null) {
            results = results.stream()
                    .filter(t -> t.getMatch() != null && t.getMatch().getId().equals(criteria.getMatchId()))
                    .collect(Collectors.toList());
        }

        // Filter by seat zone
        if (criteria.getSeatZone() != null && !criteria.getSeatZone().isEmpty()) {
            results = results.stream()
                    .filter(t -> t.getSeatZone() != null &&
                            t.getSeatZone().toLowerCase().contains(criteria.getSeatZone().toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Filter by fraud risk
        if (criteria.isHighRiskOnly()) {
            results = results.stream()
                    .filter(t -> t.getFraudScore() != null && t.getFraudScore() > 0.5)
                    .collect(Collectors.toList());
        }

        // Filter available only
        if (criteria.isAvailableOnly()) {
            results = results.stream()
                    .filter(t -> t.getOwner() == null && t.getStatus() == Ticket.TicketStatus.ACTIVE)
                    .collect(Collectors.toList());
        }

        return results;
    }

    /**
     * Search available tickets for a match.
     */
    public List<Ticket> searchAvailableTicketsForMatch(Long matchId) {
        TicketSearchCriteria criteria = new TicketSearchCriteria();
        criteria.setMatchId(matchId);
        criteria.setAvailableOnly(true);
        return searchTickets(criteria);
    }

    /**
     * Search VIP tickets.
     */
    public List<Ticket> searchVIPTickets() {
        TicketSearchCriteria criteria = new TicketSearchCriteria();
        criteria.setCategory(Ticket.TicketCategory.VIP);
        criteria.setAvailableOnly(true);
        return searchTickets(criteria);
    }

    // ===================================================================
    // Stadium Search Methods
    // ===================================================================

    /**
     * Search stadiums by multiple criteria.
     */
    public List<Stadium> searchStadiums(StadiumSearchCriteria criteria) {
        List<Stadium> results = stadiumDAO.getAllStadiums();

        // Filter by name
        if (criteria.getName() != null && !criteria.getName().isEmpty()) {
            results = results.stream()
                    .filter(s -> s.getName().toLowerCase().contains(criteria.getName().toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Filter by country
        if (criteria.getCountry() != null && !criteria.getCountry().isEmpty()) {
            results = results.stream()
                    .filter(s -> s.getCountry().equalsIgnoreCase(criteria.getCountry()))
                    .collect(Collectors.toList());
        }

        // Filter by city
        if (criteria.getCity() != null && !criteria.getCity().isEmpty()) {
            results = results.stream()
                    .filter(s -> s.getCity().equalsIgnoreCase(criteria.getCity()))
                    .collect(Collectors.toList());
        }

        // Filter by capacity range
        if (criteria.getMinCapacity() != null) {
            results = results.stream()
                    .filter(s -> s.getCapacity() >= criteria.getMinCapacity())
                    .collect(Collectors.toList());
        }

        if (criteria.getMaxCapacity() != null) {
            results = results.stream()
                    .filter(s -> s.getCapacity() <= criteria.getMaxCapacity())
                    .collect(Collectors.toList());
        }

        // Filter main venues only
        if (criteria.isMainVenuesOnly()) {
            results = results.stream()
                    .filter(s -> s.getIsMainVenue() != null && s.getIsMainVenue())
                    .collect(Collectors.toList());
        }

        return results;
    }

    /**
     * Search large stadiums (capacity > 70,000).
     */
    public List<Stadium> searchLargeStadiums() {
        StadiumSearchCriteria criteria = new StadiumSearchCriteria();
        criteria.setMinCapacity(70000);
        return searchStadiums(criteria);
    }

    // ===================================================================
    // Global Search
    // ===================================================================

    /**
     * Global search across all entities.
     */
    public GlobalSearchResult globalSearch(String query) {
        GlobalSearchResult result = new GlobalSearchResult();

        if (query == null || query.trim().isEmpty()) {
            return result;
        }

        String lowerQuery = query.toLowerCase();

        // Search matches
        List<Match> matches = matchDAO.getAllMatches().stream()
                .filter(m -> m.getHomeTeam().toLowerCase().contains(lowerQuery) ||
                        m.getAwayTeam().toLowerCase().contains(lowerQuery) ||
                        m.getStadium().toLowerCase().contains(lowerQuery) ||
                        m.getCity().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
        result.setMatches(matches);

        // Search stadiums
        List<Stadium> stadiums = stadiumDAO.getAllStadiums().stream()
                .filter(s -> s.getName().toLowerCase().contains(lowerQuery) ||
                        s.getCity().toLowerCase().contains(lowerQuery) ||
                        s.getCountry().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
        result.setStadiums(stadiums);

        // Search users
        List<User> users = userDAO.getAllUsers().stream()
                .filter(u -> u.getUsername().toLowerCase().contains(lowerQuery) ||
                        (u.getName() != null && u.getName().toLowerCase().contains(lowerQuery)) ||
                        (u.getEmail() != null && u.getEmail().toLowerCase().contains(lowerQuery)))
                .collect(Collectors.toList());
        result.setUsers(users);

        return result;
    }

    // ===================================================================
    // Inner Classes - Search Criteria
    // ===================================================================

    public static class MatchSearchCriteria {
        private String teamName;
        private String city;
        private String stadium;
        private Match.MatchPhase phase;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Double minPrice;
        private Double maxPrice;
        private boolean onlyAvailable;

        // Getters and Setters
        public String getTeamName() {
            return teamName;
        }

        public void setTeamName(String teamName) {
            this.teamName = teamName;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getStadium() {
            return stadium;
        }

        public void setStadium(String stadium) {
            this.stadium = stadium;
        }

        public Match.MatchPhase getPhase() {
            return phase;
        }

        public void setPhase(Match.MatchPhase phase) {
            this.phase = phase;
        }

        public LocalDateTime getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDateTime startDate) {
            this.startDate = startDate;
        }

        public LocalDateTime getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDateTime endDate) {
            this.endDate = endDate;
        }

        public Double getMinPrice() {
            return minPrice;
        }

        public void setMinPrice(Double minPrice) {
            this.minPrice = minPrice;
        }

        public Double getMaxPrice() {
            return maxPrice;
        }

        public void setMaxPrice(Double maxPrice) {
            this.maxPrice = maxPrice;
        }

        public boolean isOnlyAvailable() {
            return onlyAvailable;
        }

        public void setOnlyAvailable(boolean onlyAvailable) {
            this.onlyAvailable = onlyAvailable;
        }
    }

    public static class TicketSearchCriteria {
        private Ticket.TicketCategory category;
        private Ticket.TicketStatus status;
        private Double minPrice;
        private Double maxPrice;
        private Long ownerId;
        private Long matchId;
        private String seatZone;
        private boolean highRiskOnly;
        private boolean availableOnly;

        // Getters and Setters
        public Ticket.TicketCategory getCategory() {
            return category;
        }

        public void setCategory(Ticket.TicketCategory category) {
            this.category = category;
        }

        public Ticket.TicketStatus getStatus() {
            return status;
        }

        public void setStatus(Ticket.TicketStatus status) {
            this.status = status;
        }

        public Double getMinPrice() {
            return minPrice;
        }

        public void setMinPrice(Double minPrice) {
            this.minPrice = minPrice;
        }

        public Double getMaxPrice() {
            return maxPrice;
        }

        public void setMaxPrice(Double maxPrice) {
            this.maxPrice = maxPrice;
        }

        public Long getOwnerId() {
            return ownerId;
        }

        public void setOwnerId(Long ownerId) {
            this.ownerId = ownerId;
        }

        public Long getMatchId() {
            return matchId;
        }

        public void setMatchId(Long matchId) {
            this.matchId = matchId;
        }

        public String getSeatZone() {
            return seatZone;
        }

        public void setSeatZone(String seatZone) {
            this.seatZone = seatZone;
        }

        public boolean isHighRiskOnly() {
            return highRiskOnly;
        }

        public void setHighRiskOnly(boolean highRiskOnly) {
            this.highRiskOnly = highRiskOnly;
        }

        public boolean isAvailableOnly() {
            return availableOnly;
        }

        public void setAvailableOnly(boolean availableOnly) {
            this.availableOnly = availableOnly;
        }
    }

    public static class StadiumSearchCriteria {
        private String name;
        private String country;
        private String city;
        private Integer minCapacity;
        private Integer maxCapacity;
        private boolean mainVenuesOnly;

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public Integer getMinCapacity() {
            return minCapacity;
        }

        public void setMinCapacity(Integer minCapacity) {
            this.minCapacity = minCapacity;
        }

        public Integer getMaxCapacity() {
            return maxCapacity;
        }

        public void setMaxCapacity(Integer maxCapacity) {
            this.maxCapacity = maxCapacity;
        }

        public boolean isMainVenuesOnly() {
            return mainVenuesOnly;
        }

        public void setMainVenuesOnly(boolean mainVenuesOnly) {
            this.mainVenuesOnly = mainVenuesOnly;
        }
    }

    public static class GlobalSearchResult {
        private List<Match> matches = new ArrayList<>();
        private List<Stadium> stadiums = new ArrayList<>();
        private List<User> users = new ArrayList<>();

        public List<Match> getMatches() {
            return matches;
        }

        public void setMatches(List<Match> matches) {
            this.matches = matches;
        }

        public List<Stadium> getStadiums() {
            return stadiums;
        }

        public void setStadiums(List<Stadium> stadiums) {
            this.stadiums = stadiums;
        }

        public List<User> getUsers() {
            return users;
        }

        public void setUsers(List<User> users) {
            this.users = users;
        }

        public int getTotalResults() {
            return matches.size() + stadiums.size() + users.size();
        }

        public boolean isEmpty() {
            return getTotalResults() == 0;
        }

        @Override
        public String toString() {
            return String.format("Search Results: %d matches, %d stadiums, %d users (Total: %d)",
                    matches.size(), stadiums.size(), users.size(), getTotalResults());
        }
    }
}
