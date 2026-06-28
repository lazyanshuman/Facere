package com.habitflow.controller;

import com.habitflow.service.SecurityManager;
import com.habitflow.service.ThemeManager;
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

/**
 * LockScreenController — Full Desktop Style
 *
 * Uses the entire screen.
 * User types PIN on keyboard.
 * Dots fill as digits are typed.
 * Auto-verifies on 6th digit.
 * Green = correct, Red = wrong.
 * No number pad, no buttons.
 */
public class LockScreenController {

    private final SecurityManager security =
        SecurityManager.getInstance();
    private final ThemeManager themeManager =
        ThemeManager.getInstance();

    private Stage lockStage;
    private int failedAttempts = 0;
    private static final int MAX_ATTEMPTS = 5;
    private static final int PIN_LENGTH = 6;

    // Theme colours
    private String bgColor;
    private String textPrimary;
    private String textSecondary;
    private String accentColor;
    
    private String dotFilled;
    private String borderColor;
    private String overlayLeft;
    private String overlayRight;

    public boolean show(Stage ownerStage) {
        setupThemeColors();

        lockStage = new Stage();
        lockStage.initModality(
            Modality.APPLICATION_MODAL);
        lockStage.initOwner(ownerStage);
        lockStage.initStyle(
            StageStyle.UNDECORATED);
        lockStage.setTitle("Facere");

        com.habitflow.util.AppIcon
            .set(lockStage);
            
        lockStage.setWidth(
            ownerStage.getWidth());
        lockStage.setHeight(
            ownerStage.getHeight());
        lockStage.setX(ownerStage.getX());
        lockStage.setY(ownerStage.getY());

        boolean[] unlocked = {false};
        Scene scene = new Scene(
            buildScreen(unlocked));
        lockStage.setScene(scene);
        lockStage.setOnCloseRequest(
            e -> e.consume());
        lockStage.showAndWait();
        return unlocked[0];
    }

    private void setupThemeColors() {
        boolean isDark =
            themeManager.getCurrentMode() ==
                ThemeManager.ThemeMode.DARK ||
            (themeManager.getCurrentMode() ==
                ThemeManager.ThemeMode
                    .AUTO_TIME && isNightTime());

        if (isDark) {
            bgColor       = "#0D0D1A";
            textPrimary   = "#F0F0FF";
            textSecondary = "#6666AA";
            accentColor   = "#7C73FF";
        
            dotFilled     = "#7C73FF";
            borderColor   = "#3A3A60";
            overlayLeft   = "#0D0D1A";
            overlayRight  = "#130D2A";
        } else {
            bgColor       = "#FAFAFA";
            textPrimary   = "#1A1A2E";
            textSecondary = "#9090B8";
            accentColor   = "#6C63FF";
    
            dotFilled     = "#6C63FF";
            borderColor   = "#D0D0E8";
            overlayLeft   = "#F5F5FF";
            overlayRight  = "#EEEEFF";
        }
    }

    private boolean isNightTime() {
        int h = java.time.LocalTime
            .now().getHour();
        return h < 6 || h >= 19;
    }

    private BorderPane buildScreen(
            boolean[] unlocked) {

        BorderPane root = new BorderPane();
        root.setStyle(
            "-fx-background-color: " +
            bgColor + ";");

        // ── LEFT PANEL — Branding ────────────
        VBox leftPanel = new VBox(20);
        leftPanel.setAlignment(
            Pos.CENTER_LEFT);
        leftPanel.setPadding(
            new Insets(0, 0, 0, 80));
        leftPanel.setStyle(
            "-fx-background-color: " +
            overlayLeft + ";");
        leftPanel.setPrefWidth(460);

        Label appName = new Label(
            "Facere");
        appName.setStyle(
            "-fx-font-size: 42px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " +
            accentColor + ";" +
            "-fx-font-family: 'Segoe UI';");

        Label tagline = new Label(
            "Your daily productivity\n" +
            "companion.");
        tagline.setStyle(
            "-fx-font-size: 18px;" +
            "-fx-text-fill: " +
            textSecondary + ";" +
            "-fx-font-family: 'Segoe UI';" +
            "-fx-line-spacing: 4px;");

        // Decorative accent line
        Region accentLine = new Region();
        accentLine.setPrefHeight(4);
        accentLine.setPrefWidth(60);
        accentLine.setStyle(
            "-fx-background-color: " +
            accentColor + ";" +
            "-fx-background-radius: 2px;");

        // Time display
        Label timeLabel = new Label(
            getCurrentTime());
        timeLabel.setStyle(
            "-fx-font-size: 56px;" +
            "-fx-font-weight: 200;" +
            "-fx-text-fill: " +
            textPrimary + ";" +
            "-fx-font-family: 'Segoe UI';");

        Label dateLabel = new Label(
            getCurrentDate());
        dateLabel.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-text-fill: " +
            textSecondary + ";" +
            "-fx-font-family: 'Segoe UI';");

