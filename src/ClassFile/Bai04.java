package ClassFile;



import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Lớp Bai04 - Hiển thị cây thư mục kèm kích thước (Phiên bản tối ưu)
 * Chức năng: In ra cấu trúc thư mục dạng cây với thư mục in HOA kèm tổng kích
 * thước, file in thường
 * Tối ưu: Duyệt 1 lần, null check, sắp xếp, cache indent
 */
public class Bai04 {
    // Cache cho indent string để tránh tạo lại nhiều lần
    private static final String[] INDENT_CACHE = new String[100]; // Cache cho 100 level đầu

    /**
     * Phương thức chính để hiển thị cây thư mục kèm kích thước
     *
     * @param path Đường dẫn đến thư mục hoặc file cần hiển thị
     */
    public static void dirTree(String path) {
        File dir = new File(path);
        // Kiểm tra file/thư mục có tồn tại không
        if (!dir.exists()) {
            System.err.println("Path does not exist: " + path);
            return;
        }
        int level = 0; // Mức độ thụt lề ban đầu
        // Nếu là file thì in file, nếu là thư mục thì gọi hàm helper
        if (dir.isFile()) {
            printFile(dir, level);
        } else if (dir.isDirectory()) {
            dirTreeHelper(dir, level);
        }
    }

    /**
     * Hàm helper đệ quy để duyệt và in cây thư mục, tính tổng kích thước
     * Tối ưu: Duyệt 1 lần thay vì 2 lần, null check, sắp xếp
     *
     * @param dir   Thư mục hiện tại
     * @param level Mức độ thụt lề (độ sâu trong cây)
     * @return Tổng kích thước của thư mục (bao gồm tất cả file con)
     */
    private static long dirTreeHelper(File dir, int level) {
        long total = 0; // Tổng kích thước của thư mục

        File[] list = dir.listFiles();
        // Null check: listFiles() có thể trả về null nếu không có quyền truy cập
        if (list == null) {
            return 0;
        }

        // Sắp xếp: thư mục trước, file sau, cùng loại thì sắp xếp theo tên
        Arrays.sort(list, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                // Thư mục trước file
                if (f1.isDirectory() && f2.isFile())
                    return -1;
                if (f1.isFile() && f2.isDirectory())
                    return 1;
                // Cùng loại thì sắp xếp theo tên (không phân biệt hoa thường)
                return f1.getName().compareToIgnoreCase(f2.getName());
            }
        });

        // Tối ưu: Duyệt 1 lần thay vì 2 lần
        for (File f : list) {
            if (f.isDirectory()) {
                // Xử lý thư mục con trước (đệ quy)
                total += dirTreeHelper(f, level + 1);
            } else if (f.isFile()) {
                // Xử lý file và cộng dồn kích thước
                total += printFile(f, level + 1);
            }
        }

        // In thư mục với tổng kích thước
        printDir(dir, level, total);
        return total;
    }

    /**
     * In tên thư mục với định dạng HOA kèm tổng kích thước
     *
     * @param dir   Thư mục cần in
     * @param level Mức độ thụt lề
     * @param cap   Tổng kích thước của thư mục (bytes)
     */
    private static void printDir(File dir, int level, long cap) {
        String indent = getIndent(level);
        System.out.println(indent + dir.getName().toUpperCase() + ":" + cap);
    }

    /**
     * In tên file với định dạng chữ thường và trả về kích thước file
     *
     * @param file  File cần in
     * @param level Mức độ thụt lề
     * @return Kích thước của file (bytes)
     */
    private static long printFile(File file, int level) {
        String indent = getIndent(level);
        System.out.println(indent + file.getName().toLowerCase());
        return file.length(); // Trả về kích thước file
    }

    /**
     * Tạo chuỗi thụt lề để hiển thị cấu trúc cây (Tối ưu với cache)
     *
     * @param level Mức độ thụt lề
     * @return String chứa chuỗi thụt lề
     */
    private static String getIndent(int level) {
        // Sử dụng cache nếu có thể
        if (level < INDENT_CACHE.length && INDENT_CACHE[level] != null) {
            return INDENT_CACHE[level];
        }

        StringBuilder sb = new StringBuilder();
        // Mức 0: chỉ có "+-"
        if (level == 0) {
            sb.append("+-");
        } else {
            // Mức > 0: có khoảng trắng và ký tự "| " để tạo cấu trúc cây
            sb.append("   ");
            // Tối ưu: append "| " (level-1) lần
            for (int i = 1; i < level; i++) {
                sb.append("|  ");
            }
            sb.append("+-");
        }

        String result = sb.toString();
        // Cache kết quả nếu trong phạm vi cache
        if (level < INDENT_CACHE.length) {
            INDENT_CACHE[level] = result;
        }
        return result;
    }

    /**
     * Hàm main để test chức năng hiển thị cây thư mục kèm kích thước
     *
     * @param args Tham số dòng lệnh
     */
    public static void main(String[] args) {
        String path = "D:\\test";
        dirTree(path);
    }

}

