package com.habitflow.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AppDatabase
 *
 * Main entry point for database setup.
 * Called once on startup. Handles:
 * 1. Creating the Facere folder
 * 2. Creating the database file
 * 3. Running schema to create tables
 * 4. Running user migration for
 *    multi-user support
 */
public class AppDatabase {

    private static final Logger log =
        LoggerFactory.getLogger(
            AppDatabase.class);

    private AppDatabase() {}

    /**
     * Call this ONCE when the app starts.
     */
    public static void initialize() {
        log.info(
            "Initializing Facere database...");

        DatabaseManager dbManager =
            DatabaseManager.getInstance();

        log.info("Database location: {}",
            dbManager.getDatabasePath());

        boolean isFirstLaunch =
            !dbManager.databaseExists();
        if (isFirstLaunch) {
            log.info("First launch detected! " +
                "Creating database and tables...");
        } else {
            log.info("Existing database found. " +
                "Verifying tables...");
        }

        // Run schema — creates all tables
        SchemaExecutor executor =
            new SchemaExecutor(dbManager);
        executor.execute();

        // Upgrade DB for multiple users
        UserMigration.run();

        log.info("Database initialization " +
            "complete! Facere is ready.");
    }
}