package ProductShoppingTCP;

import java.io.Serializable;

/**
 * Product - Model đại diện cho thông tin sản phẩm
 */
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String productID;
    private String name;
    private int count;
    private double price;
    
    public Product() {
        this.productID = "";
        this.name = "";
        this.count = 0;
        this.price = 0.0;
    }
    
    public Product(String productID, String name, int count, double price) {
        this.productID = productID;
        this.name = name;
        this.count = count;
        this.price = price;
    }
    
    // Getters and Setters
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
    
    /**
     * Format thông tin sản phẩm để hiển thị
     */
    public String toDisplayString() {
        return String.format("Ma SP: %s | Ten: %s | So luong: %d | Gia: %.2f VNĐ", 
                           productID, name, count, price);
    }
    
    @Override
    public String toString() {
        return "Product{" +
               "productID='" + productID + '\'' +
               ", name='" + name + '\'' +
               ", count=" + count +
               ", price=" + price +
               '}';
    }
}

