package ClassFile;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Bai07 {

    /**
     * Tìm và xóa tất cả file có phần mở rộng được chỉ định trong thư mục path
     * @param path đường dẫn thư mục cần tìm kiếm
     * @param extensions các phần mở rộng cần xóa (ext1, ext2, ..., extn)
     */
    public static void deleteAll(String path, String... extensions) {
        if (path == null || extensions == null || extensions.length == 0) {
            System.out.println("Đường dẫn hoặc phần mở rộng không hợp lệ!");
            return;
        }

        // Chuyển các extension thành Set để tìm kiếm nhanh hơn
        Set<String> extSet = new HashSet<>();
        for (String ext : extensions) {
            // Đảm bảo extension có dấu chấm ở đầu
            if (ext != null && !ext.isEmpty()) {
                extSet.add(ext.startsWith(".") ? ext.toLowerCase() : "." + ext.toLowerCase());
            }
        }

        File directory = new File(path);

        // Kiểm tra đường dẫn có tồn tại không
        if (!directory.exists()) {
            System.out.println("Đường dẫn không tồn tại: " + path);
            return;
        }

        // Nếu path là một file, kiểm tra và xóa nếu đúng extension
        if (directory.isFile()) {
            deleteFileIfMatch(directory, extSet);
            return;
        }

        // Nếu path là thư mục, tìm và xóa tất cả file có extension phù hợp
        if (directory.isDirectory()) {
            deleteFilesRecursively(directory, extSet);
        }
    }

    /**
     * Xóa file nếu extension khớp với danh sách extensions
     */
    private static void deleteFileIfMatch(File file, Set<String> extensions) {
        String fileName = file.getName();
        int lastDotIndex = fileName.lastIndexOf('.');

        if (lastDotIndex > 0) {
            String fileExt = fileName.substring(lastDotIndex).toLowerCase();
            if (extensions.contains(fileExt)) {
                if (file.delete()) {
                    System.out.println("Đã xóa: " + file.getAbsolutePath());
                } else {
                    System.out.println("Không thể xóa: " + file.getAbsolutePath());
                }
            }
        }
    }

    /**
     * Đệ quy tìm và xóa tất cả file có extension phù hợp trong thư mục
     */
    private static void deleteFilesRecursively(File directory, Set<String> extensions) {
        File[] files = directory.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                // Đệ quy vào thư mục con
                deleteFilesRecursively(file, extensions);
            } else if (file.isFile()) {
                // Xóa file nếu extension khớp
                deleteFileIfMatch(file, extensions);
            }
        }
    }

    // Hàm main để test
    public static void main(String[] args) {
        // Test case 1: Xóa các file .txt và .log trong thư mục test
        System.out.println("=== Test 1: Xóa file .txt và .log ===");
        deleteAll("D:\\test", "txt", "log");

        System.out.println("\n=== Test 2: Xóa file .tmp ===");
        deleteAll("D:\\test", ".tmp");

        System.out.println("\n=== Test 3: Xóa nhiều loại file ===");
        deleteAll("D:\\test", "txt", "log", "tmp", "bak");
    }
}
