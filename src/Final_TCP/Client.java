package Final_TCP;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Scanner;

/**
 * Client class cho hệ thống quản lý tập tin tập trung
 * Cung cấp giao diện console để tương tác với server
 */
public class Client {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 2024;
    private static final String CLIENT_ROOT_DIR = "E://client";

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private Scanner scanner;
    private boolean loggedIn;
    private Path currentClientDirectory;

    public Client() {
        // Tạo thư mục client nếu chưa tồn tại
        createClientDirectory();
        scanner = new Scanner(System.in);
        loggedIn = false;
        currentClientDirectory = Paths.get(CLIENT_ROOT_DIR);
    }

    /**
     * Tạo thư mục gốc của client
     */
    private void createClientDirectory() {
        Path clientPath = Paths.get(CLIENT_ROOT_DIR);
        try {
            if (!Files.exists(clientPath)) {
                Files.createDirectories(clientPath);
                System.out.println("Thư mục client đã được tạo: " + CLIENT_ROOT_DIR);
            }
        } catch (IOException e) {
            System.err.println("Lỗi tạo thư mục client: " + e.getMessage());
        }
    }

    /**
     * Kết nối tới server
     */
    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            System.out.println("Đã kết nối tới server: " + SERVER_HOST + ":" + SERVER_PORT);
            return true;
        } catch (IOException e) {
            System.err.println("Không thể kết nối tới server: " + e.getMessage());
            return false;
        }
    }

    /**
     * Bắt đầu phiên làm việc với server
     */
    public void start() {
        if (!connect()) {
            return;
        }

        try {
            System.out.println("=== HỆ THỐNG QUẢN LÝ TẬP TIN TẬP TRUNG ===");
            System.out.println("Các lệnh có sẵn:");
            System.out.println("Trước đăng nhập: UNAME <username>, PASS <password>, EXIT");
            System.out.println("Sau đăng nhập: SC_DIR <path>, SS_DIR <path>, UPLOAD <filename>, DOWNLOAD <filename>, EXIT");

            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) continue;

                if (!processCommand(input)) {
                    break; // Thoát nếu lệnh EXIT thành công
                }
            }

        } catch (Exception e) {
            System.err.println("Lỗi: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    /**
     * Xử lý lệnh từ người dùng
     */
    private boolean processCommand(String input) {
        String[] parts = input.split(" ", 2);
        String cmd = parts[0].toUpperCase();
        String param = parts.length > 1 ? parts[1] : "";

        try {
            // Xử lý các lệnh đặc biệt (không gửi tới server)
            if (cmd.equals("SC_DIR")) {
                return handleScDir(param);
            }

            // Gửi lệnh tới server
            dos.writeUTF(input);

            // Nhận phản hồi từ server
            String response = dis.readUTF();
            System.out.println("Server: " + response);

            // Cập nhật trạng thái đăng nhập
            if (response.equals("OK Login success")) {
                loggedIn = true;
            }

            // Thoát nếu server xác nhận EXIT
            if (cmd.equals("EXIT") && response.startsWith("OK")) {
                return false;
            }

            // Xử lý upload/download đặc biệt
            if (cmd.equals("UPLOAD") && response.equals("READY")) {
                handleFileUpload(param);
            } else if (cmd.equals("DOWNLOAD")) {
                handleFileDownload(param);
            }

        } catch (IOException e) {
            System.err.println("Lỗi gửi lệnh: " + e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Xử lý lệnh SC_DIR (Set Client Directory) - chỉ xử lý ở client
     */
    private boolean handleScDir(String path) {
        try {
            Path newPath = Paths.get(CLIENT_ROOT_DIR).resolve(path).normalize();

            // Đảm bảo không đi ra ngoài thư mục client
            if (!newPath.startsWith(Paths.get(CLIENT_ROOT_DIR))) {
                System.out.println("Client: ERR Path invalid");
                return true;
            }

            // Tạo thư mục nếu chưa tồn tại
            if (!Files.exists(newPath)) {
                Files.createDirectories(newPath);
            }

            if (Files.isDirectory(newPath)) {
                currentClientDirectory = newPath;
                System.out.println("Client: OK New path is " + currentClientDirectory.toString());
            } else {
                System.out.println("Client: ERR Path invalid");
            }
        } catch (Exception e) {
            System.out.println("Client: ERR Path invalid");
        }
        return true;
    }

    /**
     * Xử lý upload file sau khi nhận READY từ server
     */
    private void handleFileUpload(String filename) {
        Path filePath = currentClientDirectory.resolve(filename);

        try {
            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                System.out.println("Client: ERR File not found: " + filename);
                return;
            }

            // Gửi kích thước file
            long fileSize = Files.size(filePath);
            dos.writeLong(fileSize);

            // Gửi dữ liệu file
            try (FileInputStream fis = new FileInputStream(filePath.toFile())) {
                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = fis.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                }
            }

            // Nhận xác nhận hoàn thành
            String response = dis.readUTF();
            System.out.println("Server: " + response);

        } catch (IOException e) {
            System.err.println("Lỗi upload file: " + e.getMessage());
        }
    }

    /**
     * Xử lý download file sau khi gửi lệnh DOWNLOAD
     */
    private void handleFileDownload(String filename) {
        try {
            // Đọc kích thước file từ server
            long fileSize = dis.readLong();

            if (fileSize == -1) {
                System.out.println("Client: ERR File not found on server");
                return;
            }

            // Tạo file tại client
            Path filePath = currentClientDirectory.resolve(filename);
            Files.createDirectories(filePath.getParent());

            // Nhận dữ liệu file
            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                byte[] buffer = new byte[4096];
                long remaining = fileSize;

                while (remaining > 0) {
                    int bytesToRead = (int) Math.min(buffer.length, remaining);
                    int bytesRead = dis.read(buffer, 0, bytesToRead);
                    if (bytesRead == -1) break;

                    fos.write(buffer, 0, bytesRead);
                    remaining -= bytesRead;
                }
            }

            System.out.println("Client: OK Download completed");

        } catch (IOException e) {
            System.err.println("Lỗi download file: " + e.getMessage());
        }
    }

    /**
     * Dọn dẹp tài nguyên
     */
    private void cleanup() {
        try {
            if (dis != null) dis.close();
            if (dos != null) dos.close();
            if (socket != null && !socket.isClosed()) socket.close();
            if (scanner != null) scanner.close();
        } catch (IOException e) {
            System.err.println("Lỗi cleanup: " + e.getMessage());
        }
    }

    /**
     * Main method để chạy client
     */
    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }
}
