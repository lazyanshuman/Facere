package com.habitflow.controller;

import com.habitflow.dao.DoDontDAO;
import com.habitflow.model.DoDont;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.List;

/**
 * DoDontsController
 *
 * Builds and manages the Dos / Don'ts screen.
 * Shows two panels side by side:
 *   Left  = DOS  (green)
 *   Right = DONTS (red)
 */
public class DoDontsController {

    private final DoDontDAO doDontDAO =
        new DoDontDAO();

    /**
     * Builds and returns the complete
     * Dos/Don'ts screen.
     */
    public HBox buildScreen() {

        // ── DOS panel (left) ─────────────────
        VBox dosPanel = buildPanel(
            DoDont.Type.DO);

        // ── DONTS panel (right) ──────────────
        VBox dontsPanel = buildPanel(
            DoDont.Type.DONT);

        // ── Side by side layout ──────────────
        HBox screen = new HBox(16,
            dosPanel, dontsPanel);
        HBox.setHgrow(dosPanel,
            Priority.ALWAYS);
        HBox.setHgrow(dontsPanel,
            Priority.ALWAYS);
        screen.setPadding(new Insets(0));

        return screen;
    }

    /**
     * Builds one panel — either DOS or DONTS.
     */
    private VBox buildPanel(DoDont.Type type) {

        boolean isDo = type == DoDont.Type.DO;

        // ── Panel header ─────────────────────
        Label header = new Label(
            isDo ? "✅  DOS" : "❌  DON'TS");
        header.getStyleClass().add(
            isDo ? "dodont-header-do" :
                   "dodont-header-dont");

        // ── Add button ───────────────────────
        Button addBtn = new Button(
            isDo ? "+ Add a Do" :
                   "+ Add a Don't");
        addBtn.getStyleClass().add(
            isDo ? "btn-add-do" :
                   "btn-add-dont");

        // Header row
        HBox headerRow = new HBox(
            header);
        headerRow.setAlignment(
            Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerRow.getChildren()
            .addAll(spacer, addBtn);

        // ── Items list ───────────────────────
        VBox itemsList = new VBox(8);

        // Load items from database
        List<DoDont> items = isDo ?
            doDontDAO.getAllDos() :
            doDontDAO.getAllDonts();

        if (items.isEmpty()) {
            Label empty = new Label(
                isDo ?
                "No dos yet.\nAdd things you\n" +
                "want to do daily!" :
                "No don'ts yet.\nAdd habits\n" +
                "you want to avoid!");
            empty.setStyle(
                "-fx-text-fill: #9CA3AF;" +
                "-fx-font-size: 12px;" +
                "-fx-text-alignment: center;");
            empty.setWrapText(true);
            itemsList.getChildren().add(empty);
        } else {
            for (DoDont item : items) {
                DoDont.DailyStatus status =
                    doDontDAO.getTodayStatus(
                        item.getId());
                item.setTodayStatus(
                    status == null ?
                    DoDont.DailyStatus.PENDING :
                    status);
                VBox card = buildItemCard(
                    item, itemsList, type);
                itemsList.getChildren().add(card);
            }
        }

        // Add button action
        addBtn.setOnAction(e -> {
            DoDont newItem =
                showAddDialog(type);
            if (newItem != null) {
                doDontDAO.save(newItem);
                // Refresh by rebuilding card
                itemsList.getChildren().clear();
                List<DoDont> updated = isDo ?
                    doDontDAO.getAllDos() :
                    doDontDAO.getAllDonts();
                if (updated.isEmpty()) {
                    Label empty = new Label(
                        "No items yet.");
                    empty.setStyle(
                        "-fx-text-fill: #9CA3AF;");
                    itemsList.getChildren()
                        .add(empty);
                } else {
                    for (DoDont i : updated) {
                        DoDont.DailyStatus s =
                            doDontDAO
                            .getTodayStatus(
                                i.getId());
                        i.setTodayStatus(
                            s == null ?
                            DoDont.DailyStatus
                                .PENDING : s);
                        itemsList.getChildren()
                            .add(buildItemCard(
                                i, itemsList,
                                type));
                    }
                }
            }
        });

        // ── Panel container ──────────────────
        VBox panel = new VBox(12,
            headerRow, itemsList);
        panel.getStyleClass().add(
            isDo ? "dodont-do-panel" :
                   "dodont-dont-panel");
        VBox.setVgrow(itemsList,
            Priority.ALWAYS);

        return panel;
    }

    /**
     * Builds one item card inside a panel.
     *
     * Layout:
     * ┌────────────────────────────────┐
     * │ 🟢  Drink water               │
     * │     [✓ Did it]  [✗ Skipped]  [✕]│
     * └────────────────────────────────┘
     */
    private VBox buildItemCard(
            DoDont item,
            VBox parentList,
            DoDont.Type type) {

        // ── Colour dot ───────────────────────
        Circle dot = new Circle(6);
        try {
            dot.setFill(
                Color.web(item.getColorHex()));
        } catch (Exception e) {
            dot.setFill(Color.web(
                type == DoDont.Type.DO ?
                "#10B981" : "#EF4444"));
        }

        // ── Title ────────────────────────────
        Label title = new Label(
            item.getTitle());
        title.getStyleClass()
            .add("dodont-item-title");
        title.setWrapText(true);

        // ── Action buttons ───────────────────
        Button btnSuccess = new Button(
            item.getSuccessLabel());
        Button btnFailed  = new Button(
            item.getFailedLabel());

        applyButtonStyles(btnSuccess,
            btnFailed, item.getTodayStatus());

        // Success clicked
        btnSuccess.setOnAction(e -> {
            doDontDAO.logToday(item.getId(),
                DoDont.DailyStatus.SUCCESS);
            item.setTodayStatus(
                DoDont.DailyStatus.SUCCESS);
            applyButtonStyles(btnSuccess,
                btnFailed,
                DoDont.DailyStatus.SUCCESS);
        });

        // Failed clicked
        btnFailed.setOnAction(e -> {
            doDontDAO.logToday(item.getId(),
                DoDont.DailyStatus.FAILED);
            item.setTodayStatus(
                DoDont.DailyStatus.FAILED);
            applyButtonStyles(btnSuccess,
                btnFailed,
                DoDont.DailyStatus.FAILED);
        });

        // ── Delete button ────────────────────
        Button btnDelete = new Button("✕");
        btnDelete.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #9CA3AF;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 12px;");
        btnDelete.setOnAction(e -> {
            Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Item");
            confirm.setHeaderText(
                "Delete \"" +
                item.getTitle() + "\"?");
            confirm.setContentText(
                "This will delete this item " +
                "and all its history.");
            confirm.showAndWait()
                .ifPresent(r -> {
                    if (r == ButtonType.OK) {
                        doDontDAO.delete(
                            item.getId());
                        // Find and remove
                        // the card's parent
                        parentList.getChildren()
                            .removeIf(n ->
                                n == btnDelete
                                .getParent()
                                .getParent());
                    }
                });
        });

        // ── Button row ───────────────────────
        HBox buttons = new HBox(6,
            btnSuccess, btnFailed, btnDelete);
        buttons.setAlignment(
            Pos.CENTER_LEFT);

        // ── Title row with dot ───────────────
        HBox titleRow = new HBox(8,
            dot, title);
        titleRow.setAlignment(
            Pos.CENTER_LEFT);

        // ── Full card ────────────────────────
        VBox card = new VBox(8,
            titleRow, buttons);
        card.getStyleClass()
            .add("dodont-item-card");

        return card;
    }

    /**
     * Applies button styles based on
     * today's log status.
     */
    private void applyButtonStyles(
            Button btnSuccess,
            Button btnFailed,
            DoDont.DailyStatus status) {

        btnSuccess.getStyleClass().removeAll(
            "btn-dodont-success",
            "btn-dodont-success-active");
        btnFailed.getStyleClass().removeAll(
            "btn-dodont-failed",
            "btn-dodont-failed-active");

        if (status ==
                DoDont.DailyStatus.SUCCESS) {
            btnSuccess.getStyleClass()
                .add("btn-dodont-success-active");
            btnFailed.getStyleClass()
                .add("btn-dodont-failed");
            btnSuccess.setDisable(true);
            btnFailed.setDisable(false);

        } else if (status ==
                DoDont.DailyStatus.FAILED) {
            btnSuccess.getStyleClass()
                .add("btn-dodont-success");
            btnFailed.getStyleClass()
                .add("btn-dodont-failed-active");
            btnSuccess.setDisable(false);
            btnFailed.setDisable(true);

        } else {
            // PENDING
            btnSuccess.getStyleClass()
                .add("btn-dodont-success");
            btnFailed.getStyleClass()
                .add("btn-dodont-failed");
            btnSuccess.setDisable(false);
            btnFailed.setDisable(false);
        }
    }

    /**
     * Shows the Add Do/Don't dialog.
     */
    public DoDont showAddDialog(
            DoDont.Type type) {

        boolean isDo = type == DoDont.Type.DO;

        Dialog<DoDont> dialog = new Dialog<>();
        dialog.setTitle(
            isDo ? "Add a Do" :
                   "Add a Don't");
        
        com.habitflow.util.AppIcon.set(dialog);
        
        dialog.setHeaderText(
            isDo ?
            "Add something you want to\n" +
            "do every day" :
            "Add something you want to\n" +
            "avoid every day");

        ButtonType addBtn = new ButtonType(
            isDo ? "Add Do" : "Add Don't",
            ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane()
            .getButtonTypes()
            .addAll(addBtn, ButtonType.CANCEL);

        // ── Form ─────────────────────────────
        TextField titleField = new TextField();
        titleField.setPromptText(
            isDo ?
            "e.g. Drink 8 glasses of water" :
            "e.g. Skip the gym");

        TextField descField = new TextField();
        descField.setPromptText(
            "Optional description...");

        ColorPicker colorPicker =
            new ColorPicker(Color.web(
                isDo ? "#10B981" : "#EF4444"));

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(
            new Insets(16, 24, 8, 24));

        grid.add(new Label(
            isDo ? "What to do *" :
                   "What to avoid *"),
            0, 0);
        grid.add(titleField, 1, 0);

        grid.add(new Label("Description"),
            0, 1);
        grid.add(descField, 1, 1);

        grid.add(new Label("Colour"), 0, 2);
        grid.add(colorPicker, 1, 2);

        dialog.getDialogPane()
            .setContent(grid);
        titleField.requestFocus();

        dialog.setResultConverter(btn -> {
            if (btn != addBtn) return null;
            String t =
                titleField.getText().trim();
            if (t.isEmpty()) return null;

            DoDont item = new DoDont(t, type);
            item.setDescription(
                descField.getText().trim());

            Color c = colorPicker.getValue();
            item.setColorHex(String.format(
                "#%02X%02X%02X",
                (int)(c.getRed()   * 255),
                (int)(c.getGreen() * 255),
                (int)(c.getBlue()  * 255)));

            return item;
        });

        return dialog.showAndWait()
            .orElse(null);
    }
}