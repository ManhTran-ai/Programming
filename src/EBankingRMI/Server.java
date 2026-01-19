package EBankingRMI;

/*
 * E-Banking System - RMI Server
 */

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class Server {
    private static final String SERVICE_NAME = "BankService";
    private static final int REGISTRY_PORT = 1099;
    private static final String HOST = "localhost";
    
    private volatile boolean running;
    
    public Server() {
        this.running = false;
    }
    
    public void start() {
        try {
            running = true;
            
            System.out.println("========================================");
            System.out.println("NLU e-Bank Server");
            System.out.println("Registry Port: " + REGISTRY_PORT);
            System.out.println("Service Name: " + SERVICE_NAME);
            System.out.println("========================================");
            System.out.println("Starting RMI Registry...");
            
            // Tạo RMI Registry trên port 1099
            LocateRegistry.createRegistry(REGISTRY_PORT);
            
            // Tạo service instance
            System.out.println("Creating BankService implementation...");
            BankService bankService = new BankServiceImpl();
            
            // Đăng ký service với RMI Registry
            String serviceURL = "rmi://" + HOST + ":" + REGISTRY_PORT + "/" + SERVICE_NAME;
            Naming.rebind(serviceURL, bankService);
            
            System.out.println("Service bound successfully!");
            System.out.println("Service URL: " + serviceURL);
            System.out.println("========================================");
            System.out.println("Server started. Waiting for clients...");
            
            // Thêm dữ liệu mẫu
            initializeSampleData();
            
            // Server chạy vô hạn
            while (running) {
                Thread.sleep(1000);
            }
            
        } catch (RemoteException e) {
            System.err.println("RMI Registry error: " + e.getMessage());
        } catch (java.net.MalformedURLException e) {
            System.err.println("Invalid service URL: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("Server interrupted.");
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        } finally {
            shutdown();
        }
    }
    
    private void initializeSampleData() {
        System.out.println("Initializing sample data...");
        
        // Sử dụng DatabaseManager trực tiếp để thêm dữ liệu mẫu
        DatabaseManager db = new DatabaseManager();
        
        // Kiểm tra và thêm tài khoản mẫu
        try {
            // Các tài khoản mẫu
            String[][] sampleAccounts = {
                {"user1", "pass123", "10000001", "5000000"},
                {"user2", "pass456", "10000002", "10000000"},
                {"user3", "pass789", "10000003", "2500000"}
            };
            
            for (String[] acc : sampleAccounts) {
                db.checkUsername(acc[0]); // Initialize connection
                try {
                    java.sql.Connection conn = java.sql.DriverManager.getConnection(
                        "jdbc:ucanaccess://./ebanking.accdb", "", "");
                    
                    String checkSQL = "SELECT COUNT(*) FROM Accounts WHERE username = ?";
                    try (java.sql.PreparedStatement pstmt = conn.prepareStatement(checkSQL)) {
                        pstmt.setString(1, acc[0]);
                        java.sql.ResultSet rs = pstmt.executeQuery();
                        if (rs.next() && rs.getInt(1) == 0) {
                            // Thêm tài khoản mới
                            String insertSQL = "INSERT INTO Accounts (username, password, accountNumber, balance) VALUES (?, ?, ?, ?)";
                            try (java.sql.PreparedStatement ipstmt = conn.prepareStatement(insertSQL)) {
                                ipstmt.setString(1, acc[0]);
                                ipstmt.setString(2, acc[1]);
                                ipstmt.setString(3, acc[2]);
                                ipstmt.setDouble(4, Double.parseDouble(acc[3]));
                                ipstmt.executeUpdate();
                            }
                        }
                    }
                    conn.close();
                } catch (Exception e) {
                    // Có thể tài khoản đã tồn tại
                }
            }
        } catch (Exception e) {
            System.out.println("Sample data initialization skipped (DB may not exist yet)");
        }
        
        System.out.println("Server ready.");
    }
    
    public void stop() {
        running = false;
    }
    
    private void shutdown() {
        System.out.println("Shutting down server...");
        System.out.println("Server shutdown complete.");
    }
    
    public static void main(String[] args) {
        Server server = new Server();
        
        // Đăng ký shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
        }));
        
        server.start();
    }
}

