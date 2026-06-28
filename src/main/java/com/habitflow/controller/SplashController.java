package com.habitflow.controller;

import com.habitflow.util.AppIcon;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * SplashController
 *
 * Full-screen greeting. No user name.
 * Time-based greeting + daily rotating
 * quote from 90 thoughts.
 * Fades out automatically after 3 seconds.
 */
public class SplashController {

    private static final String[] QUOTES = {
        "The secret of getting ahead is getting started.",
        "Small daily improvements lead to stunning results.",
        "Your habits shape your future.",
        "Progress, not perfection.",
        "Today is a new opportunity to grow.",
        "Discipline is choosing what you want most over what you want now.",
        "You don't have to be great to start, but you have to start to be great.",
        "Success is the sum of small efforts repeated daily.",
        "Be stronger than your excuses.",
        "Every day is a chance to be better than yesterday.",
        "The only bad workout is the one that didn't happen.",
        "Believe you can and you're halfway there.",
        "Great things never come from comfort zones.",
        "Focus on the step in front of you, not the whole staircase.",
        "It's not about motivation, it's about discipline.",
        "What you do every day matters more than what you do once in a while.",
        "Start where you are. Use what you have. Do what you can.",
        "A year from now you'll wish you had started today.",
        "Don't count the days, make the days count.",
        "The best time to start was yesterday. The next best time is now.",
        "Stay patient and trust the journey.",
        "Little by little, one travels far.",
        "You are what you repeatedly do.",
        "Make it happen. Shock everyone.",
        "Winners are not people who never fail, but people who never quit.",
        "Dream it. Plan it. Do it.",
        "Work hard in silence. Let success be your noise.",
        "Push yourself because no one else will.",
        "Wake up with determination. Go to bed with satisfaction.",
        "Your future self will thank you.",
        "Done is better than perfect.",
        "Consistency beats intensity.",
        "Fall seven times, stand up eight.",
        "The harder you work, the luckier you get.",
        "Action is the foundational key to all success.",
        "You miss 100% of the shots you don't take.",
        "If it doesn't challenge you, it doesn't change you.",
        "Every master was once a disaster.",
        "The pain you feel today is the strength you feel tomorrow.",
        "Be the energy you want to attract.",
        "Your limitation is only your imagination.",
        "Sometimes later becomes never. Do it now.",
        "The way to get started is to quit talking and begin doing.",
        "Don't wish it were easier. Wish you were better.",
        "Success doesn't come from what you do occasionally. It comes from what you do consistently.",
        "Doubt kills more dreams than failure ever will.",
        "Hustle until your haters ask if you're hiring.",
        "Go the extra mile. It's never crowded there.",
        "You don't need a new plan. You need a new commitment.",
        "Strive for progress, not perfection.",
        "Do something today that your future self will thank you for.",
        "The difference between who you are and who you want to be is what you do.",
        "Don't stop when you're tired. Stop when you're done.",
        "A journey of a thousand miles begins with a single step.",
        "Be so good they can't ignore you.",
        "The only way to do great work is to love what you do.",
        "Your only limit is you.",
        "Excuses don't build empires.",
        "It always seems impossible until it's done.",
        "Create the life you can't wait to wake up to.",
        "Results happen over time, not overnight.",
        "If you want something you've never had, do something you've never done.",
        "Stop waiting for the perfect moment. Take the moment and make it perfect.",
        "The expert in anything was once a beginner.",
        "What feels like struggle today will be your strength tomorrow.",
        "Stay foolish, stay hungry.",
        "You were born to win, but to be a winner you must plan to win and prepare to win.",
        "If plan A doesn't work, the alphabet has 25 more letters.",
        "Success is not for the lazy.",
        "Your time is limited. Don't waste it living someone else's life.",
        "Hard work beats talent when talent doesn't work hard.",
        "The struggle you're in today is developing the strength you need for tomorrow.",
        "Set goals that make you want to jump out of bed in the morning.",
        "Every accomplishment starts with the decision to try.",
        "Stay focused and never give up.",
        "Opportunities don't happen. You create them.",
        "Do what you can, with what you have, where you are.",
        "Nothing will work unless you do.",
        "A little progress each day adds up to big results.",
        "You didn't come this far to only come this far.",
        "Keep going. Everything you need will come to you at the perfect time.",
        "One percent better every day.",
        "Champions keep playing until they get it right.",
        "Invest in yourself. It pays the best interest.",
        "Energy and persistence conquer all things.",
        "Don't let yesterday take up too much of today.",
        "You're braver than you believe, stronger than you seem, and smarter than you think.",
        "Life begins at the end of your comfort zone.",
        "The comeback is always stronger than the setback.",
        "Good things come to those who hustle."
    };

    private Stage splashStage;

