import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;

/**
 * SQLite-backed user database helper
 */
public class UserDataBase {
    private final Path dbPath;
    private final String url;

    public UserDataBase(Path dbPath) {
        this.dbPath = dbPath;
        this.url = "jdbc:sqlite:" + dbPath.toAbsolutePath().toString();
    }

    public String getConnectionUrl() {
        return url;
    }

    /** Create the users table if it doesn't exist. 
     * creates method init
     * trys to connect to the database
     * creates the users table with the following columns: username, password_hash, full_name, email, created_at
     * if the table already exists, it does nothing
     * throws SQLException if there is an error connecting to the database or creating the table
     * returns void
    */
    public void init() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found on classpath. Please add the sqlite-jdbc jar to your classpath.", e);
        }

            try (Connection c = DriverManager.getConnection(url);
                 Statement s = c.createStatement()) {
            
                // Check for necessary column updates
                boolean needsUpdate = false;
                boolean needsAdminHash = false;
                
                // Check if user_type exists
                try (ResultSet rs = s.executeQuery("SELECT user_type FROM users LIMIT 1")) {
                    // If this succeeds, the column exists
                } catch (SQLException ex) {
                    needsUpdate = true;
                }

                // Check if admin_hash exists
                try (ResultSet rs = s.executeQuery("SELECT admin_hash FROM users LIMIT 1")) {
                    // If this succeeds, the column exists
                } catch (SQLException ex) {
                    needsAdminHash = true;
                }

                if (needsUpdate) {
                    // Drop and recreate the table with new structure
                    s.executeUpdate("DROP TABLE IF EXISTS users");
                } else if (needsAdminHash) {
                    // Add admin_hash column to existing table
                    try {
                        s.executeUpdate("ALTER TABLE users ADD COLUMN admin_hash TEXT");
                    } catch (SQLException ex) {
                        // Column might have been added by another process, ignore
                    }
                }

                // Enable foreign key support
                s.executeUpdate("PRAGMA foreign_keys = ON");

                // Create users table with all fields
                s.executeUpdate("CREATE TABLE IF NOT EXISTS users ("
                        + "username TEXT PRIMARY KEY,"
                        + "password_hash TEXT NOT NULL,"
                        + "user_type TEXT NOT NULL,"  // 'CUSTOMER', 'DRIVER', or 'ADMIN'
                        + "full_name TEXT NOT NULL,"
                        + "email TEXT UNIQUE,"
                        + "phone TEXT,"
                        + "created_at INTEGER NOT NULL,"
                        + "admin_hash TEXT,"
                        + "last_login INTEGER,"
                        + "status TEXT DEFAULT 'ACTIVE'," // 'ACTIVE', 'SUSPENDED', 'DELETED'
                        + "CHECK (user_type IN ('CUSTOMER', 'DRIVER', 'ADMIN')),"
                        + "CHECK ((user_type = 'ADMIN' AND admin_hash IS NOT NULL) OR user_type != 'ADMIN')"
                        + ")");
                
                // Create default admin accounts if they don't exist
                String[] adminHashes = {
                    "a1b2c3d4", "e5f6g7h8", "i9j0k1l2", "m3n4o5p6"
                };
                
                String adminPass = FoodDeliveryLoginUI.sha256Hex("FoodDashRocks");
                PreparedStatement adminCheck = c.prepareStatement("SELECT COUNT(*) FROM users WHERE user_type = 'ADMIN'");
                ResultSet rs = adminCheck.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    PreparedStatement adminInsert = c.prepareStatement(
                        "INSERT INTO users (username, password_hash, user_type, admin_hash, created_at) VALUES (?, ?, 'ADMIN', ?, ?)"
                    );
                    for (int i = 0; i < 4; i++) {
                        adminInsert.setString(1, "FoodDashAdmin");
                        adminInsert.setString(2, adminPass);
                        adminInsert.setString(3, adminHashes[i]);
                        adminInsert.setLong(4, Instant.now().getEpochSecond());
                        try {
                            adminInsert.executeUpdate();
                        } catch (SQLException ex) {
                            // Ignore duplicate key errors
                            if (!ex.getMessage().contains("UNIQUE constraint failed")) {
                                throw ex;
                            }
                        }
                    }
                }
            }
    }

    /** Insert a new user. Returns true on success. 
     * creates method register
     * trys to connect to the database
     * prepares an SQL statement to insert a new user into the users table
     *  throws SQLException if there is an error connecting to the database or executing the statement
     * returns true if the user was successfully inserted
    */
    public boolean register(String username, String passwordHash, String userType, 
                          String fullName, String email, String phone) throws SQLException {
        String sql = "INSERT INTO users(username,password_hash,user_type,full_name,email,phone,created_at) " +
                    "VALUES(?,?,?,?,?,?,?)";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            p.setString(2, passwordHash);
            p.setString(3, userType);
            p.setString(4, fullName);
            p.setString(5, email);
            p.setString(6, phone);
            p.setLong(7, Instant.now().getEpochSecond());
            p.executeUpdate();
            return true;
        }
    }

    /** Return true when passwordHash matches stored value. 
     * creates method authenticate
     * trys to connect to the database
     * prepares an SQL statement to select the password_hash from the users table where the username matches
     * throws SQLException if there is an error connecting to the database or executing the statement
     *  returns true if the provided passwordHash matches the stored password_hash for the given username
    */
    public boolean authenticate(String username, String passwordHash) throws SQLException {
        String sql = "SELECT password_hash, user_type FROM users WHERE username = ?";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql)) {
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

    public boolean verifyAdminHash(String username, String adminHash) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ? AND admin_hash = ? AND user_type = 'ADMIN'";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            p.setString(2, adminHash);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next();
            }
        }
    }
    
    public boolean isAdmin(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ? AND user_type = 'ADMIN'";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Return true if the username exists. 
     * creates method userExists
     * trys to connect to the database
     * prepares an SQL statement to select from the users table where the username matches
     * throws SQLException if there is an error connecting to the database or executing the statement
     * returns true if the username exists in the database
    */
    public boolean userExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            try (ResultSet rs = p.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Get the user type (CUSTOMER, DRIVER, or ADMIN) for a given username
     */
    public String getUserType(String username) throws SQLException {
        String sql = "SELECT user_type FROM users WHERE username = ?";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            try (ResultSet rs = p.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("user_type");
                }
                return null;
            }
        }
    }

    /**
     * Cancel an order by its ID. Sets the status to 'CANCELLED'.
     */
    public void cancelOrder(long orderId) throws SQLException {
        String sql = "UPDATE orders SET status = 'CANCELLED' WHERE order_id = ?";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, orderId);
            p.executeUpdate();
        }
    }
}
