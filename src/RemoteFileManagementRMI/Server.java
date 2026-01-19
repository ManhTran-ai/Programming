package RemoteFileManagementRMI;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Server - RMI Server cho hệ thống Remote File Management
 * Đăng ký service RMI để client có thể kết nối từ xa
 */
public class Server {
    private static final int REGISTRY_PORT = 55555;
    private static final String SERVICE_URL = "rmi://127.0.0.1/" + RemoteFileManager.SERVICE_NAME;
    
    private Registry registry;
    private RemoteFileManagerImpl fileManager;
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
            System.out.println("║    SERVER QUẢN LÝ FILE TỪ XA (RMI) - ĐANG CHẠY              ║");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            
            // Tạo instance của implementation
            fileManager = new RemoteFileManagerImpl();
            
            // Tạo registry trên port 55555
            registry = LocateRegistry.createRegistry(REGISTRY_PORT);
            System.out.println("║  Registry đã được tạo trên port: " + REGISTRY_PORT + "                    ║");
            
            // Đăng ký service
            registry.bind(RemoteFileManager.SERVICE_NAME, fileManager);
            
            running = true;
            System.out.println("║  Service URL: " + SERVICE_URL + "                         ║");
            System.out.println("║  Để dừng server, nhấn Ctrl+C                                ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            System.out.println("[RMI-Server] Server đã sẵn sàng nhận kết nối...");
            
            // Giữ server running
            while (running) {
                Thread.sleep(1000);
            }
            
        } catch (RemoteException e) {
            System.err.println("[RMI-Server] Lỗi RemoteException: " + e.getMessage());
        } catch (AlreadyBoundException e) {
            System.err.println("[RMI-Server] Lỗi: Service đã được đăng ký trước đó!");
        } catch (InterruptedException e) {
            System.out.println("[RMI-Server] Server bị gián đoạn.");
        } catch (Exception e) {
            System.err.println("[RMI-Server] Lỗi không xác định: " + e.getMessage());
        } finally {
            stop();
        }
    }

    /**
     * Dừng server
     */
    public void stop() {
        running = false;
        System.out.println("[RMI-Server] Đang dừng server...");
        
        try {
            // Unbind service
            if (registry != null) {
                try {
                    registry.unbind(RemoteFileManager.SERVICE_NAME);
                } catch (Exception e) {
                    // Ignore nếu không unbind được
                }
            }
            
            // Đóng database
            if (fileManager != null) {
                // fileManager cleanup nếu cần
            }
            
            System.out.println("[RMI-Server] Server đã dừng.");
        } catch (Exception e) {
            System.err.println("[RMI-Server] Lỗi dừng server: " + e.getMessage());
        }
    }

    /**
     * Main method để chạy server
     */
    public static void main(String[] args) {
        // Thiết lập shutdown hook
        Runtime runtime = Runtime.getRuntime();
        Thread shutdownHook = new Thread(() -> {
            System.out.println("\n[RMI-Server] Nhận tín hiệu shutdown...");
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

