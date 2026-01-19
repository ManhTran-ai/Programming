package RemoteFileManagement;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Server - Server chính cho hệ thống Remote File Management
 * Lắng nghe trên port 55555, hỗ trợ đa luồng (multi-threading)
 */
public class Server {
    private static final int PORT = 55555;
    private static final String SERVER_ROOT_DIR = "server_files";
    private ServerSocket serverSocket;
    private DatabaseManager dbManager;
    private boolean running;

    public Server() {
        this.running = false;
        // Tạo thư mục server nếu chưa tồn tại
        createServerDirectory();
        // Khởi tạo database manager
        dbManager = new DatabaseManager();
    }

    /**
     * Tạo thư mục gốc của server
     */
    private void createServerDirectory() {
        Path serverPath = Paths.get(SERVER_ROOT_DIR);
        try {
            if (!Files.exists(serverPath)) {
                Files.createDirectories(serverPath);
                System.out.println("[Server] Thư mục server đã được tạo: " + serverPath.toAbsolutePath());
            } else {
                System.out.println("[Server] Thư mục server: " + serverPath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("[Server] Lỗi tạo thư mục server: " + e.getMessage());
        }
    }

    /**
     * Khởi động server
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            System.out.println("╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║           SERVER QUẢN LÝ FILE TỪ XA - ĐANG CHẠY             ║");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.println("║  IP: 127.0.0.1                                               ║");
            System.out.println("║  Port: " + PORT + "                                                 ║");
            System.out.println("║  Thư mục gốc: " + SERVER_ROOT_DIR + "                                      ║");
            System.out.println("║  Để dừng server, nhấn Ctrl+C                                ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("[Server] Client mới kết nối từ: " + 
                                      clientSocket.getInetAddress() + ":" + 
                                      clientSocket.getPort());

                    // Tạo thread riêng để xử lý client
                    ClientHandler clientHandler = new ClientHandler(clientSocket, dbManager);
                    Thread thread = new Thread(clientHandler);
                    thread.start();

                } catch (IOException e) {
                    if (running) {
                        System.err.println("[Server] Lỗi chấp nhận kết nối: " + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("[Server] Lỗi khởi động server: " + e.getMessage());
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
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (dbManager != null) {
                dbManager.close();
            }
            System.out.println("[Server] Server đã dừng.");
        } catch (IOException e) {
            System.err.println("[Server] Lỗi dừng server: " + e.getMessage());
        }
    }

    /**
     * Main method để chạy server
     */
    public static void main(String[] args) {
        // Thiết lập shutdown hook để đóng server khi nhấn Ctrl+C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[Server] Nhận tín hiệu shutdown...");
        }));

        Server server = new Server();
        server.start();
    }
}

