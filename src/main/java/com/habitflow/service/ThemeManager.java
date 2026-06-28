package com.habitflow.service;

import com.habitflow.dao.DatabaseManager;
import com.jthemedetecor.OsThemeDetector;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.PrimerDark;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.Objects;

/**
 * ThemeManager — controls the 4-way theme engine.
 *
 * LIGHT      → always light
 * DARK       → always dark
 * AUTO_SYSTEM → reads Windows dark/light setting
 * AUTO_TIME  → light 6am-7pm, dark 7pm-6am
 */
public class ThemeManager {

    private static final Logger log =
        LoggerFactory.getLogger(
            ThemeManager.class);

    // The 4 theme modes
    public enum ThemeMode {
        LIGHT,
        DARK,
        AUTO_SYSTEM,
        AUTO_TIME
    }

    // Sunrise and sunset times for Auto-Time
    private static final LocalTime SUNRISE =
        LocalTime.of(6, 0);   // 6:00 AM
    private static final LocalTime SUNSET =
        LocalTime.of(19, 0);  // 7:00 PM

    // Singleton
    private static ThemeManager instance;

    private Scene      scene;
    private ThemeMode  currentMode;
    private Timeline   autoTimeChecker;
    private OsThemeDetector osDetector;

    private ThemeManager() {}

    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    /**
     * Initialize with the app scene.
     * Call this once in HabitFlowApp.start()
     * after the scene is created.
     */
    public void initialize(Scene scene) {
        this.scene = scene;

        // Load saved theme from database
        String saved = loadThemeFromDB();
        ThemeMode mode;
        try {
            mode = ThemeMode.valueOf(saved);
        } catch (Exception e) {
            mode = ThemeMode.AUTO_SYSTEM;
        }

        applyMode(mode);
    }

    /**
     * Apply a theme mode.
     * Stops any running auto-checkers first,
     * then starts the new one if needed.
     */
    public void applyMode(ThemeMode mode) {
        this.currentMode = mode;

        // Stop previous auto-checkers
        stopAutoCheckers();

        switch (mode) {
            case LIGHT ->
                applyLight();
            case DARK ->
                applyDark();
            case AUTO_SYSTEM ->
                startAutoSystem();
            case AUTO_TIME ->
                startAutoTime();
        }

        // Save to database
        saveThemeToDB(mode.name());
        log.info("Theme mode set to: {}", mode);
    }

    /**
     * Returns the current theme mode.
     */
    public ThemeMode getCurrentMode() {
        return currentMode;
    }

    // ═══════════════════════════════════════════
    // LIGHT / DARK APPLICATION
    // ═══════════════════════════════════════════

    /**
     * Applies the light theme.
     * Uses AtlantaFX PrimerLight as base
     * plus our main.css on top.
     */
    private void applyLight() {
        Platform.runLater(() -> {
            Runnable apply = () -> {
                Application
                    .setUserAgentStylesheet(
                    new PrimerLight()
                    .getUserAgentStylesheet());

                scene.getStylesheets().clear();
                String mainCss =
                    Objects.requireNonNull(
                    getClass().getResource(
                    "/com/habitflow/css/" +
                    "main.css"))
                    .toExternalForm();
                scene.getStylesheets()
                    .add(mainCss);
            };

            if (scene != null &&
                    scene.getRoot() != null) {
                com.habitflow.util
                    .AnimationHelper
                    .themeCrossfade(
                        scene.getRoot(),
                        apply);
            } else {
                apply.run();
            }
            log.info("Applied LIGHT theme");
        });
    }

    /**
     * Applies the dark theme.
     * Uses AtlantaFX PrimerDark as base
     * plus our dark.css on top.
     */
    private void applyDark() {
        Platform.runLater(() -> {
            Runnable apply = () -> {
                Application
                    .setUserAgentStylesheet(
                    new PrimerDark()
                    .getUserAgentStylesheet());

                scene.getStylesheets().clear();
                String mainCss =
                    Objects.requireNonNull(
                    getClass().getResource(
                    "/com/habitflow/css/" +
                    "main.css"))
                    .toExternalForm();
                String darkCss =
                    Objects.requireNonNull(
                    getClass().getResource(
                    "/com/habitflow/css/" +
                    "dark.css"))
                    .toExternalForm();
                scene.getStylesheets()
                    .addAll(mainCss, darkCss);
            };

            if (scene != null &&
                    scene.getRoot() != null) {
                com.habitflow.util
                    .AnimationHelper
                    .themeCrossfade(
                        scene.getRoot(),
                        apply);
            } else {
                apply.run();
            }
            log.info("Applied DARK theme");
        });
    }

