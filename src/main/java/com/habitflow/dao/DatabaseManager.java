package com.habitflow.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DatabaseManager
 *
 * Think of this like a water tap.
 * When your app needs to talk to the database it "opens the tap"
 * to get a connection. When it's done it "closes the tap".
 *
 * The database is stored as a single file on your computer:
 * %APPDATA%\Facere\facere.db
 */
public class DatabaseManager {

    // Logger — prints messages to the terminal so we can
    // see what is happening behind the scenes
    private static final Logger log =
        LoggerFactory.getLogger(DatabaseManager.class);

    // The folder where the database file will be saved
    // AppData\Roaming is the standard Windows location
    // for app data files — same place as Discord, Spotify etc.
    private static final String APP_DATA_FOLDER =
        System.getenv("APPDATA") + File.separator + "Facere";

    // The full path to the database file
    private static final String DB_FILE =
        APP_DATA_FOLDER + File.separator + "facere.db";

    // The JDBC connection string — tells Java where the
    // database file is located
    private static final String DB_URL =
        "jdbc:sqlite:" + DB_FILE;

    // ── Singleton pattern ────────────────────────────────────
    // We only ever want ONE DatabaseManager in the whole app.
    // This variable holds that single instance.
    private static DatabaseManager instance;

    // Private constructor — nobody can create a
    // DatabaseManager from outside this class
    private DatabaseManager() {
        // Create the HabitFlow folder in AppData if it
        // doesn't exist yet (first launch)
        File folder = new File(APP_DATA_FOLDER);
        if (!folder.exists()) {
            boolean created = folder.mkdirs();
            if (created) {
                log.info("Created HabitFlow data folder at: {}",
                    APP_DATA_FOLDER);
            }
        }
    }

    /**
     * Gets the single instance of DatabaseManager.
     * Call this from anywhere in the app like:
     *   DatabaseManager.getInstance()
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Opens a connection to the database.
     *
     * IMPORTANT: Every time you call getConnection()
     * you MUST close it when done. Always use it like:
     *
     *   try (Connection conn = DatabaseManager
     *            .getInstance().getConnection()) {
     *       // do your database work here
     *   }
     *
     * The "try-with-resources" above closes it automatically.
     */
    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);

        // These 3 settings are applied to EVERY connection.
        // They make SQLite faster and safer.
        try (Statement stmt = conn.createStatement()) {

            // WAL mode — allows reading while writing
            stmt.execute("PRAGMA journal_mode = WAL");

            // Foreign keys — enforces relationships between
            // tables (e.g. can't add a task with a
            // non-existent category)
            stmt.execute("PRAGMA foreign_keys = ON");

            // Synchronous normal — safe but fast
            stmt.execute("PRAGMA synchronous = NORMAL");
        }

        return conn;
    }

    /**
     * Returns the full path to the database file.
     * Useful for showing the user where their data is stored.
     */
    public String getDatabasePath() {
        return DB_FILE;
    }

    /**
     * Returns the folder where Facere stores its data.
     */
    public String getDataFolderPath() {
        return APP_DATA_FOLDER;
    }

    /**
     * Checks if the database file already exists.
     * Returns true if this is NOT the first launch.
     */
    public boolean databaseExists() {
        return new File(DB_FILE).exists();
    }
}