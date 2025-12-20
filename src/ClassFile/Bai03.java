package ClassFile;

import java.io.File;

/**
 * Lớp Bai03 - Hiển thị cây thư mục dạng cây
 * Chức năng: In ra cấu trúc thư mục dạng cây với thư mục in HOA và file in thường
 */
public class Bai03 {
    /**
     * Phương thức chính để hiển thị cây thư mục
     *
     * @param path Đường dẫn đến thư mục hoặc file cần hiển thị
     */
    public static void dirTree(String path) {
        File dir = new File(path);
        // Kiểm tra file/thư mục có tồn tại không
        if (!dir.exists()) {
            System.out.println("Đường dẫn không tồn tại: " + path);
            return;
        }
        int level = 0; // Mức độ thụt lề ban đầu
        // Nếu là file thì in file, nếu là thư mục thì gọi hàm helper
        if (dir.isFile()) {
            printFile(dir, level);
        }
        if (dir.isDirectory()) {
            dirTreeHelper(dir, level);
        }
    }

    /**
     * Hàm helper đệ quy để duyệt và in cây thư mục
     *
     * @param dir   Thư mục hiện tại
     * @param level Mức độ thụt lề (độ sâu trong cây)
     */
    private static void dirTreeHelper(File dir, int level) {
        // In tên thư mục trước (có kèm dung lượng)
        printDir(dir, level);
        File[] list = dir.listFiles();

        // Xử lý trường hợp listFiles() trả về null
        if (list == null) {
            return;
        }

        // Duyệt các thư mục con trước (in thư mục trước)
        for (File f : list) {
            if (f.isDirectory()) {
                dirTreeHelper(f, level + 1);
            }
        }
        // Sau đó mới duyệt các file (in file sau)
        for (File f : list) {
            if (f.isFile()) {
                printFile(f, level + 1);
            }
        }
    }

    /**
     * In tên thư mục với định dạng HOA và dung lượng
     *
     * @param dir   Thư mục cần in
     * @param level Mức độ thụt lề
     */
    private static void printDir(File dir, int level) {
        StringBuilder sb = getIndent(level);
        sb.append(dir.getName().toUpperCase()); // In tên thư mục chữ HOA

        // Thêm dung lượng của thư mục
        long size = calculateDirectorySize(dir);
        sb.append(" (").append(formatSize(size)).append(")");

        System.out.println(sb.toString());
    }

    /**
     * Tính tổng dung lượng của thư mục (bao gồm cả thư mục con)
     *
     * @param directory Thư mục cần tính
     * @return Tổng dung lượng (bytes)
     */
    private static long calculateDirectorySize(File directory) {
        long size = 0;
        File[] files = directory.listFiles();

        if (files == null) {
            return 0;
        }

        for (File file : files) {
            if (file.isFile()) {
                size += file.length();
            } else if (file.isDirectory()) {
                size += calculateDirectorySize(file);
            }
        }
        return size;
    }

    /**
     * Định dạng dung lượng file/thư mục
     *
     * @param size Dung lượng (bytes)
     * @return Chuỗi định dạng (KB, MB, GB)
     */
    private static String formatSize(long size) {
        if (size < 1024) {
            return size + " bytes";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * In tên file với định dạng chữ thường
     *
     * @param dir   File cần in
     * @param level Mức độ thụt lề
     */
    private static void printFile(File dir, int level) {
        StringBuilder sb = getIndent(level);
        sb.append(dir.getName().toLowerCase()); // In tên file chữ thường
        System.out.println(sb.toString());
    }

    /**
     * Tạo chuỗi thụt lề để hiển thị cấu trúc cây
     *
     * @param level Mức độ thụt lề
     * @return StringBuilder chứa chuỗi thụt lề
     */
    private static StringBuilder getIndent(int level) {
        StringBuilder sb = new StringBuilder();
        // Mức 0: chỉ có "+-"
        if (level == 0) {
            sb.append("+-");
        } else {
            // Mức > 0: có khoảng trắng và ký tự "| " để tạo cấu trúc cây
            sb.append("   ");
            for (int i = 1; i < level; i++) {
                sb.append("|  ");
            }
            sb.append("+-");
        }
        return sb;
    }

    /**
     * Hàm main để test chức năng hiển thị cây thư mục
     *
     * @param args Tham số dòng lệnh
     */
    public static void main(String[] args) {
        String path = "D:\\test";
        dirTree(path);
    }
}
