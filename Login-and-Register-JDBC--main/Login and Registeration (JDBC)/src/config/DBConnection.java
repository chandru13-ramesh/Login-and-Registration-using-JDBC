package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection - Singleton pattern for managing MySQL database connections.
 * Centralizes connection configuration so all layers share one source of truth.
 *
 * Architecture: config layer - no business logic, only infrastructure concern.
 */
public class DBConnection {

    // ── Database credentials ─────────────────────────────────────────────────
    private static final String URL      = "jdbc:mysql://localhost:3306/secure_user_db?useSSL=false&serverTimezone=UTC";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Kalai@123";

    // Private constructor — prevent instantiation
    private DBConnection() {}

    /**
     * Opens and returns a new JDBC Connection.
     * Callers are responsible for closing the connection (use try-with-resources).
     *
     * @return a live {@link Connection} to secure_user_db
     * @throws SQLException if the driver is missing or credentials are wrong
     */
    public static Connection getConnection() throws SQLException {
        // Modern JDBC (Java 6+) auto-discovers drivers via ServiceLoader — no Class.forName() needed.
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
