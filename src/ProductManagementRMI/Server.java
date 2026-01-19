package ProductManagementRMI;

/*
 * Product Management System - RMI Server
 * Đăng ký service để clients có thể gọi từ xa
 */

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final String SERVICE_NAME = "ProductService";
    private static final int REGISTRY_PORT = 1099;
    private static final String HOST = "localhost";
    
    private ExecutorService threadPool;
    private volatile boolean running;
    
    public Server() {
        this.threadPool = Executors.newFixedThreadPool(50);
        this.running = false;
    }
    
    public void start() {
        try {
            running = true;
            
            System.out.println("========================================");
            System.out.println("Product Management RMI Server");
            System.out.println("Registry Port: " + REGISTRY_PORT);
            System.out.println("Service Name: " + SERVICE_NAME);
            System.out.println("========================================");
            System.out.println("Starting RMI Registry...");
            
            // Tạo RMI Registry trên port 1099
            LocateRegistry.createRegistry(REGISTRY_PORT);
            
            // Tạo service instance
            System.out.println("Creating ProductService implementation...");
            ProductService productService = new ProductServiceImpl();
            
            // Đăng ký service với RMI Registry
            String serviceURL = "rmi://" + HOST + ":" + REGISTRY_PORT + "/" + SERVICE_NAME;
            Naming.rebind(serviceURL, productService);
            
            System.out.println("Service bound successfully!");
            System.out.println("Service URL: " + serviceURL);
            System.out.println("========================================");
            System.out.println("Server started. Waiting for clients...");
            
            // Thêm dữ liệu mẫu
            initializeSampleData();
            
            // Server chạy vô hạn (cho đến khi bị interrupted)
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
        // Thiết lập security manager để cho phép RMI
        // Lưu ý: Security Manager và RMISecurityManager đã deprecated từ Java 8/17
        // Trong môi trường học tập (local), có thể chạy không cần Security Manager
        // Nếu cần security, hãy chạy với: java -Djava.security.policy=server.policy
        
        Server server = new Server();
        
        // Đăng ký shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
        }));
        
        server.start();
    }
}

