package com.habitflow.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.habitflow.dao.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * SecurityManager
 *
 * Handles all security operations:
 * - PIN setup and verification
 * - Windows Hello biometric auth via JNA
 * - Lock/unlock state management
 * - Storing BCrypt hash in app_settings
 */
public class SecurityManager {

    private static final Logger log =
        LoggerFactory.getLogger(
            SecurityManager.class);

    // Database keys for security settings
    private static final String KEY_PIN_HASH =
        "lock_pin_hash";
    private static final String KEY_APP_LOCKED =
        "app_locked";
    private static final String KEY_BIOMETRIC =
        "biometric_enabled";

    // Singleton
    private static SecurityManager instance;

    // Current lock state
    private boolean isLocked = false;

    private SecurityManager() {}

    public static SecurityManager getInstance() {
        if (instance == null) {
            instance = new SecurityManager();
        }
        return instance;
    }

    // ═══════════════════════════════════════
    // PIN MANAGEMENT
    // ═══════════════════════════════════════

    /**
     * Returns true if a PIN has been set up.
     */
    public boolean isPinSetup() {
        String hash = getSetting(KEY_PIN_HASH);
        return hash != null &&
               !hash.isBlank();
    }

    /**
     * Sets up a new PIN.
     * Hashes it with BCrypt cost factor 12
     * and stores in app_settings.
     *
     * @param pin the raw PIN entered by user
     * @return true if saved successfully
     */
    public boolean setupPin(String pin) {
        if (pin == null ||
            pin.trim().length() < 4) {
            log.warn("PIN must be at least " +
                "4 characters.");
            return false;
        }

        // Hash with BCrypt cost 12
        String hash = BCrypt.withDefaults()
            .hashToString(12,
                pin.toCharArray());

        saveSetting(KEY_PIN_HASH, hash);
        saveSetting(KEY_APP_LOCKED, "true");
        log.info("PIN set up successfully.");
        return true;
    }

    /**
     * Verifies a PIN against the stored hash.
     *
     * @param pin the raw PIN to verify
     * @return true if PIN is correct
     */
    public boolean verifyPin(String pin) {
        if (pin == null || pin.isBlank()) {
            return false;
        }

        String storedHash =
            getSetting(KEY_PIN_HASH);
        if (storedHash == null ||
            storedHash.isBlank()) {
            log.warn("No PIN hash found.");
            return false;
        }

        BCrypt.Result result =
            BCrypt.verifyer()
                .verify(pin.toCharArray(),
                    storedHash);

        if (result.verified) {
            log.info("PIN verified. " +
                "Unlocking app.");
            isLocked = false;
        } else {
            log.warn("Incorrect PIN entered.");
        }

        return result.verified;
    }

    /**
     * Removes the PIN and disables lock.
     */
    public void removePin() {
        saveSetting(KEY_PIN_HASH, "");
        saveSetting(KEY_APP_LOCKED, "false");
        saveSetting(KEY_BIOMETRIC, "false");
        isLocked = false;
        log.info("PIN removed. " +
            "App lock disabled.");
    }

    /**
     * Changes the PIN.
     * Requires old PIN verification first.
     */
    public boolean changePin(
            String oldPin, String newPin) {
        if (!verifyPin(oldPin)) {
            log.warn("Change PIN failed: " +
                "old PIN incorrect.");
            return false;
        }
        return setupPin(newPin);
    }

    // ═══════════════════════════════════════
    // LOCK STATE
    // ═══════════════════════════════════════

    /**
     * Returns true if the app should show
     * the lock screen on startup.
     * Only locks if PIN has been set up.
     */
    public boolean shouldLockOnStartup() {
        if (!isPinSetup()) return false;
        String locked =
            getSetting(KEY_APP_LOCKED);
        return "true".equals(locked);
    }

    /**
     * Locks the app.
     * Call this when user clicks Lock App
     * in the Me menu.
     */
    public void lockApp() {
        if (!isPinSetup()) return;
        isLocked = true;
        log.info("App locked.");
    }

    /**
     * Unlocks the app after successful auth.
     */
    public void unlockApp() {
        isLocked = false;
        log.info("App unlocked.");
    }

    public boolean isLocked() {
        return isLocked;
    }

    // ═══════════════════════════════════════
    // WINDOWS HELLO / BIOMETRICS
    // ═══════════════════════════════════════

    /**
     * Returns true if biometric auth
     * is enabled in settings.
     */
    public boolean isBiometricEnabled() {
        return "true".equals(
            getSetting(KEY_BIOMETRIC));
    }

    /**
     * Enables or disables biometric auth.
     */
    public void setBiometricEnabled(
            boolean enabled) {
        saveSetting(KEY_BIOMETRIC,
            enabled ? "true" : "false");
        log.info("Biometric enabled: {}",
            enabled);
    }

