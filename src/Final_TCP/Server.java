package Final_TCP;

import java.io.*;
import java.net.*;
import java.nio.file.*;

/**
 * Server class cho hệ thống quản lý tập tin tập trung
 * Lắng nghe trên port 2024, hỗ trợ đa luồng
 */
public class Server {
    private static final int PORT = 2024;
    private static final String SERVER_ROOT_DIR = "E://server";
    private ServerSocket serverSocket;
    private DatabaseManager dbManager;

    public Server() {
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
                System.out.println("Thư mục server đã được tạo: " + SERVER_ROOT_DIR);
            }
        } catch (IOException e) {
            System.err.println("Lỗi tạo thư mục server: " + e.getMessage());
        }
    }

    /**
     * Khởi động server
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server đang lắng nghe trên port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client mới kết nối: " + clientSocket.getInetAddress());

                // Tạo thread riêng để xử lý client
                ClientHandler clientHandler = new ClientHandler(clientSocket, dbManager);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("Lỗi server: " + e.getMessage());
        } finally {
            stop();
        }
    }

    /**
     * Dừng server
     */
    public void stop() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (dbManager != null) {
                dbManager.close();
            }
        } catch (IOException e) {
            System.err.println("Lỗi đóng server: " + e.getMessage());
        }
    }

    /**
     * Main method để chạy server
     */
    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}
