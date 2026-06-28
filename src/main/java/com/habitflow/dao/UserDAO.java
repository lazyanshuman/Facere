package com.habitflow.dao;

import com.habitflow.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO — CRUD for users table.
 * Max 3 users allowed.
 */
public class UserDAO {

    private static final Logger log =
        LoggerFactory.getLogger(UserDAO.class);

    public static final int MAX_USERS = 3;

    /**
     * Saves a new user. Returns the
     * generated id, or -1 on failure.
     */
    public int save(User user) {
        String sql = """
            INSERT INTO users
                (full_name, username,
                 avatar_color, accent_color,
                 pin_hash, sort_order)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql,
                     Statement
                     .RETURN_GENERATED_KEYS)) {

            ps.setString(1,
                user.getFullName());
            ps.setString(2,
                user.getUsername());
            ps.setString(3,
                user.getAvatarColor());
            ps.setString(4,
                user.getAccentColor());
            ps.setString(5,
                user.getPinHash());
            ps.setInt(6,
                user.getSortOrder());
            ps.executeUpdate();

            try (ResultSet rs =
                     ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    user.setId(id);
                    log.info("User saved: {} (id={})",
                        user.getFullName(), id);
                    return id;
                }
            }
        } catch (SQLException e) {
            log.error("Failed to save user: {}",
                e.getMessage());
        }
        return -1;
    }

    /**
     * Gets all active users ordered by
     * sort_order.
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = """
            SELECT * FROM users
            WHERE is_active = 1
            ORDER BY sort_order ASC
            """;

        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql);
             ResultSet rs =
                 ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("Failed to get users: {}",
                e.getMessage());
        }
        return users;
    }

    /**
     * Gets a user by id.
     */
    public User getById(int id) {
        String sql =
            "SELECT * FROM users WHERE id = ?";

        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs =
                     ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            log.error("Failed to get user: {}",
                e.getMessage());
        }
        return null;
    }

    /**
     * Updates a user's profile info.
     */
    public void update(User user) {
        String sql = """
            UPDATE users SET
                full_name = ?,
                username = ?,
                avatar_color = ?,
                accent_color = ?,
                pin_hash = ?
            WHERE id = ?
            """;

        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {

            ps.setString(1,
                user.getFullName());
            ps.setString(2,
                user.getUsername());
            ps.setString(3,
                user.getAvatarColor());
            ps.setString(4,
                user.getAccentColor());
            ps.setString(5,
                user.getPinHash());
            ps.setInt(6, user.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            log.error("Failed to update user: {}",
                e.getMessage());
        }
    }

    /**
     * Deletes a user and ALL their data.
     */
    public void deleteUser(int userId) {
        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection()) {

            conn.setAutoCommit(false);
            try (Statement st =
                     conn.createStatement()) {

                // Delete user's tasks
                st.execute(
                    "DELETE FROM tasks " +
                    "WHERE user_id = " + userId);

                // Delete user's habits + logs
                st.execute(
                    "DELETE FROM habit_logs " +
                    "WHERE habit_id IN " +
                    "(SELECT id FROM habits " +
                    "WHERE user_id = " +
                    userId + ")");
                st.execute(
                    "DELETE FROM habits " +
                    "WHERE user_id = " + userId);

                // Delete user's dos/don'ts
                st.execute(
                    "DELETE FROM dodont_logs " +
                    "WHERE dodont_id IN " +
                    "(SELECT id FROM dodoants " +
                    "WHERE user_id = " +
                    userId + ")");
                st.execute(
                    "DELETE FROM dodoants " +
                    "WHERE user_id = " + userId);

                // Delete shared task links
                st.execute(
                    "DELETE FROM shared_tasks " +
                    "WHERE shared_by_user_id = " +
                    userId +
                    " OR shared_with_user_id = " +
                    userId);

                // Delete the user
                st.execute(
                    "DELETE FROM users " +
                    "WHERE id = " + userId);

                conn.commit();
                log.info(
                    "User {} deleted with " +
                    "all data.", userId);

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            log.error(
                "Failed to delete user: {}",
                e.getMessage());
        }
    }

    /**
     * Returns the number of active users.
     */
    public int getUserCount() {
        String sql =
            "SELECT COUNT(*) AS c " +
            "FROM users WHERE is_active = 1";

        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql);
             ResultSet rs =
                 ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("c");
            }
        } catch (SQLException e) {
            log.error("Failed to count users: {}",
                e.getMessage());
        }
        return 0;
    }

    /**
     * Checks if a username is already taken.
     */
    public boolean usernameExists(
            String username) {
        String sql =
            "SELECT COUNT(*) AS c FROM users " +
            "WHERE username = ? COLLATE NOCASE";

        try (Connection conn =
                 DatabaseManager.getInstance()
                     .getConnection();
             PreparedStatement ps =
                 conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs =
                     ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("c") > 0;
                }
            }
        } catch (SQLException e) {
            log.error("Username check failed: {}",
                e.getMessage());
        }
        return false;
    }

    private User mapRow(ResultSet rs)
            throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setFullName(
            rs.getString("full_name"));
        u.setUsername(
            rs.getString("username"));
        u.setAvatarColor(
            rs.getString("avatar_color"));
        u.setAccentColor(
            rs.getString("accent_color"));
        u.setPinHash(
            rs.getString("pin_hash"));
        u.setActive(
            rs.getInt("is_active") == 1);
        u.setSortOrder(
            rs.getInt("sort_order"));
        return u;
    }
}