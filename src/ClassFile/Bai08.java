package ClassFile;

import java.io.*;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;

public class Bai08 {

    /**
     * Copy tất cả các file có phần mở rộng được chỉ định từ thư mục nguồn sang thư mục đích
     * @param sDir đường dẫn thư mục nguồn
     * @param dDir đường dẫn thư mục đích
     * @param extensions các phần mở rộng cần copy (ext1, ext2, ..., extn)
     */
    public static void copyAll(String sDir, String dDir, String... extensions) {
        if (sDir == null || dDir == null || extensions == null || extensions.length == 0) {
            System.out.println("Tham số không hợp lệ!");
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

        File sourceDir = new File(sDir);
        File destDir = new File(dDir);

        // Kiểm tra thư mục nguồn có tồn tại không
        if (!sourceDir.exists()) {
            System.out.println("Thư mục nguồn không tồn tại: " + sDir);
            return;
        }

        if (!sourceDir.isDirectory()) {
            System.out.println("Đường dẫn nguồn không phải là thư mục: " + sDir);
            return;
        }

        // Tạo thư mục đích nếu chưa tồn tại
        if (!destDir.exists()) {
            if (destDir.mkdirs()) {
                System.out.println("Đã tạo thư mục đích: " + dDir);
            } else {
                System.out.println("Không thể tạo thư mục đích: " + dDir);
                return;
            }
        }

        if (!destDir.isDirectory()) {
            System.out.println("Đường dẫn đích không phải là thư mục: " + dDir);
            return;
        }

        // Bắt đầu copy file
        int copiedCount = copyFilesRecursively(sourceDir, destDir, extSet, sDir);
        System.out.println("Đã copy " + copiedCount + " file thành công.");
    }

    /**
     * Copy đệ quy tất cả các file có extension phù hợp từ thư mục nguồn sang thư mục đích
     * @param sourceDir thư mục nguồn hiện tại
     * @param destDir thư mục đích gốc
     * @param extSet tập hợp các phần mở rộng cần copy
     * @param rootSourcePath đường dẫn gốc của thư mục nguồn
     * @return số lượng file đã copy
     */
    private static int copyFilesRecursively(File sourceDir, File destDir, Set<String> extSet, String rootSourcePath) {
        int count = 0;
        File[] files = sourceDir.listFiles();

        if (files == null) {
            return count;
        }

        for (File file : files) {
            if (file.isFile()) {
                // Kiểm tra extension của file
                String fileName = file.getName();
                int lastDotIndex = fileName.lastIndexOf('.');

                if (lastDotIndex > 0) {
                    String extension = fileName.substring(lastDotIndex).toLowerCase();

                    // Nếu extension khớp, thực hiện copy
                    if (extSet.contains(extension)) {
                        try {
                            // Tính toán đường dẫn tương đối
                            String relativePath = file.getAbsolutePath()
                                    .substring(new File(rootSourcePath).getAbsolutePath().length());
                            if (relativePath.startsWith(File.separator)) {
                                relativePath = relativePath.substring(1);
                            }

                            // Tạo file đích với cấu trúc thư mục tương tự
                            File destFile = new File(destDir, relativePath);

                            // Tạo thư mục cha nếu chưa tồn tại
                            File parentDir = destFile.getParentFile();
                            if (parentDir != null && !parentDir.exists()) {
                                parentDir.mkdirs();
                            }

                            // Copy file
                            copyFile(file, destFile);
                            System.out.println("Đã copy: " + file.getAbsolutePath() + " -> " + destFile.getAbsolutePath());
                            count++;
                        } catch (IOException e) {
                            System.err.println("Lỗi khi copy file " + file.getAbsolutePath() + ": " + e.getMessage());
                        }
                    }
                }
            } else if (file.isDirectory()) {
                // Đệ quy vào thư mục con
                count += copyFilesRecursively(file, destDir, extSet, rootSourcePath);
            }
        }

        return count;
    }

    /**
     * Copy một file từ nguồn sang đích
     * @param source file nguồn
     * @param dest file đích
     * @throws IOException nếu có lỗi khi copy
     */
    private static void copyFile(File source, File dest) throws IOException {
        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(dest)) {

            byte[] buffer = new byte[8192];
            int length;

            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }

    /**
     * Hàm main để test
     */
    public static void main(String[] args) {
        // Test 1: Copy các file .txt và .java
        System.out.println("=== Test 1: Copy các file .txt và .java ===");
        copyAll("D:\\test\\source", "D:\\test\\dest", "txt", "java");

        System.out.println("\n=== Test 2: Copy các file .class và .jar ===");
        copyAll("D:\\test\\source", "D:\\test\\dest2", ".class", ".jar");

        System.out.println("\n=== Test 3: Copy file với thư mục không tồn tại ===");
        copyAll("D:\\notexist", "D:\\test\\dest3", "txt");
    }
}
