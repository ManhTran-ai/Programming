package Final_RMI;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

/**
 * Client - cung cấp giao diện command line để tương tác với server
 */
public class Client {
    private static final int RMI_PORT = 1099;
    private static final String SERVER_HOST = "localhost";

    private Registry registry;
    private LoginService loginService;
    private FileSession fileSession;
    private FileManager fileManager;
    private String currentClientPath;
    private Scanner scanner;

    public Client() {
        this.fileManager = new FileManager();
        this.currentClientPath = fileManager.getClientBaseDir();
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }

    public void start() {
        try {
            // Kết nối đến RMI Registry
            registry = LocateRegistry.getRegistry(SERVER_HOST, RMI_PORT);
            loginService = (LoginService) registry.lookup("LoginService");

            System.out.println("Đã kết nối đến server thành công!");
            System.out.println("Chào mừng đến với Remote File Manager");

            // Vòng lặp chính
            while (true) {
                if (fileSession == null) {
                    handlePreLoginCommands();
                } else {
                    handlePostLoginCommands();
                }
            }

        } catch (RemoteException | NotBoundException e) {
            System.err.println("Lỗi kết nối đến server: " + e.getMessage());
        }
    }

    /**
     * Xử lý lệnh trước khi đăng nhập
     */
    private void handlePreLoginCommands() {
        System.out.print("> ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            return;
        }

        String[] parts = input.split("\\s+", 2);
        String command = parts[0].toUpperCase();

        try {
            switch (command) {
                case "UNAME":
                    if (parts.length < 2) {
                        System.out.println("ERR Missing username");
                    } else {
                        String response = loginService.sendUsername(parts[1]);
                        System.out.println(response);
                    }
                    break;

                case "PASS":
                    if (parts.length < 2) {
                        System.out.println("ERR Missing password");
                    } else {
                        try {
                            fileSession = loginService.login(parts[1]);
                            System.out.println("OK Welcome " + fileSession.getUsername());
                        } catch (RemoteException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                    break;

                case "EXIT":
                    System.out.println("Tạm biệt!");
                    System.exit(0);
                    break;

                default:
                    System.out.println("ERR Unknown command. Available commands: UNAME, PASS, EXIT");
            }
        } catch (RemoteException e) {
            System.out.println("ERR " + e.getMessage());
        }
    }

    /**
     * Xử lý lệnh sau khi đăng nhập
     */
    private void handlePostLoginCommands() throws RemoteException {
        System.out.print(fileSession.getUsername() + "@server> ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            return;
        }

        String[] parts = input.split("\\s+");
        String command = parts[0].toUpperCase();

        try {
            switch (command) {
                case "SS_DIR":
                    if (parts.length < 2) {
                        System.out.println("ERR Missing folder name");
                    } else {
                        String response = fileSession.changeServerDirectory(parts[1]);
                        System.out.println(response);
                    }
                    break;

                case "SC_DIR":
                    if (parts.length < 2) {
                        System.out.println("ERR Missing folder name");
                    } else {
                        changeClientDirectory(parts[1]);
                    }
                    break;

                case "UPLOAD":
                    if (parts.length < 3) {
                        System.out.println("ERR Usage: UPLOAD <local_file> <server_file>");
                    } else {
                        uploadFile(parts[1], parts[2]);
                    }
                    break;

                case "DOWNLOAD":
                    if (parts.length < 3) {
                        System.out.println("ERR Usage: DOWNLOAD <server_file> <local_file>");
                    } else {
                        downloadFile(parts[1], parts[2]);
                    }
                    break;

                case "EXIT":
                    String response = fileSession.logout();
                    System.out.println(response);
                    fileSession = null;
                    System.out.println("Đã đăng xuất. Về trạng thái chưa đăng nhập.");
                    break;

                default:
                    System.out.println("ERR Unknown command. Available commands: SS_DIR, SC_DIR, UPLOAD, DOWNLOAD, EXIT");
            }
        } catch (RemoteException e) {
            System.out.println("ERR " + e.getMessage());
            // Nếu session bị lỗi, reset về trạng thái chưa đăng nhập
            if (e.getMessage().contains("Session expired")) {
                fileSession = null;
                System.out.println("Session đã hết hạn. Vui lòng đăng nhập lại.");
            }
        }
    }

    /**
     * Thay đổi thư mục client (xử lý local)
     */
    private void changeClientDirectory(String folderName) {
        try {
            Path newPath = Paths.get(currentClientPath, folderName).normalize();
            Path basePath = Paths.get(fileManager.getClientBaseDir()).normalize();

            // Đảm bảo không đi ra ngoài thư mục gốc
            if (!newPath.startsWith(basePath)) {
                System.out.println("ERR Invalid directory path");
                return;
            }

            File dir = new File(newPath.toString());
            if (dir.exists() && dir.isDirectory()) {
                currentClientPath = newPath.toString();
                System.out.println("OK Dir changed to: " + currentClientPath);
            } else {
                System.out.println("ERR Directory not found");
            }
        } catch (Exception e) {
            System.out.println("ERR " + e.getMessage());
        }
    }

    /**
     * Upload file từ client lên server
     */
    private void uploadFile(String localFileName, String serverFileName) {
        try {
            // Đọc file từ client
            byte[] fileData = fileManager.readClientFile(currentClientPath, localFileName);
            if (fileData == null) {
                System.out.println("ERR Local file not found: " + localFileName);
                return;
            }

            // Upload lên server
            String response = fileSession.uploadFile(localFileName, serverFileName, fileData);
            System.out.println(response);

        } catch (RemoteException e) {
            System.out.println("ERR " + e.getMessage());
        }
    }

    /**
     * Download file từ server về client
     */
    private void downloadFile(String serverFileName, String localFileName) {
        try {
            // Download từ server
            byte[] fileData = fileSession.downloadFile(serverFileName);
            if (fileData == null) {
                System.out.println("ERR Server file not found: " + serverFileName);
                return;
            }

            // Ghi xuống client
            if (fileManager.writeClientFile(currentClientPath, localFileName, fileData)) {
                System.out.println("OK Download success");
            } else {
                System.out.println("ERR Failed to save file locally");
            }

        } catch (RemoteException e) {
            System.out.println("ERR " + e.getMessage());
        }
    }
}