    // ═══════════════════════════════════════════
    // AUTO-SYSTEM — reads Windows registry
    // ═══════════════════════════════════════════

    /**
     * Starts the Auto-System theme detector.
     * Uses JSystemThemeDetector to listen for
     * Windows dark/light mode changes in the
     * registry (AppsUseLightTheme key).
     */
    private void startAutoSystem() {
        try {
            osDetector =
                OsThemeDetector.getDetector();

            // Apply immediately based on
            // current Windows setting
            boolean isDark =
                osDetector.isDark();
            if (isDark) {
                applyDark();
            } else {
                applyLight();
            }

            // Listen for future changes
            osDetector.registerListener(
                dark -> {
                    if (dark) {
                        applyDark();
                    } else {
                        applyLight();
                    }
                    log.info(
                        "System theme changed:" +
                        " {}", dark ?
                        "DARK" : "LIGHT");
                });

            log.info("AUTO_SYSTEM theme " +
                "detector started.");

        } catch (Exception e) {
            log.warn("Could not start OS theme" +
                " detector: {}. " +
                "Falling back to LIGHT.",
                e.getMessage());
            applyLight();
        }
    }

    // ═══════════════════════════════════════════
    // AUTO-TIME — sunrise/sunset switching
    // ═══════════════════════════════════════════

    /**
     * Starts the Auto-Time theme checker.
     * Checks every 60 seconds whether it is
     * between sunrise and sunset.
     * Light during the day, dark at night.
     */
    private void startAutoTime() {
        // Apply immediately
        applyTimeBasedTheme();

        // Check every 60 seconds
        autoTimeChecker = new Timeline(
            new KeyFrame(
                Duration.seconds(60),
                e -> applyTimeBasedTheme()));
        autoTimeChecker
            .setCycleCount(Timeline.INDEFINITE);
        autoTimeChecker.play();

        log.info("AUTO_TIME checker started." +
            " Sunrise: {}, Sunset: {}",
            SUNRISE, SUNSET);
    }

    /**
     * Applies light or dark based on
     * current time vs sunrise/sunset.
     */
    private void applyTimeBasedTheme() {
        LocalTime now = LocalTime.now();
        boolean isDaytime =
            now.isAfter(SUNRISE) &&
            now.isBefore(SUNSET);

        if (isDaytime) {
            applyLight();
        } else {
            applyDark();
        }

        log.info("Auto-Time: {} ({})",
            isDaytime ? "LIGHT" : "DARK",
            now);
    }

    // ═══════════════════════════════════════════
    // CLEANUP
    // ═══════════════════════════════════════════

    /**
     * Stops all running auto-checkers.
     * Called before switching to a new mode.
     */
    private void stopAutoCheckers() {
        if (autoTimeChecker != null) {
            autoTimeChecker.stop();
            autoTimeChecker = null;
        }
        if (osDetector != null) {
            try {
                // Just discard the reference —
                // the detector will be garbage
                // collected automatically.
                // A new one is created next time
                // AUTO_SYSTEM is activated.
                osDetector = null;
            } catch (Exception e) {
                log.warn("Could not stop " +
                    "OS detector: {}",
                    e.getMessage());
            }
        }
    }

    // ═══════════════════════════════════════════
    // DATABASE — save/load theme preference
    // ═══════════════════════════════════════════

    /**
     * Saves the current theme mode
     * to app_settings in the database.
     */
    private void saveThemeToDB(String mode) {
        String sql = """
            INSERT OR REPLACE INTO app_settings
                (key, value)
            VALUES ('theme_mode', ?)
            """;

        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {

            ps.setString(1, mode);
            ps.executeUpdate();

        } catch (SQLException e) {
            log.error("Failed to save theme: {}",
                e.getMessage());
        }
    }

    /**
     * Loads the saved theme mode from database.
     * Returns 'AUTO_SYSTEM' if not found.
     */
    private String loadThemeFromDB() {
        String sql = """
            SELECT value FROM app_settings
            WHERE key = 'theme_mode'
            """;

        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql);
             ResultSet rs =
                 ps.executeQuery()) {

            if (rs.next()) {
                return rs.getString("value");
            }

        } catch (SQLException e) {
            log.error("Failed to load theme: {}",
                e.getMessage());
        }

        return "AUTO_SYSTEM";
    }
}
