package com.mondial2030.util;

import com.mondial2030.dao.MatchDAO;
import com.mondial2030.dao.StadiumDAO;
import com.mondial2030.dao.TicketDAO;
import com.mondial2030.dao.UserDAO;
import com.mondial2030.model.Match;
import com.mondial2030.model.Stadium;
import com.mondial2030.model.Ticket;
import com.mondial2030.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * Database Seeder - Populates the database with initial data on startup.
 * Creates admin account, stadiums, and sample matches.
 */
public class DatabaseSeeder {

    private static final Logger LOGGER = Logger.getLogger(DatabaseSeeder.class.getName());

    private final UserDAO userDAO;
    private final MatchDAO matchDAO;
    private final TicketDAO ticketDAO;
    private final StadiumDAO stadiumDAO;

    public DatabaseSeeder() {
        this.userDAO = new UserDAO();
        this.matchDAO = new MatchDAO();
        this.ticketDAO = new TicketDAO();
        this.stadiumDAO = new StadiumDAO();
    }

    /**
     * Seed all initial data.
     */
    public void seedAll() {
        LOGGER.info("Starting database seeding...");
        
        seedAdmin();
        seedStadiums();
        seedMatches();
        seedSampleTickets();
        
        LOGGER.info("Database seeding completed!");
    }

    /**
     * Seed only the admin user.
     */
    public void seedAdmin() {
        // Check if admin already exists
        if (userDAO.usernameExists("admin")) {
            LOGGER.info("Admin user already exists, skipping admin seeding.");
            return;
        }

        LOGGER.info("Creating admin user...");

        // Create admin user with username: admin, password: admin
        User admin = new User(
            "admin",
            "admin",  // Will be hashed by DAO
            "Administrateur Mondial 2030",
            "admin@mondial2030.com",
            User.Role.ADMIN
        );
        userDAO.saveUser(admin);
        LOGGER.info("Admin user created (username: admin, password: admin)");
    }

    /**
     * Seed the 6 best stadiums for Mondial 2030.
     * Morocco, Spain, and Portugal host cities.
     */
    public void seedStadiums() {
        // Check if stadiums already exist
        if (stadiumDAO.getStadiumCount() > 0) {
            LOGGER.info("Stadiums already exist, skipping stadium seeding.");
            return;
        }

        LOGGER.info("Creating 6 best stadiums for Mondial 2030...");

        // 1. Grand Stade de Casablanca (Morocco) - Main venue for Final
        Stadium casablanca = new Stadium(
            "Grand Stade de Casablanca",
            "Casablanca",
            "Morocco",
            93000,
            "The largest stadium in Africa, built specifically for FIFA World Cup 2030. Will host the Final match.",
            2029,
            true
        );
        stadiumDAO.saveStadium(casablanca);

        // 2. Santiago Bernabéu (Spain) - Home of Real Madrid
        Stadium bernabeu = new Stadium(
            "Santiago Bernabéu",
            "Madrid",
            "Spain",
            81044,
            "Iconic stadium of Real Madrid, recently renovated with retractable roof and state-of-the-art technology.",
            1947,
            true
        );
        stadiumDAO.saveStadium(bernabeu);

        // 3. Camp Nou (Spain) - Home of FC Barcelona
        Stadium campNou = new Stadium(
            "Camp Nou",
            "Barcelona",
            "Spain",
            99354,
            "The largest stadium in Europe, home of FC Barcelona. Currently undergoing major renovation.",
            1957,
            true
        );
        stadiumDAO.saveStadium(campNou);

        // 4. Estádio da Luz (Portugal) - Home of Benfica
        Stadium daLuz = new Stadium(
            "Estádio da Luz",
            "Lisbon",
            "Portugal",
            65647,
            "Modern stadium that hosted the Euro 2004 final. Home of SL Benfica.",
            2003,
            true
        );
        stadiumDAO.saveStadium(daLuz);

        // 5. Stade Ibn Battouta (Morocco) - Tangier
        Stadium tangier = new Stadium(
            "Stade Ibn Battouta",
            "Tangier",
            "Morocco",
            65000,
            "Major stadium in northern Morocco, gateway between Europe and Africa.",
            2011,
            false
        );
        stadiumDAO.saveStadium(tangier);

        // 6. Stade de Marrakech (Morocco)
        Stadium marrakech = new Stadium(
            "Grand Stade de Marrakech",
            "Marrakech",
            "Morocco",
            45240,
            "Beautiful stadium in the heart of Morocco's tourist capital, surrounded by the Atlas Mountains.",
            2011,
            false
        );
        stadiumDAO.saveStadium(marrakech);

        LOGGER.info("6 stadiums created successfully");
    }

