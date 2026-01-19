package EBankingTCP;

import java.sql.*;
import java.time.LocalDateTime;

/*
 * E-Banking System - Database Setup
 * Tạo cơ sở dữ liệu mẫu để test hệ thống
 */

public class DatabaseSetup {
    private static final String DB_URL = "jdbc:ucanaccess://./ebanking.accdb";
    
    public static void main(String[] args) {
        System.out.println("Setting up E-Banking Database...\n");
        
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            
            try (Connection connection = DriverManager.getConnection(DB_URL, "", "")) {
                createTables(connection);
                insertSampleData(connection);
                System.out.println("\nDatabase setup completed successfully!");
                System.out.println("\nSample accounts:");
                System.out.println("  Account 1: user1 / password123 / 100000");
                System.out.println("  Account 2: user2 / password456 / 50000");
                System.out.println("  Account 3: admin / admin888 / 200000");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("UCanAccess driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }
    
    private static void createTables(Connection connection) throws SQLException {
        // Xóa bảng cũ nếu tồn tại
        String dropTransactionsSQL = "DROP TABLE IF EXISTS Transactions";
        String dropAccountsSQL = "DROP TABLE IF EXISTS Accounts";
        
        // Tạo bảng Accounts
        String createAccountsSQL = """
            CREATE TABLE Accounts (
                username VARCHAR(50) PRIMARY KEY,
                password VARCHAR(50) NOT NULL,
                accountNumber VARCHAR(20) UNIQUE NOT NULL,
                balance DOUBLE DEFAULT 0.0
            )
            "";
        
        // Tạo bảng Transactions
        String createTransactionsSQL = """
            CREATE TABLE Transactions (
                id COUNTER PRIMARY KEY,
                accountNumber VARCHAR(20) NOT NULL,
                operation VARCHAR(20) NOT NULL,
                transactionDate DATETIME NOT NULL,
                amount DOUBLE NOT NULL
            )
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(dropTransactionsSQL);
            stmt.execute(dropAccountsSQL);
            stmt.execute(createAccountsSQL);
            stmt.execute(createTransactionsSQL);
            connection.commit();
            System.out.println("Tables created successfully.");
        }
    }
    
    private static void insertSampleData(Connection connection) throws SQLException {
        // Thêm tài khoản mẫu
        String insertAccountSQL = "INSERT INTO Accounts (username, password, accountNumber, balance) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(insertAccountSQL)) {
            // Account 1
            pstmt.setString(1, "user1");
            pstmt.setString(2, "password123");
            pstmt.setString(3, "ACC001");
            pstmt.setDouble(4, 100000);
            pstmt.executeUpdate();
            
            // Account 2
            pstmt.setString(1, "user2");
            pstmt.setString(2, "password456");
            pstmt.setString(3, "ACC002");
            pstmt.setDouble(4, 50000);
            pstmt.executeUpdate();
            
            // Account 3
            pstmt.setString(1, "admin");
            pstmt.setString(2, "admin888");
            pstmt.setString(3, "ACC003");
            pstmt.setDouble(4, 200000);
            pstmt.executeUpdate();
        }
        
        // Thêm giao dịch mẫu
        String insertTransactionSQL = "INSERT INTO Transactions (accountNumber, operation, transactionDate, amount) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(insertTransactionSQL)) {
            // Giao dịch cho user1
            pstmt.setString(1, "ACC001");
            pstmt.setString(2, "DEPOSIT");
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().minusDays(5)));
            pstmt.setDouble(4, 50000);
            pstmt.executeUpdate();
            
            pstmt.setString(1, "ACC001");
            pstmt.setString(2, "WITHDRAW");
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().minusDays(3)));
            pstmt.setDouble(4, 20000);
            pstmt.executeUpdate();
            
            // Giao dịch cho user2
            pstmt.setString(1, "ACC002");
            pstmt.setString(2, "DEPOSIT");
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now().minusDays(2)));
            pstmt.setDouble(4, 30000);
            pstmt.executeUpdate();
        }
        
        connection.commit();
        System.out.println("Sample data inserted successfully.");
    }
}

