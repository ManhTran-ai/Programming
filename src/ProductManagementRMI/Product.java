package ProductManagementRMI;

/*
 * Product Management System - Product Model
 */

import java.io.Serializable;

public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String productID;
    private String name;
    private int count;
    private double price;
    
    public Product() {
    }
    
    public Product(String productID, String name, int count, double price) {
        this.productID = productID;
        this.name = name;
        this.count = count;
        this.price = price;
    }
    
    public String getProductID() {
        return productID;
    }
    
    public void setProductID(String productID) {
        this.productID = productID;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    @Override
    public String toString() {
        return String.format("ID: %s | Name: %s | Count: %d | Price: %.2f", 
                           productID, name, count, price);
    }
    
    // Format cho việc gửi qua network
    public String toNetworkFormat() {
        return String.format("%s\t%s\t%d\t%.2f", productID, name, count, price);
    }
    
    // Parse từ network format
    public static Product fromNetworkFormat(String line) {
        String[] parts = line.split("\t");
        if (parts.length >= 4) {
            return new Product(
                parts[0],
                parts[1],
                Integer.parseInt(parts[2]),
                Double.parseDouble(parts[3])
            );
        }
        return null;
    }
}

