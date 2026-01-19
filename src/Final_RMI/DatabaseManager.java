package Final_RMI;

import java.sql.*;

/**
 * Quản lý kết nối và thao tác với cơ sở dữ liệu Microsoft Access
 * Sử dụng PreparedStatement để tránh SQL Injection
 */
public class DatabaseManager {
    private static final String DB_URL = "jdbc:ucanaccess://users.mdb";
    private Connection connection;

    public DatabaseManager() {
        try {
            // Tạo cơ sở dữ liệu nếu chưa tồn tại
            createDatabaseIfNotExists();
            connection = DriverManager.getConnection(DB_URL);
            createUsersTableIfNotExists();
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối database: " + e.getMessage());
        }
    }

    /**
     * Tạo file database nếu chưa tồn tại
     */
    private void createDatabaseIfNotExists() {
        try {
            // Kiểm tra và tạo file database nếu cần
            java.io.File dbFile = new java.io.File("users.mdb");
            if (!dbFile.exists()) {
                // Tạo database trống
                Connection conn = DriverManager.getConnection("jdbc:ucanaccess://users.mdb;newDatabaseVersion=V2010");
                conn.close();
                System.out.println("Database users.mdb đã được tạo.");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tạo database: " + e.getMessage());
        }
    }

    /**
     * Tạo bảng USERS nếu chưa tồn tại
     */
    private void createUsersTableIfNotExists() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS USERS (" +
                               "USERNAME TEXT PRIMARY KEY, " +
                               "PASSWORD TEXT NOT NULL)";

        try (PreparedStatement stmt = connection.prepareStatement(createTableSQL)) {
            stmt.executeUpdate();
            System.out.println("Bảng USERS đã được tạo hoặc đã tồn tại.");
        } catch (SQLException e) {
            System.err.println("Lỗi tạo bảng: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra xem username có tồn tại không
     */
    public boolean userExists(String username) {
        String query = "SELECT COUNT(*) FROM USERS WHERE USERNAME = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kiểm tra user: " + e.getMessage());
        }
        return false;
    }

    /**
     * Xác thực mật khẩu của user
     */
    public boolean authenticateUser(String username, String password) {
        String query = "SELECT PASSWORD FROM USERS WHERE USERNAME = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("PASSWORD");
                return password.equals(storedPassword);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi xác thực user: " + e.getMessage());
        }
        return false;
    }

    /**
     * Thêm user mới (cho việc test)
     */
    public boolean addUser(String username, String password) {
        String insertSQL = "INSERT INTO USERS (USERNAME, PASSWORD) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertSQL)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi thêm user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Đóng kết nối database
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi đóng kết nối: " + e.getMessage());
        }
    }
}