        // Live clock update every second
        Timeline clock = new Timeline(
            new KeyFrame(
                Duration.seconds(1),
                e -> {
                    timeLabel.setText(
                        getCurrentTime());
                    dateLabel.setText(
                        getCurrentDate());
                }));
        clock.setCycleCount(
            Timeline.INDEFINITE);
        clock.play();

        leftPanel.getChildren().addAll(
            timeLabel,
            dateLabel,
            new Region() {{
                setPrefHeight(40);
            }},
            accentLine,
            new Region() {{
                setPrefHeight(8);
            }},
            appName,
            tagline);

        // ── RIGHT PANEL — PIN Entry ──────────
        VBox rightPanel = new VBox(40);
        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.setPadding(
            new Insets(0, 80, 0, 60));
        rightPanel.setStyle(
            "-fx-background-color: " +
            overlayRight + ";");

        // Lock icon
        Label lockIcon = new Label("🔐");
        lockIcon.setStyle(
            "-fx-font-size: 36px;");

        // Title
        Label pinTitle = new Label(
            "Welcome back");
        pinTitle.setStyle(
            "-fx-font-size: 26px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " +
            textPrimary + ";" +
            "-fx-font-family: 'Segoe UI';");

        Label pinSub = new Label(
            "Type your 6-digit PIN");
        pinSub.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-text-fill: " +
            textSecondary + ";" +
            "-fx-font-family: 'Segoe UI';");

        VBox titleBox = new VBox(8,
            lockIcon, pinTitle, pinSub);
        titleBox.setAlignment(Pos.CENTER);

        // ── PIN dots ─────────────────────────
        HBox dotsRow = new HBox(20);
        dotsRow.setAlignment(Pos.CENTER);

        Circle[] dots =
            new Circle[PIN_LENGTH];
        for (int i = 0; i < PIN_LENGTH; i++) {
            dots[i] = new Circle(14);
            dots[i].setFill(Color.TRANSPARENT);
            dots[i].setStroke(
                Color.web(borderColor));
            dots[i].setStrokeWidth(2);
            dotsRow.getChildren().add(dots[i]);
        }

        // ── Status label ─────────────────────
        Label statusLabel = new Label(" ");
        statusLabel.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #EF4444;" +
            "-fx-font-family: 'Segoe UI';");
        statusLabel.setMinHeight(20);

        // ── Keyboard hint ────────────────────
        Label hint = new Label(
            "Use your keyboard to enter PIN");
        hint.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-text-fill: " +
            textSecondary + ";" +
            "-fx-font-family: 'Segoe UI';");

        // ── Invisible input field ────────────
        // Captures keyboard input
        TextField hiddenInput =
            new TextField();
        hiddenInput.setMaxWidth(0);
        hiddenInput.setMaxHeight(0);
        hiddenInput.setOpacity(0);

        StringBuilder pinBuilder =
            new StringBuilder();

        // ── Verify logic ─────────────────────
        Runnable verify = () -> {
            String pin =
                pinBuilder.toString();

            if (security.verifyPin(pin)) {
                // All dots green
                for (Circle dot : dots) {
                    dot.setFill(
                        Color.web("#10B981"));
                    dot.setStroke(
                        Color.web("#10B981"));
                }
                clock.stop();
                PauseTransition pause =
                    new PauseTransition(
                    Duration.millis(300));
                pause.setOnFinished(
                    ev -> {
                        unlocked[0] = true;
                        lockStage.close();
                    });
                pause.play();

            } else {
                failedAttempts++;

                // All dots red + shake
                for (Circle dot : dots) {
                    dot.setFill(
                        Color.web("#EF4444"));
                    dot.setStroke(
                        Color.web("#EF4444"));
                }

                shakeRow(dotsRow);

                PauseTransition reset =
                    new PauseTransition(
                    Duration.millis(500));
                reset.setOnFinished(ev -> {
                    pinBuilder.setLength(0);
                    hiddenInput.clear();
                    updateDots(dots, 0);

                    if (failedAttempts >=
                            MAX_ATTEMPTS) {
                        statusLabel.setText(
                            "Too many failed" +
                            " attempts." +
                            " Wait 30s.");
                        hiddenInput
                            .setDisable(true);

                        new Thread(() -> {
                            try {
                                Thread.sleep(
                                    30000);
                            } catch (
                                InterruptedException
                                    ex) {
                                Thread
                                .currentThread()
                                .interrupt();
                            }
                            Platform.runLater(
                                () -> {
                                failedAttempts
                                    = 0;
                                hiddenInput
                                    .setDisable(
                                    false);
                                statusLabel
                                    .setText(
                                    " ");
                                hiddenInput
                                    .requestFocus();
                            });
                        }).start();
                    } else {
                        int left =
                            MAX_ATTEMPTS -
                            failedAttempts;
                        statusLabel.setText(
                            "Wrong PIN — " +
                            left + " attempt" +
                            (left == 1 ?
                                "" : "s") +
                            " remaining");
                    }
                });
                reset.play();
            }
        };

        // ── Keyboard input handler ───────────
        hiddenInput.setOnKeyPressed(e -> {
            if (hiddenInput.isDisabled())
                return;

            KeyCode code = e.getCode();

            // Backspace
            if (code == KeyCode.BACK_SPACE) {
                if (pinBuilder.length() > 0) {
                    pinBuilder.deleteCharAt(
                        pinBuilder.length() - 1);
                    updateDots(dots,
                        pinBuilder.length());
                    statusLabel.setText(" ");
                }
                return;
            }

            // Digits only
            String text = e.getText();
            if (text != null &&
                text.matches("[0-9]") &&
                pinBuilder.length() <
                    PIN_LENGTH) {

                pinBuilder.append(text);
                updateDots(dots,
                    pinBuilder.length());

                // Auto verify on 6th digit
                if (pinBuilder.length()
                        == PIN_LENGTH) {
                    Platform.runLater(verify);
                }
            }
        });

        // ── Assemble right panel ─────────────
        rightPanel.getChildren().addAll(
            titleBox,
            dotsRow,
            statusLabel,
            hint,
            hiddenInput);

        // ── Divider line ─────────────────────
        Region divLine = new Region();
        divLine.setPrefWidth(1);
        divLine.setStyle(
            "-fx-background-color: " +
            borderColor + ";");

        // ── Root layout ──────────────────────
        HBox content = new HBox(
            leftPanel, divLine, rightPanel);
        HBox.setHgrow(leftPanel,
            Priority.ALWAYS);
        HBox.setHgrow(rightPanel,
            Priority.ALWAYS);

        root.setCenter(content);

        // Auto focus hidden input
        Platform.runLater(
            hiddenInput::requestFocus);

        // Re-focus on click anywhere
        root.setOnMouseClicked(
            e -> hiddenInput.requestFocus());

        return root;
    }

    private void updateDots(
            Circle[] dots, int count) {
        for (int i = 0; i < dots.length;
                i++) {
            if (i < count) {
                dots[i].setFill(
                    Color.web(dotFilled));
                dots[i].setStroke(
                    Color.web(dotFilled));
                ScaleTransition st =
                    new ScaleTransition(
                    Duration.millis(80),
                    dots[i]);
                st.setFromX(0.7);
                st.setFromY(0.7);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();
            } else {
                dots[i].setFill(
                    Color.TRANSPARENT);
                dots[i].setStroke(
                    Color.web(borderColor));
            }
        }
    }

    private void shakeRow(HBox row) {
        TranslateTransition shake =
            new TranslateTransition(
            Duration.millis(55), row);
        shake.setFromX(0);
        shake.setByX(14);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setOnFinished(
            e -> row.setTranslateX(0));
        shake.play();
    }

    private String getCurrentTime() {
        return java.time.LocalTime.now()
            .format(java.time.format
                .DateTimeFormatter
                .ofPattern("HH:mm"));
    }

    private String getCurrentDate() {
        return java.time.LocalDate.now()
            .format(java.time.format
                .DateTimeFormatter
                .ofPattern(
                    "EEEE, MMMM d"));
    }
}