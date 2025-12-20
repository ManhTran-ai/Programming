package ClassFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class Bai02 {
    private File currentDir;

    public Bai02(String defaultDir) {
        this.currentDir = new File(defaultDir);
        if (!currentDir.exists() || !currentDir.isDirectory()) {
            System.out.println("Thư mục mặc định không tồn tại. Sử dụng thư mục hiện tại.");
            this.currentDir = new File(System.getProperty("user.dir"));
        }
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        String input;

        while (true) {
            System.out.print(currentDir.getAbsolutePath() + "> ");
            input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            String[] parts = input.split("\\s+", 2);
            String command = parts[0].toUpperCase();
            String argument = parts.length > 1 ? parts[1] : "";

            switch (command) {
                case "EXIT":
                    System.out.println("Goodbye!");
                    scanner.close();
                    return;

                case "CD":
                    handleCD(argument);
                    break;

                case "DIR":
                    handleDIR();
                    break;

                case "DELETE":
                    handleDELETE(argument);
                    break;

                default:
                    System.out.println("Lệnh không hợp lệ. Các lệnh hỗ trợ: EXIT, CD, DIR, DELETE");
            }
        }
    }

    private void handleCD(String argument) {
        if (argument.isEmpty()) {
            System.out.println("Cú pháp: CD <subfolder> hoặc CD ..");
            return;
        }

        if (argument.equals("..")) {
            // Về thư mục cha
            File parent = currentDir.getParentFile();
            if (parent != null && parent.exists()) {
                currentDir = parent;
            } else {
                System.out.println("Đã ở thư mục gốc, không thể về thư mục cha.");
            }
        } else {
            // Chuyển vào thư mục con
            File newDir = new File(currentDir, argument);
            if (newDir.exists() && newDir.isDirectory()) {
                currentDir = newDir;
            } else {
                System.out.println("Thư mục '" + argument + "' không tồn tại.");
            }
        }
    }

    private void handleDIR() {
        File[] files = currentDir.listFiles();
        if (files == null || files.length == 0) {
            System.out.println("Thư mục trống.");
            return;
        }

        List<File> directories = new ArrayList<>();
        List<File> regularFiles = new ArrayList<>();

        for (File file : files) {
            if (file.isDirectory()) {
                directories.add(file);
            } else {
                regularFiles.add(file);
            }
        }

        // Sắp xếp theo tên
        directories.sort(Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
        regularFiles.sort(Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));

        // Hiển thị thư mục (in hoa) trước
        for (File dir : directories) {
            System.out.println("[DIR]  " + dir.getName().toUpperCase());
        }

        // Hiển thị file (in thường) sau
        for (File file : regularFiles) {
            System.out.println("[FILE] " + file.getName().toLowerCase());
        }
    }

    private void handleDELETE(String argument) {
        if (argument.isEmpty()) {
            System.out.println("Cú pháp: DELETE <file/folder>");
            return;
        }

        File target = new File(currentDir, argument);
        if (!target.exists()) {
            System.out.println("File/folder '" + argument + "' không tồn tại.");
            return;
        }

        if (deleteRecursively(target)) {
            System.out.println("Đã xóa '" + argument + "' thành công.");
        } else {
            System.out.println("Không thể xóa '" + argument + "'.");
        }
    }

    private boolean deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (!deleteRecursively(child)) {
                        return false;
                    }
                }
            }
        }
        return file.delete();
    }

    public static void main(String[] args) {
        String defaultDir = "c:\\temp";

        // Nếu thư mục c:\temp không tồn tại, sử dụng thư mục hiện tại
        File tempDir = new File(defaultDir);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        System.out.println("=== Command Line Application ===");
        System.out.println("Các lệnh hỗ trợ: EXIT, CD <folder>, CD .., DIR, DELETE <file/folder>");
        System.out.println();

        Bai02 app = new Bai02(defaultDir);
        app.run();
    }
}