    /**
     * Attempts Windows Hello authentication.
     *
     * Uses JNA to call the Windows
     * Biometric Framework (WinBio).
     * Falls back to PIN if biometric fails.
     *
     * Returns true if authentication
     * succeeded.
     */
    public boolean authenticateWithHello() {
        try {
            // Check if Windows Hello
            // is available on this device
            if (!isWindowsHelloAvailable()) {
                log.warn("Windows Hello not " +
                    "available on this device.");
                return false;
            }

            // Trigger Windows Hello prompt
            // This opens the native Windows
            // biometric dialog
            boolean result =
                triggerWindowsHello();

            if (result) {
                unlockApp();
                log.info("Windows Hello " +
                    "authentication successful.");
            } else {
                log.warn("Windows Hello " +
                    "authentication failed.");
            }

            return result;

        } catch (Exception e) {
            log.error("Windows Hello error: {}",
                e.getMessage());
            return false;
        }
    }

    /**
     * Checks if Windows Hello biometrics
     * are available and enrolled on this PC.
     *
     * Uses JNA to call WinBio API.
     */
    private boolean isWindowsHelloAvailable() {
        try {
            // Check if Windows Hello is
            // available via PowerShell
            String psScript =
                "try {" +
                "[Windows.Security.Credentials" +
                ".UI.UserConsentVerifier," +
                "Windows.Security.Credentials" +
                ".UI, ContentType=" +
                "WindowsRuntime] | Out-Null;" +
                "$result = [Windows.Security" +
                ".Credentials.UI" +
                ".UserConsentVerifier]" +
                "::CheckAvailabilityAsync()" +
                ".GetAwaiter().GetResult();" +
                "if ($result -eq " +
                "'Available') { exit 0 }" +
                " else { exit 1 }" +
                "} catch { exit 1 }";

            ProcessBuilder pb =
                new ProcessBuilder(
                    "powershell.exe",
                    "-NonInteractive",
                    "-NoProfile",
                    "-WindowStyle",
                    "Hidden",
                    "-Command",
                    psScript);

            Process p = pb.start();
            int code = p.waitFor();
            return code == 0;

        } catch (Exception e) {
            log.warn("Hello availability " +
                "check failed: {}",
                e.getMessage());
            return false;
        }
    }

    /**
     * Triggers the native Windows Hello
     * authentication dialog.
     *
     * Uses PowerShell to invoke the
     * Windows Security prompt — this is
     * the most reliable cross-version
     * approach for Windows 10/11.
     */
    private boolean triggerWindowsHello() {
        try {
            // Use PowerShell with Windows
            // Runtime API properly loaded
            String psScript =
                "[Windows.Security.Credentials" +
                ".UI.UserConsentVerifier," +
                "Windows.Security.Credentials" +
                ".UI, ContentType=" +
                "WindowsRuntime] | Out-Null\n" +
                "$op = [Windows.Security" +
                ".Credentials.UI" +
                ".UserConsentVerifier]" +
                "::RequestVerificationAsync" +
                "('Unlock HabitFlow')\n" +
                "$result = $op.GetAwaiter()" +
                ".GetResult()\n" +
                "if ($result -eq 'Verified')" +
                " { exit 0 } else { exit 1 }";

            ProcessBuilder pb =
                new ProcessBuilder(
                    "powershell.exe",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-NonInteractive",
                    "-NoProfile",
                    "-WindowStyle",
                    "Hidden",
                    "-Command",
                    psScript);
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // Read output for debugging
            String output = new String(
                process.getInputStream()
                    .readAllBytes());
            if (!output.isBlank()) {
                log.info("WinHello output: {}",
                    output.trim());
            }

            int exitCode =
                process.waitFor();
            log.info("Windows Hello exit " +
                "code: {}", exitCode);

            return exitCode == 0;

        } catch (Exception e) {
            log.error("Windows Hello error:" +
                " {}", e.getMessage());
            return false;
        }
    }

    // ═══════════════════════════════════════
    // DATABASE HELPERS
    // ═══════════════════════════════════════

    private String getSetting(String key) {
        String sql = """
            SELECT value FROM app_settings
            WHERE key = ?
            """;

        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {

            ps.setString(1, key);
            try (ResultSet rs =
                     ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(
                        "value");
                }
            }

        } catch (SQLException e) {
            log.error("Failed to get " +
                "setting {}: {}",
                key, e.getMessage());
        }
        return null;
    }

    private void saveSetting(
            String key, String value) {
        String sql = """
            INSERT OR REPLACE INTO
                app_settings (key, value)
            VALUES (?, ?)
            """;

        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {

            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();

        } catch (SQLException e) {
            log.error("Failed to save " +
                "setting {}: {}",
                key, e.getMessage());
        }
    }
}