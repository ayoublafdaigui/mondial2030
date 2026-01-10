package com.mondial2030.app;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.mondial2030.util.DatabaseSeeder;
import com.mondial2030.util.HibernateUtil;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main Application Entry Point - Mondial 2030 Ticket Management System.
 * Initializes Hibernate, seeds the database, and launches the JavaFX UI.
 */
public class MainApp extends Application {
    
    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        super.init();
        
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║     🏟️  MONDIAL 2030 - Ticket Management        ║");
        System.out.println("║     Morocco • Spain • Portugal                   ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();
        
        // Initialize Hibernate (this will create/update the database schema)
        System.out.println("🔧 Initializing database connection...");
        HibernateUtil.getSessionFactory();
        
        // Seed the database with initial data
        DatabaseSeeder seeder = new DatabaseSeeder();
        if (seeder.needsSeeding()) {
            seeder.seedAll();
        } else {
            System.out.println("ℹ️ Database already contains data, skipping seeding.");
        }
        
        System.out.println();
        System.out.println("🚀 Starting application...");
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the Home FXML (landing page for all users including guests)
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Home.fxml"));
            
            // Create the scene with styles
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            // Configure the primary stage
            primaryStage.setTitle("Mondial 2030 - FIFA World Cup Tickets");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(600);
            primaryStage.centerOnScreen();
            
            // Show the stage
            primaryStage.show();
            
            System.out.println("✅ Application started successfully!");
            System.out.println();
            System.out.println("📝 Default Admin Credentials:");
            System.out.println("   Username: admin");
            System.out.println("   Password: admin123");
            System.out.println();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load the application UI", e);
            
            // Fallback to programmatic UI if FXML fails
            showFallbackUI(primaryStage);
        }
    }

    /**
     * Fallback UI in case FXML loading fails.
     */
    private void showFallbackUI(Stage primaryStage) {
        try {
            // Use the legacy programmatic UI as fallback
            LegacyDashboard legacyUI = new LegacyDashboard();
            legacyUI.start(primaryStage);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fallback UI also failed", e);
        }
    }

    @Override
    public void stop() throws Exception {
        System.out.println();
        System.out.println("🛑 Shutting down application...");
        HibernateUtil.shutdown();
        System.out.println("👋 Goodbye!");
        super.stop();
    }
}