    /**
     * Seed initial matches for Mondial 2030.
     */
    public void seedMatches() {
        // Check if matches already exist
        if (matchDAO.getMatchCount() > 0) {
            LOGGER.info("Matches already exist, skipping match seeding.");
            return;
        }

        LOGGER.info("Creating sample matches...");

        // Group Stage - Match 1
        Match match1 = new Match(
            "Morocco", "Spain",
            LocalDateTime.of(2030, 6, 13, 18, 0),
            "Grand Stade de Casablanca",
            "Casablanca",
            Match.MatchPhase.GROUP_STAGE,
            150.0,
            93000
        );
        matchDAO.saveMatch(match1);

        // Group Stage - Match 2
        Match match2 = new Match(
            "Portugal", "Argentina",
            LocalDateTime.of(2030, 6, 14, 21, 0),
            "Estádio da Luz",
            "Lisbon",
            Match.MatchPhase.GROUP_STAGE,
            180.0,
            65647
        );
        matchDAO.saveMatch(match2);

        // Group Stage - Match 3
        Match match3 = new Match(
            "France", "Brazil",
            LocalDateTime.of(2030, 6, 15, 18, 0),
            "Camp Nou",
            "Barcelona",
            Match.MatchPhase.GROUP_STAGE,
            200.0,
            99354
        );
        matchDAO.saveMatch(match3);

        // Group Stage - Match 4
        Match match4 = new Match(
            "Germany", "Netherlands",
            LocalDateTime.of(2030, 6, 16, 21, 0),
            "Stade Ibn Battouta",
            "Tangier",
            Match.MatchPhase.GROUP_STAGE,
            160.0,
            65000
        );
        matchDAO.saveMatch(match4);

        // Semi-Final
        Match semiFinal = new Match(
            "TBD", "TBD",
            LocalDateTime.of(2030, 7, 9, 21, 0),
            "Santiago Bernabéu",
            "Madrid",
            Match.MatchPhase.SEMI_FINAL,
            500.0,
            81044
        );
        matchDAO.saveMatch(semiFinal);

        // Final
        Match finalMatch = new Match(
            "TBD", "TBD",
            LocalDateTime.of(2030, 7, 13, 18, 0),
            "Grand Stade de Casablanca",
            "Casablanca",
            Match.MatchPhase.FINAL,
            1000.0,
            93000
        );
        matchDAO.saveMatch(finalMatch);

        LOGGER.info("6 matches created successfully");
    }

    /**
     * Seed some sample tickets for demonstration.
     */
    public void seedSampleTickets() {
        // Check if tickets already exist
        if (ticketDAO.getTicketCount() > 0) {
            LOGGER.info("Tickets already exist, skipping ticket seeding.");
            return;
        }

        LOGGER.info("Creating sample tickets...");

        // Get matches
        List<Match> matches = matchDAO.getAllMatches();
        if (matches.isEmpty()) {
            LOGGER.warning("No matches found, skipping ticket seeding.");
            return;
        }

        // Create sample available tickets for each match (no owner - available for purchase)
        for (Match match : matches) {
            int ticketsToCreate = Math.min(10, match.getAvailableSeats() != null ? match.getAvailableSeats() : 10);
            
            for (int i = 1; i <= ticketsToCreate; i++) {
                String zone = i <= 2 ? "VIP" : (i <= 5 ? "Premium" : "Standard");
                String seatNumber = zone + " - Row " + ((i % 5) + 1) + ", Seat " + i;
                double price = match.getBasePrice() != null ? match.getBasePrice() * (zone.equals("VIP") ? 3 : zone.equals("Premium") ? 1.5 : 1) : 100.0;
                
                Ticket ticket = new Ticket(seatNumber, price, match, null); // null owner = available
                ticket.setCategory(zone.equals("VIP") ? Ticket.TicketCategory.VIP : 
                                   zone.equals("Premium") ? Ticket.TicketCategory.PREMIUM : 
                                   Ticket.TicketCategory.STANDARD);
                ticketDAO.saveTicket(ticket);
            }
        }

        LOGGER.info("Sample tickets created for all matches");
    }

    /**
     * Check if database needs seeding.
     */
    public boolean needsSeeding() {
        return userDAO.getUserCount() == 0;
    }

    /**
     * Reset and reseed database (use with caution!).
     */
    public void resetAndSeed() {
        LOGGER.warning("Resetting database...");
        // In a real app, you'd drop tables here
        // For now, just run seeding
        seedAll();
    }
}
