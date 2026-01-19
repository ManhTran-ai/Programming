package Final_RMI;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Quản lý các thao tác file và thư mục
 */
public class FileManager {
    private static final String SERVER_BASE_DIR = "E://server";
    private static final String CLIENT_BASE_DIR = "E://client";

    public FileManager() {
        // Tạo thư mục mặc định nếu chưa tồn tại
        createDirectoryIfNotExists(SERVER_BASE_DIR);
        createDirectoryIfNotExists(CLIENT_BASE_DIR);
    }

    /**
     * Tạo thư mục nếu chưa tồn tại
     */
    private void createDirectoryIfNotExists(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("Thư mục " + dirPath + " đã được tạo.");
            } else {
                System.err.println("Không thể tạo thư mục " + dirPath);
            }
        }
    }

    /**
     * Kiểm tra và thay đổi thư mục server
     */
    public boolean changeServerDirectory(String currentPath, String folderName) {
        try {
            Path newPath = Paths.get(currentPath, folderName).normalize();

            // Đảm bảo không đi ra ngoài thư mục gốc
            Path basePath = Paths.get(SERVER_BASE_DIR).normalize();
            if (!newPath.startsWith(basePath)) {
                return false;
            }

            File dir = new File(newPath.toString());
            return dir.exists() && dir.isDirectory();
        } catch (Exception e) {
            System.err.println("Lỗi kiểm tra thư mục: " + e.getMessage());
            return false;
        }
    }

    /**
     * Upload file lên server
     */
    public boolean uploadFile(String serverPath, String serverFileName, byte[] fileData) {
        try {
            Path filePath = Paths.get(serverPath, serverFileName).normalize();

            // Đảm bảo không đi ra ngoài thư mục gốc
            Path basePath = Paths.get(SERVER_BASE_DIR).normalize();
            if (!filePath.getParent().startsWith(basePath)) {
                return false;
            }

            // Tạo thư mục cha nếu cần
            Files.createDirectories(filePath.getParent());

            // Ghi file
            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                fos.write(fileData);
            }

            return true;
        } catch (IOException e) {
            System.err.println("Lỗi upload file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Download file từ server
     */
    public byte[] downloadFile(String serverPath, String serverFileName) {
        try {
            Path filePath = Paths.get(serverPath, serverFileName).normalize();

            // Đảm bảo không đi ra ngoài thư mục gốc
            Path basePath = Paths.get(SERVER_BASE_DIR).normalize();
            if (!filePath.getParent().startsWith(basePath)) {
                return null;
            }

            File file = filePath.toFile();
            if (!file.exists() || !file.isFile()) {
                return null;
            }

            // Đọc file
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            System.err.println("Lỗi download file: " + e.getMessage());
            return null;
        }
    }

    /**
     * Lấy đường dẫn server mặc định
     */
    public String getServerBaseDir() {
        return SERVER_BASE_DIR;
    }

    /**
     * Lấy đường dẫn client mặc định
     */
    public String getClientBaseDir() {
        return CLIENT_BASE_DIR;
    }

    /**
     * Kiểm tra file có tồn tại trên client không
     */
    public boolean clientFileExists(String clientPath, String fileName) {
        Path filePath = Paths.get(clientPath, fileName).normalize();
        Path basePath = Paths.get(CLIENT_BASE_DIR).normalize();

        if (!filePath.getParent().startsWith(basePath)) {
            return false;
        }

        File file = filePath.toFile();
        return file.exists() && file.isFile();
    }

    /**
     * Đọc file từ client
     */
    public byte[] readClientFile(String clientPath, String fileName) {
        try {
            Path filePath = Paths.get(clientPath, fileName).normalize();
            Path basePath = Paths.get(CLIENT_BASE_DIR).normalize();

            if (!filePath.getParent().startsWith(basePath)) {
                return null;
            }

            File file = filePath.toFile();
            if (!file.exists() || !file.isFile()) {
                return null;
            }

            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            System.err.println("Lỗi đọc file client: " + e.getMessage());
            return null;
        }
    }

    /**
     * Ghi file xuống client
     */
    public boolean writeClientFile(String clientPath, String fileName, byte[] fileData) {
        try {
            Path filePath = Paths.get(clientPath, fileName).normalize();
            Path basePath = Paths.get(CLIENT_BASE_DIR).normalize();

            if (!filePath.getParent().startsWith(basePath)) {
                return false;
            }

            // Tạo thư mục cha nếu cần
            Files.createDirectories(filePath.getParent());

            // Ghi file
            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                fos.write(fileData);
            }

            return true;
        } catch (IOException e) {
            System.err.println("Lỗi ghi file client: " + e.getMessage());
            return false;
        }
    }
}
