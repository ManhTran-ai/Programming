package EBankingTCP;

/*
 * E-Banking System - Database Manager
 * Mỗi client có 1 kết nối độc lập xuống CSDL
 */

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private Connection connection;
    private String currentUser;
    private String currentAccountNumber;
    
    // Database configuration
    private static final String DB_URL = "jdbc:ucanaccess://./ebanking.accdb";
    
    public DatabaseManager() {
        initializeDatabase();
    }
    
    private void initializeDatabase() {
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            
            connection = DriverManager.getConnection(DB_URL, "", "");
            connection.setAutoCommit(false);
            
            createTablesIfNotExists();
            
            System.out.println("Database connection established.");
        } catch (ClassNotFoundException e) {
            System.err.println("UCanAccess driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }
    
    private void createTablesIfNotExists() {
        // Bảng Accounts
        String createAccountsSQL = """
            CREATE TABLE IF NOT EXISTS Accounts (
                username VARCHAR(50) PRIMARY KEY,
                password VARCHAR(50) NOT NULL,
                accountNumber VARCHAR(20) UNIQUE NOT NULL,
                balance DOUBLE DEFAULT 0.0
            )
            """;
        
        // Bảng Transactions
        String createTransactionsSQL = """
            CREATE TABLE IF NOT EXISTS Transactions (
                id AUTOEXEC PRIMARY KEY,
                accountNumber VARCHAR(20) NOT NULL,
                operation VARCHAR(20) NOT NULL,
                transactionDate DATETIME NOT NULL,
                amount DOUBLE NOT NULL
            )
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createAccountsSQL);
            stmt.execute(createTransactionsSQL);
            connection.commit();
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }
    
    // === Authentication Methods ===
    
    // Kiểm tra username tồn tại
    public String checkUsername(String username) {
        String querySQL = "SELECT accountNumber, balance FROM Accounts WHERE username = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(querySQL)) {
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    currentUser = username;
                    currentAccountNumber = rs.getString("accountNumber");
                    return "+OK " + currentAccountNumber + " " + rs.getDouble("balance");
                } else {
                    return "-ERR User not found";
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking username: " + e.getMessage());
            return "-ERR Database error";
        }
    }
    
    // Xác thực password
    public String authenticatePassword(String password) {
        if (currentUser == null) {
            return "-ERR Not in login phase";
        }
        
        String querySQL = "SELECT accountNumber, balance FROM Accounts WHERE username = ? AND password = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(querySQL)) {
            pstmt.setString(1, currentUser);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String accountNumber = rs.getString("accountNumber");
                    double balance = rs.getDouble("balance");
                    currentAccountNumber = accountNumber;
                    return "+OK " + accountNumber + " " + balance;
                } else {
                    currentUser = null;
                    currentAccountNumber = null;
                    return "-ERR Invalid password";
                }
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating password: " + e.getMessage());
            return "-ERR Database error";
        }
    }
    
    // Lấy account info
    public Account getCurrentAccount() {
        if (currentUser == null) {
            return null;
        }
        
        String querySQL = "SELECT * FROM Accounts WHERE username = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(querySQL)) {
            pstmt.setString(1, currentUser);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Account(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("accountNumber"),
                        rs.getDouble("balance")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting account: " + e.getMessage());
        }
        return null;
    }
    
    // === Transaction Methods ===
    
    // Gửi tiền
    public synchronized String deposit(double amount) {
        if (currentAccountNumber == null) {
            return "-ERR Not logged in";
        }
        
        if (amount <= 0) {
            return "-ERR Invalid amount";
        }
        
        String updateSQL = "UPDATE Accounts SET balance = balance + ? WHERE username = ?";
        String insertSQL = "INSERT INTO Transactions (accountNumber, operation, transactionDate, amount) VALUES (?, ?, ?, ?)";
        
        try {
            try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
                pstmt.setDouble(1, amount);
                pstmt.setString(2, currentUser);
                pstmt.executeUpdate();
            }
            
            try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
                pstmt.setString(1, currentAccountNumber);
                pstmt.setString(2, "DEPOSIT");
                pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setDouble(4, amount);
                pstmt.executeUpdate();
            }
            
            connection.commit();
            return "+OK Deposit successful";
        } catch (SQLException e) {
            System.err.println("Error depositing: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.err.println("Rollback error: " + ex.getMessage());
            }
            return "-ERR Transaction failed";
        }
    }
    
    // Rút tiền
    public synchronized String withdraw(double amount) {
        if (currentAccountNumber == null) {
            return "-ERR Not logged in";
        }
        
        if (amount <= 0) {
            return "-ERR Invalid amount";
        }
        
        Account account = getCurrentAccount();
        if (account == null || account.getBalance() < amount) {
            return "-ERR Insufficient balance";
        }
        
        String updateSQL = "UPDATE Accounts SET balance = balance - ? WHERE username = ? AND balance >= ?";
        String insertSQL = "INSERT INTO Transactions (accountNumber, operation, transactionDate, amount) VALUES (?, ?, ?, ?)";
        
        try {
            try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
                pstmt.setDouble(1, amount);
                pstmt.setString(2, currentUser);
                pstmt.setDouble(3, amount);
                int rows = pstmt.executeUpdate();
                
                if (rows == 0) {
                    connection.rollback();
                    return "-ERR Insufficient balance";
                }
            }
            
            try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
                pstmt.setString(1, currentAccountNumber);
                pstmt.setString(2, "WITHDRAW");
                pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setDouble(4, amount);
                pstmt.executeUpdate();
            }
            
            connection.commit();
            return "+OK Withdraw successful";
        } catch (SQLException e) {
            System.err.println("Error withdrawing: " + e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.err.println("Rollback error: " + ex.getMessage());
            }
            return "-ERR Transaction failed";
        }
    }
    
    // Lấy số dư
    public double getBalance() {
        Account account = getCurrentAccount();
        return account != null ? account.getBalance() : 0.0;
    }
    
    // Lấy nhật ký giao dịch
    public List<Transaction> getTransactionLog() {
        List<Transaction> transactions = new ArrayList<>();
        
        if (currentAccountNumber == null) {
            return transactions;
        }
        
        String querySQL = """
            SELECT * FROM Transactions 
            WHERE accountNumber = ? 
            ORDER BY transactionDate DESC
            """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(querySQL)) {
            pstmt.setString(1, currentAccountNumber);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new Transaction(
                        rs.getString("accountNumber"),
                        rs.getString("operation"),
                        rs.getTimestamp("transactionDate").toLocalDateTime(),
                        rs.getDouble("amount")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting transaction log: " + e.getMessage());
        }
        return transactions;
    }
    
    // Đăng xuất
    public void logout() {
        currentUser = null;
        currentAccountNumber = null;
    }
    
    // Kiểm tra trạng thái đăng nhập
    public boolean isLoggedIn() {
        return currentUser != null;
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

