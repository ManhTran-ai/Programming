package ProductManagementTCP;

/*
 * Product Management System - Server
 * TCP Server phục vụ nhiều kết nối đồng thời
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 1080;
    private static final String HOST = "127.0.0.1";
    private static final int MAX_THREADS = 50;
    
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private volatile boolean running;
    
    public Server() {
        this.threadPool = Executors.newFixedThreadPool(MAX_THREADS);
        this.running = false;
    }
    
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT, 0, java.net.InetAddress.getByName(HOST));
            running = true;
            
            System.out.println("========================================");
            System.out.println("Product Management Server");
            System.out.println("Host: " + HOST);
            System.out.println("Port: " + PORT);
            System.out.println("========================================");
            System.out.println("Server started. Waiting for connections...");
            
            // Thêm dữ liệu mẫu
            initializeSampleData();
            
            // Chấp nhận kết nối từ clients
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getRemoteSocketAddress());
                    
                    // Sử dụng thread pool để xử lý client
                    ClientHandler handler = new ClientHandler(clientSocket);
                    threadPool.execute(handler);
                    
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error accepting client: " + e.getMessage());
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        } finally {
            shutdown();
        }
    }
    
    private void initializeSampleData() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        
        // Thêm dữ liệu mẫu nếu chưa có
        String[][] sampleProducts = {
            {"P001", "Laptop Dell Inspiron 15", "50000000", "10"},
            {"P002", "Laptop HP ProBook 450", "45000000", "15"},
            {"P003", "Mouse Logitech MX Master", "2500000", "50"},
            {"P004", "Keyboard Mechanical Corsair", "3500000", "40"},
            {"P005", "Monitor Samsung 27 inch", "8500000", "25"},
            {"P006", "USB Kingston 64GB", "450000", "100"},
            {"P007", "Webcam Logitech C920", "4200000", "30"},
            {"P008", "Tai nghe Sony WH-1000XM4", "8990000", "20"},
            {"P009", "Bàn phím giả cơ Rapoo", "1200000", "60"},
            {"P010", "Chuột không dây Microsoft", "1800000", "45"}
        };
        
        for (String[] product : sampleProducts) {
            try {
                dbManager.addProduct(
                    product[0],
                    product[1],
                    Double.parseDouble(product[2]),
                    Integer.parseInt(product[3])
                );
            } catch (Exception e) {
                // Sản phẩm có thể đã tồn tại
            }
        }
        
        System.out.println("Sample data initialized.");
    }
    
    public void stop() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
    }
    
    private void shutdown() {
        System.out.println("Shutting down server...");
        
        // Đóng thread pool
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
        }
        
        // Đóng database connection
        DatabaseManager.getInstance().closeConnection();
        
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

