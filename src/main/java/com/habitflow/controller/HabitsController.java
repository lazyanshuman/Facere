package com.habitflow.controller;

import com.habitflow.dao.HabitDAO;
import com.habitflow.model.Habit;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.List;

/**
 * HabitsController
 *
 * Builds and manages the Habits screen.
 * Called by MainController when user clicks
 * the Habits nav button.
 */
public class HabitsController {

    private final HabitDAO habitDAO =
        new HabitDAO();

    /**
     * Builds and returns the complete
     * Habits screen as a VBox.
     * MainController puts this in the center pane.
     */
    public VBox buildHabitsScreen() {
        VBox screen = new VBox(12);
        screen.setPadding(new Insets(0));

        List<Habit> habits =
            habitDAO.getAllActive();

        if (habits.isEmpty()) {
            // Empty state
            VBox empty = new VBox(8);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(
                new Insets(60, 0, 0, 0));

            Label icon = new Label("🔥");
            icon.setStyle("-fx-font-size: 48px;");

            Label msg = new Label(
                "No habits yet!");
            msg.getStyleClass()
                .add("coming-soon-label");

            Label sub = new Label(
                "Click \"+ Add Habit\" to " +
                "create your first habit.");
            sub.getStyleClass()
                .add("coming-soon-sub");

            empty.getChildren()
                .addAll(icon, msg, sub);
            screen.getChildren().add(empty);

        } else {
            // Build a card for each habit
            for (Habit habit : habits) {

                // Load today's status
                Habit.LogStatus todayStatus =
                    habitDAO.getTodayStatus(
                        habit.getId());
                habit.setTodayStatus(
                    todayStatus == null ?
                    Habit.LogStatus.PENDING :
                    todayStatus);

                HBox card =
                    buildHabitCard(habit, screen);
                screen.getChildren().add(card);
            }
        }

        return screen;
    }

    /**
     * Builds one habit card.
     *
     * Layout:
     * ┌─────────────────────────────────────┐
     * │ 🟣  Drink Water    [BUILD]  🔥 7    │
     * │      Daily                          │
     * │                  [✓ Done] [✗ Skip]  │
     * └─────────────────────────────────────┘
     */
    private HBox buildHabitCard(
            Habit habit, VBox parent) {

        // ── Colour dot ───────────────────────
        Circle dot = new Circle(8);
        try {
            dot.setFill(Color.web(
                habit.getColorHex()));
        } catch (Exception e) {
            dot.setFill(Color.web("#6C63FF"));
        }

        // ── Title ────────────────────────────
        Label title = new Label(
            habit.getTitle());
        title.getStyleClass().add("habit-title");

        // ── Frequency ────────────────────────
        Label freq = new Label(
            habit.getFrequencyLabel());
        freq.getStyleClass().add("habit-subtitle");

        // ── Type badge ───────────────────────
        Label typeBadge = new Label(
            habit.getHabitType()
                == Habit.HabitType.BUILD ?
                "BUILD" : "BREAK");
        typeBadge.getStyleClass().add(
            habit.getHabitType()
                == Habit.HabitType.BUILD ?
                "habit-type-build" :
                "habit-type-break");

        // ── Streak ───────────────────────────
        Label streak = new Label(
            "🔥 " + habit.getCurrentStreak() +
            " day streak");
        streak.getStyleClass().add("habit-streak");
        if (habit.getCurrentStreak() == 0) {
            streak.setText("No streak yet");
            streak.setStyle(
                "-fx-text-fill: #9CA3AF;" +
                "-fx-font-size: 12px;");
        }

        // ── Info box ─────────────────────────
        VBox info = new VBox(4,
            title,
            new HBox(8, freq, typeBadge),
            streak);
        info.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(info, Priority.ALWAYS);

        // ── Done button ──────────────────────
        Button btnDone = new Button("✓  Done");
        Button btnSkip = new Button("✗  Skip");

        // Apply correct style based on
        // today's existing log status
        applyButtonStyles(
            btnDone, btnSkip,
            habit.getTodayStatus());

        // Done clicked
        btnDone.setOnAction(e -> {
            habitDAO.logToday(habit.getId(),
                Habit.LogStatus.DONE);
            habit.setTodayStatus(
                Habit.LogStatus.DONE);
            applyButtonStyles(
                btnDone, btnSkip,
                Habit.LogStatus.DONE);
            // Refresh streak label
            Habit updated = habitDAO
                .getAllActive()
                .stream()
                .filter(h ->
                    h.getId() == habit.getId())
                .findFirst()
                .orElse(habit);
            streak.setText("🔥 " +
                updated.getCurrentStreak() +
                " day streak");
            streak.setStyle(
                "-fx-text-fill: #F59E0B;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 13px;");
        });

        // Skip clicked
        btnSkip.setOnAction(e -> {
            habitDAO.logToday(habit.getId(),
                Habit.LogStatus.SKIPPED);
            habit.setTodayStatus(
                Habit.LogStatus.SKIPPED);
            applyButtonStyles(
                btnDone, btnSkip,
                Habit.LogStatus.SKIPPED);
        });

        // ── Delete button ────────────────────
        Button btnDelete = new Button("✕");
        btnDelete.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #9CA3AF;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 14px;");
        btnDelete.setOnAction(e -> {
            Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Habit");
            confirm.setHeaderText(
                "Delete \"" +
                habit.getTitle() + "\"?");
            confirm.setContentText(
                "This will delete the habit " +
                "and all its history.");
            confirm.showAndWait()
                .ifPresent(r -> {
                    if (r == ButtonType.OK) {
                        habitDAO.delete(
                            habit.getId());
                        // Remove card from screen
                        parent.getChildren()
                            .remove(
                                btnDelete
                                .getParent());
                    }
                });
        });

        // ── Button row ───────────────────────
        HBox buttons = new HBox(8,
            btnDone, btnSkip, btnDelete);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        // ── Full card ────────────────────────
        HBox card = new HBox(12,
            dot, info, buttons);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("habit-card");

        return card;
    }

