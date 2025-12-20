package IO.Bai13;

import java.io.*;
import java.nio.file.*;

public class FileFolderCopy {

    /**
     * fileCopy: Copy hoặc move một file từ nguồn đến đích sử dụng byte array kết hợp BIS, BOS
     *
     * @param sFile     Đường dẫn file nguồn
     * @param destFile  Đường dẫn file đích
     * @param moved     true: di chuyển file (xóa file nguồn sau khi copy), false: chỉ copy
     * @return          true nếu thành công, false nếu thất bại
     */
    public static boolean fileCopy(String sFile, String destFile, boolean moved) {
        File sourceFile = new File(sFile);
        File destinationFile = new File(destFile);

        // Kiểm tra file nguồn có tồn tại không
        if (!sourceFile.exists() || !sourceFile.isFile()) {
            System.err.println("File nguồn không tồn tại hoặc không phải là file: " + sFile);
            return false;
        }

        // Tạo thư mục chứa file đích nếu chưa tồn tại
        File parentDir = destinationFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                System.err.println("Không thể tạo thư mục đích: " + parentDir.getPath());
                return false;
            }
        }

        // Kiểm tra nếu file nguồn và file đích giống nhau
        try {
            if (sourceFile.getCanonicalPath().equals(destinationFile.getCanonicalPath())) {
                System.err.println("File nguồn và file đích trùng nhau!");
                return false;
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi kiểm tra đường dẫn: " + e.getMessage());
            return false;
        }

        // Copy file sử dụng BufferedInputStream và BufferedOutputStream với byte array
        byte[] buffer = new byte[8192]; // Buffer 8KB

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceFile));
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destinationFile))) {

            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            bos.flush(); // Đảm bảo tất cả dữ liệu được ghi vào file

            System.out.println("Copy file thành công: " + sFile + " -> " + destFile);

            // Nếu là move, xóa file nguồn
            if (moved) {
                if (sourceFile.delete()) {
                    System.out.println("Đã xóa file nguồn: " + sFile);
                } else {
                    System.err.println("Không thể xóa file nguồn: " + sFile);
                    return false;
                }
            }

            return true;

        } catch (FileNotFoundException e) {
            System.err.println("Không tìm thấy file: " + e.getMessage());
            return false;
        } catch (IOException e) {
            System.err.println("Lỗi I/O khi copy file: " + e.getMessage());
            return false;
        }
    }

    /**
     * folderCopy: Copy hoặc move một thư mục từ nguồn đến đích sử dụng byte array kết hợp BIS, BOS
     *
     * @param sFolder     Đường dẫn thư mục nguồn
     * @param destFolder  Đường dẫn thư mục đích
     * @param moved       true: di chuyển thư mục (xóa thư mục nguồn sau khi copy), false: chỉ copy
     * @return            true nếu thành công, false nếu thất bại
     */
    public static boolean folderCopy(String sFolder, String destFolder, boolean moved) {
        File sourceDir = new File(sFolder);
        File destDir = new File(destFolder);

        // Kiểm tra thư mục nguồn có tồn tại không
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            System.err.println("Thư mục nguồn không tồn tại hoặc không phải là thư mục: " + sFolder);
            return false;
        }

        // Kiểm tra nếu thư mục đích nằm trong thư mục nguồn
        try {
            String sourcePath = sourceDir.getCanonicalPath();
            String destPath = destDir.getCanonicalPath();

            if (sourcePath.equals(destPath)) {
                System.err.println("Thư mục nguồn và thư mục đích trùng nhau!");
                return false;
            }

            if (destPath.startsWith(sourcePath + File.separator)) {
                System.err.println("Thư mục đích không thể nằm trong thư mục nguồn!");
                return false;
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi kiểm tra đường dẫn: " + e.getMessage());
            return false;
        }

        // Tạo thư mục đích nếu chưa tồn tại
        if (!destDir.exists()) {
            if (!destDir.mkdirs()) {
                System.err.println("Không thể tạo thư mục đích: " + destFolder);
                return false;
            }
        }

        // Copy đệ quy tất cả các file và thư mục con
        boolean success = copyDirectoryRecursive(sourceDir, destDir);

        if (!success) {
            System.err.println("Lỗi khi copy thư mục");
            return false;
        }

        // Nếu là move, xóa thư mục nguồn
        if (moved) {
            if (deleteDirectory(sourceDir)) {
                System.out.println("Đã xóa thư mục nguồn: " + sFolder);
            } else {
                System.err.println("Không thể xóa hoàn toàn thư mục nguồn: " + sFolder);
                return false;
            }
        }

        System.out.println("Copy thư mục thành công: " + sFolder + " -> " + destFolder);
        return true;
    }

    /**
     * copyDirectoryRecursive: Copy đệ quy các file và thư mục con
     *
     * @param sourceDir  Thư mục nguồn
     * @param destDir    Thư mục đích
     * @return           true nếu thành công, false nếu thất bại
     */
    private static boolean copyDirectoryRecursive(File sourceDir, File destDir) {
        File[] files = sourceDir.listFiles();

        if (files == null) {
            System.err.println("Không thể đọc nội dung thư mục: " + sourceDir.getPath());
            return false;
        }

        for (File file : files) {
            File destFile = new File(destDir, file.getName());

            if (file.isDirectory()) {
                // Tạo thư mục con trong đích
                if (!destFile.exists()) {
                    if (!destFile.mkdirs()) {
                        System.err.println("Không thể tạo thư mục: " + destFile.getPath());
                        return false;
                    }
                }
                // Đệ quy copy thư mục con
                if (!copyDirectoryRecursive(file, destFile)) {
                    return false;
                }
            } else {
                // Copy file sử dụng byte array với BIS, BOS
                if (!copyFileWithBuffer(file, destFile)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * copyFileWithBuffer: Copy file sử dụng byte array với BufferedInputStream và BufferedOutputStream
     *
     * @param sourceFile  File nguồn
     * @param destFile    File đích
     * @return            true nếu thành công, false nếu thất bại
     */
    private static boolean copyFileWithBuffer(File sourceFile, File destFile) {
        byte[] buffer = new byte[8192]; // Buffer 8KB

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceFile));
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destFile))) {

            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }

            bos.flush();
            return true;

        } catch (IOException e) {
            System.err.println("Lỗi khi copy file " + sourceFile.getPath() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * deleteDirectory: Xóa đệ quy thư mục và tất cả nội dung bên trong
     *
     * @param directory  Thư mục cần xóa
     * @return           true nếu xóa thành công, false nếu thất bại
     */
    private static boolean deleteDirectory(File directory) {
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (!deleteDirectory(file)) {
                        return false;
                    }
                } else {
                    if (!file.delete()) {
                        System.err.println("Không thể xóa file: " + file.getPath());
                        return false;
                    }
                }
            }
        }

        return directory.delete();
    }
}

