package com.habitflow.controller;

import com.habitflow.dao.UserDAO;
import com.habitflow.model.User;
import com.habitflow.service.UserSession;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * ProfileSetupController
 *
 * Shows a "Create your profile" screen.
 * Used on first launch and when adding
 * a 2nd or 3rd user.
 *
 * One screen: name, username, avatar colour.
 * PIN is optional — set later from Me menu.
 */
public class ProfileSetupController {

    // Preset avatar colours (Google-style)
    private static final String[] COLORS = {
        "#6C63FF", "#EF4444", "#F59E0B",
        "#10B981", "#3B82F6", "#EC4899",
        "#8B5CF6", "#14B8A6", "#F97316"
    };

    private final UserDAO userDAO =
        new UserDAO();

    private String selectedColor = COLORS[0];
    private boolean wasSaved = false;

    /**
     * Shows the profile setup screen.
     *
     * @param owner the main app stage
     * @param isFirstUser true if this is
     *        the very first user (no cancel)
     * @return true if a user was created
     */
    public boolean show(Stage owner,
            boolean isFirstUser) {

        Stage stage = new Stage();
        stage.initModality(
            Modality.APPLICATION_MODAL);
        stage.initOwner(owner);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("Create Profile");

        com.habitflow.util.AppIcon.set(stage);

        VBox root = new VBox(0);
        root.setStyle(
            "-fx-background-color: #FFFFFF;" +
            "-fx-background-radius: 16px;");
        root.setPrefWidth(460);

        // ── Header ───────────────────────
        VBox header = new VBox(6);
        header.setPadding(
            new Insets(36, 40, 20, 40));
        header.setAlignment(Pos.CENTER);

        Label emoji = new Label("👤");
        emoji.setStyle(
            "-fx-font-size: 40px;");

        Label title = new Label(
            isFirstUser
                ? "Welcome to Facere!"
                : "Add a New User");
        title.setStyle(
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #1A1A2E;" +
            "-fx-font-family: 'Segoe UI';");

        Label subtitle = new Label(
            "Create your profile to get started");
        subtitle.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #6B7280;" +
            "-fx-font-family: 'Segoe UI';");

        header.getChildren().addAll(
            emoji, title, subtitle);

        // ── Avatar preview ───────────────
        StackPane avatarPreview =
            new StackPane();
        avatarPreview.setAlignment(Pos.CENTER);
        avatarPreview.setPadding(
            new Insets(8, 0, 16, 0));

        Circle avatarCircle = new Circle(40);
        avatarCircle.setFill(
            Color.web(selectedColor));

        Label initialsLabel = new Label("?");
        initialsLabel.setStyle(
            "-fx-font-size: 28px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-font-family: 'Segoe UI';");

        avatarPreview.getChildren().addAll(
            avatarCircle, initialsLabel);

        // ── Form fields ──────────────────
        VBox form = new VBox(16);
        form.setPadding(
            new Insets(0, 40, 0, 40));

        // Full name
        VBox nameBox = new VBox(6);
        Label nameLabel = new Label(
            "FULL NAME");
        nameLabel.setStyle(
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #9CA3AF;" +
            "-fx-font-family: 'Segoe UI';");
        TextField nameField = new TextField();
        nameField.setPromptText(
            "e.g. John");
        nameField.setStyle(inputStyle());

        nameBox.getChildren().addAll(
            nameLabel, nameField);

        // Username
        VBox userBox = new VBox(6);
        Label userLabel = new Label(
            "USERNAME");
        userLabel.setStyle(
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #9CA3AF;" +
            "-fx-font-family: 'Segoe UI';");
        TextField userField = new TextField();
        userField.setPromptText(
            "e.g. john");
        userField.setStyle(inputStyle());

        userBox.getChildren().addAll(
            userLabel, userField);

        // ── Colour picker ────────────────
        VBox colorBox = new VBox(8);
        Label colorLabel = new Label(
            "AVATAR COLOUR");
        colorLabel.setStyle(
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #9CA3AF;" +
            "-fx-font-family: 'Segoe UI';");

        HBox colorRow = new HBox(10);
        colorRow.setAlignment(
            Pos.CENTER_LEFT);

        for (String hex : COLORS) {
            Circle dot = new Circle(16);
            dot.setFill(Color.web(hex));
            dot.setStroke(Color.TRANSPARENT);
            dot.setStrokeWidth(3);
            dot.setCursor(
                javafx.scene.Cursor.HAND);

            if (hex.equals(selectedColor)) {
                dot.setStroke(
                    Color.web("#1A1A2E"));
            }

            final String color = hex;
            dot.setOnMouseClicked(e -> {
                selectedColor = color;
                avatarCircle.setFill(
                    Color.web(color));
                // Reset all borders
                colorRow.getChildren()
                    .forEach(n -> {
                    if (n instanceof Circle c) {
                        c.setStroke(
                            Color.TRANSPARENT);
                    }
                });
                dot.setStroke(
                    Color.web("#1A1A2E"));
            });

            colorRow.getChildren().add(dot);
        }

