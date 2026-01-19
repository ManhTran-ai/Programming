package RemoteFileManagement;

import java.sql.*;
import java.io.File;

/**
 * DatabaseManager - Quản lý kết nối và thao tác với cơ sở dữ liệu Microsoft Access
 * Sử dụng UCanAccess JDBC driver
 */
public class DatabaseManager {
    private static final String DB_URL = "jdbc:ucanaccess://users.mdb";
    private Connection connection;
    private static final String DB_FILE = "users.mdb";

    public DatabaseManager() {
        try {
            // Tải driver UCanAccess
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            // Tạo cơ sở dữ liệu nếu chưa tồn tại
            createDatabaseIfNotExists();
            connection = DriverManager.getConnection(DB_URL);
            createUsersTableIfNotExists();
            System.out.println("Kết nối database thành công!");
        } catch (ClassNotFoundException e) {
            System.err.println("Lỗi: Không tìm thấy UCanAccess driver!");
            System.err.println("Vui lòng thêm các JAR sau vào classpath:");
            System.err.println("- ucanaccess-x.x.x.jar");
            System.err.println("- lib/commons-lang3-xx.jar");
            System.err.println("- lib/commons-logging-xx.jar");
            System.err.println("- lib/hsqldb-xx.jar");
            System.err.println("- lib/jackcess-xx.jar");
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối database: " + e.getMessage());
        }
    }

    /**
     * Tạo file database nếu chưa tồn tại
     */
    private void createDatabaseIfNotExists() {
        try {
            File dbFile = new File(DB_FILE);
            if (!dbFile.exists()) {
                // Tạo database trống
                Connection conn = DriverManager.getConnection(
                    "jdbc:ucanaccess://" + DB_FILE + ";newDatabaseVersion=V2010");
                conn.close();
                System.out.println("Database " + DB_FILE + " đã được tạo.");
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
            // Thêm user mặc định nếu bảng trống
            if (isTableEmpty()) {
                addUser("admin", "admin123");
                addUser("user", "123456");
                System.out.println("Đã thêm user mặc định: admin/admin123, user/123456");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tạo bảng: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra bảng có trống không
     */
    private boolean isTableEmpty() {
        String query = "SELECT COUNT(*) FROM USERS";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kiểm tra bảng: " + e.getMessage());
        }
        return true;
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
     * Thêm user mới
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
                System.out.println("Đã đóng kết nối database.");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi đóng kết nối: " + e.getMessage());
        }
    }
}

