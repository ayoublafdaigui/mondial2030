package com.mondial2030.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import com.mondial2030.model.Match;
import com.mondial2030.model.Stadium;
import com.mondial2030.model.Ticket;
import com.mondial2030.model.TicketTransfer;
import com.mondial2030.model.User;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hibernate Utility Class - Manages the SessionFactory singleton.
 * Configured for PostgreSQL database.
 */
public class HibernateUtil {
    
    private static final Logger LOGGER = Logger.getLogger(HibernateUtil.class.getName());
    private static SessionFactory sessionFactory;

    static {
        try {
            // Configuration native Hibernate sans JPA EntityManager
            sessionFactory = new Configuration()
                    .configure("hibernate.cfg.xml")
                    .addAnnotatedClass(User.class)
                    .addAnnotatedClass(Ticket.class)
                    .addAnnotatedClass(Match.class)
                    .addAnnotatedClass(TicketTransfer.class)
                    .addAnnotatedClass(Stadium.class)
                    .buildSessionFactory();
            
            LOGGER.info("Hibernate SessionFactory initialized successfully.");
        } catch (ExceptionInInitializerError | RuntimeException ex) {
            LOGGER.log(Level.SEVERE, "Failed to create SessionFactory", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
            LOGGER.info("Hibernate SessionFactory closed.");
        }
    }
}
