package com.habitflow.controller;

import com.habitflow.dao.TaskDAO;
import com.habitflow.model.Task;
import com.habitflow.util.AppIcon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * DueTasksModalController
 *
 * Shows only NOTIFY_LATER tasks.
 * No task count displayed.
 */
public class DueTasksModalController {

    private final TaskDAO taskDAO =
        new TaskDAO();

    private int remainingCount = 0;
    private Stage modalStage;

    public static void checkAndShow(
            Stage ownerStage) {
        DueTasksModalController ctrl =
            new DueTasksModalController();
        List<DueTask> dueTasks =
            ctrl.getDueTasks();
        if (!dueTasks.isEmpty()) {
            ctrl.show(ownerStage, dueTasks);
        }
    }

    private void show(
            Stage ownerStage,
            List<DueTask> dueTasks) {

        modalStage = new Stage();
        modalStage.initModality(
            Modality.APPLICATION_MODAL);
        modalStage.initOwner(ownerStage);
        modalStage.setTitle(
            "Facere — Tasks Need Attention");

        AppIcon.set(modalStage);

        remainingCount = dueTasks.size();

        VBox root = new VBox(0);
        root.setStyle(
            "-fx-background-color: #FFFFFF;");

        VBox header = buildHeader();

        ScrollPane scrollPane =
            new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(
            "-fx-background: white;" +
            "-fx-background-color: white;" +
            "-fx-border-width: 0;");
        scrollPane.setPrefHeight(400);

        VBox taskList = new VBox(0);
        taskList.setStyle(
            "-fx-background-color: white;");

        for (int i = 0;
                i < dueTasks.size(); i++) {
            DueTask dt = dueTasks.get(i);
            VBox row = buildTaskRow(
                dt, taskList, i,
                dueTasks.size());
            taskList.getChildren().add(row);
        }

        scrollPane.setContent(taskList);

        HBox footer = buildFooter(
            dueTasks, taskList);

        root.getChildren().addAll(
            header, scrollPane, footer);

        javafx.scene.Scene scene =
            new javafx.scene.Scene(root,
                640, 580);
        modalStage.setScene(scene);
        modalStage.setResizable(false);

        modalStage.setX(
            ownerStage.getX() +
            ownerStage.getWidth() / 2 - 320);
        modalStage.setY(
            ownerStage.getY() +
            ownerStage.getHeight() / 2 - 290);

        modalStage.show();
    }

    // ═══════════════════════════════════════
    // BUILD HEADER — no count
    // ═══════════════════════════════════════

    private VBox buildHeader() {
        VBox header = new VBox(6);
        header.setPadding(
            new Insets(28, 32, 20, 32));
        header.setStyle(
            "-fx-background-color: #FAFAFA;" +
            "-fx-border-color: transparent" +
            " transparent #F0F0F0" +
            " transparent;" +
            "-fx-border-width: 0 0 1px 0;");

        HBox titleRow = new HBox(12);
        titleRow.setAlignment(
            Pos.CENTER_LEFT);

        Label bellIcon = new Label("🔔");
        bellIcon.setStyle(
            "-fx-font-size: 28px;");

        VBox titleBox = new VBox(3);
        Label title = new Label(
            "Tasks Need Your Attention");
        title.setStyle(
            "-fx-font-size: 20px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #1A1A2E;" +
            "-fx-font-family: 'Segoe UI';");

        Label subtitle = new Label(
            "These tasks are in your " +
            "Notify Later list.");
        subtitle.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #6B7280;" +
            "-fx-font-family: 'Segoe UI';");

        titleBox.getChildren().addAll(
            title, subtitle);
        titleRow.getChildren().addAll(
            bellIcon, titleBox);

