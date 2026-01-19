package RemoteFileManagement;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;

/**
 * ClientHandler - Xử lý từng kết nối client
 * Hỗ trợ đa luồng và quản lý trạng thái đăng nhập
 * Xử lý các lệnh theo giao thức POP3-like và các lệnh quản lý file
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private DatabaseManager dbManager;
    private boolean loggedIn;
    private String currentUser;
    private Path currentDirectory;
    private static final String SERVER_ROOT_DIR = "server_files";

    public ClientHandler(Socket socket, DatabaseManager dbManager) {
        this.socket = socket;
        this.dbManager = dbManager;
        this.loggedIn = false;
        this.currentUser = null;
        this.currentDirectory = Paths.get(SERVER_ROOT_DIR);
    }

    @Override
    public void run() {
        System.out.println("[Server] Client mới kết nối: " + socket.getInetAddress());

        try {
            // Khởi tạo streams - sử dụng BufferedReader và PrintWriter cho text-based communication
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            // Gửi thông điệp chào mừng
            writer.println("Welcome to File management");

            // Xử lý các lệnh từ client
            while (!socket.isClosed()) {
                String command = reader.readLine();
                if (command == null) {
                    break; // Client ngắt kết nối
                }

                System.out.println("[Server] Nhận lệnh từ " + currentUser + ": " + command);
                String response = processCommand(command);
                writer.println(response);

                // Thoát nếu client gửi QUIT
                if (command.equalsIgnoreCase("QUIT") && 
                    (response.startsWith("OK") || response.startsWith("BYE"))) {
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("[Server] Lỗi xử lý client: " + e.getMessage());
        } finally {
            cleanup();
            System.out.println("[Server] Client ngắt kết nối: " + socket.getInetAddress());
        }
    }

    /**
     * Xử lý lệnh từ client
     * @param command Lệnh từ client
     * @return Phản hồi cho client
     */
    private String processCommand(String command) {
        // Kiểm tra đăng nhập cho các lệnh quản lý file
        boolean requiresLogin = !isLoginCommand(command);

        if (requiresLogin && !loggedIn) {
            return "ERR Vui lòng đăng nhập trước! Sử dụng: USER | <username>, PASS | <password>";
        }

        try {
            // Phân tích cú pháp lệnh: COMMAND | Param1 | Param2...
            String[] parts = command.split("\\|");
            String cmd = parts[0].trim().toUpperCase();
            String param1 = parts.length > 1 ? parts[1].trim() : "";
            String param2 = parts.length > 2 ? parts[2].trim() : "";

            switch (cmd) {
                // === Giai đoạn đăng nhập (POP3-like) ===
                case "USER":
                    return handleUser(param1);
                case "PASS":
                    return handlePass(param1);
                case "QUIT":
                    return handleQuit();

                // === Giai đoạn quản lý file ===
                case "SET FOLDER":
                    return handleSetFolder(param1);
                case "VIEW":
                    return handleView(param1);
                case "COPY":
                    return handleCopy(param1, param2);
                case "MOVE":
                    return handleMove(param1, param2);
                case "RENAME":
                    return handleRename(param1, param2);
                case "QUIT":
                    return handleQuit();

                default:
                    return "ERR Lệnh không hợp lệ! Các lệnh hợp lệ: " +
                           "USER | <username>, PASS | <password>, QUIT, " +
                           "SET FOLDER | <path>, VIEW | <file/path>, " +
                           "COPY | <source> | <dest>, MOVE | <source> | <dest>, " +
                           "RENAME | <source> | <dest>, QUIT";
            }
        } catch (Exception e) {
            return "ERR Lỗi xử lý lệnh: " + e.getMessage();
        }
    }

    /**
     * Kiểm tra xem lệnh có phải là lệnh đăng nhập không
     */
    private boolean isLoginCommand(String command) {
        String upperCmd = command.toUpperCase().trim();
        return upperCmd.startsWith("USER") || 
               upperCmd.startsWith("PASS") || 
               upperCmd.equals("QUIT");
    }

    // ==================== CÁC HÀM XỬ LÝ ĐĂNG NHẬP ====================

    /**
     * Xử lý lệnh USER
     */
    private String handleUser(String username) {
        if (loggedIn) {
            return "ERR Bạn đã đăng nhập rồi!";
        }

        if (username == null || username.isEmpty()) {
            return "ERR Vui lòng nhập username! Cú pháp: USER | <username>";
        }

        if (dbManager.userExists(username)) {
            currentUser = username;
            return "OK User accepted. Vui lòng nhập mật khẩu: PASS | <password>";
        } else {
            return "ERR User không tồn tại!";
        }
    }

    /**
     * Xử lý lệnh PASS
     */
    private String handlePass(String password) {
        if (loggedIn) {
            return "ERR Bạn đã đăng nhập rồi!";
        }

        if (currentUser == null) {
            return "ERR Vui lòng nhập username trước! Cú pháp: USER | <username>";
        }

        if (password == null || password.isEmpty()) {
            return "ERR Vui lòng nhập mật khẩu! Cú pháp: PASS | <password>";
        }

        if (dbManager.authenticateUser(currentUser, password)) {
            loggedIn = true;
            // Đảm bảo thư mục gốc tồn tại
            ensureDirectoryExists(currentDirectory);
            return "OK Đăng nhập thành công! Chào mừng " + currentUser + 
                   ". Thư mục hiện tại: " + currentDirectory.toAbsolutePath();
        } else {
            currentUser = null;
            return "ERR Mật khẩu không đúng!";
        }
    }

    /**
     * Xử lý lệnh QUIT
     */
    private String handleQuit() {
        loggedIn = false;
        currentUser = null;
        return "BYE Đã ngắt kết nối. Tạm biệt!";
    }

    // ==================== CÁC HÀM XỬ LÝ FILE ====================

    /**
     * Xử lý lệnh SET FOLDER - Đặt thư mục làm việc
     */
    private String handleSetFolder(String path) {
        if (path == null || path.isEmpty()) {
            return "ERR Vui lòng nhập đường dẫn! Cú pháp: SET FOLDER | <path>";
        }

        try {
            Path newPath = currentDirectory.resolve(path).normalize();

            // Đảm bảo không đi ra ngoài thư mục server
            Path rootPath = Paths.get(SERVER_ROOT_DIR).toAbsolutePath().normalize();
            if (!newPath.startsWith(rootPath)) {
                return "ERR Đường dẫn không hợp lệ! Không thể truy cập thư mục ngoài: " + rootPath;
            }

            // Tạo thư mục nếu chưa tồn tại
            if (!Files.exists(newPath)) {
                Files.createDirectories(newPath);
            }

            if (Files.isDirectory(newPath)) {
                currentDirectory = newPath;
                return "OK Đã đặt thư mục làm việc: " + currentDirectory.toAbsolutePath();
            } else {
                return "ERR Đường dẫn không phải là thư mục!";
            }
        } catch (IOException e) {
            return "ERR Lỗi tạo thư mục: " + e.getMessage();
        } catch (Exception e) {
            return "ERR Lỗi: " + e.getMessage();
        }
    }

    /**
     * Xử lý lệnh VIEW - Xem nội dung file hoặc danh sách thư mục
     */
    private String handleView(String path) {
        if (path == null || path.isEmpty()) {
            return "ERR Vui lòng nhập đường dẫn! Cú pháp: VIEW | <file hoặc path>";
        }

        try {
            Path targetPath = currentDirectory.resolve(path).normalize();

            // Đảm bảo không đi ra ngoài thư mục server
            Path rootPath = Paths.get(SERVER_ROOT_DIR).toAbsolutePath().normalize();
            if (!targetPath.startsWith(rootPath)) {
                return "ERR Đường dẫn không hợp lệ!";
            }

            if (!Files.exists(targetPath)) {
                return "ERR Đường dẫn không tồn tại: " + path;
            }

            if (Files.isDirectory(targetPath)) {
                // Liệt kê các file và thư mục con
                StringBuilder sb = new StringBuilder();
                sb.append("OK Danh sách trong thư mục ").append(targetPath.getFileName()).append(":\n");
                
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(targetPath)) {
                    for (Path entry : stream) {
                        String type = Files.isDirectory(entry) ? "[DIR]" : "[FILE]";
                        String size = Files.isRegularFile(entry) ? 
                            String.format(" %d bytes", Files.size(entry)) : "";
                        sb.append("  ").append(type).append(" ").append(entry.getFileName())
                          .append(size).append("\n");
                    }
                }
                return sb.toString().trim();
            } else if (Files.isRegularFile(targetPath)) {
                // Đọc nội dung file
                StringBuilder content = new StringBuilder();
                try (BufferedReader fileReader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(targetPath.toFile()), "UTF-8"))) {
                    String line;
                    int lineCount = 0;
                    while ((line = fileReader.readLine()) != null && lineCount < 100) {
                        content.append(line).append("\n");
                        lineCount++;
                    }
                    if (lineReaderHasMore(fileReader)) {
                        content.append("... (nội dung đã rút gọn) ...");
                    }
                }
                return "OK Nội dung file " + targetPath.getFileName() + ":\n" + content.toString();
            } else {
                return "ERR Đường dẫn không hợp lệ!";
            }
        } catch (IOException e) {
            return "ERR Lỗi đọc file: " + e.getMessage();
        } catch (Exception e) {
            return "ERR Lỗi: " + e.getMessage();
        }
    }

    /**
     * Kiểm tra còn dữ liệu trong stream không
     */
    private boolean lineReaderHasMore(BufferedReader reader) throws IOException {
        reader.mark(1);
        int nextChar = reader.read();
        reader.reset();
        return nextChar != -1;
    }

    /**
     * Xử lý lệnh COPY - Sao chép file
     */
    private String handleCopy(String source, String dest) {
        if (source == null || source.isEmpty() || dest == null || dest.isEmpty()) {
            return "ERR Cú pháp: COPY | <source_file> | <dest_file>";
        }

        try {
            Path sourcePath = currentDirectory.resolve(source).normalize();
            Path destPath = currentDirectory.resolve(dest).normalize();

            // Kiểm tra đường dẫn hợp lệ
            Path rootPath = Paths.get(SERVER_ROOT_DIR).toAbsolutePath().normalize();
            if (!sourcePath.startsWith(rootPath) || !destPath.startsWith(rootPath)) {
                return "ERR Đường dẫn không hợp lệ!";
            }

            if (!Files.exists(sourcePath)) {
                return "ERR File nguồn không tồn tại: " + source;
            }

            if (!Files.isRegularFile(sourcePath)) {
                return "ERR Nguồn phải là file!";
            }

            // Tạo thư mục đích nếu cần
            Files.createDirectories(destPath.getParent());

            // Sao chép file
            Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
            return "OK Đã sao chép " + source + " -> " + dest;

        } catch (IOException e) {
            return "ERR Lỗi sao chép: " + e.getMessage();
        } catch (Exception e) {
            return "ERR Lỗi: " + e.getMessage();
        }
    }

    /**
     * Xử lý lệnh MOVE - Di chuyển file
     */
    private String handleMove(String source, String dest) {
        if (source == null || source.isEmpty() || dest == null || dest.isEmpty()) {
            return "ERR Cú pháp: MOVE | <source_file> | <dest_file>";
        }

        try {
            Path sourcePath = currentDirectory.resolve(source).normalize();
            Path destPath = currentDirectory.resolve(dest).normalize();

            // Kiểm tra đường dẫn hợp lệ
            Path rootPath = Paths.get(SERVER_ROOT_DIR).toAbsolutePath().normalize();
            if (!sourcePath.startsWith(rootPath) || !destPath.startsWith(rootPath)) {
                return "ERR Đường dẫn không hợp lệ!";
            }

            if (!Files.exists(sourcePath)) {
                return "ERR File nguồn không tồn tại: " + source;
            }

            if (!Files.isRegularFile(sourcePath)) {
                return "ERR Nguồn phải là file!";
            }

            // Tạo thư mục đích nếu cần
            Files.createDirectories(destPath.getParent());

            // Di chuyển file
            Files.move(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
            return "OK Đã di chuyển " + source + " -> " + dest;

        } catch (IOException e) {
            return "ERR Lỗi di chuyển: " + e.getMessage();
        } catch (Exception e) {
            return "ERR Lỗi: " + e.getMessage();
        }
    }

    /**
     * Xử lý lệnh RENAME - Đổi tên file
     */
    private String handleRename(String source, String dest) {
        if (source == null || source.isEmpty() || dest == null || dest.isEmpty()) {
            return "ERR Cú pháp: RENAME | <source_file> | <dest_file>";
        }

        try {
            Path sourcePath = currentDirectory.resolve(source).normalize();
            Path destPath = currentDirectory.resolve(source).resolveSibling(dest).normalize();

            // Kiểm tra đường dẫn hợp lệ
            Path rootPath = Paths.get(SERVER_ROOT_DIR).toAbsolutePath().normalize();
            if (!sourcePath.startsWith(rootPath) || !destPath.startsWith(rootPath)) {
                return "ERR Đường dẫn không hợp lệ!";
            }

            if (!Files.exists(sourcePath)) {
                return "ERR File không tồn tại: " + source;
            }

            // Đổi tên file
            Files.move(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
            return "OK Đã đổi tên " + source + " -> " + dest;

        } catch (IOException e) {
            return "ERR Lỗi đổi tên: " + e.getMessage();
        } catch (Exception e) {
            return "ERR Lỗi: " + e.getMessage();
        }
    }

    /**
     * Đảm bảo thư mục tồn tại
     */
    private void ensureDirectoryExists(Path dir) {
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (IOException e) {
            System.err.println("[Server] Lỗi tạo thư mục: " + e.getMessage());
        }
    }

    /**
     * Dọn dẹp tài nguyên khi ngắt kết nối
     */
    private void cleanup() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("[Server] Lỗi cleanup: " + e.getMessage());
        }
    }
}

