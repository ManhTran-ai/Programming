package ProductManagementRMI;

/*
 * Product Management System - Database Manager
 * Sử dụng Microsoft Access Database với UCanAccess JDBC driver
 * Chỉ sử dụng 1 kết nối duy nhất cho tất cả clients
 */

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    // Singleton instance
    private static DatabaseManager instance;
    private Connection connection;
    
    // Database configuration - sử dụng đường dẫn tương đối
    private static final String DB_URL = "jdbc:ucanaccess://./product.accdb";
    
    private DatabaseManager() {
        initializeDatabase();
    }
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    private void initializeDatabase() {
        try {
            // Load UCanAccess driver
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            
            // Tạo kết nối duy nhất với auto-commit disabled để tối ưu
            connection = DriverManager.getConnection(DB_URL, "", "");
            connection.setAutoCommit(false);
            
            // Tạo bảng nếu chưa tồn tại
            createTableIfNotExists();
            
            System.out.println("Database connection established successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("UCanAccess driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }
    
    private void createTableIfNotExists() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS Products (
                productID VARCHAR(20) PRIMARY KEY,
                name VARCHAR(200) NOT NULL,
                count INTEGER DEFAULT 0,
                price DOUBLE DEFAULT 0.0
            )
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
            connection.commit();
        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
        }
    }
    
    // Thêm sản phẩm mới
    public synchronized boolean addProduct(String productID, String name, double price, int count) {
        // Kiểm tra mã sản phẩm đã tồn tại chưa
        if (isProductExists(productID)) {
            return false;
        }
        
        String insertSQL = "INSERT INTO Products (productID, name, count, price) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
            pstmt.setString(1, productID);
            pstmt.setString(2, name);
            pstmt.setInt(3, count);
            pstmt.setDouble(4, price);
            
            pstmt.executeUpdate();
            connection.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding product: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.err.println("Rollback error: " + ex.getMessage());
            }
            return false;
        }
    }
    
    // Kiểm tra sản phẩm tồn tại
    private boolean isProductExists(String productID) {
        String querySQL = "SELECT COUNT(*) FROM Products WHERE productID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(querySQL)) {
            pstmt.setString(1, productID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking product: " + e.getMessage());
        }
        return false;
    }
    
    // Mua sản phẩm - giảm số lượng đi 1
    public synchronized boolean buyProduct(String productID) {
        // Kiểm tra sản phẩm tồn tại và còn hàng
        String checkSQL = "SELECT count FROM Products WHERE productID = ?";
        int currentCount = 0;
        
        try (PreparedStatement pstmt = connection.prepareStatement(checkSQL)) {
            pstmt.setString(1, productID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    currentCount = rs.getInt("count");
                } else {
                    return false; // Sản phẩm không tồn tại
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking product count: " + e.getMessage());
            return false;
        }
        
        if (currentCount <= 0) {
            return false; // Hết hàng
        }
        
        // Giảm số lượng
        String updateSQL = "UPDATE Products SET count = count - 1 WHERE productID = ? AND count > 0";
        
        try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
            pstmt.setString(1, productID);
            int rowsAffected = pstmt.executeUpdate();
            connection.commit();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error buying product: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.err.println("Rollback error: " + ex.getMessage());
            }
            return false;
        }
    }
    
    // Tìm sản phẩm theo tên (tìm gần đúng, không phân biệt hoa thường)
    public synchronized List<Product> findProductsByName(String name) {
        List<Product> products = new ArrayList<>();
        String querySQL = "SELECT * FROM Products WHERE LOWER(name) LIKE LOWER(?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(querySQL)) {
            pstmt.setString(1, "%" + name + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(new Product(
                        rs.getString("productID"),
                        rs.getString("name"),
                        rs.getInt("count"),
                        rs.getDouble("price")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding products by name: " + e.getMessage());
        }
        return products;
    }
    
    // Tìm sản phẩm theo khoảng giá
    public synchronized List<Product> findProductsByPriceRange(double fromPrice, double toPrice) {
        List<Product> products = new ArrayList<>();
        String querySQL = "SELECT * FROM Products WHERE price >= ? AND price <= ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(querySQL)) {
            pstmt.setDouble(1, fromPrice);
            pstmt.setDouble(2, toPrice);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(new Product(
                        rs.getString("productID"),
                        rs.getString("name"),
                        rs.getInt("count"),
                        rs.getDouble("price")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding products by price: " + e.getMessage());
        }
        return products;
    }
    
    // Lấy tất cả sản phẩm
    public synchronized List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String querySQL = "SELECT * FROM Products";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(querySQL)) {
            while (rs.next()) {
                products.add(new Product(
                    rs.getString("productID"),
                    rs.getString("name"),
                    rs.getInt("count"),
                    rs.getDouble("price")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all products: " + e.getMessage());
        }
        return products;
    }
    
    // Lấy số lượng sản phẩm hiện có
    public synchronized int getProductCount(String productID) {
        String querySQL = "SELECT count FROM Products WHERE productID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(querySQL)) {
            pstmt.setString(1, productID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting product count: " + e.getMessage());
        }
        return -1;
    }
    
    // Đóng kết nối
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}

