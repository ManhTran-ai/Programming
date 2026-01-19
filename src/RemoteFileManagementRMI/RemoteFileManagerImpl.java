package RemoteFileManagementRMI;

import java.io.*;
import java.nio.file.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RemoteFileManagerImpl - Implementation của RemoteFileManager interface
 * Xử lý các yêu cầu từ client qua RMI
 */
public class RemoteFileManagerImpl extends UnicastRemoteObject implements RemoteFileManager {
    private static final long serialVersionUID = 1L;
    private static final String SERVER_ROOT_DIR = "server_files_rmi";
    
    private DatabaseManager dbManager;
    private ConcurrentHashMap<String, Path> userDirectories;
    private ConcurrentHashMap<String, Boolean> loggedInUsers;

    public RemoteFileManagerImpl() throws RemoteException {
        super();
        this.dbManager = new DatabaseManager();
        this.userDirectories = new ConcurrentHashMap<>();
        this.loggedInUsers = new ConcurrentHashMap<>();
        
        // Tạo thư mục gốc của server
        createServerDirectory();
        System.out.println("[RMI-Server] RemoteFileManagerImpl đã được khởi tạo!");
    }

    /**
     * Tạo thư mục gốc của server
     */
    private void createServerDirectory() {
        Path serverPath = Paths.get(SERVER_ROOT_DIR);
        try {
            if (!Files.exists(serverPath)) {
                Files.createDirectories(serverPath);
                System.out.println("[RMI-Server] Thư mục server đã được tạo: " + serverPath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("[RMI-Server] Lỗi tạo thư mục server: " + e.getMessage());
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
            System.err.println("[RMI-Server] Lỗi tạo thư mục: " + e.getMessage());
        }
    }

    /**
     * Lấy đường dẫn đầy đủ với kiểm tra bảo mật
     */
    private Path getFullPath(String username, String path) throws RemoteException {
        Path userDir = userDirectories.get(username);
        if (userDir == null) {
            throw new RemoteException("Chưa đăng nhập! Vui lòng đăng nhập trước.");
        }
        
        Path fullPath = userDir.resolve(path).normalize();
        Path rootPath = Paths.get(SERVER_ROOT_DIR).toAbsolutePath().normalize();
        
        // Kiểm tra bảo mật: không cho phép truy cập ngoài thư mục server
        if (!fullPath.startsWith(rootPath)) {
            throw new RemoteException("Đường dẫn không hợp lệ! Không thể truy cập ngoài thư mục server.");
        }
        
        return fullPath;
    }

    @Override
    public String login(String username, String password) throws RemoteException {
        if (username == null || username.trim().isEmpty()) {
            return "ERR Vui lòng nhập username! Cú pháp: USER | <username>";
        }
        
        if (password == null || password.isEmpty()) {
            return "ERR Vui lòng nhập mật khẩu! Cú pháp: PASS | <password>";
        }
        
        if (loggedInUsers.getOrDefault(username, false)) {
            return "ERR User " + username + " đã đăng nhập trên phiên khác!";
        }
        
        // Kiểm tra username trước
        if (!dbManager.userExists(username.trim())) {
            return "ERR User không tồn tại!";
        }
        
        // Xác thực password
        if (dbManager.authenticateUser(username.trim(), password)) {
            loggedInUsers.put(username, true);
            userDirectories.put(username, Paths.get(SERVER_ROOT_DIR).toAbsolutePath());
            System.out.println("[RMI-Server] User " + username + " đã đăng nhập thành công!");
            return "OK Đăng nhập thành công! Chào mừng " + username + 
                   ". Thư mục hiện tại: " + getCurrentDirectory(username);
        } else {
            return "ERR Mật khẩu không đúng!";
        }
    }

    @Override
    public String logout(String username) throws RemoteException {
        if (username != null && loggedInUsers.containsKey(username)) {
            loggedInUsers.remove(username);
            userDirectories.remove(username);
            System.out.println("[RMI-Server] User " + username + " đã đăng xuất.");
        }
        return "OK Đã đăng xuất. Tạm biệt!";
    }

    @Override
    public String setFolder(String username, String path) throws RemoteException {
        if (!loggedInUsers.getOrDefault(username, false)) {
            return "ERR Vui lòng đăng nhập trước!";
        }
        
        if (path == null || path.trim().isEmpty()) {
            return "ERR Vui lòng nhập đường dẫn! Cú pháp: SET FOLDER | <path>";
        }
        
        try {
            Path userDir = userDirectories.get(username);
            Path newPath = userDir.resolve(path).normalize();
            Path rootPath = Paths.get(SERVER_ROOT_DIR).toAbsolutePath().normalize();
            
            // Kiểm tra bảo mật
            if (!newPath.startsWith(rootPath)) {
                return "ERR Đường dẫn không hợp lệ! Không thể truy cập ngoài thư mục server.";
            }
            
            // Tạo thư mục nếu chưa tồn tại
            if (!Files.exists(newPath)) {
                Files.createDirectories(newPath);
            }
            
            if (Files.isDirectory(newPath)) {
                userDirectories.put(username, newPath);
                return "OK Đã đặt thư mục làm việc: " + newPath.toAbsolutePath();
            } else {
                return "ERR Đường dẫn không phải là thư mục!";
            }
        } catch (IOException e) {
            return "ERR Lỗi tạo thư mục: " + e.getMessage();
        } catch (Exception e) {
            return "ERR Lỗi: " + e.getMessage();
        }
    }

    @Override
    public String view(String username, String path) throws RemoteException {
        if (!loggedInUsers.getOrDefault(username, false)) {
            return "ERR Vui lòng đăng nhập trước!";
        }
        
        if (path == null || path.trim().isEmpty()) {
            return "ERR Vui lòng nhập đường dẫn! Cú pháp: VIEW | <file hoặc path>";
        }
        
        try {
            // Xử lý view thư mục hiện tại
            if (path.trim().equals(".")) {
                path = "";
            }
            
            Path targetPath = getFullPath(username, path);
            
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
                // Đọc nội dung file (giới hạn 100 dòng)
                StringBuilder content = new StringBuilder();
                try (BufferedReader fileReader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(targetPath.toFile()), "UTF-8"))) {
                    String line;
                    int lineCount = 0;
                    while ((line = fileReader.readLine()) != null && lineCount < 100) {
                        content.append(line).append("\n");
                        lineCount++;
                    }
                    // Kiểm tra còn dữ liệu không
                    fileReader.mark(1);
                    int nextChar = fileReader.read();
                    fileReader.reset();
                    if (nextChar != -1) {
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

    @Override
    public String copy(String username, String sourceFile, String destFile) throws RemoteException {
        if (!loggedInUsers.getOrDefault(username, false)) {
            return "ERR Vui lòng đăng nhập trước!";
        }
        
        if (sourceFile == null || sourceFile.trim().isEmpty() || 
            destFile == null || destFile.trim().isEmpty()) {
            return "ERR Cú pháp: COPY | <source_file> | <dest_file>";
        }
        
        try {
            Path sourcePath = getFullPath(username, sourceFile);
            Path destPath = getFullPath(username, destFile);
            
            if (!Files.exists(sourcePath)) {
                return "ERR File nguồn không tồn tại: " + sourceFile;
            }
            
            if (!Files.isRegularFile(sourcePath)) {
                return "ERR Nguồn phải là file!";
            }
            
            // Tạo thư mục cha nếu cần
            Files.createDirectories(destPath.getParent());
            
            // Sao chép file
            Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
            return "OK Đã sao chép " + sourceFile + " -> " + destFile;
            
        } catch (IOException e) {
            return "ERR Lỗi sao chép: " + e.getMessage();
        } catch (Exception e) {
            return "ERR Lỗi: " + e.getMessage();
        }
    }

    @Override
    public String move(String username, String sourceFile, String destFile) throws RemoteException {
        if (!loggedInUsers.getOrDefault(username, false)) {
            return "ERR Vui lòng đăng nhập trước!";
        }
        
        if (sourceFile == null || sourceFile.trim().isEmpty() || 
            destFile == null || destFile.trim().isEmpty()) {
            return "ERR Cú pháp: MOVE | <source_file> | <dest_file>";
        }
        
        try {
            Path sourcePath = getFullPath(username, sourceFile);
            Path destPath = getFullPath(username, destFile);
            
            if (!Files.exists(sourcePath)) {
                return "ERR File nguồn không tồn tại: " + sourceFile;
            }
            
            if (!Files.isRegularFile(sourcePath)) {
                return "ERR Nguồn phải là file!";
            }
            
            // Tạo thư mục cha nếu cần
            Files.createDirectories(destPath.getParent());
            
            // Di chuyển file
            Files.move(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
            return "OK Đã di chuyển " + sourceFile + " -> " + destFile;
            
        } catch (IOException e) {
            return "ERR Lỗi di chuyển: " + e.getMessage();
        } catch (Exception e) {
            return "ERR Lỗi: " + e.getMessage();
        }
    }

    @Override
    public String rename(String username, String sourceFile, String destFile) throws RemoteException {
        if (!loggedInUsers.getOrDefault(username, false)) {
            return "ERR Vui lòng đăng nhập trước!";
        }
        
        if (sourceFile == null || sourceFile.trim().isEmpty() || 
            destFile == null || destFile.trim().isEmpty()) {
            return "ERR Cú pháp: RENAME | <source_file> | <dest_file>";
        }
        
        try {
            Path userDir = userDirectories.get(username);
            Path sourcePath = userDir.resolve(sourceFile).normalize();
            Path destPath = userDir.resolve(sourceFile).resolveSibling(destFile).normalize();
            
            // Kiểm tra bảo mật
            Path rootPath = Paths.get(SERVER_ROOT_DIR).toAbsolutePath().normalize();
            if (!sourcePath.startsWith(rootPath) || !destPath.startsWith(rootPath)) {
                return "ERR Đường dẫn không hợp lệ!";
            }
            
            if (!Files.exists(sourcePath)) {
                return "ERR File không tồn tại: " + sourceFile;
            }
            
            // Đổi tên file
            Files.move(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
            return "OK Đã đổi tên " + sourceFile + " -> " + destFile;
            
        } catch (IOException e) {
            return "ERR Lỗi đổi tên: " + e.getMessage();
        } catch (Exception e) {
            return "ERR Lỗi: " + e.getMessage();
        }
    }

    @Override
    public String getCurrentDirectory(String username) throws RemoteException {
        Path userDir = userDirectories.get(username);
        if (userDir == null) {
            return SERVER_ROOT_DIR;
        }
        return userDir.toAbsolutePath().toString();
    }

    @Override
    public boolean isLoggedIn(String username) throws RemoteException {
        return loggedInUsers.getOrDefault(username, false);
    }
}

