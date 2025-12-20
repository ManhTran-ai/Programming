package IO.Bai13;

import java.io.*;

public class TestFileFolderCopy {
    public static void main(String[] args) {
        // Tạo dữ liệu test trước
        System.out.println("=== TẠO DỮ LIỆU TEST ===");
        createTestData();
        System.out.println();

        // Test 1: Copy file
        System.out.println("=== TEST 1: COPY FILE ===");
        boolean result1 = FileFolderCopy.fileCopy(
                "test_source.txt",
                "test_destination.txt",
                false // Copy, không move
        );
        System.out.println("Kết quả copy file: " + (result1 ? "Thành công" : "Thất bại"));
        System.out.println();

        // Test 2: Move file
        System.out.println("=== TEST 2: MOVE FILE ===");
        boolean result2 = FileFolderCopy.fileCopy(
                "test_source2.txt",
                "test_moved.txt",
                true // Move (xóa file nguồn)
        );
        System.out.println("Kết quả move file: " + (result2 ? "Thành công" : "Thất bại"));
        System.out.println();

        // Test 3: Copy thư mục
        System.out.println("=== TEST 3: COPY FOLDER ===");
        boolean result3 = FileFolderCopy.folderCopy(
                "test_folder",
                "test_folder_copy",
                false // Copy, không move
        );
        System.out.println("Kết quả copy thư mục: " + (result3 ? "Thành công" : "Thất bại"));
        System.out.println();

        // Test 4: Move thư mục
        System.out.println("=== TEST 4: MOVE FOLDER ===");
        boolean result4 = FileFolderCopy.folderCopy(
                "test_folder2",
                "test_folder_moved",
                true // Move (xóa thư mục nguồn)
        );
        System.out.println("Kết quả move thư mục: " + (result4 ? "Thành công" : "Thất bại"));
        System.out.println();
    }

    /**
     * Tạo dữ liệu test
     */
    private static void createTestData() {
        try {
            // Tạo file test
            try (PrintWriter pw = new PrintWriter(new FileWriter("test_source.txt"))) {
                pw.println("Đây là file test cho copy");
                pw.println("Dòng thứ 2");
            }

            try (PrintWriter pw = new PrintWriter(new FileWriter("test_source2.txt"))) {
                pw.println("Đây là file test cho move");
            }

            // Tạo thư mục test
            File folder1 = new File("test_folder");
            if (folder1.mkdirs()) {
                System.out.println("Tạo thư mục: test_folder");
            }
            try (PrintWriter pw = new PrintWriter(new FileWriter("test_folder/file1.txt"))) {
                pw.println("File 1 trong test_folder");
            }
            try (PrintWriter pw = new PrintWriter(new FileWriter("test_folder/file2.txt"))) {
                pw.println("File 2 trong test_folder");
            }

            File subfolder = new File("test_folder/subfolder");
            if (subfolder.mkdirs()) {
                System.out.println("Tạo thư mục: test_folder/subfolder");
            }
            try (PrintWriter pw = new PrintWriter(new FileWriter("test_folder/subfolder/file3.txt"))) {
                pw.println("File 3 trong subfolder");
            }

            File folder2 = new File("test_folder2");
            if (folder2.mkdirs()) {
                System.out.println("Tạo thư mục: test_folder2");
            }
            try (PrintWriter pw = new PrintWriter(new FileWriter("test_folder2/fileA.txt"))) {
                pw.println("File A trong test_folder2");
            }

            System.out.println("Đã tạo dữ liệu test");

        } catch (IOException e) {
            System.err.println("Lỗi khi tạo dữ liệu test: " + e.getMessage());
        }
    }
}