    /**
     * Applies the correct visual style to
     * Done and Skip buttons based on
     * today's log status.
     *
     * If already marked Done → Done button
     * appears filled/active.
     * If already Skipped → Skip button
     * appears filled/active.
     */
    private void applyButtonStyles(
            Button btnDone,
            Button btnSkip,
            Habit.LogStatus status) {

        // Reset both first
        btnDone.getStyleClass().removeAll(
            "btn-habit-done",
            "btn-habit-done-active");
        btnSkip.getStyleClass().removeAll(
            "btn-habit-skip",
            "btn-habit-skip-active");

        if (status == Habit.LogStatus.DONE) {
            btnDone.getStyleClass()
                .add("btn-habit-done-active");
            btnSkip.getStyleClass()
                .add("btn-habit-skip");
            btnDone.setDisable(true);
            btnSkip.setDisable(false);
        } else if (
            status == Habit.LogStatus.SKIPPED) {
            btnDone.getStyleClass()
                .add("btn-habit-done");
            btnSkip.getStyleClass()
                .add("btn-habit-skip-active");
            btnDone.setDisable(false);
            btnSkip.setDisable(true);
        } else {
            // PENDING — neither marked yet
            btnDone.getStyleClass()
                .add("btn-habit-done");
            btnSkip.getStyleClass()
                .add("btn-habit-skip");
            btnDone.setDisable(false);
            btnSkip.setDisable(false);
        }
    }

    /**
     * Shows the Add Habit modal dialog.
     * Called when + Add Habit is clicked.
     */
    public Habit showAddHabitDialog() {

        Dialog<Habit> dialog =
            new Dialog<>();
        dialog.setTitle("Add New Habit");
        com.habitflow.util.AppIcon.set(dialog);
        dialog.setHeaderText(
            "Create a new habit to track");

        // Buttons
        ButtonType addBtn = new ButtonType(
            "Add Habit",
            ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane()
            .getButtonTypes()
            .addAll(addBtn,
                ButtonType.CANCEL);

        // ── Form fields ──────────────────────

        // Title
        TextField titleField =
            new TextField();
        titleField.setPromptText(
            "e.g. Drink 8 glasses of water");

        // Description
        TextField descField =
            new TextField();
        descField.setPromptText(
            "Optional description...");

        // Habit type toggle
        ToggleGroup typeGroup =
            new ToggleGroup();
        RadioButton buildBtn =
            new RadioButton("BUILD ✅" +
            "  (positive habit)");
        RadioButton breakBtn =
            new RadioButton("BREAK ❌" +
            "  (bad habit to stop)");
        buildBtn.setToggleGroup(typeGroup);
        breakBtn.setToggleGroup(typeGroup);
        buildBtn.setSelected(true);

        // Frequency
        ComboBox<String> freqBox =
            new ComboBox<>();
        freqBox.getItems().addAll(
            "Daily",
            "Weekdays (Mon-Fri)",
            "Weekends (Sat-Sun)",
            "Custom");
        freqBox.setValue("Daily");

        // Colour picker
        ColorPicker colorPicker =
            new ColorPicker(
                Color.web("#6C63FF"));

        // ── Layout ───────────────────────────
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(
            new Insets(16, 24, 8, 24));

        grid.add(new Label("Habit Name *"),
            0, 0);
        grid.add(titleField, 1, 0);

        grid.add(new Label("Description"),
            0, 1);
        grid.add(descField, 1, 1);

        grid.add(new Label("Type"), 0, 2);
        VBox typeBox = new VBox(6,
            buildBtn, breakBtn);
        grid.add(typeBox, 1, 2);

        grid.add(new Label("Frequency"),
            0, 3);
        grid.add(freqBox, 1, 3);

        grid.add(new Label("Colour"), 0, 4);
        grid.add(colorPicker, 1, 4);

        dialog.getDialogPane()
            .setContent(grid);

        // Focus title field on open
        titleField.requestFocus();

        // ── Result converter ─────────────────
        dialog.setResultConverter(btn -> {
            if (btn != addBtn) return null;

            String title =
                titleField.getText().trim();
            if (title.isEmpty()) return null;

            Habit habit = new Habit();
            habit.setTitle(title);
            habit.setDescription(
                descField.getText().trim());

            // Habit type
            habit.setHabitType(
                buildBtn.isSelected() ?
                Habit.HabitType.BUILD :
                Habit.HabitType.BREAK);

            // Frequency
            String freq = freqBox.getValue();
            habit.setRecurrenceDays(
                switch (freq) {
                    case "Weekdays (Mon-Fri)" ->
                        "1,2,3,4,5";
                    case "Weekends (Sat-Sun)" ->
                        "6,7";
                    default -> "1,2,3,4,5,6,7";
                });

            // Colour
            Color c = colorPicker.getValue();
            habit.setColorHex(String.format(
                "#%02X%02X%02X",
                (int)(c.getRed()   * 255),
                (int)(c.getGreen() * 255),
                (int)(c.getBlue()  * 255)));

            return habit;
        });

        return dialog.showAndWait()
            .orElse(null);
    }
}
