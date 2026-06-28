package com.habitflow.dao;

import com.habitflow.model.DoDont;
import com.habitflow.service.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DoDontDAO — multi-user aware.
 * All queries filter by current user_id.
 */
public class DoDontDAO {

    private static final Logger log =
        LoggerFactory.getLogger(
            DoDontDAO.class);

    private final DatabaseManager db;

    public DoDontDAO() {
        this.db = DatabaseManager.getInstance();
    }

    // ═══════════════════════════════════════
    // SAVE
    // ═══════════════════════════════════════

    public DoDont save(DoDont item) {
        String sql = """
            INSERT INTO dodoants (
                title, description,
                type, color_hex,
                sort_order, user_id
            ) VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn =
                 db.getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql,
                     Statement
                     .RETURN_GENERATED_KEYS)) {

            ps.setString(1, item.getTitle());
            ps.setString(2,
                item.getDescription());
            ps.setString(3,
                item.getType().name());
            ps.setString(4,
                item.getColorHex());
            ps.setInt(5,
                item.getSortOrder());
            ps.setInt(6,
                UserSession.getCurrentUserId());

            ps.executeUpdate();

            try (ResultSet keys =
                     ps.getGeneratedKeys()) {
                if (keys.next()) {
                    item.setId(keys.getInt(1));
                    log.info(
                        "Saved DoDont: " +
                        "id={}, title='{}'",
                        item.getId(),
                        item.getTitle());
                }
            }

        } catch (SQLException e) {
            log.error(
                "Failed to save DoDont: {}",
                e.getMessage());
            throw new RuntimeException(
                "Could not save item.", e);
        }

        return item;
    }

    // ═══════════════════════════════════════
    // LOAD
    // ═══════════════════════════════════════

    public List<DoDont> getAllDos() {
        return getByType(DoDont.Type.DO);
    }

    public List<DoDont> getAllDonts() {
        return getByType(DoDont.Type.DONT);
    }

    private List<DoDont> getByType(
            DoDont.Type type) {

        String sql = """
            SELECT * FROM dodoants
            WHERE type = ?
              AND is_archived = 0
              AND user_id = ?
            ORDER BY sort_order ASC,
                     created_at ASC
            """;

        List<DoDont> items = new ArrayList<>();

        try (Connection conn =
                 db.getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {

            ps.setString(1, type.name());
            ps.setInt(2,
                UserSession.getCurrentUserId());

            try (ResultSet rs =
                     ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            log.error(
                "Failed to load {} items: {}",
                type, e.getMessage());
        }

        return items;
    }

    // ═══════════════════════════════════════
    // DAILY LOGGING
    // ═══════════════════════════════════════

    public void logToday(int dodontId,
            DoDont.DailyStatus status) {

        String today =
            LocalDate.now().toString();

        String sql = """
            INSERT INTO dodont_logs
                (dodont_id, log_date, status)
            VALUES (?, ?, ?)
            ON CONFLICT(dodont_id, log_date)
            DO UPDATE SET status = ?
            """;

        try (Connection conn =
                 db.getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {

            ps.setInt(1, dodontId);
            ps.setString(2, today);
            ps.setString(3, status.name());
            ps.setString(4, status.name());

            ps.executeUpdate();

        } catch (SQLException e) {
            log.error(
                "Failed to log DoDont: {}",
                e.getMessage());
        }
    }

    public DoDont.DailyStatus getTodayStatus(
            int dodontId) {

        String sql = """
            SELECT status FROM dodont_logs
            WHERE dodont_id = ?
              AND log_date  = ?
            """;

        try (Connection conn =
                 db.getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {

            ps.setInt(1, dodontId);
            ps.setString(2,
                LocalDate.now().toString());

            try (ResultSet rs =
                     ps.executeQuery()) {
                if (rs.next()) {
                    return DoDont.DailyStatus
                        .valueOf(
                            rs.getString(
                                "status"));
                }
            }

        } catch (SQLException e) {
            log.error(
                "Failed to get today " +
                "DoDont status: {}",
                e.getMessage());
        }

        return null;
    }

    // ═══════════════════════════════════════
    // DELETE
    // ═══════════════════════════════════════

    public void delete(int dodontId) {
        String sql =
            "DELETE FROM dodoants " +
            "WHERE id = ?";

        try (Connection conn =
                 db.getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {

            ps.setInt(1, dodontId);
            ps.executeUpdate();

        } catch (SQLException e) {
            log.error(
                "Failed to delete DoDont: {}",
                e.getMessage());
        }
    }

    // ═══════════════════════════════════════
    // MAP ROW
    // ═══════════════════════════════════════

    private DoDont mapRow(ResultSet rs)
            throws SQLException {

        DoDont item = new DoDont();
        item.setId(rs.getInt("id"));
        item.setTitle(
            rs.getString("title"));
        item.setDescription(
            rs.getString("description"));
        item.setType(DoDont.Type.valueOf(
            rs.getString("type")));
        item.setColorHex(
            rs.getString("color_hex"));
        item.setSortOrder(
            rs.getInt("sort_order"));
        item.setArchived(
            rs.getInt("is_archived") == 1);
        return item;
    }
}