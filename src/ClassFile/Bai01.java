package ClassFile;

import java.io.File;

/**
 * Lớp Bai01 - Xử lý xóa file và thư mục
 * Chức năng: Xóa file hoặc thư mục (bao gồm cả các file con bên trong)
 */

public class Bai01 {
    private boolean option; //
    /*
     * */
    public boolean deleteFile(String path) {
        try {
            File myFile = new File(path);
            // Nếu là thư mục , xóa đệ quy các file/thư mục con
            if (myFile.isDirectory()) {
                File[] files = myFile.listFiles();
                if (files != null) {
                    for (File f : files) {
                        deleteFile(f.getAbsolutePath());
                    }
                }
            }
            // Xóa file hoặc thư mục rỗng
            return myFile.delete();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean deleteFilesOnly(String path){
        try {
            File myFile = new File(path);
            if (myFile.isDirectory()) {
                File[] files = myFile.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isFile()) {
                            f.delete(); // Chỉ xóaFile
                        } else {
                            deleteFilesOnly(f.getAbsolutePath()); // Đệ quy với thư mục con
                        }
                    }
                }
                return true; // Không xóa thư mục, chí báo thành công
            } else if (myFile.isFile()) {
                return myFile.delete();
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Tìm và hiển thị tất cả các file có phần mở rộng được chỉ định
     * @param path Đường dẫn thư mục cần tìm kiếm
     * @param exts Các phần mở rộng cần tìm (ví dụ: "txt", "java", "pdf")
     */
    public void findAllByExts(String path, String... exts){
        try{
            File myFile = new File(path);

            //
            if (!myFile.exists()){
                System.out.println("Đường dẫn không tồn tại");
                return;
            }
            // Nếu là thư mục, duyệt qua các file/thư mục con
            if (myFile.isDirectory()){
                File[] files = myFile.listFiles();
                if (files != null) {
                    for (File f : files){
                            if (f.isDirectory()){
                                // Đệ quy với thưc mục con
                                findAllByExts(f.getAbsolutePath(), exts);
                            } else {
                                // Kiểm tra phần mở rộng của file
                                String fileName = f.getName();
                                        for (String ext : exts){
                                            if (fileName.endsWith("." + ext)){
                                                System.out.println(myFile.getAbsolutePath());
                                                break;
                                            }
                                        }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    /**
     * Hàm main để test chức năng xóa file/thư mục
     * @param args Tham số dòng lệnh
     */
    public static void main(String[] args) {
        Bai01 file = new Bai01();
        System.out.println(file.deleteFile("D:\\ltm\\a"));
    }
}



