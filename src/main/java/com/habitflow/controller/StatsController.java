package com.habitflow.controller;

import com.habitflow.dao.DatabaseManager;
import com.habitflow.service.ThemeManager;
import com.habitflow.service.UserSession;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.habitflow.util.AnimationHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * StatsController — theme-aware, multi-user.
 * All queries filter by current user_id.
 */
public class StatsController {

    private static final String ACCENT = "#6C63FF";
    private static final String GREEN  = "#10B981";
    private static final String AMBER  = "#F59E0B";
    private static final String RED    = "#EF4444";

    private String currentRange = "This Week";

    private boolean dark;
    private String cardBg;
    private String cardBorder;
    private String textDark;
    private String textGray;
    private String chartGrid;
    private String chartAxisText;

    public ScrollPane buildStatsScreen() {
        setupThemeColors();

        VBox root = new VBox(20);
        root.setPadding(new Insets(4, 4, 20, 4));

        root.getChildren().add(
            buildRangeSelector(root));

        VBox body = new VBox(20);
        body.setId("statsBody");
        rebuildBody(body);
        root.getChildren().add(body);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle(
            "-fx-background: transparent;" +
            "-fx-background-color: transparent;");
        return scroll;
    }

    private void setupThemeColors() {
        ThemeManager tm =
            ThemeManager.getInstance();
        dark =
            tm.getCurrentMode() ==
                ThemeManager.ThemeMode.DARK ||
            (tm.getCurrentMode() ==
                ThemeManager.ThemeMode.AUTO_TIME
                && isNightTime());

        if (dark) {
            cardBg        = "#1A1A2E";
            cardBorder    = "#2E2E45";
            textDark      = "#E8E8F0";
            textGray      = "#9090B8";
            chartGrid     = "#2E2E45";
            chartAxisText = "#A0A0B8";
        } else {
            cardBg        = "#FFFFFF";
            cardBorder    = "#EEEEF5";
            textDark      = "#1A1A2E";
            textGray      = "#6B7280";
            chartGrid     = "#F0F0F5";
            chartAxisText = "#6B7280";
        }
    }

    private boolean isNightTime() {
        int h = java.time.LocalTime
            .now().getHour();
        return h < 6 || h >= 19;
    }

