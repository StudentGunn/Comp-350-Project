import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;

/**
 * SQLite-backed driver database helper
 */
public class DriverDatabase {
    private final Path dbPath;
    private final String url;

    public DriverDatabase(Path dbPath) {
        this.dbPath = dbPath;
        this.url = "jdbc:sqlite:" + dbPath.toAbsolutePath().toString();
    }

    public void init() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found on classpath", e);
        }

        try (Connection c = DriverManager.getConnection(url);
             Statement s = c.createStatement()) {
            
            // Enable foreign key support
            s.executeUpdate("PRAGMA foreign_keys = ON");

            // Create drivers table
            s.executeUpdate("CREATE TABLE IF NOT EXISTS drivers ("
                    + "username TEXT PRIMARY KEY,"
                    + "vehicle_type TEXT,"           // Type of vehicle used for delivery
                    + "license_number TEXT,"         // Driver's license number
                    + "current_status TEXT DEFAULT 'OFFLINE'," // 'AVAILABLE', 'ON_DELIVERY', 'OFFLINE'
                    + "total_deliveries INTEGER DEFAULT 0,"
                    + "rating DECIMAL(3,2) DEFAULT 0.0,"      // Average rating
                    + "service_area TEXT,"           // Preferred delivery area
                    + "account_status TEXT DEFAULT 'ACTIVE'," // 'ACTIVE', 'SUSPENDED', 'TERMINATED'
                    + "joined_date INTEGER NOT NULL,"
                    + "FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE ON UPDATE CASCADE"
                    + ")");

            // Create delivery_history table
            s.executeUpdate("CREATE TABLE IF NOT EXISTS delivery_history ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "driver_username TEXT NOT NULL,"
                    + "order_id INTEGER NOT NULL,"
                    + "pickup_time INTEGER,"
                    + "delivery_time INTEGER,"
                    + "delivery_status TEXT,"        // 'PICKED_UP', 'DELIVERED', 'CANCELLED'
                    + "customer_rating INTEGER,"     // 1-5 stars
                    + "customer_feedback TEXT,"
                    + "earnings DECIMAL(10,2),"
                    + "FOREIGN KEY (driver_username) REFERENCES drivers(username),"
                    + "FOREIGN KEY (order_id) REFERENCES orders(order_id)"
                    + ")");

            // Create driver_schedule table
            s.executeUpdate("CREATE TABLE IF NOT EXISTS driver_schedule ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "driver_username TEXT NOT NULL,"
                    + "day_of_week INTEGER,"         // 1-7 (Sunday-Saturday)
                    + "start_time TEXT,"             // HH:MM format
                    + "end_time TEXT,"               // HH:MM format
                    + "is_active BOOLEAN DEFAULT 1,"
                    + "FOREIGN KEY (driver_username) REFERENCES drivers(username)"
                    + ")");
        }
    }

    public void registerDriver(String username, String vehicleType, String licenseNumber, 
                             String serviceArea) throws SQLException {
        String sql = "INSERT INTO drivers (username, vehicle_type, license_number, service_area, "
                  + "current_status, account_status, joined_date) "
                  + "VALUES (?, ?, ?, ?, 'OFFLINE', 'ACTIVE', ?)";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            p.setString(2, vehicleType);
            p.setString(3, licenseNumber);
            p.setString(4, serviceArea);
            p.setLong(5, Instant.now().getEpochSecond());
            p.executeUpdate();
        }
    }

    public void updateDriverStatus(String username, String status) throws SQLException {
        String sql = "UPDATE drivers SET current_status = ? WHERE username = ?";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, status);
            p.setString(2, username);
            p.executeUpdate();
        }
    }

    public void recordDelivery(String username, long orderId, long pickupTime, 
                             long deliveryTime, String status) throws SQLException {
        String sql = "INSERT INTO delivery_history (driver_username, order_id, pickup_time, "
                  + "delivery_time, delivery_status) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            p.setLong(2, orderId);
            p.setLong(3, pickupTime);
            p.setLong(4, deliveryTime);
            p.setString(5, status);
            p.executeUpdate();

            // Update total deliveries count
            if ("DELIVERED".equals(status)) {
                sql = "UPDATE drivers SET total_deliveries = total_deliveries + 1 WHERE username = ?";
                try (PreparedStatement p2 = c.prepareStatement(sql)) {
                    p2.setString(1, username);
                    p2.executeUpdate();
                }
            }
        }
    }

    public void updateRating(String username, long orderId, int rating, String feedback) throws SQLException {
        String sql = "UPDATE delivery_history SET customer_rating = ?, customer_feedback = ? "
                  + "WHERE driver_username = ? AND order_id = ?";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, rating);
            p.setString(2, feedback);
            p.setString(3, username);
            p.setLong(4, orderId);
            p.executeUpdate();

            // Update average rating
            sql = "UPDATE drivers SET rating = (SELECT AVG(customer_rating) FROM delivery_history "
                + "WHERE driver_username = ? AND customer_rating IS NOT NULL) WHERE username = ?";
            try (PreparedStatement p2 = c.prepareStatement(sql)) {
                p2.setString(1, username);
                p2.setString(2, username);
                p2.executeUpdate();
            }
        }
    }

    public void setSchedule(String username, int dayOfWeek, String startTime, 
                          String endTime) throws SQLException {
        String sql = "INSERT INTO driver_schedule (driver_username, day_of_week, start_time, end_time) "
                  + "VALUES (?, ?, ?, ?)";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, username);
            p.setInt(2, dayOfWeek);
            p.setString(3, startTime);
            p.setString(4, endTime);
            p.executeUpdate();
        }
    }

    public ResultSet getDeliveryHistory(String username) throws SQLException {
        String sql = "SELECT dh.*, o.restaurant_name, o.total_amount "
                  + "FROM delivery_history dh "
                  + "JOIN orders o ON dh.order_id = o.order_id "
                  + "WHERE driver_username = ? "
                  + "ORDER BY delivery_time DESC";
        Connection c = DriverManager.getConnection(url);
        PreparedStatement p = c.prepareStatement(sql);
        p.setString(1, username);
        return p.executeQuery();
    }

    public ResultSet getDriverStats(String username) throws SQLException {
        String sql = "SELECT d.*, "
                  + "(SELECT COUNT(*) FROM delivery_history WHERE driver_username = d.username) as total_orders, "
                  + "(SELECT AVG(customer_rating) FROM delivery_history WHERE driver_username = d.username) as avg_rating, "
                  + "(SELECT SUM(earnings) FROM delivery_history WHERE driver_username = d.username) as total_earnings "
                  + "FROM drivers d WHERE d.username = ?";
        Connection c = DriverManager.getConnection(url);
        PreparedStatement p = c.prepareStatement(sql);
        p.setString(1, username);
        return p.executeQuery();
    }
}