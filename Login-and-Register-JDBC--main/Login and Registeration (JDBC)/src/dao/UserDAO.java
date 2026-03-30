package dao;

import config.DBConnection;
import model.User;

import java.sql.*;
import java.util.Optional;

/**
 * UserDAO - Data Access Object for the 'users' table.
 *
 * Architecture: DAO layer — ONLY responsible for SQL execution.
 * All PreparedStatements protect against SQL injection.
 * No business logic lives here (validation/hashing is handled by ServiceLayer).
 */
public class UserDAO {

    // ── SQL constants ────────────────────────────────────────────────────────

    private static final String INSERT_USER =
        "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";

    private static final String FIND_BY_USERNAME =
        "SELECT id, username, email, password FROM users WHERE username = ?";

    private static final String FIND_BY_EMAIL =
        "SELECT id, username, email, password FROM users WHERE email = ?";

    private static final String USERNAME_EXISTS =
        "SELECT COUNT(*) FROM users WHERE username = ?";

    private static final String EMAIL_EXISTS =
        "SELECT COUNT(*) FROM users WHERE email = ?";

    // ── Public DAO Methods ───────────────────────────────────────────────────

    /**
     * Persists a new User to the database.
     *
     * @param user User object (password must already be BCrypt-hashed)
     * @return true if exactly one row was inserted
     * @throws SQLException on connection or constraint failure
     */
    public boolean registerUser(User user) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_USER)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword()); // already hashed

            int rowsAffected = ps.executeUpdate();
            return rowsAffected == 1;
        }
    }

    /**
     * Fetches a user by their username (used during login).
     *
     * @param username the username to look up
     * @return an Optional wrapping the User if found, empty otherwise
     * @throws SQLException on connection failure
     */
    public Optional<User> findByUsername(String username) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_USERNAME)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password")
                    );
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Fetches a user by their email address.
     *
     * @param email the email to look up
     * @return an Optional wrapping the User if found, empty otherwise
     * @throws SQLException on connection failure
     */
    public Optional<User> findByEmail(String email) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_EMAIL)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password")
                    );
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Checks if a username already exists (for duplicate registration guard).
     *
     * @param username the username to check
     * @return true if the username is already taken
     * @throws SQLException on connection failure
     */
    public boolean usernameExists(String username) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(USERNAME_EXISTS)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Checks if an email already exists (for duplicate registration guard).
     *
     * @param email the email to check
     * @return true if the email is already registered
     * @throws SQLException on connection failure
     */
    public boolean emailExists(String email) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(EMAIL_EXISTS)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