    private HBox buildRangeSelector(VBox root) {
        Label label = new Label(
            "Showing stats for:");
        label.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: " + textGray + ";");

        ComboBox<String> rangeBox =
            new ComboBox<>();
        rangeBox.getItems().addAll(
            "Today", "This Week",
            "This Month", "All Time");
        rangeBox.setValue(currentRange);
        rangeBox.setStyle(
            "-fx-background-radius: 8px;" +
            "-fx-font-size: 13px;");

        rangeBox.setOnAction(e -> {
            currentRange = rangeBox.getValue();
            VBox body =
                (VBox) root.lookup("#statsBody");
            if (body != null) {
                body.getChildren().clear();
                rebuildBody(body);
            }
        });

        HBox row = new HBox(12, label, rangeBox);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void rebuildBody(VBox body) {
        int days = rangeToDays();
        TaskStats taskStats = getTaskStats(days);

        // ── Number cards ─────────────────
        HBox cards = new HBox(16);
        cards.setAlignment(Pos.CENTER_LEFT);

        int rate = taskStats.total == 0 ? 0 :
            Math.round(
                (taskStats.done * 100f)
                / taskStats.total);

        cards.getChildren().addAll(
            numberCard(rate + "%",
                "Completion Rate", ACCENT,
                dark ? "#2D2B4E" : "#EDE9FE"),
            numberCard(
                String.valueOf(taskStats.done),
                "Tasks Done", GREEN,
                dark ? "#0D2818" : "#D1FAE5"),
            numberCard(
                String.valueOf(taskStats.pending),
                "Pending", AMBER,
                dark ? "#2D2410" : "#FEF3C7"),
            numberCard(
                "🔥 " + getBestStreak(),
                "Best Streak", RED,
                dark ? "#2D1414" : "#FEE2E2"));

        body.getChildren().add(cards);

        // Animate number cards
        AnimationHelper.fadeSlideIn(
            cards, 400, 15);

        // ── Charts row 1 ─────────────────
        HBox chartRow1 = new HBox(16);
        VBox donutCard = chartCard(
            "Task Completion",
            buildDonut(taskStats));
        HBox.setHgrow(donutCard, Priority.ALWAYS);
        VBox lineCard = chartCard(
            "Tasks Completed Over Time",
            buildLineChart(days));
        HBox.setHgrow(lineCard, Priority.ALWAYS);
        chartRow1.getChildren().addAll(
            donutCard, lineCard);
        body.getChildren().add(chartRow1);

        // Animate charts row 1
        AnimationHelper.chartEntrance(
            chartRow1, 200);

        // ── Charts row 2 ─────────────────
        HBox chartRow2 = new HBox(16);
        VBox habitCard = chartCard(
            "Habit Success Rate",
            buildHabitChart(days));
        HBox.setHgrow(habitCard, Priority.ALWAYS);
        VBox dodontCard = chartCard(
            "Dos / Don'ts Success",
            buildDoDontChart(days));
        HBox.setHgrow(dodontCard, Priority.ALWAYS);
        chartRow2.getChildren().addAll(
            habitCard, dodontCard);
        body.getChildren().add(chartRow2);

        // Animate charts row 2
        AnimationHelper.chartEntrance(
            chartRow2, 400);
    }

    // ═══════════════════════════════════════
    // NUMBER CARDS
    // ═══════════════════════════════════════

    private VBox numberCard(
            String value, String label,
            String textColor, String bgColor) {

        Label valueLabel = new Label(value);
        valueLabel.setStyle(
            "-fx-font-size: 30px;" +
            "-fx-font-weight: 800;" +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-font-family: 'Segoe UI';");

        Label nameLabel = new Label(label);
        nameLabel.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-font-weight: 600;" +
            "-fx-text-fill: " + textGray + ";" +
            "-fx-font-family: 'Segoe UI';");

        VBox card = new VBox(8,
            valueLabel, nameLabel);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(
            new Insets(20, 24, 20, 24));
        card.setPrefWidth(200);
        card.setStyle(
            "-fx-background-color: " +
            bgColor + ";" +
            "-fx-background-radius: 16px;");
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    // ═══════════════════════════════════════
    // CHART CARD WRAPPER
    // ═══════════════════════════════════════

    private VBox chartCard(
            String title,
            javafx.scene.Node chart) {

        Label titleLabel = new Label(title);
        titleLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: 700;" +
            "-fx-text-fill: " + textDark + ";" +
            "-fx-font-family: 'Segoe UI';");

        VBox card = new VBox(14,
            titleLabel, chart);
        card.setPadding(new Insets(20));
        card.setPrefWidth(420);
        card.setStyle(
            "-fx-background-color: " +
            cardBg + ";" +
            "-fx-background-radius: 16px;" +
            "-fx-border-color: " +
            cardBorder + ";" +
            "-fx-border-radius: 16px;" +
            "-fx-border-width: 1px;");
        return card;
    }

    private void styleChart(Chart chart) {
        chart.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " +
            chartAxisText + ";");
        chart.lookupAll(".axis").forEach(n ->
            n.setStyle(
                "-fx-tick-label-fill: " +
                chartAxisText + ";" +
                "-fx-font-size: 10px;"));
        chart.lookupAll(
                ".chart-vertical-grid-lines")
            .forEach(n -> n.setStyle(
                "-fx-stroke: " +
                chartGrid + ";"));
        chart.lookupAll(
                ".chart-horizontal-grid-lines")
            .forEach(n -> n.setStyle(
                "-fx-stroke: " +
                chartGrid + ";"));
    }

    // ═══════════════════════════════════════
    // DONUT CHART
    // ═══════════════════════════════════════

    private javafx.scene.Node buildDonut(
            TaskStats stats) {

        if (stats.total == 0) {
            return emptyChart(
                "No tasks in this period");
        }

        PieChart pie = new PieChart();
        pie.setLabelsVisible(false);
        pie.setLegendVisible(true);
        pie.setLegendSide(Side.BOTTOM);
        pie.setPrefHeight(240);
        pie.setMinHeight(240);
        pie.setStyle(
            "-fx-background-color: transparent;");

        PieChart.Data done = new PieChart.Data(
            "Done (" + stats.done + ")",
            stats.done);
        PieChart.Data pending =
            new PieChart.Data(
                "Pending (" + stats.pending + ")",
                stats.pending);
        PieChart.Data delayed =
            new PieChart.Data(
                "Delayed (" + stats.delayed + ")",
                stats.delayed);

        pie.getData().addAll(
            done, pending, delayed);

        javafx.application.Platform.runLater(() -> {
            applySliceColor(done, GREEN);
            applySliceColor(pending, AMBER);
            applySliceColor(delayed, RED);
            pie.lookupAll(".chart-legend-item")
                .forEach(n -> n.setStyle(
                    "-fx-text-fill: " +
                    textGray + ";"));
            pie.lookupAll(".chart-legend")
                .forEach(n -> n.setStyle(
                    "-fx-background-color: " +
                    "transparent;"));
        });

        return pie;
    }

