package ProductShoppingRMI;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Scanner;

/**
 * Client - Ứng dụng console cho hệ thống Mua bán Sản phẩm qua RMI
 * Kết nối tới RMI Server và gọi các phương thức từ xa
 */
public class Client {
    private static final String SERVICE_URL = "rmi://127.0.0.1:" + 5918 + "/" + ProductService.SERVICE_NAME;
    
    private ProductService server;
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
            server = (ProductService) Naming.lookup(SERVICE_URL);
            connected = true;
            System.out.println("Đã kết nối tới RMI Server: " + SERVICE_URL);
            return true;
        } catch (NotBoundException e) {
            System.err.println("Lỗi: Service '" + ProductService.SERVICE_NAME + "' chưa được đăng ký!");
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
            // Lấy và hiển thị banner từ server
            String banner = server.getBanner();
            System.out.println("\n" + banner);
            System.out.println("\n═══════════════════════════════════════════════════════════════");

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

            // Giai đoạn giao dịch (sau khi đăng nhập)
            while (connected && loggedIn) {
                System.out.print("SHOPPING> ");
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
                processTransactionCommand(input);
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
     * Xử lý lệnh đăng nhập (POP3-like: TEN, MATKHAU, EXIT)
     */
    private boolean processLoginCommand(String input) {
        try {
            // Tách lệnh và tham số bằng Tab hoặc space
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
                    System.out.println("ERR Lệnh không hợp lệ! Trong giai đoạn đăng nhập, sử dụng:");
                    System.out.println("  TEN <username>   - Nhập tên đăng nhập");
                    System.out.println("  MATKHAU <password> - Nhập mật khẩu");
                    System.out.println("  EXIT             - Thoát");
                    return true;
            }
        } catch (Exception e) {
            System.out.println("ERR Lỗi xử lý lệnh: " + e.getMessage());
            return true;
        }
    }

    /**
     * Xử lý lệnh TEN (username)
     */
    private boolean handleTEN(String username) {
        if (username == null || username.isEmpty()) {
            System.out.println("ERR Vui lòng nhập tên đăng nhập! Cú pháp: TEN <username>");
            return true;
        }

        currentUser = username;
        System.out.println("OK Đã nhận username. Vui lòng nhập mật khẩu: MATKHAU <password>");
        return true;
    }

    /**
     * Xử lý lệnh MATKHAU (password)
     */
    private boolean handleMATKHAU(String password) {
        if (currentUser == null) {
            System.out.println("ERR Vui lòng nhập username trước! Cú pháp: TEN <username>");
            return true;
        }

        if (password == null || password.isEmpty()) {
            System.out.println("ERR Vui lòng nhập mật khẩu! Cú pháp: MATKHAU <password>");
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
     * Xử lý lệnh EXIT
     */
    private void handleEXIT() {
        System.out.println("Đã thoát. Tạm biệt!");
        connected = false;
        loggedIn = false;
        currentUser = null;
    }

    /**
     * Xử lý lệnh giao dịch (MA, TEN, MUA, QUIT)
     */
    private void processTransactionCommand(String input) {
        try {
            // Tách lệnh và tham số bằng Tab hoặc space
            String[] parts = input.split("\\s+", 2);
            String cmd = parts[0].trim().toUpperCase();
            String param = parts.length > 1 ? parts[1].trim() : "";

            switch (cmd) {
                case "MA":
                    handleMA(param);
                    break;
                    
                case "TEN":
                    handleTENCommand(param);
                    break;
                    
                case "MUA":
                    handleMUA(parts.length > 1 ? param : "");
                    break;
                    
                case "QUIT":
                    handleQuit();
                    break;
                    
                default:
                    System.out.println("ERR Lệnh không hợp lệ! Các lệnh hợp lệ:");
                    System.out.println("  MA <mã sp>            - Tìm sản phẩm theo mã");
                    System.out.println("  TEN <tên sản phẩm>    - Tìm sản phẩm theo tên");
                    System.out.println("  MUA <mã1> <mã2> ...   - Mua sản phẩm");
                    System.out.println("  QUIT                  - Thoát");
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
     * Xử lý lệnh MA - Tìm theo mã sản phẩm
     */
    private void handleMA(String productID) throws RemoteException {
        if (productID == null || productID.isEmpty()) {
            System.out.println("ERR Vui lòng nhập mã sản phẩm! Cú pháp: MA <mã sp>");
            return;
        }

        String response = server.findByID(productID);
        System.out.println(response);
    }

    /**
     * Xử lý lệnh TEN - Tìm theo tên sản phẩm
     */
    private void handleTENCommand(String productName) throws RemoteException {
        if (productName == null || productName.isEmpty()) {
            System.out.println("ERR Vui lòng nhập tên sản phẩm! Cú pháp: TEN <tên sp>");
            return;
        }

        List<Product> products = server.findByName(productName);
        
        if (products.isEmpty()) {
            System.out.println("ERR Không tìm thấy sản phẩm nào với tên: " + productName);
        } else {
            System.out.println("OK Tìm thấy " + products.size() + " sản phẩm:");
            System.out.println("─────────────────────────────────────────");
            for (Product p : products) {
                System.out.println(p.toDisplayString());
            }
            System.out.println("─────────────────────────────────────────");
        }
    }

    /**
     * Xử lý lệnh MUA - Mua sản phẩm
     */
    private void handleMUA(String params) throws RemoteException {
        if (params == null || params.trim().isEmpty()) {
            System.out.println("ERR Vui lòng nhập mã sản phẩm cần mua! Cú pháp: MUA <mã1> <mã2> ...");
            return;
        }

        // Tách các mã sản phẩm
        String[] productIDs = params.trim().split("\\s+");
        if (productIDs.length == 0 || productIDs[0].isEmpty()) {
            System.out.println("ERR Vui lòng nhập mã sản phẩm cần mua! Cú pháp: MUA <mã1> <mã2> ...");
            return;
        }

        String response = server.buy(currentUser, productIDs);
        System.out.println(response);
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
        System.out.println("║  GIAI ĐOẠN ĐĂNG NHẬP:                                       ║");
        System.out.println("║    TEN <username>      - Nhập tên đăng nhập                 ║");
        System.out.println("║    MATKHAU <password>  - Nhập mật khẩu                      ║");
        System.out.println("║    EXIT                - Thoát                              ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  GIAI ĐOẠN GIAO DỊCH (sau khi đăng nhập thành công):        ║");
        System.out.println("║    MA <mã sp>          - Tìm sản phẩm theo mã               ║");
        System.out.println("║    TEN <tên sp>        - Tìm sản phẩm theo tên              ║");
        System.out.println("║    MUA <mã1> <mã2>...  - Mua sản phẩm                      ║");
        System.out.println("║    QUIT                - Thoát                              ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  VÍ DỤ:                                                     ║");
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
        System.out.println("║       ỨNG DỤNG CLIENT (RMI) - MUA BÁN SẢN PHẨM             ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        Client client = new Client();
        
        // Hiển thị hướng dẫn
        client.printHelp();
        
        // Bắt đầu kết nối
        client.start();
    }
}

