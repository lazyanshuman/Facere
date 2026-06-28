package com.habitflow.dao;

import com.habitflow.model.Habit;
import com.habitflow.service.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * HabitDAO — multi-user aware.
 * All queries filter by current user_id.
 */
public class HabitDAO {

    private static final Logger log =
        LoggerFactory.getLogger(HabitDAO.class);

    private final DatabaseManager db;

    public HabitDAO() {
        this.db = DatabaseManager.getInstance();
    }

    // ═══════════════════════════════════════
    // SAVE
    // ═══════════════════════════════════════

    public Habit save(Habit habit) {
        String sql = """
            INSERT INTO habits (
                title, description,
                recurrence_days, color_hex,
                habit_type, sort_order,
                user_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn =
                 db.getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql,
                     Statement
                     .RETURN_GENERATED_KEYS)) {

            ps.setString(1, habit.getTitle());
            ps.setString(2,
                habit.getDescription());
            ps.setString(3,
                habit.getRecurrenceDays());
            ps.setString(4,
                habit.getColorHex());
            ps.setString(5,
                habit.getHabitType().name());
            ps.setInt(6,
                habit.getSortOrder());
            ps.setInt(7,
                UserSession.getCurrentUserId());

            ps.executeUpdate();

            try (ResultSet keys =
                     ps.getGeneratedKeys()) {
                if (keys.next()) {
                    habit.setId(keys.getInt(1));
                    log.info(
                        "Saved habit: id={}" +
                        ", title='{}'",
                        habit.getId(),
                        habit.getTitle());
                }
            }

        } catch (SQLException e) {
            log.error(
                "Failed to save habit: {}",
                e.getMessage());
            throw new RuntimeException(
                "Could not save habit.", e);
        }

        return habit;
    }

    // ═══════════════════════════════════════
    // LOAD
    // ═══════════════════════════════════════

    public List<Habit> getAllActive() {
        String sql = """
            SELECT * FROM habits
            WHERE is_archived = 0
              AND user_id = ?
            ORDER BY sort_order ASC,
                     created_at ASC
            """;

        List<Habit> habits = new ArrayList<>();

        try (Connection conn =
                 db.getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {

            ps.setInt(1,
                UserSession.getCurrentUserId());

            try (ResultSet rs =
                     ps.executeQuery()) {
                while (rs.next()) {
                    habits.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            log.error(
                "Failed to load habits: {}",
                e.getMessage());
        }

        return habits;
    }

    // ═══════════════════════════════════════
    // HABIT LOGS
    // ═══════════════════════════════════════

    public void logToday(int habitId,
            Habit.LogStatus status) {

        String today =
            LocalDate.now().toString();

        String sql = """
            INSERT INTO habit_logs
                (habit_id, log_date, status)
            VALUES (?, ?, ?)
            ON CONFLICT(habit_id, log_date)
            DO UPDATE SET status = ?
            """;

        try (Connection conn =
                 db.getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {

            ps.setInt(1, habitId);
            ps.setString(2, today);
            ps.setString(3, status.name());
            ps.setString(4, status.name());

            ps.executeUpdate();

            log.info(
                "Logged habit {} as {} " +
                "for {}", habitId,
                status, today);

            updateStreak(habitId);

        } catch (SQLException e) {
            log.error(
                "Failed to log habit: {}",
                e.getMessage());
        }
    }

    public Habit.LogStatus getTodayStatus(
            int habitId) {

        String sql = """
            SELECT status FROM habit_logs
            WHERE habit_id = ?
              AND log_date = ?
            """;

        try (Connection conn =
                 db.getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {

            ps.setInt(1, habitId);
            ps.setString(2,
                LocalDate.now().toString());

            try (ResultSet rs =
                     ps.executeQuery()) {
                if (rs.next()) {
                    return Habit.LogStatus
                        .valueOf(
                            rs.getString(
                                "status"));
                }
            }

        } catch (SQLException e) {
            log.error(
                "Failed to get today " +
                "status: {}",
                e.getMessage());
        }

        return null;
    }

    // ═══════════════════════════════════════
    // STREAK
    // ═══════════════════════════════════════

    private void updateStreak(int habitId) {
        String sql = """
            SELECT log_date, status
            FROM habit_logs
            WHERE habit_id = ?
            ORDER BY log_date DESC
            """;

        try (Connection conn =
                 db.getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {

            ps.setInt(1, habitId);

            int streak = 0;
            LocalDate expected =
                LocalDate.now();

            try (ResultSet rs =
                     ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate logDate =
                        LocalDate.parse(
                            rs.getString(
                                "log_date"));
                    String status =
                        rs.getString("status");

                    if (logDate.equals(
                            expected) &&
                        status.equals("DONE")) {
                        streak++;
                        expected =
                            expected.minusDays(1);
                    } else {
                        break;
                    }
                }
            }

            String updateSql = """
                UPDATE habits
                SET current_streak = ?,
                    longest_streak = CASE
                        WHEN ? > longest_streak
                        THEN ?
                        ELSE longest_streak
                    END
                WHERE id = ?
                """;

            try (PreparedStatement ups =
                     conn.prepareStatement(
                         updateSql)) {
                ups.setInt(1, streak);
                ups.setInt(2, streak);
                ups.setInt(3, streak);
                ups.setInt(4, habitId);
                ups.executeUpdate();
            }

        } catch (SQLException e) {
            log.error(
                "Failed to update streak: {}",
                e.getMessage());
        }
    }

    // ═══════════════════════════════════════
    // DELETE
    // ═══════════════════════════════════════

    public void delete(int habitId) {
        String sql =
            "DELETE FROM habits WHERE id = ?";

        try (Connection conn =
                 db.getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {

            ps.setInt(1, habitId);
            ps.executeUpdate();

        } catch (SQLException e) {
            log.error(
                "Failed to delete habit: {}",
                e.getMessage());
        }
    }

    // ═══════════════════════════════════════
    // MAP ROW
    // ═══════════════════════════════════════

    private Habit mapRow(ResultSet rs)
            throws SQLException {

        Habit habit = new Habit();
        habit.setId(rs.getInt("id"));
        habit.setTitle(
            rs.getString("title"));
        habit.setDescription(
            rs.getString("description"));
        habit.setRecurrenceDays(
            rs.getString("recurrence_days"));
        habit.setColorHex(
            rs.getString("color_hex"));
        habit.setHabitType(
            Habit.HabitType.valueOf(
                rs.getString("habit_type")));
        habit.setCurrentStreak(
            rs.getInt("current_streak"));
        habit.setLongestStreak(
            rs.getInt("longest_streak"));
        habit.setArchived(
            rs.getInt("is_archived") == 1);
        habit.setSortOrder(
            rs.getInt("sort_order"));
        return habit;
    }
}