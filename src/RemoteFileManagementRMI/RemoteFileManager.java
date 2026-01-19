package RemoteFileManagementRMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * RemoteInterface - Interface RMI cho hệ thống quản lý file từ xa
 * Định nghĩa các phương thức mà client có thể gọi từ xa
 */
public interface RemoteFileManager extends Remote {
    
    /**
     * Hằng số tên service RMI
     */
    String SERVICE_NAME = "RemoteFileManager";
    
    /**
     * Đăng nhập user (POP3-like)
     * @param username Tên đăng nhập
     * @param password Mật khẩu
     * @return Kết quả đăng nhập
     * @throws RemoteException Lỗi kết nối RMI
     */
    String login(String username, String password) throws RemoteException;
    
    /**
     * Đăng xuất và ngắt kết nối
     * @param username Tên user
     * @return Thông báo kết quả
     * @throws RemoteException Lỗi kết nối RMI
     */
    String logout(String username) throws RemoteException;
    
    /**
     * Đặt thư mục làm việc hiện tại
     * @param username Tên user
     * @param path Đường dẫn thư mục
     * @return Kết quả thực hiện
     * @throws RemoteException Lỗi kết nối RMI
     */
    String setFolder(String username, String path) throws RemoteException;
    
    /**
     * Xem nội dung file hoặc danh sách thư mục
     * @param username Tên user
     * @param path Đường dẫn file/thư mục
     * @return Nội dung file hoặc danh sách
     * @throws RemoteException Lỗi kết nối RMI
     */
    String view(String username, String path) throws RemoteException;
    
    /**
     * Sao chép file
     * @param username Tên user
     * @param sourceFile File nguồn
     * @param destFile File đích
     * @return Kết quả thực hiện
     * @throws RemoteException Lỗi kết nối RMI
     */
    String copy(String username, String sourceFile, String destFile) throws RemoteException;
    
    /**
     * Di chuyển file
     * @param username Tên user
     * @param sourceFile File nguồn
     * @param destFile File đích
     * @return Kết quả thực hiện
     * @throws RemoteException Lỗi kết nối RMI
     */
    String move(String username, String sourceFile, String destFile) throws RemoteException;
    
    /**
     * Đổi tên file
     * @param username Tên user
     * @param sourceFile File gốc
     * @param destFile File mới
     * @return Kết quả thực hiện
     * @throws RemoteException Lỗi kết nối RMI
     */
    String rename(String username, String sourceFile, String destFile) throws RemoteException;
    
    /**
     * Lấy thư mục hiện tại của user
     * @param username Tên user
     * @return Đường dẫn thư mục hiện tại
     * @throws RemoteException Lỗi kết nối RMI
     */
    String getCurrentDirectory(String username) throws RemoteException;
    
    /**
     * Kiểm tra user đã đăng nhập chưa
     * @param username Tên user
     * @return true nếu đã đăng nhập
     * @throws RemoteException Lỗi kết nối RMI
     */
    boolean isLoggedIn(String username) throws RemoteException;
}

