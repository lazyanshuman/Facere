package com.habitflow.controller;

import com.habitflow.model.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.Objects;

/**
 * AddTaskModal — clean light themed modal.
 * Always light regardless of app theme.
 */
public class AddTaskModal {

    // Track selected priority
    private Task.Priority selectedPriority =
        Task.Priority.NORMAL;

    public Task show(
            Task.Section defaultSection) {

        // Reset priority each time
        selectedPriority = Task.Priority.NORMAL;

        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Add New Task");
        com.habitflow.util.AppIcon.set(dialog);
        dialog.setHeaderText(null);
        dialog.setGraphic(null);

        ButtonType addButtonType =
            new ButtonType("✚  Add Task",
                ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane()
            .getButtonTypes()
            .addAll(addButtonType,
                ButtonType.CANCEL);

        // Force white background on dialog
        dialog.getDialogPane().setStyle(
            "-fx-background-color: #FFFFFF;" +
            "-fx-border-radius: 16px;" +
            "-fx-background-radius: 16px;");
        dialog.getDialogPane()
            .setPrefWidth(520);

        // Override dark theme on dialog
        // by loading main.css explicitly
        try {
            String mainCss = Objects
                .requireNonNull(
                    getClass().getResource(
                    "/com/habitflow/css/" +
                    "main.css"))
                .toExternalForm();
            dialog.getDialogPane()
                .getStylesheets()
                .clear();
            dialog.getDialogPane()
                .getStylesheets()
                .add(mainCss);
        } catch (Exception e) {
            // CSS not critical — continue
        }

        // Build form
        VBox content = buildForm(
            defaultSection);
        dialog.getDialogPane()
            .setContent(content);

        // Style Add Task button
        Button addBtn = (Button) dialog
            .getDialogPane()
            .lookupButton(addButtonType);
        addBtn.setStyle(
            "-fx-background-color: #6C63FF;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 10px 28px;" +
            "-fx-font-size: 13px;" +
            "-fx-cursor: hand;");

        // Style Cancel button
        Button cancelBtn = (Button) dialog
            .getDialogPane()
            .lookupButton(ButtonType.CANCEL);
        cancelBtn.setStyle(
            "-fx-background-color: #F9FAFB;" +
            "-fx-text-fill: #374151;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 10px 20px;" +
            "-fx-border-color: #E5E7EB;" +
            "-fx-border-radius: 8px;" +
            "-fx-border-width: 1px;" +
            "-fx-font-size: 13px;" +
            "-fx-cursor: hand;");

        // Style button bar
        try {
            dialog.getDialogPane()
                .lookup(".button-bar")
                .setStyle(
                    "-fx-background-color:" +
                    " #FFFFFF;" +
                    "-fx-padding: " +
                    "8px 24px 16px;");
        } catch (Exception e) {
            // Not critical
        }

        // Result converter
        dialog.setResultConverter(btn -> {
            if (btn != addButtonType)
                return null;
            return buildTask(
                content, defaultSection);
        });

        return dialog.showAndWait()
            .orElse(null);
    }

    // ═══════════════════════════════════════
    // BUILD FORM
    // ═══════════════════════════════════════

    private VBox buildForm(
            Task.Section defaultSection) {

        VBox form = new VBox(20);
        form.setPadding(
            new Insets(24, 28, 4, 28));
        form.setStyle(
            "-fx-background-color: #FFFFFF;");

        // ── Title ────────────────────────────
        Label titleLabel = fieldLabel(
            "TASK TITLE");
        TextField titleField =
            new TextField();
        titleField.setPromptText(
            "What do you need to do?");
        titleField.setStyle(inputStyle());
        titleField.setId("titleField");

        // ── Description ──────────────────────
        Label descLabel = fieldLabel(
            "DESCRIPTION");
        TextArea descField = new TextArea();
        descField.setPromptText(
            "Add notes or details (optional)");
        descField.setPrefRowCount(2);
        descField.setWrapText(true);
        descField.setStyle(
            inputStyle() +
            "-fx-pref-height: 70px;");
        descField.setId("descField");

        // ── Priority ─────────────────────────
        Label priorityLabel = fieldLabel(
            "PRIORITY");

        Button btnLow = priorityPill(
            "Low", "#10B981", "#D1FAE5");
        Button btnNormal = priorityPill(
            "Normal", "#6C63FF", "#EDE9FE");
        Button btnHigh = priorityPill(
            "High", "#F59E0B", "#FEF3C7");
        Button btnCritical = priorityPill(
            "Critical", "#EF4444", "#FEE2E2");

        // Normal selected by default
        activatePill(btnNormal,
            "#6C63FF", "#EDE9FE");

        btnLow.setOnAction(e -> {
            selectedPriority =
                Task.Priority.LOW;
            resetPills(btnLow, btnNormal,
                btnHigh, btnCritical);
            activatePill(btnLow,
                "#10B981", "#D1FAE5");
        });
        btnNormal.setOnAction(e -> {
            selectedPriority =
                Task.Priority.NORMAL;
            resetPills(btnLow, btnNormal,
                btnHigh, btnCritical);
            activatePill(btnNormal,
                "#6C63FF", "#EDE9FE");
        });
        btnHigh.setOnAction(e -> {
            selectedPriority =
                Task.Priority.HIGH;
            resetPills(btnLow, btnNormal,
                btnHigh, btnCritical);
            activatePill(btnHigh,
                "#F59E0B", "#FEF3C7");
        });
        btnCritical.setOnAction(e -> {
            selectedPriority =
                Task.Priority.CRITICAL;
            resetPills(btnLow, btnNormal,
                btnHigh, btnCritical);
            activatePill(btnCritical,
                "#EF4444", "#FEE2E2");
        });

        HBox priorityRow = new HBox(8,
            btnLow, btnNormal,
            btnHigh, btnCritical);
        priorityRow.setAlignment(
            Pos.CENTER_LEFT);
        priorityRow.setId("priorityRow");

        // ── Due Date ─────────────────────────
        Label dueDateLbl = fieldLabel(
            "DUE DATE");
        DatePicker datePicker =
            new DatePicker(LocalDate.now());
        datePicker.setId("datePicker");
        datePicker.setStyle(
            "-fx-background-color: #F9FAFB;" +
            "-fx-border-color: #E5E7EB;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-pref-width: 220px;" +
            "-fx-font-size: 13px;");
        datePicker.getEditor().setStyle(
            "-fx-background-color: #F9FAFB;" +
            "-fx-text-fill: #111827;" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 8px 12px;");

        // ── Section ──────────────────────────
        Label sectionLbl = fieldLabel(
            "SECTION");
        ComboBox<String> sectionBox =
            new ComboBox<>();
        sectionBox.getItems().addAll(
            "My Day",
            "Important",
            "Notify Later");
        sectionBox.setId("sectionBox");
        sectionBox.setStyle(
            "-fx-background-color: #F9FAFB;" +
            "-fx-border-color: #E5E7EB;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-pref-width: 220px;" +
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #111827;");

        switch (defaultSection) {
            case IMPORTANT ->
                sectionBox.setValue(
                    "Important");
            case NOTIFY_LATER ->
                sectionBox.setValue(
                    "Notify Later");
            default ->
                sectionBox.setValue("My Day");
        }

        VBox dateCol = new VBox(6,
            dueDateLbl, datePicker);
        VBox secCol = new VBox(6,
            sectionLbl, sectionBox);
        HBox dateRow = new HBox(16,
            dateCol, secCol);

        // ── Category ─────────────────────────
        Label catLabel = fieldLabel(
            "CATEGORY");
        ComboBox<String> categoryBox =
            new ComboBox<>();
        categoryBox.getItems().addAll(
            "None", "Personal", "Work",
            "Health", "Study", "Finance");
        categoryBox.setValue("None");
        categoryBox.setId("categoryBox");
        categoryBox.setStyle(
            "-fx-background-color: #F9FAFB;" +
            "-fx-border-color: #E5E7EB;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-pref-width: 464px;" +
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #111827;");

        // ── Divider ──────────────────────────
        Region div = new Region();
        div.setPrefHeight(1);
        div.setMaxHeight(1);
        div.setStyle(
            "-fx-background-color: #F3F4F6;");

        // ── Assemble ─────────────────────────
        form.getChildren().addAll(
            new VBox(6, titleLabel,
                titleField),
            new VBox(6, descLabel,
                descField),
            new VBox(6, priorityLabel,
                priorityRow),
            dateRow,
            new VBox(6, catLabel,
                categoryBox),
            div);

        return form;
    }

    // ═══════════════════════════════════════
    // BUILD TASK FROM FORM
    // ═══════════════════════════════════════

    @SuppressWarnings("unchecked")
    private Task buildTask(
            VBox form,
            Task.Section defaultSection) {

        // Find fields by ID
        TextField titleField =
            (TextField) form.lookup(
                "#titleField");
        TextArea descField =
            (TextArea) form.lookup(
                "#descField");
        DatePicker datePicker =
            (DatePicker) form.lookup(
                "#datePicker");

        // Use typed ComboBox with
        // suppressed warning
        ComboBox<String> sectionBox =
            (ComboBox<String>) form.lookup(
                "#sectionBox");
        ComboBox<String> categoryBox =
            (ComboBox<String>) form.lookup(
                "#categoryBox");

        // Title is required
        if (titleField == null) return null;
        String title =
            titleField.getText().trim();
        if (title.isEmpty()) return null;

        Task task = new Task(title);

        // Description
        if (descField != null) {
            String desc =
                descField.getText().trim();
            if (!desc.isEmpty()) {
                task.setDescription(desc);
            }
        }

        // Priority from instance variable
        task.setPriority(selectedPriority);

        // Due date
        if (datePicker != null &&
            datePicker.getValue() != null) {
            task.setDueDate(
                datePicker.getValue());
        }

        // Section
        if (sectionBox != null &&
            sectionBox.getValue() != null) {
            task.setSection(
                switch (sectionBox.getValue()) {
                    case "Important" ->
                        Task.Section.IMPORTANT;
                    case "Notify Later" ->
                        Task.Section.NOTIFY_LATER;
                    default ->
                        Task.Section.MY_DAY;
                });
        } else {
            task.setSection(defaultSection);
        }

        // Category
        if (categoryBox != null &&
            categoryBox.getValue() != null) {
            task.setCategoryId(
                switch (categoryBox.getValue()) {
                    case "Personal" -> 1;
                    case "Work"     -> 2;
                    case "Health"   -> 3;
                    case "Study"    -> 4;
                    case "Finance"  -> 5;
                    default         -> 0;
                });
        }

        return task;
    }

    // ═══════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setStyle(
            "-fx-font-size: 10px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #9CA3AF;");
        return l;
    }

    private String inputStyle() {
        return
            "-fx-background-color: #F9FAFB;" +
            "-fx-border-color: #E5E7EB;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 10px 14px;" +
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #111827;" +
            "-fx-prompt-text-fill: #9CA3AF;";
    }

    private Button priorityPill(
            String text,
            String textColor,
            String bgColor) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: " +
            bgColor + ";" +
            "-fx-text-fill: " +
            textColor + ";" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 20px;" +
            "-fx-padding: 6px 16px;" +
            "-fx-cursor: hand;" +
            "-fx-opacity: 0.5;");
        return btn;
    }

