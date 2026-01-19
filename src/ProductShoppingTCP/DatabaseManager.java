package ProductShoppingTCP;

import java.sql.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseManager - Quản lý kết nối và thao tác với cơ sở dữ liệu Microsoft Access
 */
public class DatabaseManager {
    private static final String DB_URL = "jdbc:ucanaccess://shopping_tcp.mdb";
    private Connection connection;
    private static final String DB_FILE = "shopping_tcp.mdb";

    public DatabaseManager() {
        try {
            // Tải driver UCanAccess
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            // Tạo cơ sở dữ liệu nếu chưa tồn tại
            createDatabaseIfNotExists();
            connection = DriverManager.getConnection(DB_URL);
            createTablesIfNotExists();
            System.out.println("[Server] Ket noi database thanh cong!");
        } catch (ClassNotFoundException e) {
            System.err.println("[Server] Loi: Khong tim thay UCanAccess driver!");
            System.err.println("[Server] Vui long them cac JAR sau vao classpath:");
            System.err.println("[Server] - ucanaccess-x.x.x.jar");
            System.err.println("[Server] - lib/commons-lang3-xx.jar");
        } catch (SQLException e) {
            System.err.println("[Server] Loi ket noi database: " + e.getMessage());
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
                System.out.println("[Server] Database " + DB_FILE + " da duoc tao.");
            }
        } catch (SQLException e) {
            System.err.println("[Server] Loi tao database: " + e.getMessage());
        }
    }

    /**
     * Tạo các bảng nếu chưa tồn tại
     */
    private void createTablesIfNotExists() {
        try {
            // Tạo bảng USERS
            String createUsersSQL = "CREATE TABLE IF NOT EXISTS USERS (" +
                                   "USERNAME TEXT PRIMARY KEY, " +
                                   "PASSWORD TEXT NOT NULL)";
            try (PreparedStatement stmt = connection.prepareStatement(createUsersSQL)) {
                stmt.executeUpdate();
            }

            // Tạo bảng PRODUCTS
            String createProductsSQL = "CREATE TABLE IF NOT EXISTS PRODUCTS (" +
                                      "PRODUCTID TEXT PRIMARY KEY, " +
                                      "NAME TEXT NOT NULL, " +
                                      "COUNT INTEGER, " +
                                      "PRICE DOUBLE)";
            try (PreparedStatement stmt = connection.prepareStatement(createProductsSQL)) {
                stmt.executeUpdate();
            }

            // Thêm dữ liệu mẫu nếu bảng trống
            if (isProductsTableEmpty()) {
                insertSampleData();
            }
            
            // Thêm user mặc định nếu bảng users trống
            if (isUsersTableEmpty()) {
                addUser("admin", "admin123");
                addUser("user", "123456");
                System.out.println("[Server] Da them user mac dinh: admin/admin123, user/123456");
            }

        } catch (SQLException e) {
            System.err.println("[Server] Loi tao bang: " + e.getMessage());
        }
    }

    /**
     * Thêm dữ liệu mẫu sản phẩm
     */
    private void insertSampleData() {
        String[] productIDs = {"SP001", "SP002", "SP003", "SP004", "SP005", 
                               "SP006", "SP007", "SP008", "SP009", "SP010"};
        String[] names = {"Laptop Dell Inspiron 15", "Laptop HP Pavilion 14", 
                         "iPhone 14 Pro Max", "Samsung Galaxy S23 Ultra",
                         "Tai nghe Sony WH-1000XM5", "Loa JBL Flip 6",
                         "Chuot khong day Logitech", "Ban phim co Corsair",
                         "Man hinh Samsung 27 inch", "O cung SSD Samsung 1TB"};
        int[] counts = {50, 30, 100, 80, 200, 150, 300, 120, 60, 250};
        double[] prices = {18990000, 15990000, 28990000, 24990000, 
                          7990000, 3290000, 1290000, 4590000, 
                          8990000, 2490000};

        String insertSQL = "INSERT INTO PRODUCTS (PRODUCTID, NAME, COUNT, PRICE) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertSQL)) {
            for (int i = 0; i < productIDs.length; i++) {
                stmt.setString(1, productIDs[i]);
                stmt.setString(2, names[i]);
                stmt.setInt(3, counts[i]);
                stmt.setDouble(4, prices[i]);
                stmt.addBatch();
            }
            stmt.executeBatch();
            System.out.println("[Server] Da them " + productIDs.length + " san pham mau.");
        } catch (SQLException e) {
            System.err.println("[Server] Loi them san pham mau: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra bảng PRODUCTS có trống không
     */
    private boolean isProductsTableEmpty() {
        String query = "SELECT COUNT(*) FROM PRODUCTS";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            System.err.println("[Server] Loi kiem tra bang: " + e.getMessage());
        }
        return true;
    }

    /**
     * Kiểm tra bảng USERS có trống không
     */
    private boolean isUsersTableEmpty() {
        String query = "SELECT COUNT(*) FROM USERS";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            System.err.println("[Server] Loi kiem tra bang: " + e.getMessage());
        }
        return true;
    }

    // ==================== QUẢN LÝ USER ====================

    /**
     * Kiểm tra username có tồn tại không
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
            System.err.println("[Server] Loi kiem tra user: " + e.getMessage());
        }
        return false;
    }

    /**
     * Xác thực user
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
            System.err.println("[Server] Loi xac thuc user: " + e.getMessage());
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
            System.err.println("[Server] Loi them user: " + e.getMessage());
            return false;
        }
    }

    // ==================== QUẢN LÝ SẢN PHẨM ====================

    /**
     * Lấy tất cả sản phẩm
     */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM PRODUCTS";
        
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Product product = new Product(
                    rs.getString("PRODUCTID"),
                    rs.getString("NAME"),
                    rs.getInt("COUNT"),
                    rs.getDouble("PRICE")
                );
                products.add(product);
            }
        } catch (SQLException e) {
            System.err.println("[Server] Loi lay san pham: " + e.getMessage());
        }
        return products;
    }

    /**
     * Tìm sản phẩm theo mã
     */
    public Product findByID(String productID) {
        String query = "SELECT * FROM PRODUCTS WHERE PRODUCTID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, productID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Product(
                    rs.getString("PRODUCTID"),
                    rs.getString("NAME"),
                    rs.getInt("COUNT"),
                    rs.getDouble("PRICE")
                );
            }
        } catch (SQLException e) {
            System.err.println("[Server] Loi tim san pham: " + e.getMessage());
        }
        return null;
    }

    /**
     * Tìm sản phẩm theo tên (tìm kiếm tương đối)
     */
    public List<Product> findByName(String productName) {
        List<Product> products = new ArrayList<>();
        String query = "SELECT * FROM PRODUCTS WHERE NAME LIKE ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + productName + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Product product = new Product(
                    rs.getString("PRODUCTID"),
                    rs.getString("NAME"),
                    rs.getInt("COUNT"),
                    rs.getDouble("PRICE")
                );
                products.add(product);
            }
        } catch (SQLException e) {
            System.err.println("[Server] Loi tim san pham: " + e.getMessage());
        }
        return products;
    }

    /**
     * Mua sản phẩm (giảm số lượng)
     */
    public boolean buyProduct(String productID) {
        String updateSQL = "UPDATE PRODUCTS SET COUNT = COUNT - 1 WHERE PRODUCTID = ? AND COUNT > 0";
        try (PreparedStatement stmt = connection.prepareStatement(updateSQL)) {
            stmt.setString(1, productID);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("[Server] Loi mua san pham: " + e.getMessage());
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
                System.out.println("[Server] Da dong ket noi database.");
            }
        } catch (SQLException e) {
            System.err.println("[Server] Loi dong ket noi: " + e.getMessage());
        }
    }
}

