package IO;

import java.io.*;
import java.util.*;

public class Bai09 {
    /**
     * Chia file thành nhiều file thành phần với phần mở rộng .001, .002, ...
     * @param source đường dẫn file nguồn
     * @param pSize dung lượng mỗi file thành phần (bytes)
     * @throws Exception
     */
    public void split(String source, int pSize) throws Exception {
        File fileSource = new File(source);

        if (!fileSource.exists() || !fileSource.isFile()) {
            throw new FileNotFoundException("File không tồn tại: " + source);
        }

        long totalSize = fileSource.length();
        int numParts = (int) Math.ceil((double) totalSize / pSize);

        if (numParts > 999) {
            throw new IllegalArgumentException("File quá lớn, vượt quá 999 phần");
        }

        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileSource));

        for (int i = 1; i <= numParts; i++) {
            String partFilename = source + String.format(".%03d", i);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(partFilename));

            byte[] buffer = new byte[1024];
            int bytesRead;
            long bytesWritten = 0;

            // Đọc và ghi dữ liệu cho phần này
            while (bytesWritten < pSize && (bytesRead = bis.read(buffer)) != -1) {
                int bytesToWrite = (int) Math.min(bytesRead, pSize - bytesWritten);
                bos.write(buffer, 0, bytesToWrite);
                bytesWritten += bytesToWrite;
            }

            bos.close();
        }

        bis.close();
        System.out.println("Đã chia file thành " + numParts + " phần");
    }

    /**
     * Ghép nối các file thành phần thành file ban đầu
     * @param partFilename tên 1 file thành phần bất kỳ (ví dụ: "file.txt.003")
     * @throws Exception
     */
    public void join(String partFilename) throws Exception {
        File partFile = new File(partFilename);

        if (!partFile.exists() || !partFile.isFile()) {
            throw new FileNotFoundException("File thành phần không tồn tại: " + partFilename);
        }

        // Tìm tên file gốc và thư mục chứa các phần
        String partPath = partFile.getAbsolutePath();

        // Kiểm tra xem file có phần mở rộng .001 - .999 không
        if (!partPath.matches(".*\\.\\d{3}$")) {
            throw new IllegalArgumentException("File không đúng định dạng (phải có phần mở rộng .001 - .999)");
        }

        // Lấy tên file gốc (bỏ phần .xxx)
        String originalFilename = partPath.substring(0, partPath.length() - 4);
        File originalFile = new File(originalFilename);

        // Tìm tất cả các file thành phần
        List<File> partFiles = new ArrayList<>();
        int partNum = 1;

        while (true) {
            String partName = originalFilename + String.format(".%03d", partNum);
            File part = new File(partName);
            if (!part.exists()) {
                break;
            }
            partFiles.add(part);
            partNum++;
        }

        if (partFiles.isEmpty()) {
            throw new FileNotFoundException("Không tìm thấy file thành phần nào");
        }

        // Ghép nối các file thành phần
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(originalFile));

        for (File part : partFiles) {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(part));

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            bis.close();
        }

        bos.close();
        System.out.println("Đã ghép " + partFiles.size() + " phần thành file: " + originalFile.getName());
    }
}
