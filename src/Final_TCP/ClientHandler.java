package Final_TCP;

import java.io.*;
import java.net.*;
import java.nio.file.*;

/**
 * ClientHandler class xử lý từng client connection
 * Hỗ trợ đa luồng và quản lý trạng thái đăng nhập
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private DatabaseManager dbManager;
    private boolean loggedIn;
    private String currentUser;
    private Path currentServerDirectory;
    private static final String SERVER_ROOT_DIR = "E://server";

    public ClientHandler(Socket socket, DatabaseManager dbManager) {
        this.socket = socket;
        this.dbManager = dbManager;
        this.loggedIn = false;
        this.currentUser = null;
        this.currentServerDirectory = Paths.get(SERVER_ROOT_DIR);
    }

    @Override
    public void run() {
        try {
            // Khởi tạo streams
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            System.out.println("Client handler started for: " + socket.getInetAddress());

            // Xử lý các lệnh từ client
            while (true) {
                String command = dis.readUTF();
                System.out.println("Received command: " + command);

                if (!processCommand(command)) {
                    break; // Thoát khỏi vòng lặp nếu lệnh EXIT
                }
            }

        } catch (EOFException e) {
            System.out.println("Client disconnected: " + socket.getInetAddress());
        } catch (IOException e) {
            System.err.println("Lỗi xử lý client: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    /**
     * Xử lý lệnh từ client
     */
    private boolean processCommand(String command) throws IOException {
        String[] parts = command.split(" ", 2);
        String cmd = parts[0].toUpperCase();
        String param = parts.length > 1 ? parts[1] : "";

        switch (cmd) {
            case "UNAME":
                return handleUname(param);
            case "PASS":
                return handlePass(param);
            case "EXIT":
                return handleExit();
            case "SS_DIR":
                return handleSsDir(param);
            case "UPLOAD":
                return handleUpload(param);
            case "DOWNLOAD":
                return handleDownload(param);
            default:
                dos.writeUTF("ERR Unknown command");
                return true;
        }
    }

    /**
     * Xử lý lệnh UNAME
     */
    private boolean handleUname(String username) throws IOException {
        if (loggedIn) {
            dos.writeUTF("ERR Already logged in");
            return true;
        }

        if (dbManager.userExists(username)) {
            currentUser = username;
            dos.writeUTF("OK User accepted");
        } else {
            dos.writeUTF("ERR User not found");
        }
        return true;
    }

    /**
     * Xử lý lệnh PASS
     */
    private boolean handlePass(String password) throws IOException {
        if (loggedIn) {
            dos.writeUTF("ERR Already logged in");
            return true;
        }

        if (currentUser == null) {
            dos.writeUTF("ERR Username not provided");
            return true;
        }

        if (dbManager.authenticateUser(currentUser, password)) {
            loggedIn = true;
            dos.writeUTF("OK Login success");
        } else {
            dos.writeUTF("ERR Wrong password");
            currentUser = null;
        }
        return true;
    }

    /**
     * Xử lý lệnh EXIT
     */
    private boolean handleExit() throws IOException {
        dos.writeUTF("OK Goodbye");
        return false; // Thoát khỏi vòng lặp
    }

    /**
     * Xử lý lệnh SS_DIR (Set Server Directory)
     */
    private boolean handleSsDir(String path) throws IOException {
        if (!loggedIn) {
            dos.writeUTF("ERR Not logged in");
            return true;
        }

        try {
            Path newPath = Paths.get(SERVER_ROOT_DIR).resolve(path).normalize();

            // Đảm bảo không đi ra ngoài thư mục server
            if (!newPath.startsWith(Paths.get(SERVER_ROOT_DIR))) {
                dos.writeUTF("ERR Path invalid");
                return true;
            }

            // Tạo thư mục nếu chưa tồn tại
            if (!Files.exists(newPath)) {
                Files.createDirectories(newPath);
            }

            if (Files.isDirectory(newPath)) {
                currentServerDirectory = newPath;
                dos.writeUTF("OK New path is " + currentServerDirectory.toString());
            } else {
                dos.writeUTF("ERR Path invalid");
            }
        } catch (Exception e) {
            dos.writeUTF("ERR Path invalid");
        }
        return true;
    }

    /**
     * Xử lý lệnh UPLOAD
     */
    private boolean handleUpload(String filename) throws IOException {
        if (!loggedIn) {
            dos.writeUTF("ERR Not logged in");
            return true;
        }

        // Kiểm tra quyền ghi
        Path filePath = currentServerDirectory.resolve(filename);
        try {
            // Đảm bảo không ghi ra ngoài thư mục server
            if (!filePath.startsWith(Paths.get(SERVER_ROOT_DIR))) {
                dos.writeUTF("ERR Access denied");
                return true;
            }

            // Tạo thư mục cha nếu cần
            Files.createDirectories(filePath.getParent());

            dos.writeUTF("READY");

            // Đọc kích thước file
            long fileSize = dis.readLong();

            // Đọc dữ liệu file
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

            dos.writeUTF("OK Upload completed");

        } catch (Exception e) {
            dos.writeUTF("ERR Upload failed: " + e.getMessage());
        }
        return true;
    }

    /**
     * Xử lý lệnh DOWNLOAD
     */
    private boolean handleDownload(String filename) throws IOException {
        if (!loggedIn) {
            dos.writeUTF("ERR Not logged in");
            return true;
        }

        Path filePath = currentServerDirectory.resolve(filename);

        // Đảm bảo không đọc ra ngoài thư mục server
        if (!filePath.startsWith(Paths.get(SERVER_ROOT_DIR))) {
            dos.writeLong(-1);
            return true;
        }

        try {
            if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
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
            } else {
                dos.writeLong(-1);
            }

        } catch (Exception e) {
            dos.writeLong(-1);
        }
        return true;
    }

    /**
     * Dọn dẹp tài nguyên
     */
    private void cleanup() {
        try {
            if (dis != null) dis.close();
            if (dos != null) dos.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Lỗi cleanup: " + e.getMessage());
        }
    }
}
