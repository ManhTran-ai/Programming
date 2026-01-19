package EBankingRMI;

/*
 * E-Banking System - Remote Service Interface
 * Định nghĩa các phương thức mà client có thể gọi từ xa
 */

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface BankService extends Remote {
    
    // === Phase 1: Login ===
    
    // Gửi banner chào mừng
    // Returns: "Welcome to NLU e-Bank..."
    String getBanner() throws RemoteException;
    
    // Xác thực username
    // Returns: "+OK User accepted, please enter password" hoặc "-ERR User not found"
    String authenticateUser(String username) throws RemoteException;
    
    // Xác thực password
    // Returns: Account info string hoặc "-ERR Invalid password"
    String authenticatePassword(String password) throws RemoteException;
    
    // === Phase 2: Transactions (sau khi đăng nhập thành công) ===
    
    // Gửi tiền vào tài khoản
    // Returns: "OK" hoặc "ERROR\tmessage"
    String deposit(double amount) throws RemoteException;
    
    // Rút tiền từ tài khoản
    // Returns: "OK" hoặc "ERROR\tmessage"
    String withdraw(double amount) throws RemoteException;
    
    // Kiểm tra số dư
    // Returns: Số dư tài khoản
    double getBalance() throws RemoteException;
    
    // Lấy nhật ký giao dịch
    // Returns: List<Transaction>
    List<Transaction> getTransactionLog() throws RemoteException;
    
    // Kiểm tra trạng thái đăng nhập
    // Returns: true nếu đã đăng nhập
    boolean isLoggedIn() throws RemoteException;
    
    // Đăng xuất
    // Returns: "OK"
    String logout() throws RemoteException;
    
    // Ping kiểm tra kết nối
    // Returns: "PONG"
    String ping() throws RemoteException;
}