    /**
     * Shows the full-screen splash greeting.
     * No user name, no emoji.
     */
    public void showSplash(Stage owner) {

        String greeting = getGreeting();

        int dayOfYear = LocalDate.now()
            .getDayOfYear();
        String quote = QUOTES[
            dayOfYear % QUOTES.length];

        splashStage = new Stage();
        splashStage.initModality(
            Modality.APPLICATION_MODAL);
        splashStage.initOwner(owner);
        splashStage.initStyle(
            StageStyle.UNDECORATED);

        AppIcon.set(splashStage);

        splashStage.setWidth(
            owner.getWidth());
        splashStage.setHeight(
            owner.getHeight());
        splashStage.setX(owner.getX());
        splashStage.setY(owner.getY());

        // ── Root ─────────────────────────
        VBox root = new VBox(0);
        root.setAlignment(Pos.CENTER);
        root.setStyle(
            "-fx-background-color: #0D0D1A;");

        // ── Content area ─────────────────
        VBox content = new VBox(28);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(520);

        // App icon circle
        StackPane iconPane = new StackPane();
        Circle iconBg = new Circle(44);
        iconBg.setFill(
            Color.web("#6C63FF"));
        Label iconLabel = new Label("F");
        iconLabel.setStyle(
            "-fx-font-size: 36px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-font-family: 'Segoe UI';");
        iconPane.getChildren().addAll(
            iconBg, iconLabel);

        // Greeting text — no name, no emoji
        Label greetLabel = new Label(
            greeting + "!");
        greetLabel.setStyle(
            "-fx-font-size: 32px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #F0F0FF;" +
            "-fx-font-family: 'Segoe UI';");
        greetLabel.setWrapText(true);

        // Accent line
        Region accentLine = new Region();
        accentLine.setPrefHeight(3);
        accentLine.setPrefWidth(80);
        accentLine.setMaxWidth(80);
        accentLine.setStyle(
            "-fx-background-color: #6C63FF;" +
            "-fx-background-radius: 2px;");

        // Quote
        Label quoteLabel = new Label(
            "\"" + quote + "\"");
        quoteLabel.setStyle(
            "-fx-font-size: 15px;" +
            "-fx-text-fill: #8888AA;" +
            "-fx-font-style: italic;" +
            "-fx-font-family: 'Segoe UI';" +
            "-fx-text-alignment: center;");
        quoteLabel.setWrapText(true);
        quoteLabel.setMaxWidth(440);

        // Date
        Label dateLabel = new Label(
            LocalDate.now().format(
                java.time.format
                    .DateTimeFormatter
                    .ofPattern(
                        "EEEE, MMMM d, yyyy")));
        dateLabel.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #5555AA;" +
            "-fx-font-family: 'Segoe UI';");

        // Branding
        Label brand = new Label("Facere");
        brand.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #6C63FF;" +
            "-fx-font-family: 'Segoe UI';");

        content.getChildren().addAll(
            iconPane,
            greetLabel,
            accentLine,
            quoteLabel,
            dateLabel);

        VBox bottom = new VBox(brand);
        bottom.setAlignment(Pos.CENTER);
        bottom.setPadding(
            new Insets(0, 0, 40, 0));

        Region topSpacer = new Region();
        VBox.setVgrow(topSpacer,
            Priority.ALWAYS);
        Region bottomSpacer = new Region();
        VBox.setVgrow(bottomSpacer,
            Priority.ALWAYS);

        root.getChildren().addAll(
            topSpacer, content,
            bottomSpacer, bottom);

        Scene scene = new Scene(root);
        splashStage.setScene(scene);

        // ── Entrance animations ──────────
        content.setOpacity(0);
        content.setTranslateY(30);

        FadeTransition fadeIn =
            new FadeTransition(
                Duration.millis(600), content);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideUp =
            new TranslateTransition(
                Duration.millis(600), content);
        slideUp.setFromY(30);
        slideUp.setToY(0);
        slideUp.setInterpolator(
            Interpolator.EASE_OUT);

        ParallelTransition entrance =
            new ParallelTransition(
                fadeIn, slideUp);

        PauseTransition hold =
            new PauseTransition(
                Duration.seconds(2.5));

        FadeTransition fadeOut =
            new FadeTransition(
                Duration.millis(500), root);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e ->
            splashStage.close());

        SequentialTransition sequence =
            new SequentialTransition(
                entrance, hold, fadeOut);
        sequence.play();

        root.setOnMouseClicked(e -> {
            sequence.stop();
            FadeTransition quickFade =
                new FadeTransition(
                    Duration.millis(200), root);
            quickFade.setFromValue(
                root.getOpacity());
            quickFade.setToValue(0);
            quickFade.setOnFinished(ev ->
                splashStage.close());
            quickFade.play();
        });

        splashStage.setOnCloseRequest(
            e -> e.consume());

        splashStage.showAndWait();
    }

    private String getGreeting() {
        int hour = LocalTime.now().getHour();
        if (hour < 5)  return "Good night";
        if (hour < 12) return "Good morning";
        if (hour < 17) return "Good afternoon";
        if (hour < 21) return "Good evening";
        return "Good night";
    }
}