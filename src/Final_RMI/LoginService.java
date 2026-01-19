package Final_RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Giao diện LoginService - Factory Pattern để tạo session
 * Xử lý việc đăng nhập và trả về FileSession cho user đã đăng nhập thành công
 */
public interface LoginService extends Remote {
    /**
     * Gửi tên đăng nhập
     * @param username tên người dùng
     * @return "OK User accepted" nếu tồn tại, "ERR User not found" nếu không
     * @throws RemoteException
     */
    String sendUsername(String username) throws RemoteException;

    /**
     * Gửi mật khẩu để xác thực
     * @param password mật khẩu
     * @return FileSession nếu đăng nhập thành công, null nếu thất bại
     * @throws RemoteException
     */
    FileSession login(String password) throws RemoteException;
}
