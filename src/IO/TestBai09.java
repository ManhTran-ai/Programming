package IO;

import java.io.*;

public class TestBai09 {
    public static void main(String[] args) {
        Bai09 bai09 = new Bai09();

        try {
            // Tạo file test
            String testFilePath = "test.txt";
            createTestFile(testFilePath, 10000); // Tạo file 10KB

            System.out.println("=== Test Split ===");
            // Chia file thành các phần 3000 bytes
            bai09.split(testFilePath, 3000);

            System.out.println("\n=== Test Join ===");
            // Xóa file gốc
            new File(testFilePath).delete();

            // Ghép lại từ file thành phần bất kỳ
            bai09.join(testFilePath + ".002");

            // Kiểm tra file đã được tạo lại
            File restoredFile = new File(testFilePath);
            if (restoredFile.exists()) {
                System.out.println("File đã được khôi phục thành công!");
                System.out.println("Kích thước: " + restoredFile.length() + " bytes");
            }

            // Dọn dẹp
            cleanup(testFilePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createTestFile(String path, int size) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path));
        for (int i = 0; i < size; i++) {
            bos.write((byte) (i % 256));
        }
        bos.close();
        System.out.println("Đã tạo file test: " + path + " (" + size + " bytes)");
    }

    private static void cleanup(String testFilePath) {
        // Xóa file gốc
        new File(testFilePath).delete();

        // Xóa các file thành phần
        int i = 1;
        while (true) {
            File part = new File(testFilePath + String.format(".%03d", i));
            if (!part.exists()) {
                break;
            }
            part.delete();
            i++;
        }
        System.out.println("\nĐã dọn dẹp các file test");
    }
}

