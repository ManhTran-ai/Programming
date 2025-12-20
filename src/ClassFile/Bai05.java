package ClassFile;
import java.io.File;

/**
 * Lớp Bai05 - Tìm kiếm tất cả file theo phần mở rộng
 * Chức năng: Tìm và in ra tất cả các file có phần mở rộng khớp với danh sách extensions trong thư mục và các thư mục con
 */
public class Bai05 {
    /**
     * Hàm main để test chức năng tìm kiếm file
     * @param args Tham số dòng lệnh
     */
    public static void main(String[] args) {
        String path = "D:\\text\\";
        System.out.println(path);// Thay đổi đường dẫn thư mục cha ở đây
        String[] extensions = { "txt" }; // Thay đổi các phần mở rộng ở đây
        Bai05 file = new Bai05();
        file.findAll(path, extensions);
    }

    /**
     * Phương thức tìm tất cả file có phần mở rộng khớp với danh sách extensions
     * @param path Đường dẫn đến thư mục cần tìm
     * @param extensions Mảng các phần mở rộng cần tìm (ví dụ: {"txt", "doc"})
     */
    public void findAll(String path, String[] extensions) {
        File directory = new File(path);

        // Kiểm tra xem thư mục tồn tại và là một thư mục
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Thư mục không tồn tại hoặc không hợp lệ.");
            return;
        }

        // Lấy danh sách tệp và thư mục trong thư mục hiện tại
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) { // Nếu là tệp
                    // Kiểm tra phần mở rộng của tệp có nằm trong danh sách được chỉ định không
                    String fileName = file.getName();
                    for (String ext : extensions) {
                        if (fileName.endsWith("." + ext)) {
                            System.out.println(file.getAbsolutePath());
                            break; // Bỏ qua các phần mở rộng khác nếu tìm thấy
                        }
                    }
                } else if (file.isDirectory()) { // Nếu là thư mục
                    // Đệ quy để tìm trong thư mục con
                    findAll(file.getAbsolutePath(), extensions);
                }
            }
        }
    }
}

