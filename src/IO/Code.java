package IO;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Code {
    void split(String source, int pSize) throws Exception {
        File fileSource = new File(source);

        if (!fileSource.exists() || !fileSource.isFile()) {
            throw new FileNotFoundException("File not found");
        }

        long totalSize = fileSource.length();
        int numParts = (int) Math.ceil((double) totalSize / pSize);

        if (numParts > 999) {
            throw new IllegalArgumentException("File too large");
        }

        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileSource));

        for (int i = 1; i <= numParts; i++) {
            String partFileName = source + String.format(".%03d", i);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(partFileName));

            byte[] buffer = new byte[1024];
            int bytesRead;
            long bytesWritten = 0;

            while (bytesWritten < pSize && (bytesRead = bis.read(buffer)) != -1) {
                int bytesToWrite = (int) Math.min(bytesRead, pSize - bytesWritten);
                bos.write(buffer, 0, bytesToWrite);
                bytesWritten += bytesToWrite;
            }
            bos.close();
        }
        bis.close();
        System.out.println("");

    }

    void join(String partFileName) throws Exception {
        File partFile = new File(partFileName);

        if (!partFile.exists() || !partFile.isFile()) {
            throw new FileNotFoundException("File not found");
        }
        String partPath = partFile.getAbsolutePath();

        if (!partPath.matches(".*\\.\\d{3}$")) {
            throw new IllegalArgumentException("File name is not valid");
        }
        String originalFileName = partPath.substring(0, partPath.length() - 4);
        File originalFile = new File(originalFileName);

        List<File> partFiles = new ArrayList<>();
        int partNum = 1;

        while (true) {
            String partName = originalFileName + String.format(".%03d", partNum);
            File part = new File(partName);
            if (!part.exists()) {
                break;
            }
            partFiles.add(part);
            partNum++;
        }
        if (partFiles.isEmpty()) {
            throw new FileNotFoundException("File not found");
        }
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
        System.out.println();
    }
}
