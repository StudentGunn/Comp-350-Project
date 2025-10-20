import java.nio.file.Path;
import java.sql.*;
import java.time.Instant;

/**
 * Simple SQLite-backed user database helper.
 */
public class UserDatabase {
    private final Path dbPath;
    private final String url;

    public UserDatabase(Path dbPath) {
        this.dbPath = dbPath;
        this.url = "jdbc:sqlite:" + dbPath.toAbsolutePath().toString();
    }

    public void init() throws SQLException {
        // explicit driver load to provide a clearer error if the JDBC driver jar is missing
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found on classpath. Please add the sqlite-jdbc jar to your classpath.", e);
        }
        try (Connection c = DriverManager.getConnection(url)) {
            try (Statement s = c.createStatement()) {
                s.executeUpdate("CREATE TABLE IF NOT EXISTS users ("
                        + "username TEXT PRIMARY KEY,"
                        + "password_hash TEXT NOT NULL,"
                        + "full_name TEXT,"
                        + "email TEXT,"
                        + "created_at INTEGER"
                        + ")");
            }
        }
    }

    public boolean register(String username, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users(username,password_hash,created_at) VALUES(?,?,?)";
        try (Connection c = DriverManager.getConnection(url)) {
            try (PreparedStatement p = c.prepareStatement(sql)) {
                p.setString(1, username);
                p.setString(2, passwordHash);
                p.setLong(3, Instant.now().getEpochSecond());
                p.executeUpdate();
                return true;
            }
        }
    }

    public boolean authenticate(String username, String passwordHash) throws SQLException {
        String sql = "SELECT password_hash FROM users WHERE username = ?";
        try (Connection c = DriverManager.getConnection(url)) {
            try (PreparedStatement p = c.prepareStatement(sql)) {
                p.setString(1, username);
                try (ResultSet rs = p.executeQuery()) {
                    if (rs.next()) {
                        String stored = rs.getString(1);
                        return stored != null && stored.equals(passwordHash);
                    }
                    return false;
                }
            }
        }
    }

    public boolean userExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection c = DriverManager.getConnection(url)) {
            try (PreparedStatement p = c.prepareStatement(sql)) {
                p.setString(1, username);
                try (ResultSet rs = p.executeQuery()) {
                    return rs.next();
                }
            }
        }
    }
}
