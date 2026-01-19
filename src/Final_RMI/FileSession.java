package Final_RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Giao diện FileSession - xử lý các thao tác file sau khi đăng nhập thành công
 */
public interface FileSession extends Remote {

    /**
     * Thay đổi thư mục làm việc của Server
     * @param folderName tên thư mục con
     * @return "OK Dir changed" hoặc "ERR Directory not found"
     * @throws RemoteException
     */
    String changeServerDirectory(String folderName) throws RemoteException;

    /**
     * Upload file từ client lên server
     * @param localFileName tên file ở client
     * @param serverFileName tên file sẽ lưu ở server
     * @param fileData dữ liệu file dạng byte array
     * @return "OK Upload success" hoặc "ERR <chi tiết lỗi>"
     * @throws RemoteException
     */
    String uploadFile(String localFileName, String serverFileName, byte[] fileData) throws RemoteException;

    /**
     * Download file từ server về client
     * @param serverFileName tên file trên server
     * @return dữ liệu file dạng byte array hoặc null nếu lỗi
     * @throws RemoteException
     */
    byte[] downloadFile(String serverFileName) throws RemoteException;

    /**
     * Đăng xuất và kết thúc session
     * @return "OK Logout success"
     * @throws RemoteException
     */
    String logout() throws RemoteException;

    /**
     * Lấy tên người dùng hiện tại
     * @return username
     * @throws RemoteException
     */
    String getUsername() throws RemoteException;
}
