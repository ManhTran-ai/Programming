package IO.Bai14;

import java.io.*;

public class FileTypeDetector {

    /**
     * fileType: Xác định kiểu file dựa trên file signature (magic numbers)
     *
     * @param fname Đường dẫn đến file cần kiểm tra
     * @return Kiểu file (zip, rar, doc, pdf, ...) hoặc "unknown" nếu không xác định được
     */
    public static String fileType(String fname) {
        File file = new File(fname);

        // Kiểm tra file có tồn tại và là file không
        if (!file.exists() || !file.isFile()) {
            return "invalid file";
        }

        // Kiểm tra file có rỗng không
        if (file.length() == 0) {
            return "empty file";
        }

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            // Đọc tối đa 8 bytes đầu tiên để kiểm tra signature
            byte[] signature = new byte[8];
            int bytesRead = bis.read(signature);

            if (bytesRead < 4) {
                return "unknown";
            }

            // Kiểm tra các file signature phổ biến
            return identifyFileType(signature, bytesRead);

        } catch (IOException e) {
            System.err.println("Lỗi khi đọc file: " + e.getMessage());
            return "error";
        }
    }

    /**
     * identifyFileType: Xác định kiểu file dựa trên signature bytes
     *
     * @param signature Mảng bytes chứa file signature
     * @param length    Số bytes đã đọc được
     * @return Kiểu file
     */
    private static String identifyFileType(byte[] signature, int length) {
        // ZIP files (PK..)
        if (length >= 4 && signature[0] == 0x50 && signature[1] == 0x4B &&
                (signature[2] == 0x03 || signature[2] == 0x05 || signature[2] == 0x07) &&
                (signature[3] == 0x04 || signature[3] == 0x06 || signature[3] == 0x08)) {
            return "zip";
        }

        // RAR files (Rar!....)
        if (length >= 7 && signature[0] == 0x52 && signature[1] == 0x61 &&
                signature[2] == 0x72 && signature[3] == 0x21 &&
                signature[4] == 0x1A && signature[5] == 0x07) {
            return "rar";
        }

        // 7z files (7z....)
        if (length >= 6 && signature[0] == 0x37 && signature[1] == 0x7A &&
                signature[2] == (byte) 0xBC && signature[3] == (byte) 0xAF &&
                signature[4] == 0x27 && signature[5] == 0x1C) {
            return "7z";
        }

        // PDF files (%PDF)
        if (length >= 4 && signature[0] == 0x25 && signature[1] == 0x50 &&
                signature[2] == 0x44 && signature[3] == 0x46) {
            return "pdf";
        }

        // PNG files (.PNG....)
        if (length >= 8 && signature[0] == (byte) 0x89 && signature[1] == 0x50 &&
                signature[2] == 0x4E && signature[3] == 0x47 &&
                signature[4] == 0x0D && signature[5] == 0x0A &&
                signature[6] == 0x1A && signature[7] == 0x0A) {
            return "png";
        }

        // JPEG files (ÿØÿ)
        if (length >= 3 && signature[0] == (byte) 0xFF && signature[1] == (byte) 0xD8 &&
                signature[2] == (byte) 0xFF) {
            return "jpg";
        }

        // GIF files (GIF87a or GIF89a)
        if (length >= 6 && signature[0] == 0x47 && signature[1] == 0x49 &&
                signature[2] == 0x46 && signature[3] == 0x38 &&
                (signature[4] == 0x37 || signature[4] == 0x39) &&
                signature[5] == 0x61) {
            return "gif";
        }

        // BMP files (BM)
        if (length >= 2 && signature[0] == 0x42 && signature[1] == 0x4D) {
            return "bmp";
        }

        // Microsoft Office files (DOC, XLS, PPT) - OLE Compound File
        if (length >= 8 && signature[0] == (byte) 0xD0 && signature[1] == (byte) 0xCF &&
                signature[2] == 0x11 && signature[3] == (byte) 0xE0 &&
                signature[4] == (byte) 0xA1 && signature[5] == (byte) 0xB1 &&
                signature[6] == 0x1A && signature[7] == (byte) 0xE1) {
            return "doc/xls/ppt (MS Office 97-2003)";
        }

        // EXE files (MZ)
        if (length >= 2 && signature[0] == 0x4D && signature[1] == 0x5A) {
            return "exe";
        }

        // TAR files (ustar)
        if (length >= 8) {
            // TAR signature is at offset 257-262 (ustar), but we check what we can
            return "tar";
        }

        // MP3 files (ID3 or ÿû)
        if (length >= 3) {
            if ((signature[0] == 0x49 && signature[1] == 0x44 && signature[2] == 0x33) ||
                    (signature[0] == (byte) 0xFF && (signature[1] & 0xE0) == 0xE0)) {
                return "mp3";
            }
        }

        // MP4 files (....ftyp)
        if (length >= 8 && signature[4] == 0x66 && signature[5] == 0x74 &&
                signature[6] == 0x79 && signature[7] == 0x70) {
            return "mp4";
        }

        // AVI files (RIFF....AVI)
        if (length >= 8 && signature[0] == 0x52 && signature[1] == 0x49 &&
                signature[2] == 0x46 && signature[3] == 0x46) {
            return "avi/wav";
        }

        // Plain text file check (all printable ASCII or common whitespace)
        if (isProbablyText(signature, length)) {
            return "txt";
        }

        return "unknown";
    }

    /**
     * isProbablyText: Kiểm tra xem file có phải là text file không
     *
     * @param bytes  Mảng bytes cần kiểm tra
     * @param length Số bytes cần kiểm tra
     * @return true nếu có vẻ là text file
     */
    private static boolean isProbablyText(byte[] bytes, int length) {
        int printableCount = 0;
        for (int i = 0; i < length; i++) {
            byte b = bytes[i];
            // Kiểm tra printable ASCII (32-126) hoặc whitespace (9, 10, 13)
            if ((b >= 32 && b <= 126) || b == 9 || b == 10 || b == 13) {
                printableCount++;
            }
        }
        // Nếu hơn 90% là printable characters thì có thể là text
        return (printableCount * 100.0 / length) > 90;
    }

    /**
     * bytesToHex: Chuyển mảng bytes thành chuỗi hex (để debug)
     *
     * @param bytes  Mảng bytes
     * @param length Số bytes cần chuyển
     * @return Chuỗi hex
     */
    private static String bytesToHex(byte[] bytes, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02X ", bytes[i]));
        }
        return sb.toString().trim();
    }

    /**
     * getFileSignature: Lấy file signature dưới dạng hex string (để debug/hiển thị)
     *
     * @param fname Đường dẫn file
     * @return Chuỗi hex signature hoặc null nếu lỗi
     */
    public static String getFileSignature(String fname) {
        File file = new File(fname);

        if (!file.exists() || !file.isFile()) {
            return null;
        }

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            byte[] signature = new byte[16];
            int bytesRead = bis.read(signature);

            if (bytesRead <= 0) {
                return null;
            }

            return bytesToHex(signature, bytesRead);

        } catch (IOException e) {
            return null;
        }
    }
}

