package ProductManagementRMI;

/*
 * Product Management System - RMI Service Implementation
 * Triển khai các phương thức remote cho ProductService
 */

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class ProductServiceImpl extends UnicastRemoteObject implements ProductService {
    
    private static final long serialVersionUID = 1L;
    private DatabaseManager dbManager;
    
    // Constructor phải throws RemoteException
    public ProductServiceImpl() throws RemoteException {
        super();
        this.dbManager = DatabaseManager.getInstance();
    }
    
    @Override
    public String addProduct(String productID, String name, double price, int count) 
            throws RemoteException {
        // Validate input
        if (productID == null || productID.trim().isEmpty()) {
            return "ERROR\tProduct ID cannot be empty";
        }
        if (name == null || name.trim().isEmpty()) {
            return "ERROR\tProduct name cannot be empty";
        }
        if (price < 0) {
            return "ERROR\tPrice cannot be negative";
        }
        if (count < 0) {
            return "ERROR\tCount cannot be negative";
        }
        
        boolean success = dbManager.addProduct(
            productID.trim(),
            name.trim(),
            price,
            count
        );
        
        if (success) {
            return "OK";
        } else {
            return "ERROR\tProduct ID already exists";
        }
    }
    
    @Override
    public String buyProducts(String[] productIDs) throws RemoteException {
        if (productIDs == null || productIDs.length == 0) {
            return "ERROR\tNo product IDs provided";
        }
        
        StringBuilder result = new StringBuilder("OK");
        int successCount = 0;
        int failCount = 0;
        
        for (String productID : productIDs) {
            if (productID == null || productID.trim().isEmpty()) {
                continue;
            }
            
            boolean success = dbManager.buyProduct(productID.trim());
            
            if (success) {
                result.append("\t").append(productID.trim()).append(":OK");
                successCount++;
            } else {
                // Kiểm tra lý do thất bại
                int count = dbManager.getProductCount(productID.trim());
                if (count < 0) {
                    result.append("\t").append(productID.trim()).append(":NOT_FOUND");
                } else if (count == 0) {
                    result.append("\t").append(productID.trim()).append(":OUT_OF_STOCK");
                } else {
                    result.append("\t").append(productID.trim()).append(":FAIL");
                }
                failCount++;
            }
        }
        
        // Thêm thống kê
        result.append("\t[Success: ").append(successCount).append(", Failed: ").append(failCount).append("]");
        
        return result.toString();
    }
    
    @Override
    public List<Product> findProductsByPriceRange(double fromPrice, double toPrice) 
            throws RemoteException {
        if (fromPrice < 0 || toPrice < 0) {
            throw new RemoteException("Price cannot be negative");
        }
        if (fromPrice > toPrice) {
            throw new RemoteException("fromPrice cannot be greater than toPrice");
        }
        
        return dbManager.findProductsByPriceRange(fromPrice, toPrice);
    }
    
    @Override
    public List<Product> findProductsByName(String name) throws RemoteException {
        if (name == null || name.trim().isEmpty()) {
            throw new RemoteException("Product name cannot be empty");
        }
        
        return dbManager.findProductsByName(name.trim());
    }
    
    @Override
    public List<Product> getAllProducts() throws RemoteException {
        return dbManager.getAllProducts();
    }
    
    @Override
    public String ping() throws RemoteException {
        return "PONG";
    }
}

