package ProductShoppingRMI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ProductServiceImpl - Implementation của ProductService interface
 * Xử lý các yêu cầu từ client qua RMI
 */
public class ProductServiceImpl extends UnicastRemoteObject implements ProductService {
    private static final long serialVersionUID = 1L;
    
    private DatabaseManager dbManager;
    private ConcurrentHashMap<String, Boolean> loggedInUsers;

    public ProductServiceImpl() throws RemoteException {
        super();
        this.dbManager = new DatabaseManager();
        this.loggedInUsers = new ConcurrentHashMap<>();
        System.out.println("[Server] ProductServiceImpl đã được khởi tạo!");
    }

    @Override
    public String getBanner() throws RemoteException {
        return "Xin chào mừng đến với Hệ thống Mua bán Sản phẩm!\n" +
               "Vui lòng đăng nhập để tiếp tục.\n" +
               "Các lệnh: TEN <username>, MATKHAU <password>, EXIT";
    }

    @Override
    public String login(String username, String password) throws RemoteException {
        if (username == null || username.trim().isEmpty()) {
            return "ERR Vui lòng nhập tên đăng nhập! Cú pháp: TEN <username>";
        }
        
        if (password == null || password.isEmpty()) {
            return "ERR Vui lòng nhập mật khẩu! Cú pháp: MATKHAU <password>";
        }
        
        if (loggedInUsers.getOrDefault(username, false)) {
            return "ERR User " + username + " đã đăng nhập trên phiên khác!";
        }
        
        // Kiểm tra username
        if (!dbManager.userExists(username.trim())) {
            return "ERR Tên đăng nhập không tồn tại!";
        }
        
        // Xác thực password
        if (dbManager.authenticateUser(username.trim(), password)) {
            loggedInUsers.put(username, true);
            System.out.println("[Server] User " + username + " đã đăng nhập thành công!");
            return "OK Đăng nhập thành công! Chào mừng " + username + 
                   "!\nBạn có thể sử dụng các lệnh:\n" +
                   "  MA <mã sp>   - Tìm sản phẩm theo mã\n" +
                   "  TEN <tên sp> - Tìm sản phẩm theo tên\n" +
                   "  MUA <mã1> <mã2> ... - Mua sản phẩm\n" +
                   "  QUIT - Thoát";
        } else {
            return "ERR Mật khẩu không đúng!";
        }
    }

    @Override
    public String logout(String username) throws RemoteException {
        if (username != null && loggedInUsers.containsKey(username)) {
            loggedInUsers.remove(username);
            System.out.println("[Server] User " + username + " đã đăng xuất.");
        }
        return "OK Đã đăng xuất. Tạm biệt!";
    }

    @Override
    public String findByID(String productID) throws RemoteException {
        if (productID == null || productID.trim().isEmpty()) {
            return "ERR Vui lòng nhập mã sản phẩm! Cú pháp: MA <mã sp>";
        }
        
        Product product = dbManager.findByID(productID.trim());
        if (product != null) {
            return "OK Tìm thấy sản phẩm:\n" + product.toDisplayString();
        } else {
            return "ERR Không tìm thấy sản phẩm với mã: " + productID;
        }
    }

    @Override
    public List<Product> findByName(String productName) throws RemoteException {
        if (productName == null || productName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        return dbManager.findByName(productName);
    }

    @Override
    public String buy(String username, String[] productIDs) throws RemoteException {
        if (!loggedInUsers.getOrDefault(username, false)) {
            return "ERR Vui lòng đăng nhập trước!";
        }
        
        if (productIDs == null || productIDs.length == 0) {
            return "ERR Vui lòng nhập mã sản phẩm cần mua! Cú pháp: MUA <mã1> <mã2> ...";
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
                result.append("  - ").append(trimmedID).append(": KHÔNG TÌM THẤY\n");
                failCount++;
            } else if (product.getCount() <= 0) {
                result.append("  - ").append(product.getName()).append(": HẾT HÀNG\n");
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
                    result.append("  - ").append(product.getName()).append(": LỖI\n");
                    failCount++;
                }
            }
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("=== KẾT QUẢ MUA HÀNG ===\n");
        summary.append("Thành công: ").append(successCount).append(" sản phẩm\n");
        summary.append("Thất bại: ").append(failCount).append(" sản phẩm\n");
        
        if (successCount > 0) {
            summary.append("Tổng tiền: ").append(String.format("%.2f VNĐ", totalAmount)).append("\n");
        }
        
        summary.append("\nChi tiết:\n");
        summary.append(result.toString());
        
        if (successCount > 0) {
            summary.append("\nCảm ơn bạn đã mua hàng!");
        }
        
        return summary.toString();
    }

    @Override
    public List<Product> getAllProducts() throws RemoteException {
        return dbManager.getAllProducts();
    }

    @Override
    public boolean isLoggedIn(String username) throws RemoteException {
        return loggedInUsers.getOrDefault(username, false);
    }
}

