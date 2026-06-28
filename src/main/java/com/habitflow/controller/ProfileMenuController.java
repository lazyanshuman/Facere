package com.habitflow.controller;

import com.habitflow.dao.UserDAO;
import com.habitflow.model.User;
import com.habitflow.service.SecurityManager;
import com.habitflow.service.ThemeManager;
import com.habitflow.service.UserSession;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * ProfileMenuController — with multi-user.
 */
public class ProfileMenuController {

    private final ThemeManager themeManager =
        ThemeManager.getInstance();
    private final SecurityManager security =
        SecurityManager.getInstance();
    private final SecuritySetupController
        securitySetup =
            new SecuritySetupController();
    private final UserDAO userDAO =
        new UserDAO();

    public void show(
            Button profileButton,
            Stage ownerStage) {

        ContextMenu menu = new ContextMenu();
        menu.setStyle(
            "-fx-background-radius: 12px;" +
            "-fx-padding: 4px;");

        // ── USER section ─────────────────
        User current =
            UserSession.getCurrentUser();
        String userName = current != null
            ? current.getFullName()
            : "User";

        MenuItem userHeader = disabledItem(
            "👤  " + userName);

        // Switch User
        MenuItem switchUser = new MenuItem(
            "🔄  Switch User");
        switchUser.setStyle(
            "-fx-font-size: 13px;");
        switchUser.setOnAction(e -> {
            menu.hide();
            UserPickerController picker =
                new UserPickerController();
            boolean changed =
                picker.show(ownerStage);
            if (changed) {
                // Reload the main view
                reloadApp(ownerStage);
            }
        });

        // Add User
        MenuItem addUser = new MenuItem(
            "➕  Add User");
        addUser.setStyle(
            "-fx-font-size: 13px;");
        addUser.setDisable(
            userDAO.getUserCount()
                >= UserDAO.MAX_USERS);
        addUser.setOnAction(e -> {
            menu.hide();
            ProfileSetupController setup =
                new ProfileSetupController();
            boolean created =
                setup.show(ownerStage, false);
            if (created) {
                reloadApp(ownerStage);
            }
        });

        // Delete User
        MenuItem deleteUser = new MenuItem(
            "🗑  Delete This User");
        deleteUser.setStyle(
            "-fx-font-size: 13px;");
        deleteUser.setDisable(
            userDAO.getUserCount() <= 1);
        deleteUser.setOnAction(e -> {
            menu.hide();
            confirmDeleteUser(ownerStage);
        });

        SeparatorMenuItem sepUser =
            new SeparatorMenuItem();

        // ── THEME section ────────────────
        MenuItem themeHeader =
            disabledItem("🎨  Theme");

        MenuItem lightItem = menuItem(
            "☀️   Light Mode",
            themeManager.getCurrentMode() ==
            ThemeManager.ThemeMode.LIGHT);
        lightItem.setOnAction(e ->
            themeManager.applyMode(
                ThemeManager.ThemeMode.LIGHT));

        MenuItem darkItem = menuItem(
            "🌙   Dark Mode",
            themeManager.getCurrentMode() ==
            ThemeManager.ThemeMode.DARK);
        darkItem.setOnAction(e ->
            themeManager.applyMode(
                ThemeManager.ThemeMode.DARK));

        MenuItem autoSystemItem = menuItem(
            "🖥️   Auto (System)",
            themeManager.getCurrentMode() ==
            ThemeManager.ThemeMode.AUTO_SYSTEM);
        autoSystemItem.setOnAction(e ->
            themeManager.applyMode(
                ThemeManager.ThemeMode
                    .AUTO_SYSTEM));

        MenuItem autoTimeItem = menuItem(
            "🌅   Auto (Time of Day)",
            themeManager.getCurrentMode() ==
            ThemeManager.ThemeMode.AUTO_TIME);
        autoTimeItem.setOnAction(e ->
            themeManager.applyMode(
                ThemeManager.ThemeMode
                    .AUTO_TIME));

        SeparatorMenuItem sep1 =
            new SeparatorMenuItem();

        // ── SECURITY section ─────────────
        MenuItem secHeader =
            disabledItem("🔒  Security");

        MenuItem pinItem;
        if (security.isPinSetup()) {
            pinItem = new MenuItem(
                "🔄  Change PIN");
            pinItem.setOnAction(e ->
                securitySetup
                    .showChangePinDialog());
        } else {
            pinItem = new MenuItem(
                "🔐  Set Up PIN Lock");
            pinItem.setOnAction(e ->
                securitySetup
                    .showSetupDialog());
        }

        MenuItem lockItem = new MenuItem(
            "🔒  Lock App Now");
        lockItem.setDisable(
            !security.isPinSetup());
        lockItem.setOnAction(e -> {
            security.lockApp();
            menu.hide();
            new LockScreenController()
                .show(ownerStage);
        });

        MenuItem removePinItem =
            new MenuItem(
                "🗑️  Remove PIN Lock");
        removePinItem.setDisable(
            !security.isPinSetup());
        removePinItem.setOnAction(e ->
            securitySetup
                .showRemovePinDialog());

        SeparatorMenuItem sep2 =
            new SeparatorMenuItem();
        
        SeparatorMenuItem sep3 =
            new SeparatorMenuItem();

        MenuItem resetApp = new MenuItem(
            "⚠️  Reset App (Factory)");
        resetApp.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #EF4444;");
        resetApp.setOnAction(e -> {
            menu.hide();
            confirmResetApp(ownerStage);
        });

        MenuItem appInfo = disabledItem(
            "ℹ️   Facere v1.0");

        // ── Assemble ─────────────────────
       menu.getItems().addAll(
            userHeader,
            switchUser,
            addUser,
            deleteUser,
            sepUser,
            themeHeader,
            lightItem,
            darkItem,
            autoSystemItem,
            autoTimeItem,
            sep1,
            secHeader,
            pinItem,
            lockItem,
            removePinItem,
            sep2,
            sep3,
            resetApp,
            appInfo);

menu.show(profileButton,
            javafx.geometry.Side.BOTTOM,
            0, 4);
    }

