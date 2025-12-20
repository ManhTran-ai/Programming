package IO.Bai14;

import java.io.*;

public class TestFileTypeDetector {

    public static void main(String[] args) {
        System.out.println("=== Test FileTypeDetector ===\n");

        // Test với các file mẫu
        String[] testFiles = {
                "test.zip",
                "test.rar",
                "test.pdf",
                "test.txt",
                "test.jpg",
                "test.png",
                "test.doc",
                "test.docx"
        };

        System.out.println("Test 1: Kiểm tra các file mẫu");
        System.out.println("-----------------------------");
        for (String filename : testFiles) {
            String fileType = FileTypeDetector.fileType(filename);
            System.out.println("File: " + filename + " => Type: " + fileType);
        }

        // Tạo một số file mẫu để test
        System.out.println("\n\nTest 2: Tạo file mẫu và kiểm tra");
        System.out.println("-----------------------------");

        // Tạo file text mẫu
        createSampleTextFile("sample.txt");
        String type1 = FileTypeDetector.fileType("sample.txt");
        System.out.println("sample.txt => " + type1);
        String signature1 = FileTypeDetector.getFileSignature("sample.txt");
        System.out.println("Signature: " + signature1);

        // Tạo file PDF mẫu (fake signature)
        createSamplePDFFile("sample.pdf");
        String type2 = FileTypeDetector.fileType("sample.pdf");
        System.out.println("\nsample.pdf => " + type2);
        String signature2 = FileTypeDetector.getFileSignature("sample.pdf");
        System.out.println("Signature: " + signature2);

        // Tạo file ZIP mẫu (fake signature)
        createSampleZIPFile("sample.zip");
        String type3 = FileTypeDetector.fileType("sample.zip");
        System.out.println("\nsample.zip => " + type3);
        String signature3 = FileTypeDetector.getFileSignature("sample.zip");
        System.out.println("Signature: " + signature3);

        // Tạo file RAR mẫu (fake signature)
        createSampleRARFile("sample.rar");
        String type4 = FileTypeDetector.fileType("sample.rar");
        System.out.println("\nsample.rar => " + type4);
        String signature4 = FileTypeDetector.getFileSignature("sample.rar");
        System.out.println("Signature: " + signature4);

        // Tạo file JPEG mẫu (fake signature)
        createSampleJPEGFile("sample.jpg");
        String type5 = FileTypeDetector.fileType("sample.jpg");
        System.out.println("\nsample.jpg => " + type5);
        String signature5 = FileTypeDetector.getFileSignature("sample.jpg");
        System.out.println("Signature: " + signature5);

        // Test với file không tồn tại
        System.out.println("\n\nTest 3: File không tồn tại");
        System.out.println("-----------------------------");
        String type6 = FileTypeDetector.fileType("nonexistent.txt");
        System.out.println("nonexistent.txt => " + type6);

        // Test với file rỗng
        System.out.println("\n\nTest 4: File rỗng");
        System.out.println("-----------------------------");
        createEmptyFile("empty.txt");
        String type7 = FileTypeDetector.fileType("empty.txt");
        System.out.println("empty.txt => " + type7);

        System.out.println("\n=== Hoàn thành test ===");
    }

    /**
     * Tạo file text mẫu
     */
    private static void createSampleTextFile(String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("This is a sample text file.\n");
            writer.write("Hello World!\n");
            writer.write("Testing file type detection.\n");
        } catch (IOException e) {
            System.err.println("Lỗi tạo file: " + e.getMessage());
        }
    }

    /**
     * Tạo file PDF mẫu với signature giả
     */
    private static void createSamplePDFFile(String filename) {
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            // PDF signature: %PDF
            fos.write(new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E, 0x34});
            fos.write("\nThis is fake PDF content for testing.\n".getBytes());
        } catch (IOException e) {
            System.err.println("Lỗi tạo file: " + e.getMessage());
        }
    }

    /**
     * Tạo file ZIP mẫu với signature giả
     */
    private static void createSampleZIPFile(String filename) {
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            // ZIP signature: PK..
            fos.write(new byte[]{0x50, 0x4B, 0x03, 0x04});
            fos.write("Fake ZIP content for testing.".getBytes());
        } catch (IOException e) {
            System.err.println("Lỗi tạo file: " + e.getMessage());
        }
    }

    /**
     * Tạo file RAR mẫu với signature giả
     */
    private static void createSampleRARFile(String filename) {
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            // RAR signature: Rar!..
            fos.write(new byte[]{0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x00});
            fos.write("Fake RAR content for testing.".getBytes());
        } catch (IOException e) {
            System.err.println("Lỗi tạo file: " + e.getMessage());
        }
    }

    /**
     * Tạo file JPEG mẫu với signature giả
     */
    private static void createSampleJPEGFile(String filename) {
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            // JPEG signature: ÿØÿ
            fos.write(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0});
            fos.write("Fake JPEG content for testing.".getBytes());
        } catch (IOException e) {
            System.err.println("Lỗi tạo file: " + e.getMessage());
        }
    }

    /**
     * Tạo file rỗng
     */
    private static void createEmptyFile(String filename) {
        try {
            new File(filename).createNewFile();
        } catch (IOException e) {
            System.err.println("Lỗi tạo file: " + e.getMessage());
        }
    }
}

