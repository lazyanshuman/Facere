package com.habitflow.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * UserMigration
 *
 * Safely upgrades an existing database to
 * support multiple users.
 *
 * - Adds user_id column to tasks, habits,
 *   dodoants (if missing)
 * - Creates a default "User 1" from existing
 *   data if no users exist yet
 * - Assigns all existing data to User 1
 *
 * Runs once on startup. Safe to run repeatedly
 * (checks before making changes).
 */
public class UserMigration {

    private static final Logger log =
        LoggerFactory.getLogger(
            UserMigration.class);

    public static void run() {
        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection();
             Statement st =
                 conn.createStatement()) {

            // 1. Add user_id columns if missing
            addColumnIfMissing(conn, st,
                "tasks", "user_id");
            addColumnIfMissing(conn, st,
                "habits", "user_id");
            addColumnIfMissing(conn, st,
                "dodoants", "user_id");

            // 2. Create default user if none
            //    and assign existing data
            ensureDefaultUser(conn, st);

            log.info("User migration complete.");

        } catch (Exception e) {
            log.error("User migration failed: {}",
                e.getMessage());
        }
    }

    /**
     * Adds a column to a table if it doesn't
     * already exist.
     */
    private static void addColumnIfMissing(
            Connection conn,
            Statement st,
            String table,
            String column) {
        try {
            boolean exists = false;
            try (ResultSet rs = st.executeQuery(
                    "PRAGMA table_info(" +
                    table + ")")) {
                while (rs.next()) {
                    if (column.equalsIgnoreCase(
                            rs.getString("name"))) {
                        exists = true;
                        break;
                    }
                }
            }

            if (!exists) {
                st.execute(
                    "ALTER TABLE " + table +
                    " ADD COLUMN " + column +
                    " INTEGER DEFAULT NULL");
                log.info("Added {}.{} column",
                    table, column);
            }
        } catch (Exception e) {
            log.warn("Could not add {}.{}: {}",
                table, column, e.getMessage());
        }
    }

    /**
     * If no users exist, creates "User 1"
     * and assigns all existing tasks, habits,
     * and dos/don'ts to them.
     */
    private static void ensureDefaultUser(
            Connection conn,
            Statement st) throws Exception {

        // Check if any users exist
        int userCount = 0;
        try (ResultSet rs = st.executeQuery(
                "SELECT COUNT(*) AS c " +
                "FROM users")) {
            if (rs.next()) {
                userCount = rs.getInt("c");
            }
        }

        if (userCount > 0) {
            return; // Users already exist
        }

        // Create default User 1
        st.execute(
            "INSERT INTO users " +
            "(full_name, username, " +
            "avatar_color, accent_color, " +
            "sort_order) VALUES " +
            "('User 1', 'user1', " +
            "'#6C63FF', '#6C63FF', 0)");

        // Get the new user's id
        int userId = 1;
        try (ResultSet rs = st.executeQuery(
                "SELECT id FROM users " +
                "WHERE username = 'user1'")) {
            if (rs.next()) {
                userId = rs.getInt("id");
            }
        }

        // Assign all existing data to User 1
        st.execute(
            "UPDATE tasks SET user_id = " +
            userId + " WHERE user_id IS NULL");
        st.execute(
            "UPDATE habits SET user_id = " +
            userId + " WHERE user_id IS NULL");
        st.execute(
            "UPDATE dodoants SET user_id = " +
            userId + " WHERE user_id IS NULL");

        log.info("Created default User 1 " +
            "and assigned existing data.");
    }
}