    /**
     * Confirms and deletes the current user.
     */
    private void confirmDeleteUser(
            Stage owner) {
        User current =
            UserSession.getCurrentUser();
        if (current == null) return;

        Alert confirm = new Alert(
            Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete User");
        confirm.setHeaderText(
            "Delete \"" +
            current.getFullName() + "\"?");
        confirm.setContentText(
            "This will permanently delete " +
            "this user and ALL their data " +
            "(tasks, habits, dos/don'ts).\n\n" +
            "This cannot be undone!");

        confirm.showAndWait()
            .ifPresent(r -> {
                if (r == ButtonType.OK) {
                    int deletedId =
                        current.getId();
                    userDAO.deleteUser(
                        deletedId);

                    // Switch to first
                    // remaining user
                    java.util.List<User> remaining =
                        userDAO.getAllUsers();
                    if (!remaining.isEmpty()) {
                        UserSession.setCurrentUser(
                            remaining.get(0));
                        reloadApp(owner);
                    }
                }
            });
    }

    /**
     * Reloads the main view to reflect
     * the new user's data.
     */
private void reloadApp(Stage owner) {
        try {
            javafx.fxml.FXMLLoader loader =
                new javafx.fxml.FXMLLoader(
                    getClass().getResource(
                        "/com/habitflow/fxml/" +
                        "MainView.fxml"));
            javafx.scene.layout.BorderPane root =
                loader.load();
            owner.getScene().setRoot(root);

            // Refresh avatar on new controller
            MainController newCtrl =
                loader.getController();
            newCtrl.refreshAvatar();

            ThemeManager.getInstance()
                .initialize(owner.getScene());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MenuItem menuItem(
            String text, boolean isActive) {
        MenuItem item = new MenuItem(
            isActive
                ? "✓  " + text
                : "     " + text);
        item.setStyle(
            "-fx-font-size: 13px;" +
            (isActive
                ? "-fx-font-weight: bold;"
                : ""));
        return item;
    }

    private MenuItem disabledItem(
            String text) {
        MenuItem item = new MenuItem(text);
        item.setStyle(
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #9CA3AF;" +
            "-fx-font-size: 11px;");
        item.setDisable(true);
        return item;
    }

    /**
     * Completely resets the app — deletes
     * all data and restarts fresh.
     */
    private void confirmResetApp(
            Stage owner) {
        Alert warn = new Alert(
            Alert.AlertType.WARNING);
        warn.setTitle("Factory Reset");
        warn.setHeaderText(
            "Reset Facere completely?");
        warn.setContentText(
            "This will permanently delete:\n" +
            "• All users and profiles\n" +
            "• All tasks, habits, " +
            "dos/don'ts\n" +
            "• All stats and settings\n" +
            "• Everything.\n\n" +
            "The app will restart as if " +
            "freshly installed.\n\n" +
            "THIS CANNOT BE UNDONE!");

        warn.getButtonTypes().setAll(
            ButtonType.OK,
            ButtonType.CANCEL);

        warn.showAndWait()
            .ifPresent(r -> {
                if (r == ButtonType.OK) {
                    // Double confirm
                    Alert confirm = new Alert(
                        Alert.AlertType
                            .CONFIRMATION);
                    confirm.setTitle(
                        "Are you sure?");
                    confirm.setHeaderText(
                        "Last chance — delete " +
                        "everything?");
                    confirm.setContentText(
                        "Type YES in your " +
                        "mind and click OK.");

                    confirm.showAndWait()
                        .ifPresent(r2 -> {
                        if (r2 ==
                                ButtonType.OK) {
                            doFactoryReset(
                                owner);
                        }
                    });
                }
            });
    }

    private void doFactoryReset(
            Stage owner) {
        try (var conn =
                 com.habitflow.dao
                     .DatabaseManager
                     .getInstance()
                     .getConnection();
             var st =
                 conn.createStatement()) {

            // Delete all data
            st.execute(
                "DELETE FROM dodont_logs");
            st.execute(
                "DELETE FROM habit_logs");
            st.execute(
                "DELETE FROM shared_tasks");
            st.execute(
                "DELETE FROM dodoants");
            st.execute(
                "DELETE FROM habits");
            st.execute(
                "DELETE FROM tasks");
            st.execute(
                "DELETE FROM users");
            st.execute(
                "DELETE FROM app_settings");
            st.execute(
                "DELETE FROM " +
                "daily_catchup_log");
            st.execute(
                "DELETE FROM notifications");
            st.execute(
                "DELETE FROM ai_summaries");

            com.habitflow.service.UserSession
                .clear();

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Restart the app
        javafx.application.Platform.runLater(
            () -> {
            try {
                owner.close();
                new com.habitflow.app
                    .HabitFlowApp()
                    .start(new javafx.stage
                        .Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}