    private void applySliceColor(
            PieChart.Data data, String color) {
        if (data.getNode() != null) {
            data.getNode().setStyle(
                "-fx-pie-color: " + color + ";");
        }
    }

    // ═══════════════════════════════════════
    // LINE CHART
    // ═══════════════════════════════════════

    private javafx.scene.Node buildLineChart(
            int days) {

        CategoryAxis xAxis =
            new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        LineChart<String, Number> chart =
            new LineChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setPrefHeight(240);
        chart.setCreateSymbols(true);
        chart.setAnimated(false);

        XYChart.Series<String, Number> series =
            new XYChart.Series<>();
        Map<String, Integer> data =
            getTasksPerDay(days);
        for (Map.Entry<String, Integer> e
                : data.entrySet()) {
            series.getData().add(
                new XYChart.Data<>(
                    e.getKey(), e.getValue()));
        }
        chart.getData().add(series);

        javafx.application.Platform.runLater(() -> {
            styleChart(chart);
            javafx.scene.Node line =
                series.getNode().lookup(
                    ".chart-series-line");
            if (line != null) {
                line.setStyle(
                    "-fx-stroke: " + ACCENT + ";" +
                    "-fx-stroke-width: 2.5px;");
            }
            for (XYChart.Data<String, Number> d
                    : series.getData()) {
                if (d.getNode() != null) {
                    d.getNode()
                        .setMouseTransparent(true);
                    d.getNode().setStyle(
                        "-fx-background-color: " +
                        ACCENT + ", white;");
                }
            }
        });

        return chart;
    }

    // ═══════════════════════════════════════
    // BAR CHARTS
    // ═══════════════════════════════════════

    private javafx.scene.Node buildHabitChart(
            int days) {
        List<NamePercent> habits =
            getHabitSuccessRates(days);
        if (habits.isEmpty()) {
            return emptyChart("No habits tracked");
        }
        return buildBarChart(habits, ACCENT);
    }

    private javafx.scene.Node buildDoDontChart(
            int days) {
        List<NamePercent> items =
            getDoDontSuccessRates(days);
        if (items.isEmpty()) {
            return emptyChart(
                "No dos/don'ts tracked");
        }
        return buildBarChart(items, GREEN);
    }

    private javafx.scene.Node buildBarChart(
            List<NamePercent> data,
            String barColor) {

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(
            0, 100, 20);

        BarChart<String, Number> chart =
            new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setPrefHeight(240);
        chart.setAnimated(false);
        chart.setBarGap(2);
        chart.setCategoryGap(14);

        XYChart.Series<String, Number> series =
            new XYChart.Series<>();
        for (NamePercent np : data) {
            series.getData().add(
                new XYChart.Data<>(
                    np.name, np.percent));
        }
        chart.getData().add(series);

        javafx.application.Platform.runLater(() -> {
            styleChart(chart);
            for (XYChart.Data<String, Number> d
                    : series.getData()) {
                if (d.getNode() != null) {
                    d.getNode().setStyle(
                        "-fx-bar-fill: " +
                        barColor + ";");
                    d.getNode()
                        .setMouseTransparent(true);
                }
            }
        });

        return chart;
    }

    // ═══════════════════════════════════════
    // EMPTY CHART
    // ═══════════════════════════════════════

    private javafx.scene.Node emptyChart(
            String msg) {
        Label label = new Label(msg);
        label.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: " + textGray + ";");
        VBox box = new VBox(label);
        box.setAlignment(Pos.CENTER);
        box.setPrefHeight(240);
        return box;
    }

    // ═══════════════════════════════════════
    // DATA QUERIES (multi-user filtered)
    // ═══════════════════════════════════════

    private int rangeToDays() {
        return switch (currentRange) {
            case "Today" -> 1;
            case "This Week" -> 7;
            case "This Month" -> 30;
            default -> 3650;
        };
    }

