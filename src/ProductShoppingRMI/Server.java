package ProductShoppingRMI;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Server - RMI Server cho hệ thống Mua bán Sản phẩm
 * Đăng ký service RMI trên port 5918
 */
public class Server {
    private static final int REGISTRY_PORT = 5918;
    private static final String SERVICE_URL = "rmi://127.0.0.1/" + ProductService.SERVICE_NAME;
    
    private Registry registry;
    private ProductServiceImpl productService;
    private boolean running;

    public Server() {
        this.running = false;
    }

    /**
     * Khởi động RMI Server
     */
    public void start() {
        try {
            System.out.println("╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║    SERVER MUA BÁN SẢN PHẨM (RMI) - ĐANG CHẠY                ║");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            
            // Tạo instance của implementation
            productService = new ProductServiceImpl();
            
            // Tạo registry trên port 5918
            registry = LocateRegistry.createRegistry(REGISTRY_PORT);
            System.out.println("║  Registry đã được tạo trên port: " + REGISTRY_PORT + "                      ║");
            
            // Đăng ký service
            registry.bind(ProductService.SERVICE_NAME, productService);
            
            running = true;
            System.out.println("║  Service URL: " + SERVICE_URL + "                        ║");
            System.out.println("║  Để dừng server, nhấn Ctrl+C                                ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            System.out.println("[Server] Server đã sẵn sàng nhận kết nối...");
            
            // Giữ server running
            while (running) {
                Thread.sleep(1000);
            }
            
        } catch (RemoteException e) {
            System.err.println("[Server] Lỗi RemoteException: " + e.getMessage());
        } catch (AlreadyBoundException e) {
            System.err.println("[Server] Lỗi: Service đã được đăng ký trước đó!");
        } catch (InterruptedException e) {
            System.out.println("[Server] Server bị gián đoạn.");
        } catch (Exception e) {
            System.err.println("[Server] Lỗi không xác định: " + e.getMessage());
        } finally {
            stop();
        }
    }

    /**
     * Dừng server
     */
    public void stop() {
        running = false;
        System.out.println("[Server] Đang dừng server...");
        
        try {
            // Unbind service
            if (registry != null) {
                try {
                    registry.unbind(ProductService.SERVICE_NAME);
                } catch (Exception e) {
                    // Ignore nếu không unbind được
                }
            }
            
            System.out.println("[Server] Server đã dừng.");
        } catch (Exception e) {
            System.err.println("[Server] Lỗi dừng server: " + e.getMessage());
        }
    }

    /**
     * Main method để chạy server
     */
    public static void main(String[] args) {
        // Thiết lập shutdown hook
        Runtime runtime = Runtime.getRuntime();
        Thread shutdownHook = new Thread(() -> {
            System.out.println("\n[Server] Nhận tín hiệu shutdown...");
        });
        runtime.addShutdownHook(shutdownHook);
        
        Server server = new Server();
        
        // Thiết lập Security Manager (cần cho RMI)
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        
        server.start();
    }
}

