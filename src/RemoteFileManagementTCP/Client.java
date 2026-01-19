package RemoteFileManagement;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

/**
 * Client - Ứng dụng console cho hệ thống Remote File Management
 * Kết nối tới server qua TCP, giao tiếp bằng text lines
 */
public class Client {
    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 55555;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Scanner scanner;
    private boolean connected;
    private boolean loggedIn;

    public Client() {
        scanner = new Scanner(System.in);
        connected = false;
        loggedIn = false;
    }

    /**
     * Kết nối tới server
     */
    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            connected = true;
            System.out.println("Đã kết nối tới server " + SERVER_HOST + ":" + SERVER_PORT);
            return true;
        } catch (IOException e) {
            System.err.println("Lỗi kết nối tới server: " + e.getMessage());
            return false;
        }
    }

    /**
     * Bắt đầu phiên làm việc với server
     */
    public void start() {
        if (!connect()) {
            return;
        }

        try {
            // Hiển thị thông điệp chào mừng từ server
            String welcomeMessage = reader.readLine();
            System.out.println("\n" + welcomeMessage);
            System.out.println("═══════════════════════════════════════════════════════════════");

            // Vòng lặp xử lý lệnh
            while (connected) {
                displayPrompt();
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    continue;
                }

                // Kiểm tra lệnh QUIT đặc biệt (không cần gửi tới server)
                if (input.toUpperCase().startsWith("QUIT")) {
                    handleQuitCommand(input);
                    break;
                }

                // Gửi lệnh tới server và nhận phản hồi
                sendCommand(input);
            }

        } catch (SocketException e) {
            System.err.println("Mất kết nối với server!");
        } catch (IOException e) {
            System.err.println("Lỗi đọc dữ liệu: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    /**
     * Hiển thị prompt dựa trên trạng thái đăng nhập
     */
    private void displayPrompt() {
        if (loggedIn) {
            System.out.print("FILE-MGT> ");
        } else {
            System.out.print("LOGIN> ");
        }
    }

    /**
     * Gửi lệnh tới server và nhận phản hồi
     */
    private void sendCommand(String command) {
        try {
            // Gửi lệnh tới server
            writer.println(command);
            System.out.flush();

            // Nhận phản hồi từ server
            String response;
            while ((response = reader.readLine()) != null) {
                System.out.println(response);

                // Cập nhật trạng thái đăng nhập
                if (response.startsWith("OK Đăng nhập thành công")) {
                    loggedIn = true;
                } else if (response.startsWith("BYE")) {
                    connected = false;
                    loggedIn = false;
                }

                // Thoát vòng lặp nếu là phản hồi cuối cùng của lệnh hiện tại
                // (tránh nhầm với nội dung file trong lệnh VIEW)
                if (response.startsWith("OK") || response.startsWith("ERR") || 
                    response.startsWith("BYE") || response.startsWith("Vui")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi giao tiếp với server: " + e.getMessage());
            connected = false;
        }
    }

    /**
     * Xử lý lệnh QUIT đặc biệt ở client
     */
    private void handleQuitCommand(String input) {
        String[] parts = input.split("\\|");
        String cmd = parts[0].trim().toUpperCase();

        if (cmd.equals("QUIT")) {
            if (connected) {
                // Gửi QUIT tới server nếu đang kết nối
                try {
                    writer.println("QUIT");
                    String response = reader.readLine();
                    if (response != null) {
                        System.out.println(response);
                    }
                } catch (IOException e) {
                    System.err.println("Lỗi gửi lệnh QUIT: " + e.getMessage());
                }
            }
            connected = false;
            loggedIn = false;
            System.out.println("Đã ngắt kết nối. Tạm biệt!");
        }
    }

    /**
     * Dọn dẹp tài nguyên
     */
    private void cleanup() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) socket.close();
            if (scanner != null) scanner.close();
        } catch (IOException e) {
            System.err.println("Lỗi cleanup: " + e.getMessage());
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
        System.out.println("║          ỨNG DỤNG CLIENT - QUẢN LÝ FILE TỪ XA              ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        Client client = new Client();
        
        // Hiển thị hướng dẫn
        client.printHelp();
        
        // Bắt đầu kết nối
        client.start();
    }
}