    private void activatePill(
            Button btn,
            String textColor,
            String bgColor) {
        btn.setStyle(
            "-fx-background-color: " +
            bgColor + ";" +
            "-fx-text-fill: " +
            textColor + ";" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 20px;" +
            "-fx-padding: 6px 16px;" +
            "-fx-cursor: hand;" +
            "-fx-opacity: 1.0;" +
            "-fx-border-color: " +
            textColor + ";" +
            "-fx-border-radius: 20px;" +
            "-fx-border-width: 1.5px;");
    }

    private void resetPills(
            Button btnLow,
            Button btnNormal,
            Button btnHigh,
            Button btnCritical) {
        priorityPillReset(btnLow,
            "#10B981", "#D1FAE5");
        priorityPillReset(btnNormal,
            "#6C63FF", "#EDE9FE");
        priorityPillReset(btnHigh,
            "#F59E0B", "#FEF3C7");
        priorityPillReset(btnCritical,
            "#EF4444", "#FEE2E2");
    }

    private void priorityPillReset(
            Button btn,
            String textColor,
            String bgColor) {
        btn.setStyle(
            "-fx-background-color: " +
            bgColor + ";" +
            "-fx-text-fill: " +
            textColor + ";" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 20px;" +
            "-fx-padding: 6px 16px;" +
            "-fx-cursor: hand;" +
            "-fx-opacity: 0.5;");
    }
}