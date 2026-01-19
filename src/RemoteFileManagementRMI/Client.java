package RemoteFileManagementRMI;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

/**
 * Client - Ứng dụng console cho hệ thống Remote File Management qua RMI
 * Kết nối tới RMI Server và gọi các phương thức từ xa
 */
public class Client {
    private static final String SERVICE_URL = "rmi://127.0.0.1/" + RemoteFileManager.SERVICE_NAME;
    
    private RemoteFileManager server;
    private Scanner scanner;
    private boolean connected;
    private boolean loggedIn;
    private String currentUser;

    public Client() {
        scanner = new Scanner(System.in);
        connected = false;
        loggedIn = false;
        currentUser = null;
    }

    /**
     * Kết nối tới RMI Server
     */
    public boolean connect() {
        try {
            // Thiết lập Security Manager (cần cho RMI)
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }
            
            // Tìm kiếm service RMI
            server = (RemoteFileManager) Naming.lookup(SERVICE_URL);
            connected = true;
            System.out.println("Đã kết nối tới RMI Server: " + SERVICE_URL);
            return true;
        } catch (NotBoundException e) {
            System.err.println("Lỗi: Service '" + RemoteFileManager.SERVICE_NAME + "' chưa được đăng ký!");
            System.err.println("Vui lòng chạy Server trước.");
        } catch (MalformedURLException e) {
            System.err.println("Lỗi: URL không hợp lệ - " + e.getMessage());
        } catch (RemoteException e) {
            System.err.println("Lỗi kết nối RMI: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Lỗi không xác định: " + e.getMessage());
        }
        return false;
    }

    /**
     * Bắt đầu phiên làm việc với server
     */
    public void start() {
        if (!connect()) {
            return;
        }

        try {
            // Hiển thị thông điệp chào mừng
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║           HỆ THỐNG QUẢN LÝ FILE TỪ XA (RMI)                  ║");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.println("║  Kết nối thành công tới RMI Server!                          ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            
            // Vòng lặp xử lý lệnh
            while (connected && !loggedIn) {
                // Giai đoạn đăng nhập
                System.out.print("LOGIN> ");
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    continue;
                }

                if (!processLoginCommand(input)) {
                    break;
                }
            }

            // Giai đoạn quản lý file (sau khi đăng nhập)
            while (connected && loggedIn) {
                displayPrompt();
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    continue;
                }

                // Kiểm tra QUIT đặc biệt
                if (input.toUpperCase().startsWith("QUIT")) {
                    handleQuit();
                    break;
                }

                // Gửi lệnh tới server
                processFileCommand(input);
            }

        } catch (RemoteException e) {
            System.err.println("Lỗi kết nối với server: " + e.getMessage());
            System.err.println("Server có thể đã ngắt kết nối.");
        } catch (Exception e) {
            System.err.println("Lỗi: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    /**
     * Xử lý lệnh đăng nhập (POP3-like)
     */
    private boolean processLoginCommand(String input) {
        try {
            String[] parts = input.split("\\|");
            String cmd = parts[0].trim().toUpperCase();
            String param = parts.length > 1 ? parts[1].trim() : "";

            switch (cmd) {
                case "USER":
                    return handleUser(param);
                case "PASS":
                    return handlePass(param);
                case "QUIT":
                    handleQuit();
                    return false;
                default:
                    System.out.println("ERR Lệnh không hợp lệ! Trong giai đoạn đăng nhập, sử dụng:");
                    System.out.println("  USER | <username> - Nhập tên đăng nhập");
                    System.out.println("  PASS | <password> - Nhập mật khẩu");
                    System.out.println("  QUIT - Thoát");
                    return true;
            }
        } catch (Exception e) {
            System.out.println("ERR Lỗi xử lý lệnh: " + e.getMessage());
            return true;
        }
    }

    /**
     * Xử lý lệnh USER
     */
    private boolean handleUser(String username) {
        if (username == null || username.isEmpty()) {
            System.out.println("ERR Vui lòng nhập username! Cú pháp: USER | <username>");
            return true;
        }

        currentUser = username;
        System.out.println("OK User accepted. Vui lòng nhập mật khẩu: PASS | <password>");
        return true;
    }

    /**
     * Xử lý lệnh PASS
     */
    private boolean handlePass(String password) {
        if (currentUser == null) {
            System.out.println("ERR Vui lòng nhập username trước! Cú pháp: USER | <username>");
            return true;
        }

        if (password == null || password.isEmpty()) {
            System.out.println("ERR Vui lòng nhập mật khẩu! Cú pháp: PASS | <password>");
            return true;
        }

        try {
            String response = server.login(currentUser, password);
            System.out.println(response);
            
            if (response.startsWith("OK Đăng nhập thành công")) {
                loggedIn = true;
            } else {
                currentUser = null;
            }
        } catch (RemoteException e) {
            System.out.println("ERR Lỗi kết nối server: " + e.getMessage());
        }

        return true;
    }

    /**
     * Xử lý lệnh quản lý file
     */
    private void processFileCommand(String input) {
        try {
            String[] parts = input.split("\\|");
            String cmd = parts[0].trim().toUpperCase();
            String param1 = parts.length > 1 ? parts[1].trim() : "";
            String param2 = parts.length > 2 ? parts[2].trim() : "";

            String response;

            switch (cmd) {
                case "SET FOLDER":
                    response = server.setFolder(currentUser, param1);
                    System.out.println(response);
                    break;
                    
                case "VIEW":
                    response = server.view(currentUser, param1);
                    System.out.println(response);
                    break;
                    
                case "COPY":
                    if (param2.isEmpty()) {
                        System.out.println("ERR Cú pháp: COPY | <source_file> | <dest_file>");
                    } else {
                        response = server.copy(currentUser, param1, param2);
                        System.out.println(response);
                    }
                    break;
                    
                case "MOVE":
                    if (param2.isEmpty()) {
                        System.out.println("ERR Cú pháp: MOVE | <source_file> | <dest_file>");
                    } else {
                        response = server.move(currentUser, param1, param2);
                        System.out.println(response);
                    }
                    break;
                    
                case "RENAME":
                    if (param2.isEmpty()) {
                        System.out.println("ERR Cú pháp: RENAME | <source_file> | <dest_file>");
                    } else {
                        response = server.rename(currentUser, param1, param2);
                        System.out.println(response);
                    }
                    break;
                    
                case "QUIT":
                    handleQuit();
                    break;
                    
                default:
                    System.out.println("ERR Lệnh không hợp lệ! Các lệnh hợp lệ:");
                    System.out.println("  SET FOLDER | <path> - Đặt thư mục làm việc");
                    System.out.println("  VIEW | <file/path>  - Xem nội dung");
                    System.out.println("  COPY | <src> | <dest> - Sao chép file");
                    System.out.println("  MOVE | <src> | <dest> - Di chuyển file");
                    System.out.println("  RENAME | <src> | <dest> - Đổi tên file");
                    System.out.println("  QUIT - Thoát");
                    break;
            }
        } catch (RemoteException e) {
            System.out.println("ERR Lỗi kết nối server: " + e.getMessage());
            connected = false;
            loggedIn = false;
        } catch (Exception e) {
            System.out.println("ERR Lỗi xử lý: " + e.getMessage());
        }
    }

    /**
     * Xử lý lệnh QUIT
     */
    private void handleQuit() {
        try {
            if (loggedIn && currentUser != null) {
                String response = server.logout(currentUser);
                System.out.println(response);
            }
        } catch (RemoteException e) {
            System.out.println("Lỗi gửi logout: " + e.getMessage());
        }
        
        connected = false;
        loggedIn = false;
        currentUser = null;
        System.out.println("Đã ngắt kết nối. Tạm biệt!");
    }

    /**
     * Hiển thị prompt
     */
    private void displayPrompt() {
        System.out.print("FILE-MGT> ");
    }

    /**
     * Dọn dẹp tài nguyên
     */
    private void cleanup() {
        if (scanner != null) {
            scanner.close();
        }
    }

    /**
     * Hiển thị hướng dẫn sử dụng
     */
    private void printHelp() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                     HƯỚNG DẪN SỬ DỤNG                        ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  GIAI ĐOẠN ĐĂNG NHẬP (POP3-like):                            ║");
        System.out.println("║    USER | <username>   - Nhập tên đăng nhập                  ║");
        System.out.println("║    PASS | <password>   - Nhập mật khẩu                      ║");
        System.out.println("║    QUIT                - Thoát                              ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  GIAI ĐOẠN QUẢN LÝ FILE (sau khi đăng nhập thành công):     ║");
        System.out.println("║    SET FOLDER | <path> - Đặt thư mục làm việc               ║");
        System.out.println("║    VIEW | <file/path>  - Xem nội dung file/thư mục          ║");
        System.out.println("║    COPY | <src> | <dest> - Sao chép file                    ║");
        System.out.println("║    MOVE | <src> | <dest> - Di chuyển file                   ║");
        System.out.println("║    RENAME | <src> | <dest> - Đổi tên file                   ║");
        System.out.println("║    QUIT                - Thoát                              ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  LƯU Ý: Sử dụng dấu '|' để phân tách các tham số            ║");
        System.out.println("║         Ví dụ: COPY | file1.txt | file2.txt                 ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");
    }

    /**
     * Main method để chạy client
     */
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║       ỨNG DỤNG CLIENT (RMI) - QUẢN LÝ FILE TỪ XA           ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        Client client = new Client();
        
        // Hiển thị hướng dẫn
        client.printHelp();
        
        // Bắt đầu kết nối
        client.start();
    }
}

