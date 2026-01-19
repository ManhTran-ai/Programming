package ProductShoppingTCP;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

/**
 * Client - Ứng dụng console cho hệ thống Mua bán Sản phẩm qua TCP
 * Kết nối tới server qua TCP, giao tiếp bằng text lines
 */
public class Client {
    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 5918;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
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
     * Kết nối tới server
     */
    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            connected = true;
            System.out.println("Da ket noi toi server " + SERVER_HOST + ":" + SERVER_PORT);
            return true;
        } catch (IOException e) {
            System.err.println("Loi ket noi toi server: " + e.getMessage());
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
            // Xử lý đăng nhập
            while (connected && !loggedIn) {
                System.out.print("LOGIN> ");
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    continue;
                }

                if (!processLoginCommand(input)) {
                    break;
                }
            }

            // Xử lý giao dịch (sau khi đăng nhập)
            while (connected && loggedIn) {
                System.out.print("SHOPPING> ");
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    continue;
                }

                // Kiểm tra QUIT đặc biệt
                if (input.toUpperCase().startsWith("QUIT")) {
                    handleQUIT();
                    break;
                }

                // Gửi lệnh tới server và nhận phản hồi
                sendCommand(input);
            }

        } catch (SocketException e) {
            System.err.println("Mat ket noi voi server!");
        } catch (IOException e) {
            System.err.println("Loi doc du lieu: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    /**
     * Xử lý lệnh đăng nhập (POP3-like: TEN, MATKHAU, EXIT)
     */
    private boolean processLoginCommand(String input) {
        try {
            // Tách lệnh và tham số bằng space
            String[] parts = input.split("\\s+", 2);
            String cmd = parts[0].trim().toUpperCase();
            String param = parts.length > 1 ? parts[1].trim() : "";

            switch (cmd) {
                case "TEN":
                    return handleTEN(param);
                case "MATKHAU":
                    return handleMATKHAU(param);
                case "EXIT":
                    handleEXIT();
                    return false;
                default:
                    System.out.println("ERR Lenh khong hop le! Trong giai doan dang nhap, su dung:");
                    System.out.println("  TEN <username>   - Nhap ten dang nhap");
                    System.out.println("  MATKHAU <password> - Nhap mat khau");
                    System.out.println("  EXIT             - Thoat");
                    return true;
            }
        } catch (Exception e) {
            System.out.println("ERR Loi xu ly lenh: " + e.getMessage());
            return true;
        }
    }

    /**
     * Xử lý lệnh TEN (username)
     */
    private boolean handleTEN(String username) {
        if (username == null || username.isEmpty()) {
            System.out.println("ERR Vui long nhap username! Cu phap: TEN <username>");
            return true;
        }

        currentUser = username;
        System.out.println("OK Da nhan username. Vui long nhap mat khau: MATKHAU <password>");
        return true;
    }

    /**
     * Xử lý lệnh MATKHAU (password)
     */
    private boolean handleMATKHAU(String password) {
        if (currentUser == null) {
            System.out.println("ERR Vui long nhap username truoc! Cu phap: TEN <username>");
            return true;
        }

        if (password == null || password.isEmpty()) {
            System.out.println("ERR Vui long nhap mat khau! Cu phap: MATKHAU <password>");
            return true;
        }

        // Gửi lệnh tới server
        String command = "MATKHAU " + password;
        String response = sendCommandAndGetResponse(command);

        if (response != null) {
            System.out.println(response);
            
            if (response.startsWith("OK Dang nhap thanh cong")) {
                loggedIn = true;
            } else {
                currentUser = null;
            }
        }

        return true;
    }

    /**
     * Xử lý lệnh EXIT
     */
    private void handleEXIT() {
        System.out.println("Da thoat. Tam biet!");
        connected = false;
        loggedIn = false;
        currentUser = null;
    }

    /**
     * Gửi lệnh và nhận phản hồi
     */
    private String sendCommandAndGetResponse(String command) {
        try {
            writer.println(command);
            System.out.flush();

            String response = reader.readLine();
            return response;
        } catch (IOException e) {
            System.err.println("Loi giao tiep voi server: " + e.getMessage());
            connected = false;
            return null;
        }
    }

    /**
     * Gửi lệnh tới server
     */
    private void sendCommand(String input) {
        try {
            // Gửi lệnh tới server
            writer.println(input);
            System.out.flush();

            // Nhận phản hồi từ server (có thể nhiều dòng)
            String response;
            while ((response = reader.readLine()) != null) {
                System.out.println(response);

                // Thoát vòng lặp nếu là phản hồi cuối cùng
                if (response.startsWith("OK") || response.startsWith("ERR") || 
                    response.startsWith("BYE") || response.startsWith("=== KET QUA") ||
                    response.startsWith("Cam on") || response.startsWith("Da dang xuat")) {
                    break;
                }
            }

            // Cập nhật trạng thái đăng nhập
            if (input.toUpperCase().startsWith("QUIT")) {
                if (response != null && response.startsWith("OK")) {
                    connected = false;
                    loggedIn = false;
                    currentUser = null;
                }
            }

        } catch (IOException e) {
            System.err.println("Loi giao tiep voi server: " + e.getMessage());
            connected = false;
            loggedIn = false;
        }
    }

    /**
     * Xử lý lệnh QUIT
     */
    private void handleQUIT() {
        try {
            writer.println("QUIT");
            String response = reader.readLine();
            if (response != null) {
                System.out.println(response);
            }
        } catch (IOException e) {
            System.err.println("Loi gui lenh QUIT: " + e.getMessage());
        }
        
        connected = false;
        loggedIn = false;
        currentUser = null;
        System.out.println("Da ngat ket noi. Tam biet!");
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
            System.err.println("Loi cleanup: " + e.getMessage());
        }
    }

    /**
     * Hiển thị hướng dẫn sử dụng
     */
    private void printHelp() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                     HUONG DAN SU DUNG                        ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  GIAI DOAN DANG NHAP:                                       ║");
        System.out.println("║    TEN <username>      - Nhap ten dang nhap                 ║");
        System.out.println("║    MATKHAU <password>  - Nhap mat khau                      ║");
        System.out.println("║    EXIT                - Thoat                              ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  GIAI DOAN GIAO DICH (sau khi dang nhap thanh cong):         ║");
        System.out.println("║    MA <ma sp>          - Tim san pham theo ma               ║");
        System.out.println("║    TEN <ten sp>        - Tim san pham theo ten              ║");
        System.out.println("║    MUA <ma1> <ma2>...  - Mua san pham                      ║");
        System.out.println("║    QUIT                - Thoat                              ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  VI DU:                                                     ║");
        System.out.println("║    MA SP001                                                  ║");
        System.out.println("║    TEN laptop                                                ║");
        System.out.println("║    MUA SP001 SP002 SP003                                     ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");
    }

    /**
     * Main method để chạy client
     */
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║       UNG DUNG CLIENT (TCP) - MUA BAN SAN PHAM             ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        Client client = new Client();
        
        // Hiển thị hướng dẫn
        client.printHelp();
        
        // Bắt đầu kết nối
        client.start();
    }
}