        colorBox.getChildren().addAll(
            colorLabel, colorRow);

        form.getChildren().addAll(
            nameBox, userBox, colorBox);

        // ── Update initials live ─────────
        nameField.textProperty()
            .addListener((obs, old, val) -> {
            if (val == null || val.isBlank()) {
                initialsLabel.setText("?");
            } else {
                String[] parts =
                    val.trim().split("\\s+");
                if (parts.length == 1) {
                    initialsLabel.setText(
                        parts[0].substring(0, 1)
                            .toUpperCase());
                } else {
                    initialsLabel.setText(
                        (parts[0]
                            .substring(0, 1) +
                        parts[parts.length - 1]
                            .substring(0, 1))
                        .toUpperCase());
                }
            }
        });

        // ── Error label ──────────────────
        Label errorLabel = new Label("");
        errorLabel.setStyle(
            "-fx-text-fill: #EF4444;" +
            "-fx-font-size: 12px;" +
            "-fx-font-family: 'Segoe UI';");
        errorLabel.setVisible(false);
        errorLabel.setPadding(
            new Insets(0, 40, 0, 40));

        // ── Buttons ──────────────────────
        HBox btnRow = new HBox(12);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(
            new Insets(24, 40, 32, 40));

        Button cancelBtn = new Button(
            "Cancel");
        cancelBtn.setStyle(
            "-fx-background-color: " +
            "transparent;" +
            "-fx-text-fill: #9CA3AF;" +
            "-fx-font-size: 13px;" +
            "-fx-cursor: hand;");
        cancelBtn.setOnAction(e ->
            stage.close());

        if (isFirstUser) {
            cancelBtn.setVisible(false);
            cancelBtn.setManaged(false);
        }

        Button createBtn = new Button(
            "✨  Create Profile");
        createBtn.setStyle(
            "-fx-background-color: #6C63FF;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 10px;" +
            "-fx-padding: 10px 28px;" +
            "-fx-cursor: hand;");

        createBtn.setOnAction(e -> {
            String name =
                nameField.getText().trim();
            String uname =
                userField.getText().trim()
                    .toLowerCase();

            // Validate
            if (name.isEmpty()) {
                errorLabel.setText(
                    "Please enter your name");
                errorLabel.setVisible(true);
                return;
            }
            if (uname.isEmpty()) {
                errorLabel.setText(
                    "Please enter a username");
                errorLabel.setVisible(true);
                return;
            }
            if (uname.contains(" ")) {
                errorLabel.setText(
                    "Username cannot " +
                    "contain spaces");
                errorLabel.setVisible(true);
                return;
            }
            if (userDAO.getUserCount()
                    >= UserDAO.MAX_USERS) {
                errorLabel.setText(
                    "Maximum 3 users allowed");
                errorLabel.setVisible(true);
                return;
            }
            if (userDAO.usernameExists(uname)) {
                errorLabel.setText(
                    "Username already taken");
                errorLabel.setVisible(true);
                return;
            }

            // Create user
            User user = new User();
            user.setFullName(name);
            user.setUsername(uname);
            user.setAvatarColor(selectedColor);
            user.setAccentColor(selectedColor);
            user.setSortOrder(
                userDAO.getUserCount());

            int id = userDAO.save(user);
            if (id > 0) {
                user.setId(id);
                UserSession.setCurrentUser(user);
                wasSaved = true;
                stage.close();
            } else {
                errorLabel.setText(
                    "Failed to create profile");
                errorLabel.setVisible(true);
            }
        });

        btnRow.getChildren().addAll(
            cancelBtn, createBtn);

        // Prevent closing without creating
        // on first launch
        if (isFirstUser) {
            stage.setOnCloseRequest(
                e -> e.consume());
        }

        root.getChildren().addAll(
            header, avatarPreview,
            form, errorLabel, btnRow);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);

        // Center on owner
        stage.setOnShown(e -> {
            stage.setX(
                owner.getX() +
                owner.getWidth() / 2 -
                stage.getWidth() / 2);
            stage.setY(
                owner.getY() +
                owner.getHeight() / 2 -
                stage.getHeight() / 2);
        });

        stage.showAndWait();
        return wasSaved;
    }

    private String inputStyle() {
        return
            "-fx-background-color: #F9FAFB;" +
            "-fx-border-color: #E5E7EB;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 10px 14px;" +
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #111827;";
    }
}