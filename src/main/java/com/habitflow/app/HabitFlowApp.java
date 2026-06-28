package com.habitflow.app;

import atlantafx.base.theme.PrimerLight;
import com.habitflow.controller.DueTasksModalController;
import com.habitflow.controller.LockScreenController;
import com.habitflow.controller.MainController;
import com.habitflow.controller.ProfileSetupController;
import com.habitflow.controller.SplashController;
import com.habitflow.controller.UserPickerController;
import com.habitflow.dao.AppDatabase;
import com.habitflow.dao.DatabaseManager;
import com.habitflow.dao.UserDAO;
import com.habitflow.model.User;
import com.habitflow.service.SecurityManager;
import com.habitflow.service.ThemeManager;
import com.habitflow.service.UserSession;
import com.habitflow.util.AppIcon;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

/**
 * Facere — startup flow:
 * 1. DB init + load UI
 * 2. Profile setup (first launch only)
 * 3. Splash greeting (no name)
 * 4. User picker (ALWAYS)
 * 5. Lock screen (if PIN)
 * 6. Due tasks (notify later only)
 */
public class HabitFlowApp extends Application {

    @Override
    public void start(Stage primaryStage)
            throws IOException {

        Application.setUserAgentStylesheet(
            new PrimerLight()
                .getUserAgentStylesheet());

        AppDatabase.initialize();

        URL fxmlUrl = getClass().getResource(
            "/com/habitflow/fxml/" +
            "MainView.fxml");
        if (fxmlUrl == null) {
            throw new IOException(
                "Cannot find MainView.fxml!");
        }

        FXMLLoader loader =
            new FXMLLoader(fxmlUrl);
        BorderPane root = loader.load();
        MainController mainCtrl =
            loader.getController();

        Scene scene = new Scene(root,
            1100, 700);

        ThemeManager.getInstance()
            .initialize(scene);

        primaryStage.setTitle("Facere");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(400);
        AppIcon.set(primaryStage);

        restoreWindowState(primaryStage);
        primaryStage.show();

        primaryStage.setOnCloseRequest(e ->
            saveWindowState(primaryStage));

        // ── User setup (first launch) ────
        UserDAO userDAO = new UserDAO();
        List<User> users =
            userDAO.getAllUsers();

        if (users.isEmpty()) {
            ProfileSetupController setup =
                new ProfileSetupController();
            boolean created =
                setup.show(primaryStage, true);
            if (!created) {
                javafx.application.Platform
                    .exit();
                return;
            }
            // Refresh user list
            users = userDAO.getAllUsers();
        } else if (users.size() == 1 &&
                "User 1".equals(
                    users.get(0)
                        .getFullName())) {
            User defaultUser = users.get(0);
            ProfileSetupController setup =
                new ProfileSetupController();
            boolean created =
                setup.show(primaryStage, true);

            if (created) {
                User newUser =
                    UserSession.getCurrentUser();
                reassignData(
                    defaultUser.getId(),
                    newUser.getId());
                userDAO.deleteUser(
                    defaultUser.getId());
            } else {
                UserSession.setCurrentUser(
                    defaultUser);
            }
            users = userDAO.getAllUsers();
        }

        // ── Splash greeting (no name) ────
        new SplashController()
            .showSplash(primaryStage);

        // ── ALWAYS show user picker ──────
        if (users.size() == 1) {
            // Only 1 user — auto-select
            UserSession.setCurrentUser(
                users.get(0));
        } else {
            // Multiple users — show picker
            UserPickerController picker =
                new UserPickerController();
            boolean picked =
                picker.show(primaryStage);
            if (!picked) {
                // Cancelled — use first user
                UserSession.setCurrentUser(
                    users.get(0));
            }
        }

        // ── Refresh avatar ───────────────
        mainCtrl.refreshAvatar();

        SecurityManager security =
            SecurityManager.getInstance();
        if (security.shouldLockOnStartup()) {
            security.lockApp();
            boolean unlocked =
                new LockScreenController()
                    .show(primaryStage);
            if (!unlocked) {
                javafx.application.Platform
                    .exit();
                return;
            }
        }

        // ── Due tasks (notify later) ─────
        DueTasksModalController
            .checkAndShow(primaryStage);
    }

    private void restoreWindowState(
            Stage stage) {
        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(
                     "SELECT key, value " +
                     "FROM app_settings " +
                     "WHERE key LIKE " +
                     "'window_%'")) {

            ResultSet rs = ps.executeQuery();
            boolean found = false;
            double x = 0, y = 0;
            double w = 1100, h = 700;
            boolean maximized = true;

            while (rs.next()) {
                found = true;
                String key =
                    rs.getString("key");
                String val =
                    rs.getString("value");
                switch (key) {
                    case "window_x" ->
                        x = Double.parseDouble(
                            val);
                    case "window_y" ->
                        y = Double.parseDouble(
                            val);
                    case "window_w" ->
                        w = Double.parseDouble(
                            val);
                    case "window_h" ->
                        h = Double.parseDouble(
                            val);
                    case "window_maximized" ->
                        maximized =
                            "true".equals(val);
                }
            }

            if (found && !maximized) {
                stage.setX(x);
                stage.setY(y);
                stage.setWidth(w);
                stage.setHeight(h);
                stage.setMaximized(false);
            } else {
                stage.setMaximized(true);
            }
        } catch (Exception e) {
            stage.setMaximized(true);
        }
    }

    private void saveWindowState(
            Stage stage) {
        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection()) {

            String sql =
                "INSERT OR REPLACE INTO " +
                "app_settings (key, value) " +
                "VALUES (?, ?)";

            saveSetting(conn, sql,
                "window_maximized",
                String.valueOf(
                    stage.isMaximized()));

            if (!stage.isMaximized()) {
                saveSetting(conn, sql,
                    "window_x",
                    String.valueOf(
                        stage.getX()));
                saveSetting(conn, sql,
                    "window_y",
                    String.valueOf(
                        stage.getY()));
                saveSetting(conn, sql,
                    "window_w",
                    String.valueOf(
                        stage.getWidth()));
                saveSetting(conn, sql,
                    "window_h",
                    String.valueOf(
                        stage.getHeight()));
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private void saveSetting(
            Connection conn, String sql,
            String key, String value) {
        try (PreparedStatement ps =
                 conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (Exception e) {
            // ignore
        }
    }

    private void reassignData(
            int fromUserId, int toUserId) {
        try (var conn =
                 DatabaseManager.getInstance()
                     .getConnection();
             var st =
                 conn.createStatement()) {

            st.execute(
                "UPDATE tasks SET user_id = " +
                toUserId +
                " WHERE user_id = " +
                fromUserId);
            st.execute(
                "UPDATE habits SET user_id = " +
                toUserId +
                " WHERE user_id = " +
                fromUserId);
            st.execute(
                "UPDATE dodoants SET user_id = " +
                toUserId +
                " WHERE user_id = " +
                fromUserId);

        } catch (Exception e) {
            // log error
        }
    }
}