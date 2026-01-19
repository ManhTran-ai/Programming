package Final_RMI;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

/**
 * Server chính - khởi tạo RMI Registry và đăng ký LoginService
 */
public class Server {
    private static final int RMI_PORT = 1099;

    public static void main(String[] args) {
        try {
            // Khởi tạo DatabaseManager để tạo DB nếu cần
            DatabaseManager dbManager = new DatabaseManager();

            // Thêm một số user mẫu để test
            addSampleUsers(dbManager);

            // Tạo LoginService
            LoginService loginService = new LoginServiceImpl();

            // Khởi tạo RMI Registry
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);

            // Đăng ký LoginService
            registry.rebind("LoginService", loginService);

            System.out.println("Server đã khởi động thành công!");
            System.out.println("RMI Registry đang chạy trên port " + RMI_PORT);
            System.out.println("LoginService đã được đăng ký.");
            System.out.println("Nhấn Enter để dừng server...");

            // Đợi người dùng nhấn Enter để dừng
            Scanner scanner = new Scanner(System.in);
            scanner.nextLine();

            // Đóng kết nối database
            dbManager.close();

            System.out.println("Server đã dừng.");

        } catch (RemoteException e) {
            System.err.println("Lỗi khởi tạo server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Thêm một số user mẫu để test
     */
    private static void addSampleUsers(DatabaseManager dbManager) {
        // Thêm user mẫu
        if (!dbManager.userExists("admin")) {
            dbManager.addUser("admin", "123456");
            System.out.println("Đã thêm user mẫu: admin/123456");
        }
        if (!dbManager.userExists("user1")) {
            dbManager.addUser("user1", "password");
            System.out.println("Đã thêm user mẫu: user1/password");
        }
        if (!dbManager.userExists("test")) {
            dbManager.addUser("test", "test123");
            System.out.println("Đã thêm user mẫu: test/test123");
        }
    }
}
