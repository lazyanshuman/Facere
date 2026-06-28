package com.habitflow.controller;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.habitflow.dao.UserDAO;
import com.habitflow.model.User;
import com.habitflow.service.UserSession;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.List;

/**
 * UserPickerController
 *
 * Shows a screen with avatar circles for
 * each user. Click one to switch to them.
 * If the user has a PIN, prompts for it.
 *
 * Also shows "+ Add User" if < 3 users.
 */
public class UserPickerController {

    private final UserDAO userDAO =
        new UserDAO();

    private Stage pickerStage;
    private boolean switched = false;

    /**
     * Shows the user picker.
     * Returns true if user was switched.
     */
    public boolean show(Stage owner) {
        pickerStage = new Stage();
        pickerStage.initModality(
            Modality.APPLICATION_MODAL);
        pickerStage.initOwner(owner);
        pickerStage.initStyle(
            StageStyle.UNDECORATED);
        pickerStage.setTitle("Switch User");

        com.habitflow.util.AppIcon
            .set(pickerStage);

        pickerStage.setWidth(
            owner.getWidth());
        pickerStage.setHeight(
            owner.getHeight());
        pickerStage.setX(owner.getX());
        pickerStage.setY(owner.getY());

        Scene scene = new Scene(
            buildScreen(owner));
        pickerStage.setScene(scene);
        pickerStage.showAndWait();
        return switched;
    }

    private BorderPane buildScreen(
            Stage owner) {

        BorderPane root = new BorderPane();
        root.setStyle(
            "-fx-background-color: #0D0D1A;");

        // ── Center content ───────────────
        VBox center = new VBox(40);
        center.setAlignment(Pos.CENTER);
        center.setPadding(
            new Insets(60));

        // Title
        Label title = new Label(
            "Who's using Facere?");
        title.setStyle(
            "-fx-font-size: 28px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #F0F0FF;" +
            "-fx-font-family: 'Segoe UI';");

        // Avatar row
        HBox avatarRow = new HBox(32);
        avatarRow.setAlignment(Pos.CENTER);

        List<User> users =
            userDAO.getAllUsers();

        for (User user : users) {
            VBox avatar = buildAvatar(
                user, owner);
            avatarRow.getChildren().add(avatar);
        }

        // Add User button (if < 3)
        if (users.size() < UserDAO.MAX_USERS) {
            VBox addBtn = buildAddButton(owner);
            avatarRow.getChildren().add(addBtn);
        }

        // Cancel button
        Button cancelBtn = new Button(
            "Cancel");
        cancelBtn.setStyle(
            "-fx-background-color: " +
            "transparent;" +
            "-fx-text-fill: #6666AA;" +
            "-fx-font-size: 13px;" +
            "-fx-cursor: hand;" +
            "-fx-font-family: 'Segoe UI';");
        cancelBtn.setOnAction(e ->
            pickerStage.close());

        center.getChildren().addAll(
            title, avatarRow, cancelBtn);

        root.setCenter(center);
        return root;
    }

    /**
     * Builds one user avatar circle.
     */
    private VBox buildAvatar(
            User user, Stage owner) {

        // Coloured circle with initials
        StackPane circlePane =
            new StackPane();

        Circle bg = new Circle(48);
        bg.setFill(Color.web(
            user.getAvatarColor()));

        Label initials = new Label(
            user.getInitials());
        initials.setStyle(
            "-fx-font-size: 28px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-font-family: 'Segoe UI';");

        circlePane.getChildren().addAll(
            bg, initials);

        // Highlight if this is the
        // current user
        if (UserSession.isLoggedIn() &&
            UserSession.getCurrentUser()
                .getId() == user.getId()) {
            bg.setStroke(
                Color.web("#7C73FF"));
            bg.setStrokeWidth(3);
        }

        // Name label
        Label nameLabel = new Label(
            user.getFullName());
        nameLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #E0E0F0;" +
            "-fx-font-family: 'Segoe UI';");

        // Username label
        Label userLabel = new Label(
            "@" + user.getUsername());
        userLabel.setStyle(
            "-fx-font-size: 11px;" +
            "-fx-text-fill: #6666AA;" +
            "-fx-font-family: 'Segoe UI';");

