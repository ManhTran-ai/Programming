package ProductShoppingTCP;

import java.io.*;
import java.net.*;

/**
 * Server - TCP Server cho hệ thống Mua bán Sản phẩm
 * Lắng nghe trên port 5918, hỗ trợ đa luồng
 */
public class Server {
    private static final int PORT = 5918;
    private ServerSocket serverSocket;
    private DatabaseManager dbManager;

    public Server() {
        // Khởi tạo database manager
        dbManager = new DatabaseManager();
    }

    /**
     * Khởi động server
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║    SERVER MUA BÁN SẢN PHẨM (TCP) - ĐANG CHẠY                ║");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.println("║  IP: 127.0.0.1                                               ║");
            System.out.println("║  Port: " + PORT + "                                                 ║");
            System.out.println("║  De dung server, nhan Ctrl+C                                ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            System.out.println("[Server] Server da san sang nhan ket noi...");

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("[Server] Client moi ket noi tu: " + 
                                      clientSocket.getInetAddress() + ":" + 
                                      clientSocket.getPort());

                    // Tạo thread riêng để xử lý client
                    ClientHandler clientHandler = new ClientHandler(clientSocket, dbManager);
                    Thread thread = new Thread(clientHandler);
                    thread.start();

                } catch (IOException e) {
                    System.err.println("[Server] Loi chap nhan ket noi: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[Server] Loi khoi dong server: " + e.getMessage());
        } finally {
            stop();
        }
    }

    /**
     * Dừng server
     */
    public void stop() {
        System.out.println("[Server] Dang dung server...");
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (dbManager != null) {
                dbManager.close();
            }
            System.out.println("[Server] Server da dung.");
        } catch (IOException e) {
            System.err.println("[Server] Loi dung server: " + e.getMessage());
        }
    }

    /**
     * Main method để chạy server
     */
    public static void main(String[] args) {
        // Thiết lập shutdown hook
        Runtime runtime = Runtime.getRuntime();
        Thread shutdownHook = new Thread(() -> {
            System.out.println("\n[Server] Nhan tin hieu shutdown...");
        });
        runtime.addShutdownHook(shutdownHook);

        Server server = new Server();
        server.start();
    }
}