    private TaskStats getTaskStats(int days) {
        TaskStats stats = new TaskStats();
        String cutoff = LocalDate.now()
            .minusDays(days - 1).toString();
        String sql = """
            SELECT
              SUM(CASE WHEN task_status='DONE'
                  THEN 1 ELSE 0 END) as done,
              SUM(CASE WHEN task_status='PENDING'
                  THEN 1 ELSE 0 END) as pending,
              SUM(CASE WHEN task_status='DELAYED'
                  THEN 1 ELSE 0 END) as delayed,
              COUNT(*) as total
            FROM tasks
            WHERE is_deleted = 0
              AND date(created_at) >= date(?)
              AND user_id = ?
            """;
        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {
            ps.setString(1, cutoff);
            ps.setInt(2,
                UserSession.getCurrentUserId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats.done = rs.getInt("done");
                    stats.pending =
                        rs.getInt("pending");
                    stats.delayed =
                        rs.getInt("delayed");
                    stats.total =
                        rs.getInt("total");
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return stats;
    }

    private Map<String, Integer> getTasksPerDay(
            int days) {
        Map<String, Integer> result =
            new LinkedHashMap<>();
        int showDays = Math.min(days, 14);
        for (int i = showDays - 1; i >= 0; i--) {
            LocalDate d =
                LocalDate.now().minusDays(i);
            result.put(d.format(
                DateTimeFormatter
                    .ofPattern("MMM d")), 0);
        }
        String cutoff = LocalDate.now()
            .minusDays(showDays - 1).toString();
        String sql = """
            SELECT date(completed_at) as day,
                   COUNT(*) as cnt
            FROM tasks
            WHERE task_status = 'DONE'
              AND completed_at IS NOT NULL
              AND date(completed_at) >= date(?)
              AND user_id = ?
            GROUP BY date(completed_at)
            """;
        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {
            ps.setString(1, cutoff);
            ps.setInt(2,
                UserSession.getCurrentUserId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String day =
                        rs.getString("day");
                    if (day != null) {
                        LocalDate d =
                            LocalDate.parse(day);
                        String key = d.format(
                            DateTimeFormatter
                            .ofPattern("MMM d"));
                        if (result.containsKey(
                                key)) {
                            result.put(key,
                                rs.getInt("cnt"));
                        }
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return result;
    }

    private List<NamePercent>
            getHabitSuccessRates(int days) {
        List<NamePercent> result =
            new ArrayList<>();
        String cutoff = LocalDate.now()
            .minusDays(days - 1).toString();
        String sql = """
            SELECT h.title,
              SUM(CASE WHEN l.status='DONE'
                  THEN 1 ELSE 0 END) as done,
              COUNT(l.id) as total
            FROM habits h
            LEFT JOIN habit_logs l
              ON h.id = l.habit_id
              AND date(l.log_date) >= date(?)
            WHERE h.is_archived = 0
              AND h.user_id = ?
            GROUP BY h.id
            """;
        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {
            ps.setString(1, cutoff);
            ps.setInt(2,
                UserSession.getCurrentUserId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int total =
                        rs.getInt("total");
                    int done = rs.getInt("done");
                    int pct = total == 0 ? 0 :
                        Math.round(
                            (done * 100f) / total);
                    String name =
                        rs.getString("title");
                    if (name.length() > 10) {
                        name = name.substring(
                            0, 9) + "…";
                    }
                    result.add(new NamePercent(
                        name, pct));
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return result;
    }

    private List<NamePercent>
            getDoDontSuccessRates(int days) {
        List<NamePercent> result =
            new ArrayList<>();
        String cutoff = LocalDate.now()
            .minusDays(days - 1).toString();
        String sql = """
            SELECT d.title,
              SUM(CASE WHEN l.status='SUCCESS'
                  THEN 1 ELSE 0 END) as success,
              COUNT(l.id) as total
            FROM dodoants d
            LEFT JOIN dodont_logs l
              ON d.id = l.dodont_id
              AND date(l.log_date) >= date(?)
            WHERE d.is_archived = 0
              AND d.user_id = ?
            GROUP BY d.id
            """;
        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {
            ps.setString(1, cutoff);
            ps.setInt(2,
                UserSession.getCurrentUserId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int total =
                        rs.getInt("total");
                    int success =
                        rs.getInt("success");
                    int pct = total == 0 ? 0 :
                        Math.round(
                            (success * 100f)
                            / total);
                    String name =
                        rs.getString("title");
                    if (name.length() > 10) {
                        name = name.substring(
                            0, 9) + "…";
                    }
                    result.add(new NamePercent(
                        name, pct));
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return result;
    }

    private int getBestStreak() {
        String sql = """
            SELECT MAX(current_streak) as best
            FROM habits
            WHERE is_archived = 0
              AND user_id = ?
            """;
        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {
            ps.setInt(1,
                UserSession.getCurrentUserId());
            try (ResultSet rs =
                     ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("best");
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return 0;
    }

    // ═══════════════════════════════════════
    // DATA CLASSES
    // ═══════════════════════════════════════

    private static class TaskStats {
        int done;
        int pending;
        int delayed;
        int total;
    }

    private static class NamePercent {
        String name;
        int    percent;
        NamePercent(String name, int percent) {
            this.name = name;
            this.percent = percent;
        }
    }
}