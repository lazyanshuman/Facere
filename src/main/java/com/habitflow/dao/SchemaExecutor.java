package com.habitflow.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SchemaExecutor
 * Reads schema.sql and runs each statement one by one.
 * Each statement is on a single line ending with semicolon.
 */
public class SchemaExecutor {

    private static final Logger log =
        LoggerFactory.getLogger(SchemaExecutor.class);

    private final DatabaseManager dbManager;

    public SchemaExecutor(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public void execute() {
        log.info("Running schema executor...");

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             InputStream is = getClass()
                 .getResourceAsStream("/db/schema.sql");
             BufferedReader reader = new BufferedReader(
                 new InputStreamReader(is,
                     StandardCharsets.UTF_8))) {

            if (is == null) {
                log.error("schema.sql not found!");
                return;
            }

            String line;
            StringBuilder current = new StringBuilder();
            int count = 0;

            while ((line = reader.readLine()) != null) {
                // Skip empty lines and comment lines
                String trimmed = line.trim();
                if (trimmed.isEmpty() ||
                    trimmed.startsWith("--")) {
                    continue;
                }

                current.append(trimmed).append(" ");

                // When we hit a semicolon at end of line
                // the statement is complete — execute it
                if (trimmed.endsWith(";")) {
                    String sql = current.toString()
                        .trim()
                        // remove the trailing semicolon
                        .replaceAll(";\\s*$", "");

                    if (!sql.isBlank()) {
                        stmt.execute(sql);
                        count++;
                    }
                    current = new StringBuilder();
                }
            }

            log.info("Schema done. {} statements executed.",
                count);

        } catch (SQLException e) {
            log.error("Schema SQL error: {}", e.getMessage());
            throw new RuntimeException(
                "Database schema setup failed.", e);
        } catch (IOException e) {
            log.error("Could not read schema.sql: {}",
                e.getMessage());
            throw new RuntimeException(
                "Could not read schema file.", e);
        }
    }
}