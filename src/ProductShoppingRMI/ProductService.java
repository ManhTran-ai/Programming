package ProductShoppingRMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * ProductService - Interface RMI cho hệ thống mua bán sản phẩm
 * Định nghĩa các phương thức mà client có thể gọi từ xa
 */
public interface ProductService extends Remote {
    
    /**
     * Hằng số tên service RMI
     */
    String SERVICE_NAME = "ProductService";
    
    /**
     * Lấy thông điệp chào mừng từ server
     * @return Thông điệp chào mừng
     * @throws RemoteException Lỗi kết nối RMI
     */
    String getBanner() throws RemoteException;
    
    /**
     * Đăng nhập người dùng (POP3-like)
     * @param username Tên đăng nhập
     * @param password Mật khẩu
     * @return Kết quả đăng nhập
     * @throws RemoteException Lỗi kết nối RMI
     */
    String login(String username, String password) throws RemoteException;
    
    /**
     * Đăng xuất người dùng
     * @param username Tên user
     * @return Thông báo kết quả
     * @throws RemoteException Lỗi kết nối RMI
     */
    String logout(String username) throws RemoteException;
    
    /**
     * Tìm sản phẩm theo mã
     * @param productID Mã sản phẩm
     * @return Thông tin sản phẩm hoặc thông báo không tìm thấy
     * @throws RemoteException Lỗi kết nối RMI
     */
    String findByID(String productID) throws RemoteException;
    
    /**
     * Tìm sản phẩm theo tên
     * @param productName Tên sản phẩm (có thể là chuỗi con)
     * @return Danh sách sản phẩm tìm được
     * @throws RemoteException Lỗi kết nối RMI
     */
    List<Product> findByName(String productName) throws RemoteException;
    
    /**
     * Mua sản phẩm
     * @param username Tên người dùng
     * @param productIDs Danh sách mã sản phẩm cần mua
     * @return Kết quả giao dịch mua hàng
     * @throws RemoteException Lỗi kết nối RMI
     */
    String buy(String username, String[] productIDs) throws RemoteException;
    
    /**
     * Lấy danh sách tất cả sản phẩm
     * @return Danh sách sản phẩm
     * @throws RemoteException Lỗi kết nối RMI
     */
    List<Product> getAllProducts() throws RemoteException;
    
    /**
     * Kiểm tra user đã đăng nhập chưa
     * @param username Tên user
     * @return true nếu đã đăng nhập
     * @throws RemoteException Lỗi kết nối RMI
     */
    boolean isLoggedIn(String username) throws RemoteException;
}

