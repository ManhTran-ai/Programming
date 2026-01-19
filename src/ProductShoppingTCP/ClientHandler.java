package ProductShoppingTCP;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClientHandler - Xử lý từng kết nối client
 * Hỗ trợ đa luồng và quản lý trạng thái đăng nhập
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private DatabaseManager dbManager;
    private boolean loggedIn;
    private String currentUser;
    private ConcurrentHashMap<String, Boolean> loggedInUsers;

    public ClientHandler(Socket socket, DatabaseManager dbManager) {
        this.socket = socket;
        this.dbManager = dbManager;
        this.loggedIn = false;
        this.currentUser = null;
        this.loggedInUsers = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        System.out.println("[Server] Client moi ket noi: " + socket.getInetAddress());

        try {
            // Khởi tạo streams - sử dụng BufferedReader và PrintWriter cho text-based communication
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            // Xử lý các lệnh từ client
            while (!socket.isClosed()) {
                String command = reader.readLine();
                if (command == null) {
                    break; // Client ngắt kết nối
                }

                System.out.println("[Server] Nhan lenh tu " + currentUser + ": " + command);
                String response = processCommand(command);
                writer.println(response);

                // Thoát nếu client gửi EXIT hoặc QUIT
                if ((command.toUpperCase().startsWith("EXIT") || 
                     command.toUpperCase().startsWith("QUIT")) && 
                    (response.startsWith("OK") || response.startsWith("BYE"))) {
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("[Server] Loi xu ly client: " + e.getMessage());
        } finally {
            cleanup();
            System.out.println("[Server] Client ngat ket noi: " + socket.getInetAddress());
        }
    }

    /**
     * Xử lý lệnh từ client
     */
    private String processCommand(String command) {
        // Kiểm tra đăng nhập cho các lệnh giao dịch
        boolean requiresLogin = !isLoginCommand(command);

        if (requiresLogin && !loggedIn) {
            return "ERR Vui long dang nhap truoc! Su dung: TEN <username>, MATKHAU <password>";
        }

        try {
            // Phân tích cú pháp lệnh: COMMAND <param1> <param2>...
            String[] parts = command.split("\\s+", 2);
            String cmd = parts[0].trim().toUpperCase();
            String param = parts.length > 1 ? parts[1].trim() : "";

            switch (cmd) {
                // === Giai đoạn đăng nhập (POP3-like) ===
                case "TEN":
                    return handleTEN(param);
                case "MATKHAU":
                    return handleMATKHAU(param);
                case "EXIT":
                    return handleEXIT();

                // === Giai đoạn giao dịch ===
                case "MA":
                    return handleMA(param);
                case "TEN_SEARCH":
                    return handleTENSearch(param);
                case "MUA":
                    return handleMUA(param);
                case "QUIT":
                    return handleQUIT();

                default:
                    return "ERR Lenh khong hop le! Cac lenh hop le:\n" +
                           "  Giai doan dang nhap:\n" +
                           "    TEN <username>   - Nhap ten dang nhap\n" +
                           "    MATKHAU <password> - Nhap mat khau\n" +
                           "    EXIT             - Thoat\n" +
                           "  Giai doan giao dich:\n" +
                           "    MA <ma sp>       - Tim san pham theo ma\n" +
                           "    TEN <ten sp>     - Tim san pham theo ten\n" +
                           "    MUA <ma1> <ma2>... - Mua san pham\n" +
                           "    QUIT             - Thoat";
            }
        } catch (Exception e) {
            return "ERR Loi xu ly lenh: " + e.getMessage();
        }
    }

    /**
     * Kiểm tra xem lệnh có phải là lệnh đăng nhập không
     */
    private boolean isLoginCommand(String command) {
        String upperCmd = command.toUpperCase().trim();
        return upperCmd.startsWith("TEN") || 
               upperCmd.startsWith("MATKHAU") || 
               upperCmd.startsWith("EXIT");
    }

    // ==================== CÁC HÀM XỬ LÝ ĐĂNG NHẬP ====================

    /**
     * Xử lý lệnh TEN (username)
     */
    private String handleTEN(String username) {
        if (loggedIn) {
            return "ERR Ban da dang nhap roi!";
        }

        if (username == null || username.isEmpty()) {
            return "ERR Vui long nhap username! Cu phap: TEN <username>";
        }

        if (dbManager.userExists(username)) {
            currentUser = username;
            return "OK Da nhan username. Vui long nhap mat khau: MATKHAU <password>";
        } else {
            return "ERR Ten dang nhap khong ton tai!";
        }
    }

    /**
     * Xử lý lệnh MATKHAU (password)
     */
    private String handleMATKHAU(String password) {
        if (loggedIn) {
            return "ERR Ban da dang nhap roi!";
        }

        if (currentUser == null) {
            return "ERR Vui long nhap username truoc! Cu phap: TEN <username>";
        }

        if (password == null || password.isEmpty()) {
            return "ERR Vui long nhap mat khau! Cu phap: MATKHAU <password>";
        }

        if (dbManager.authenticateUser(currentUser, password)) {
            loggedIn = true;
            loggedInUsers.put(currentUser, true);
            return "OK Dang nhap thanh cong! Chao mung " + currentUser + "!\n" +
                   "Ban co the su dung cac lenh:\n" +
                   "  MA <ma sp>   - Tim san pham theo ma\n" +
                   "  TEN <ten sp> - Tim san pham theo ten\n" +
                   "  MUA <ma1> <ma2>... - Mua san pham\n" +
                   "  QUIT - Thoat";
        } else {
            currentUser = null;
            return "ERR Mat khau khong dung!";
        }
    }

    /**
     * Xử lý lệnh EXIT
     */
    private String handleEXIT() {
        loggedIn = false;
        currentUser = null;
        return "BYE Da thoat. Tam biet!";
    }

    // ==================== CÁC HÀM XỬ LÝ GIAO DỊCH ====================

    /**
     * Xử lý lệnh MA - Tìm theo mã sản phẩm
     */
    private String handleMA(String productID) {
        if (productID == null || productID.isEmpty()) {
            return "ERR Vui long nhap ma san pham! Cu phap: MA <ma sp>";
        }

        Product product = dbManager.findByID(productID.trim());
        if (product != null) {
            return "OK Tim thay san pham:\n" + product.toDisplayString();
        } else {
            return "ERR Khong tim thay san pham voi ma: " + productID;
        }
    }

    /**
     * Xử lý lệnh TEN - Tìm theo tên sản phẩm
     */
    private String handleTENSearch(String productName) {
        if (productName == null || productName.isEmpty()) {
            return "ERR Vui long nhap ten san pham! Cu phap: TEN <ten sp>";
        }

        List<Product> products = dbManager.findByName(productName);

        if (products.isEmpty()) {
            return "ERR Khong tim thay san pham nao voi ten: " + productName;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("OK Tim thay ").append(products.size()).append(" san pham:\n");
            sb.append("─────────────────────────────────────────\n");
            for (Product p : products) {
                sb.append(p.toDisplayString()).append("\n");
            }
            sb.append("─────────────────────────────────────────");
            return sb.toString();
        }
    }

    /**
     * Xử lý lệnh MUA - Mua sản phẩm
     */
    private String handleMUA(String params) {
        if (params == null || params.trim().isEmpty()) {
            return "ERR Vui long nhap ma san pham can mua! Cu phap: MUA <ma1> <ma2> ...";
        }

        // Tách các mã sản phẩm
        String[] productIDs = params.trim().split("\\s+");
        if (productIDs.length == 0 || productIDs[0].isEmpty()) {
            return "ERR Vui long nhap ma san pham can mua! Cu phap: MUA <ma1> <ma2> ...";
        }

        StringBuilder result = new StringBuilder();
        int successCount = 0;
        int failCount = 0;
        double totalAmount = 0.0;

        for (String productID : productIDs) {
            if (productID == null || productID.trim().isEmpty()) {
                continue;
            }

            String trimmedID = productID.trim();
            Product product = dbManager.findByID(trimmedID);

            if (product == null) {
                result.append("  - ").append(trimmedID).append(": KHONG TIM THAY\n");
                failCount++;
            } else if (product.getCount() <= 0) {
                result.append("  - ").append(product.getName()).append(": HET HANG\n");
                failCount++;
            } else {
                boolean success = dbManager.buyProduct(trimmedID);
                if (success) {
                    result.append("  - ").append(product.getName())
                          .append(" (").append(trimmedID).append("): OK - ")
                          .append(String.format("%.2f VNĐ", product.getPrice())).append("\n");
                    successCount++;
                    totalAmount += product.getPrice();
                } else {
                    result.append("  - ").append(product.getName()).append(": LOI\n");
                    failCount++;
                }
            }
        }

        StringBuilder summary = new StringBuilder();
        summary.append("=== KET QUA MUA HANG ===\n");
        summary.append("Thanh cong: ").append(successCount).append(" san pham\n");
        summary.append("That bai: ").append(failCount).append(" san pham\n");

        if (successCount > 0) {
            summary.append("Tong tien: ").append(String.format("%.2f VNĐ", totalAmount)).append("\n");
        }

        summary.append("\nChi tiet:\n");
        summary.append(result.toString());

        if (successCount > 0) {
            summary.append("\nCam on ban da mua hang!");
        }

        return summary.toString();
    }

    /**
     * Xử lý lệnh QUIT
     */
    private String handleQUIT() {
        if (currentUser != null) {
            loggedInUsers.remove(currentUser);
        }
        loggedIn = false;
        currentUser = null;
        return "OK Da dang xuat. Tam biet!";
    }

    /**
     * Dọn dẹp tài nguyên khi ngắt kết nối
     */
    private void cleanup() {
        try {
            if (currentUser != null) {
                loggedInUsers.remove(currentUser);
            }
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("[Server] Loi cleanup: " + e.getMessage());
        }
    }
}