        VBox box = new VBox(10,
            circlePane, nameLabel, userLabel);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(16));
        box.setStyle(
            "-fx-background-color: " +
            "transparent;" +
            "-fx-background-radius: 16px;" +
            "-fx-cursor: hand;");

        // Hover effect
        box.setOnMouseEntered(e ->
            box.setStyle(
                "-fx-background-color: " +
                "#1A1A2E;" +
                "-fx-background-radius: 16px;" +
                "-fx-cursor: hand;"));
        box.setOnMouseExited(e ->
            box.setStyle(
                "-fx-background-color: " +
                "transparent;" +
                "-fx-background-radius: 16px;" +
                "-fx-cursor: hand;"));

        // Click to switch
        box.setOnMouseClicked(e -> {
            if (user.hasPin()) {
                // Show PIN prompt
                showPinPrompt(user);
            } else {
                // No PIN — switch directly
                UserSession.setCurrentUser(user);
                switched = true;
                pickerStage.close();
            }
        });

        return box;
    }

    /**
     * Shows an inline PIN prompt for a user.
     */
    private void showPinPrompt(User user) {
        Stage pinStage = new Stage();
        pinStage.initModality(
            Modality.APPLICATION_MODAL);
        pinStage.initOwner(pickerStage);
        pinStage.initStyle(
            StageStyle.UNDECORATED);

        VBox root = new VBox(24);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40, 56, 40, 56));
        root.setStyle(
            "-fx-background-color: #161625;" +
            "-fx-background-radius: 20px;");

        // Avatar
        StackPane avatar = new StackPane();
        Circle bg = new Circle(36);
        bg.setFill(Color.web(
            user.getAvatarColor()));
        Label init = new Label(
            user.getInitials());
        init.setStyle(
            "-fx-font-size: 22px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;");
        avatar.getChildren().addAll(bg, init);

        Label name = new Label(
            user.getFullName());
        name.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #F0F0FF;" +
            "-fx-font-family: 'Segoe UI';");

        Label sub = new Label(
            "Enter your 6-digit PIN");
        sub.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-text-fill: #6666AA;" +
            "-fx-font-family: 'Segoe UI';");

        // PIN dots
        HBox dotsRow = new HBox(14);
        dotsRow.setAlignment(Pos.CENTER);
        Circle[] dots = new Circle[6];
        for (int i = 0; i < 6; i++) {
            dots[i] = new Circle(10);
            dots[i].setFill(Color.TRANSPARENT);
            dots[i].setStroke(
                Color.web("#3A3A60"));
            dots[i].setStrokeWidth(2);
            dotsRow.getChildren().add(dots[i]);
        }

        Label status = new Label(" ");
        status.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-text-fill: #EF4444;");
        status.setMinHeight(18);

        // Hidden input
        TextField hidden = new TextField();
        hidden.setMaxWidth(0);
        hidden.setMaxHeight(0);
        hidden.setOpacity(0);

        StringBuilder pinBuilder =
            new StringBuilder();

        Runnable verify = () -> {
            String pin =
                pinBuilder.toString();
            BCrypt.Result result =
                BCrypt.verifyer()
                    .verify(pin.toCharArray(),
                        user.getPinHash());

            if (result.verified) {
                for (Circle d : dots) {
                    d.setFill(
                        Color.web("#10B981"));
                    d.setStroke(
                        Color.web("#10B981"));
                }
                PauseTransition pause =
                    new PauseTransition(
                        Duration.millis(200));
                pause.setOnFinished(ev -> {
                    UserSession.setCurrentUser(
                        user);
                    switched = true;
                    pinStage.close();
                    pickerStage.close();
                });
                pause.play();
            } else {
                for (Circle d : dots) {
                    d.setFill(
                        Color.web("#EF4444"));
                    d.setStroke(
                        Color.web("#EF4444"));
                }
                TranslateTransition shake =
                    new TranslateTransition(
                        Duration.millis(55),
                        dotsRow);
                shake.setByX(14);
                shake.setCycleCount(6);
                shake.setAutoReverse(true);
                shake.setOnFinished(ev ->
                    dotsRow.setTranslateX(0));
                shake.play();

                PauseTransition reset =
                    new PauseTransition(
                        Duration.millis(500));
                reset.setOnFinished(ev -> {
                    pinBuilder.setLength(0);
                    for (int i = 0; i < 6; i++) {
                        dots[i].setFill(
                            Color.TRANSPARENT);
                        dots[i].setStroke(
                            Color.web("#3A3A60"));
                    }
                    status.setText("Wrong PIN");
                });
                reset.play();
            }
        };

        hidden.setOnKeyPressed(e -> {
            if (e.getCode() ==
                    KeyCode.BACK_SPACE) {
                if (pinBuilder.length() > 0) {
                    pinBuilder.deleteCharAt(
                        pinBuilder.length() - 1);
                    updateDots(dots,
                        pinBuilder.length());
                }
                return;
            }
            if (e.getCode() == KeyCode.ESCAPE) {
                pinStage.close();
                return;
            }
            String text = e.getText();
            if (text != null &&
                text.matches("[0-9]") &&
                pinBuilder.length() < 6) {
                pinBuilder.append(text);
                updateDots(dots,
                    pinBuilder.length());
                if (pinBuilder.length() == 6) {
                    Platform.runLater(verify);
                }
            }
        });

        Button cancelBtn = new Button(
            "Cancel");
        cancelBtn.setStyle(
            "-fx-background-color: " +
            "transparent;" +
            "-fx-text-fill: #6666AA;" +
            "-fx-font-size: 12px;" +
            "-fx-cursor: hand;");
        cancelBtn.setOnAction(e ->
            pinStage.close());

        root.getChildren().addAll(
            avatar, name, sub,
            dotsRow, status,
            hidden, cancelBtn);

        Scene scene = new Scene(root);
        scene.setFill(Color.web("#0D0D1ACC"));

        com.habitflow.util.AppIcon
            .set(pinStage); 
            
        pinStage.setScene(scene);
        pinStage.sizeToScene();

        pinStage.setOnShown(e -> {
            pinStage.setX(
                pickerStage.getX() +
                pickerStage.getWidth() / 2 -
                pinStage.getWidth() / 2);
            pinStage.setY(
                pickerStage.getY() +
                pickerStage.getHeight() / 2 -
                pinStage.getHeight() / 2);
            hidden.requestFocus();
        });

        root.setOnMouseClicked(e ->
            hidden.requestFocus());

        pinStage.showAndWait();
    }

    /**
     * Builds the "+ Add User" circle button.
     */
    private VBox buildAddButton(Stage owner) {
        StackPane circlePane =
            new StackPane();

        Circle bg = new Circle(48);
        bg.setFill(Color.TRANSPARENT);
        bg.setStroke(Color.web("#3A3A60"));
        bg.setStrokeWidth(2);
        bg.getStrokeDashArray()
            .addAll(8.0, 6.0);

        Label plus = new Label("+");
        plus.setStyle(
            "-fx-font-size: 36px;" +
            "-fx-text-fill: #6666AA;" +
            "-fx-font-family: 'Segoe UI';");

        circlePane.getChildren().addAll(
            bg, plus);

        Label label = new Label("Add User");
        label.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #6666AA;" +
            "-fx-font-family: 'Segoe UI';");

        VBox box = new VBox(10,
            circlePane, label);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(16));
        box.setStyle(
            "-fx-cursor: hand;");

        box.setOnMouseClicked(e -> {
            ProfileSetupController setup =
                new ProfileSetupController();
            boolean created =
                setup.show(owner, false);
            if (created) {
                switched = true;
                pickerStage.close();
            }
        });

        return box;
    }

    private void updateDots(
            Circle[] dots, int count) {
        for (int i = 0; i < dots.length; i++) {
            if (i < count) {
                dots[i].setFill(
                    Color.web("#7C73FF"));
                dots[i].setStroke(
                    Color.web("#7C73FF"));
            } else {
                dots[i].setFill(
                    Color.TRANSPARENT);
                dots[i].setStroke(
                    Color.web("#3A3A60"));
            }
        }
    }
}