        header.getChildren().add(titleRow);
        return header;
    }

    // ═══════════════════════════════════════
    // BUILD TASK ROW
    // ═══════════════════════════════════════

    private VBox buildTaskRow(
            DueTask dt,
            VBox taskList,
            int index,
            int total) {

        VBox wrapper = new VBox();

        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(
            new Insets(16, 32, 16, 32));
        row.setStyle(
            "-fx-background-color: white;");

        row.setOnMouseEntered(e ->
            row.setStyle(
                "-fx-background-color: " +
                "#FAFBFF;"));
        row.setOnMouseExited(e ->
            row.setStyle(
                "-fx-background-color: " +
                "white;"));

        Circle urgencyDot = new Circle(6);
        urgencyDot.setFill(
            Color.web(dt.urgencyColor));

        VBox infoBox = new VBox(4);
        HBox.setHgrow(infoBox,
            Priority.ALWAYS);

        Label taskTitle = new Label(
            dt.task.getTitle());
        taskTitle.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #1A1A2E;" +
            "-fx-font-family: 'Segoe UI';");
        taskTitle.setWrapText(true);

        HBox badgeRow = new HBox(8);
        badgeRow.setAlignment(
            Pos.CENTER_LEFT);

        Label urgencyBadge = new Label(
            dt.urgencyLabel);
        urgencyBadge.setStyle(
            "-fx-background-color: " +
            dt.urgencyBg + ";" +
            "-fx-text-fill: " +
            dt.urgencyColor + ";" +
            "-fx-font-size: 10px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 4px;" +
            "-fx-padding: 2px 8px;");

        Label dueInfo = new Label(
            dt.dueText);
        dueInfo.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-text-fill: #9CA3AF;" +
            "-fx-font-family: 'Segoe UI';");

        badgeRow.getChildren().addAll(
            urgencyBadge, dueInfo);

        infoBox.getChildren().addAll(
            taskTitle, badgeRow);

        HBox buttons = new HBox(8);
        buttons.setAlignment(
            Pos.CENTER_RIGHT);

        Button btnDone = actionButton(
            "✅  Done",
            "#10B981", "#D1FAE5");
        Button btnToday = actionButton(
            "📅  Today",
            "#6C63FF", "#EDE9FE");
        Button btnDelay = actionButton(
            "⏳  Delay",
            "#F59E0B", "#FEF3C7");

        Button btnDismiss = new Button(
            "🔕");
        btnDismiss.setStyle(
            "-fx-background-color: " +
            "transparent;" +
            "-fx-text-fill: #9CA3AF;" +
            "-fx-font-size: 14px;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 4px 6px;");
        btnDismiss.setTooltip(
            new Tooltip("Dismiss"));

        buttons.getChildren().addAll(
            btnDone, btnToday,
            btnDelay, btnDismiss);

        row.getChildren().addAll(
            urgencyDot, infoBox, buttons);

        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setStyle(
            "-fx-background-color: #F3F4F6;");

        wrapper.getChildren().addAll(
            row,
            index < total - 1 ?
                divider : new Region());

        btnDone.setOnAction(e -> {
            taskDAO.updateStatus(
                dt.task.getId(),
                Task.TaskStatus.DONE);
            removeRow(wrapper, taskList);
        });

        btnToday.setOnAction(e -> {
            moveToToday(dt.task.getId());
            removeRow(wrapper, taskList);
        });

        btnDelay.setOnAction(e -> {
            taskDAO.updateStatus(
                dt.task.getId(),
                Task.TaskStatus.DELAYED);
            removeRow(wrapper, taskList);
        });

        btnDismiss.setOnAction(e ->
            removeRow(wrapper, taskList));

        return wrapper;
    }

    // ═══════════════════════════════════════
    // BUILD FOOTER
    // ═══════════════════════════════════════

    private HBox buildFooter(
            List<DueTask> dueTasks,
            VBox taskList) {

        HBox footer = new HBox(12);
        footer.setAlignment(
            Pos.CENTER_RIGHT);
        footer.setPadding(
            new Insets(16, 32, 16, 32));
        footer.setStyle(
            "-fx-background-color: #FAFAFA;" +
            "-fx-border-color: #F0F0F0" +
            " transparent transparent" +
            " transparent;" +
            "-fx-border-width: 1px 0 0 0;");

        Button btnDoneAll = new Button(
            "✅  Mark All Done");
        btnDoneAll.setStyle(
            "-fx-background-color: #10B981;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 8px 20px;" +
            "-fx-cursor: hand;");
        btnDoneAll.setOnAction(e -> {
            for (DueTask dt : dueTasks) {
                taskDAO.updateStatus(
                    dt.task.getId(),
                    Task.TaskStatus.DONE);
            }
            modalStage.close();
        });

        Button btnDelayAll = new Button(
            "⏳  Delay All");
        btnDelayAll.setStyle(
            "-fx-background-color: " +
            "#F9FAFB;" +
            "-fx-text-fill: #374151;" +
            "-fx-font-size: 13px;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 8px 20px;" +
            "-fx-border-color: #E5E7EB;" +
            "-fx-border-radius: 8px;" +
            "-fx-border-width: 1px;" +
            "-fx-cursor: hand;");
        btnDelayAll.setOnAction(e -> {
            for (DueTask dt : dueTasks) {
                taskDAO.updateStatus(
                    dt.task.getId(),
                    Task.TaskStatus.DELAYED);
            }
            modalStage.close();
        });

        Button btnClose = new Button(
            "Close");
        btnClose.setStyle(
            "-fx-background-color: " +
            "transparent;" +
            "-fx-text-fill: #9CA3AF;" +
            "-fx-font-size: 13px;" +
            "-fx-cursor: hand;");
        btnClose.setOnAction(e ->
            modalStage.close());

        footer.getChildren().addAll(
            btnClose, btnDelayAll,
            btnDoneAll);

        return footer;
    }

    // ═══════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════

    private void removeRow(
            VBox wrapper, VBox taskList) {
        taskList.getChildren()
            .remove(wrapper);
        remainingCount--;
        if (remainingCount <= 0) {
            modalStage.close();
        }
    }

    private void moveToToday(int taskId) {
        String sql = """
            UPDATE tasks
            SET due_date   = ?,
                section    = 'MY_DAY',
                task_status = 'PENDING',
                is_rolled_over = 1,
                rollover_count =
                    rollover_count + 1
            WHERE id = ?
            """;

        try (var conn =
                 com.habitflow.dao
                     .DatabaseManager
                     .getInstance()
                     .getConnection();
             var ps = conn
                 .prepareStatement(sql)) {

            ps.setString(1,
                LocalDate.now().toString());
            ps.setInt(2, taskId);
            ps.executeUpdate();

        } catch (Exception e) {
            // log error
        }
    }

    private Button actionButton(
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
            "-fx-background-radius: 6px;" +
            "-fx-padding: 5px 12px;" +
            "-fx-cursor: hand;");

        btn.setOnMouseEntered(e ->
            btn.setOpacity(0.85));
        btn.setOnMouseExited(e ->
            btn.setOpacity(1.0));

        return btn;
    }

    // ═══════════════════════════════════════
    // FETCH ONLY NOTIFY_LATER TASKS
    // ═══════════════════════════════════════

    private List<DueTask> getDueTasks() {
        String sql = """
            SELECT * FROM tasks
            WHERE is_deleted = 0
              AND task_status = 'PENDING'
              AND user_id = ?
              AND section = 'NOTIFY_LATER'
            ORDER BY due_date ASC
            """;

        List<DueTask> result =
            new ArrayList<>();

        try (var conn =
                 com.habitflow.dao
                     .DatabaseManager
                     .getInstance()
                     .getConnection();
             var ps = conn
                 .prepareStatement(sql)) {

            ps.setInt(1,
                com.habitflow.service
                    .UserSession
                    .getCurrentUserId());

            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    Task task = mapTask(rs);
                    DueTask dt = new DueTask();
                    dt.task = task;
                    dt.urgencyLabel = "REMINDER";
                    dt.urgencyColor = "#6C63FF";
                    dt.urgencyBg    = "#EDE9FE";

                    if (task.getDueDate() != null) {
                        dt.dueText = "Due " +
                            task.getDueDate()
                                .format(
                                DateTimeFormatter
                                .ofPattern(
                                    "MMM d"));
                    } else {
                        dt.dueText =
                            "Notify Later task";
                    }

                    result.add(dt);
                }
            }

        } catch (Exception e) {
            // log error
        }

        return result;
    }

    private Task mapTask(
            java.sql.ResultSet rs)
            throws java.sql.SQLException {

        Task task = new Task();
        task.setId(rs.getInt("id"));
        task.setTitle(
            rs.getString("title"));
        task.setDescription(
            rs.getString("description"));
        task.setTaskStatus(
            Task.TaskStatus.valueOf(
                rs.getString("task_status")));
        task.setPriority(
            Task.Priority.valueOf(
                rs.getString("priority")));
        task.setSection(
            Task.Section.valueOf(
                rs.getString("section")));

        String due = rs.getString("due_date");
        if (due != null) {
            task.setDueDate(
                LocalDate.parse(due));
        }
        task.setDueTime(
            rs.getString("due_time"));
        task.setDeleted(
            rs.getInt("is_deleted") == 1);
        return task;
    }

    private static class DueTask {
        Task   task;
        String urgencyLabel;
        String urgencyColor;
        String urgencyBg;
        String dueText;
    }
}