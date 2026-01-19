package ProductManagementRMI;

/*
 * Product Management System - Remote Service Interface
 * Định nghĩa các phương thức mà client có thể gọi từ xa
 */

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ProductService extends Remote {
    
    // Thêm sản phẩm mới
    // Returns: "OK" nếu thành công, "ERROR\tmessage" nếu thất bại
    String addProduct(String productID, String name, double price, int count) 
        throws RemoteException;
    
    // Mua sản phẩm - giảm số lượng đi 1
    // productIDs: danh sách mã sản phẩm muốn mua
    // Returns: "OK\tproductID1:OK\tproductID2:FAIL..." hoặc "ERROR\tmessage"
    String buyProducts(String[] productIDs) throws RemoteException;
    
    // Tìm sản phẩm theo khoảng giá
    // Returns: List<Product> hoặc empty list nếu không tìm thấy
    List<Product> findProductsByPriceRange(double fromPrice, double toPrice) 
        throws RemoteException;
    
    // Tìm sản phẩm theo tên (tìm gần đúng, không phân biệt hoa thường)
    // Returns: List<Product> hoặc empty list nếu không tìm thấy
    List<Product> findProductsByName(String name) 
        throws RemoteException;
    
    // Lấy tất cả sản phẩm
    // Returns: List<Product>
    List<Product> getAllProducts() throws RemoteException;
    
    // Kiểm tra kết nối với server
    // Returns: "PONG" nếu server đang hoạt động
    String ping() throws RemoteException;
}

