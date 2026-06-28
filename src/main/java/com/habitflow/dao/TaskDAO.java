package com.habitflow.dao;

import com.habitflow.model.Task;
import com.habitflow.service.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * TaskDAO — multi-user aware.
 * All queries filter by current user_id.
 */
public class TaskDAO {

    private static final Logger log =
        LoggerFactory.getLogger(TaskDAO.class);

    // ═══════════════════════════════════════
    // SAVE
    // ═══════════════════════════════════════

    public int save(Task task) {
        String sql = """
            INSERT INTO tasks
                (title, description, section,
                 priority, due_date, due_time,
                 category_id, user_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql,
                     Statement
                     .RETURN_GENERATED_KEYS)) {

            ps.setString(1, task.getTitle());
            ps.setString(2,
                task.getDescription());
            ps.setString(3,
                task.getSection().name());
            ps.setString(4,
                task.getPriority().name());
            ps.setString(5,
                task.getDueDate() != null
                    ? task.getDueDate()
                        .toString()
                    : null);
            ps.setString(6,
                task.getDueTime());
            if (task.getCategoryId() > 0) {
                ps.setInt(7,
                    task.getCategoryId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setInt(8,
                UserSession.getCurrentUserId());

            ps.executeUpdate();

            try (ResultSet rs =
                     ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    task.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            log.error("Failed to save task: {}",
                e.getMessage());
        }
        return -1;
    }

    // ═══════════════════════════════════════
    // GET BY SECTION
    // ═══════════════════════════════════════

    public List<Task> getBySection(
            Task.Section section) {

        // DELETED section uses special query
        if (section == Task.Section.DELETED) {
            return getDeleted();
        }

        String sql = """
            SELECT * FROM tasks
            WHERE section = ?
              AND is_deleted = 0
              AND user_id = ?
            ORDER BY sort_order ASC,
                     created_at DESC
            """;

        List<Task> tasks = new ArrayList<>();

        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {

            ps.setString(1, section.name());
            ps.setInt(2,
                UserSession.getCurrentUserId());

            try (ResultSet rs =
                     ps.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error(
                "Failed to get tasks: {}",
                e.getMessage());
        }
        return tasks;
    }

    private List<Task> getDeleted() {
        String sql = """
            SELECT * FROM tasks
            WHERE is_deleted = 1
              AND user_id = ?
            ORDER BY deleted_at DESC
            """;

        List<Task> tasks = new ArrayList<>();

        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {

            ps.setInt(1,
                UserSession.getCurrentUserId());

            try (ResultSet rs =
                     ps.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error(
                "Failed to get deleted: {}",
                e.getMessage());
        }
        return tasks;
    }

    // ═══════════════════════════════════════
    // GET OVERDUE PENDING TASKS
    // ═══════════════════════════════════════

    public List<Task> getOverduePendingTasks() {
        String sql = """
            SELECT * FROM tasks
            WHERE task_status = 'PENDING'
              AND is_deleted = 0
              AND user_id = ?
              AND due_date IS NOT NULL
              AND due_date < ?
            ORDER BY due_date ASC
            """;

        List<Task> tasks = new ArrayList<>();

        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {

            ps.setInt(1,
                UserSession.getCurrentUserId());
            ps.setString(2,
                LocalDate.now().toString());

            try (ResultSet rs =
                     ps.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            log.error(
                "Failed to get overdue: {}",
                e.getMessage());
        }
        return tasks;
    }

    // ═══════════════════════════════════════
    // UPDATE STATUS
    // ═══════════════════════════════════════

    public void updateStatus(int taskId,
            Task.TaskStatus status) {

        String newSection;
        if (status == Task.TaskStatus.DONE) {
            newSection = "COMPLETED";
        } else if (status ==
                Task.TaskStatus.DELAYED) {
            newSection = "DELAYED";
        } else {
            newSection = "MY_DAY";
        }

        String sql = """
            UPDATE tasks SET
                task_status = ?,
                section = ?
            WHERE id = ?
            """;

        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setString(2, newSection);
            ps.setInt(3, taskId);
            ps.executeUpdate();

        } catch (SQLException e) {
            log.error(
                "Failed to update status: {}",
                e.getMessage());
        }
    }

    // ═══════════════════════════════════════
    // SOFT DELETE
    // ═══════════════════════════════════════

    public void softDelete(int taskId) {
        String sql = """
            UPDATE tasks SET
                is_deleted = 1,
                deleted_at = datetime('now'),
                section = 'DELETED'
            WHERE id = ?
            """;

        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {

            ps.setInt(1, taskId);
            ps.executeUpdate();

        } catch (SQLException e) {
            log.error(
                "Failed to soft delete: {}",
                e.getMessage());
        }
    }

    // ═══════════════════════════════════════
    // HARD DELETE
    // ═══════════════════════════════════════

    public void hardDelete(int taskId) {
        String sql =
            "DELETE FROM tasks WHERE id = ?";

        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {

            ps.setInt(1, taskId);
            ps.executeUpdate();

        } catch (SQLException e) {
            log.error(
                "Failed to hard delete: {}",
                e.getMessage());
        }
    }

    // ═══════════════════════════════════════
    // RESTORE TASK
    // ═══════════════════════════════════════

    public void restoreTask(int taskId) {
        String sql = """
            UPDATE tasks SET
                is_deleted = 0,
                deleted_at = NULL,
                section = 'MY_DAY',
                task_status = 'PENDING'
            WHERE id = ?
            """;

        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {

            ps.setInt(1, taskId);
            ps.executeUpdate();

        } catch (SQLException e) {
            log.error(
                "Failed to restore task: {}",
                e.getMessage());
        }
    }

    // ═══════════════════════════════════════
    // CLEAR OPERATIONS
    // ═══════════════════════════════════════

    public void clearDeleted() {
        String sql =
            "DELETE FROM tasks " +
            "WHERE is_deleted = 1 " +
            "AND user_id = ?";

        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {

            ps.setInt(1,
                UserSession.getCurrentUserId());
            ps.executeUpdate();

        } catch (SQLException e) {
            log.error(
                "Failed to clear deleted: {}",
                e.getMessage());
        }
    }

    public void clearCompleted() {
        String sql =
            "DELETE FROM tasks " +
            "WHERE section = 'COMPLETED' " +
            "AND user_id = ?";

        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {

            ps.setInt(1,
                UserSession.getCurrentUserId());
            ps.executeUpdate();

        } catch (SQLException e) {
            log.error(
                "Failed to clear completed: {}",
                e.getMessage());
        }
    }

    public void clearDelayed() {
        String sql =
            "DELETE FROM tasks " +
            "WHERE section = 'DELAYED' " +
            "AND user_id = ?";

        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {

            ps.setInt(1,
                UserSession.getCurrentUserId());
            ps.executeUpdate();

        } catch (SQLException e) {
            log.error(
                "Failed to clear delayed: {}",
                e.getMessage());
        }
    }

    // ═══════════════════════════════════════
    // COUNT BY SECTION
    // ═══════════════════════════════════════

    public int countBySection(
            Task.Section section) {
        String sql;
        if (section == Task.Section.DELETED) {
            sql = "SELECT COUNT(*) AS c " +
                "FROM tasks " +
                "WHERE is_deleted = 1 " +
                "AND user_id = ?";
        } else {
            sql = "SELECT COUNT(*) AS c " +
                "FROM tasks " +
                "WHERE section = '" +
                section.name() + "' " +
                "AND is_deleted = 0 " +
                "AND user_id = ?";
        }

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
                    return rs.getInt("c");
                }
            }
        } catch (SQLException e) {
            log.error(
                "Failed to count tasks: {}",
                e.getMessage());
        }
        return 0;
    }

    // ═══════════════════════════════════════
    // MAP ROW
    // ═══════════════════════════════════════

    @SuppressWarnings("unused")
    private Task mapRow(ResultSet rs)
            throws SQLException {

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

        String dueDate =
            rs.getString("due_date");
        if (dueDate != null) {
            task.setDueDate(
                LocalDate.parse(dueDate));
        }
        task.setDueTime(
            rs.getString("due_time"));

        task.setRolledOver(
            rs.getInt("is_rolled_over") == 1);
        task.setRolloverCount(
            rs.getInt("rollover_count"));
        task.setDeleted(
            rs.getInt("is_deleted") == 1);

        try {
            task.setCategoryId(
                rs.getInt("category_id"));
        } catch (SQLException e) {
            // column may not exist
        }

        return task;
    }
}