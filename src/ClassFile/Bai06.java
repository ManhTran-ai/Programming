package ClassFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Bai06 {

    /**
     * Tìm tất cả các file trong thư mục path có tên khớp với pattern
     * Pattern có thể chứa tối đa 1 ký tự '*' đại diện cho nhóm ký tự bất kỳ
     *
     * @param path Đường dẫn thư mục cần tìm
     * @param pattern Mẫu tên file (có thể chứa 1 ký tự '*')
     * @return Danh sách đường dẫn đầy đủ của các file khớp với pattern
     */
    public static List<String> findAll(String path, String pattern) {
        List<String> result = new ArrayList<>();
        File directory = new File(path);

        // Kiểm tra thư mục có tồn tại và là thư mục
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Đường dẫn không tồn tại hoặc không phải là thư mục: " + path);
            return result;
        }

        // Chuyển pattern thành regex
        String regex = convertPatternToRegex(pattern);

        // Tìm kiếm đệ quy trong thư mục
        searchFiles(directory, regex, result);

        return result;
    }

    /**
     * Chuyển đổi pattern (với *) thành regular expression
     *
     * @param pattern Mẫu với ký tự '*'
     * @return Biểu thức chính quy tương ứng
     */
    private static String convertPatternToRegex(String pattern) {
        // Escape các ký tự đặc biệt trong regex (trừ *)
        String regex = pattern.replace("\\", "\\\\")
                             .replace(".", "\\.")
                             .replace("(", "\\(")
                             .replace(")", "\\)")
                             .replace("[", "\\[")
                             .replace("]", "\\]")
                             .replace("{", "\\{")
                             .replace("}", "\\}")
                             .replace("+", "\\+")
                             .replace("?", "\\?")
                             .replace("^", "\\^")
                             .replace("$", "\\$")
                             .replace("|", "\\|");

        // Thay thế * bằng .* (đại diện cho bất kỳ chuỗi ký tự nào)
        regex = regex.replace("*", ".*");

        return regex;
    }

    /**
     * Tìm kiếm đệ quy các file khớp với regex trong thư mục
     *
     * @param directory Thư mục hiện tại
     * @param regex Biểu thức chính quy để khớp tên file
     * @param result Danh sách kết quả
     */
    private static void searchFiles(File directory, String regex, List<String> result) {
        File[] files = directory.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                // Đệ quy vào thư mục con
                searchFiles(file, regex, result);
            } else {
                // Kiểm tra tên file có khớp với pattern không
                if (file.getName().matches(regex)) {
                    result.add(file.getAbsolutePath());
                }
            }
        }
    }

    /**
     * Hiển thị danh sách các file tìm được
     *
     * @param files Danh sách đường dẫn file
     */
    public static void displayResults(List<String> files) {
        if (files.isEmpty()) {
            System.out.println("Không tìm thấy file nào khớp với pattern.");
        } else {
            System.out.println("Tìm thấy " + files.size() + " file:");
            for (String file : files) {
                System.out.println(file);
            }
        }
    }

    // Ví dụ sử dụng
    public static void main(String[] args) {
        // Test 1: Tìm tất cả file .java
        System.out.println("=== Test 1: Tìm file *.java ===");
        List<String> javaFiles = findAll("src", "*.java");
        displayResults(javaFiles);

        System.out.println("\n=== Test 2: Tìm file Bai*.java ===");
        List<String> baiFiles = findAll("src", "Bai*.java");
        displayResults(baiFiles);

        System.out.println("\n=== Test 3: Tìm file *06.java ===");
        List<String> files06 = findAll("src", "*06.java");
        displayResults(files06);

        System.out.println("\n=== Test 4: Tìm file không có * ===");
        List<String> exactMatch = findAll("src", "Bai06.java");
        displayResults(exactMatch);
    }
}
