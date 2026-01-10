package com.mondial2030.service;

import com.mondial2030.dao.StadiumDAO;
import com.mondial2030.model.Stadium;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.logging.Logger;

/**
 * Stadium Service - Business logic layer for stadium operations.
 * Provides high-level operations and analytics for stadium management.
 */
public class StadiumService {

    private static final Logger LOGGER = Logger.getLogger(StadiumService.class.getName());
    private final StadiumDAO stadiumDAO;

    public StadiumService() {
        this.stadiumDAO = new StadiumDAO();
    }

    /**
     * Get all stadiums.
     */
    public List<Stadium> getAllStadiums() {
        return stadiumDAO.getAllStadiums();
    }

    /**
     * Get stadium by ID.
     */
    public Optional<Stadium> getStadiumById(Long id) {
        return stadiumDAO.findById(id);
    }

    /**
     * Get stadiums by country.
     */
    public List<Stadium> getStadiumsByCountry(String country) {
        return stadiumDAO.getStadiumsByCountry(country);
    }

    /**
     * Get stadiums by city.
     */
    public List<Stadium> getStadiumsByCity(String city) {
        return stadiumDAO.getStadiumsByCity(city);
    }

    /**
     * Get all main venue stadiums.
     */
    public List<Stadium> getMainVenues() {
        return stadiumDAO.getMainVenues();
    }

    /**
     * Get total count of stadiums.
     */
    public long getTotalStadiumCount() {
        return stadiumDAO.getStadiumCount();
    }

    /**
     * Get total capacity across all stadiums.
     */
    public int getTotalCapacity() {
        return stadiumDAO.getAllStadiums().stream()
                .mapToInt(Stadium::getCapacity)
                .sum();
    }

    /**
     * Get average stadium capacity.
     */
    public double getAverageCapacity() {
        return stadiumDAO.getAllStadiums().stream()
                .mapToInt(Stadium::getCapacity)
                .average()
                .orElse(0.0);
    }

    /**
     * Get the largest stadium.
     */
    public Optional<Stadium> getLargestStadium() {
        return stadiumDAO.getAllStadiums().stream()
                .max((s1, s2) -> Integer.compare(s1.getCapacity(), s2.getCapacity()));
    }

    /**
     * Get stadiums grouped by country.
     */
    public Map<String, List<Stadium>> getStadiumsByCountryGrouped() {
        return stadiumDAO.getAllStadiums().stream()
                .collect(Collectors.groupingBy(Stadium::getCountry));
    }

    /**
     * Get stadiums grouped by city.
     */
    public Map<String, List<Stadium>> getStadiumsByCityGrouped() {
        return stadiumDAO.getAllStadiums().stream()
                .collect(Collectors.groupingBy(Stadium::getCity));
    }

    /**
     * Get list of all host countries.
     */
    public List<String> getAllHostCountries() {
        return stadiumDAO.getAllStadiums().stream()
                .map(Stadium::getCountry)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Get list of all host cities.
     */
    public List<String> getAllHostCities() {
        return stadiumDAO.getAllStadiums().stream()
                .map(Stadium::getCity)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Check if a stadium exists by name.
     */
    public boolean stadiumExists(String name) {
        return stadiumDAO.findByName(name).isPresent();
    }

    /**
     * Save a new stadium.
     */
    public void saveStadium(Stadium stadium) {
        if (stadiumExists(stadium.getName())) {
            LOGGER.warning("Stadium already exists: " + stadium.getName());
            throw new IllegalArgumentException("Stadium with this name already exists");
        }
        stadiumDAO.saveStadium(stadium);
        LOGGER.info("Stadium saved successfully: " + stadium.getName());
    }

    /**
     * Update an existing stadium.
     */
    public void updateStadium(Stadium stadium) {
        if (stadium.getId() == null || stadiumDAO.findById(stadium.getId()).isEmpty()) {
            LOGGER.warning("Cannot update non-existent stadium");
            throw new IllegalArgumentException("Stadium does not exist");
        }
        stadiumDAO.updateStadium(stadium);
        LOGGER.info("Stadium updated successfully: " + stadium.getName());
    }

    /**
     * Delete a stadium by ID.
     */
    public void deleteStadium(Long id) {
        if (stadiumDAO.findById(id).isEmpty()) {
            LOGGER.warning("Cannot delete non-existent stadium with ID: " + id);
            throw new IllegalArgumentException("Stadium does not exist");
        }
        stadiumDAO.deleteStadium(id);
        LOGGER.info("Stadium deleted successfully with ID: " + id);
    }

    /**
     * Get stadium statistics.
     */
    public StadiumStats getStadiumStatistics() {
        List<Stadium> allStadiums = stadiumDAO.getAllStadiums();

        return new StadiumStats(
                allStadiums.size(),
                getTotalCapacity(),
                getAverageCapacity(),
                getLargestStadium().map(Stadium::getName).orElse("N/A"),
                getAllHostCountries().size(),
                getAllHostCities().size());
    }

    /**
     * Inner class for stadium statistics.
     */
    public static class StadiumStats {
        private final int totalStadiums;
        private final int totalCapacity;
        private final double averageCapacity;
        private final String largestStadiumName;
        private final int totalCountries;
        private final int totalCities;

        public StadiumStats(int totalStadiums, int totalCapacity, double averageCapacity,
                String largestStadiumName, int totalCountries, int totalCities) {
            this.totalStadiums = totalStadiums;
            this.totalCapacity = totalCapacity;
            this.averageCapacity = averageCapacity;
            this.largestStadiumName = largestStadiumName;
            this.totalCountries = totalCountries;
            this.totalCities = totalCities;
        }

        public int getTotalStadiums() {
            return totalStadiums;
        }

        public int getTotalCapacity() {
            return totalCapacity;
        }

        public double getAverageCapacity() {
            return averageCapacity;
        }

        public String getLargestStadiumName() {
            return largestStadiumName;
        }

        public int getTotalCountries() {
            return totalCountries;
        }

        public int getTotalCities() {
            return totalCities;
        }

        @Override
        public String toString() {
            return String.format(
                    "Stadium Statistics:%n" +
                            "  Total Stadiums: %d%n" +
                            "  Total Capacity: %,d%n" +
                            "  Average Capacity: %,.0f%n" +
                            "  Largest Stadium: %s%n" +
                            "  Host Countries: %d%n" +
                            "  Host Cities: %d",
                    totalStadiums, totalCapacity, averageCapacity,
                    largestStadiumName, totalCountries, totalCities);
        }
    }
}
