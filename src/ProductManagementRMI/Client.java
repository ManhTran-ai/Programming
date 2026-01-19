package ProductManagementRMI;

/*
 * Product Management System - RMI Client
 * Kết nối với RMI Server và gọi các phương thức từ xa
 */

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Scanner;

public class Client {
    private static final String SERVICE_URL = "rmi://localhost:1099/ProductService";
    
    private ProductService productService;
    private Scanner scanner;
    private boolean connected;
    
    public Client() {
        this.scanner = new Scanner(System.in);
        this.connected = false;
    }
    
    public void connect() {
        try {
            System.out.println("Connecting to RMI Server: " + SERVICE_URL);
            
            // Lookup service từ RMI Registry
            productService = (ProductService) Naming.lookup(SERVICE_URL);
            
            // Kiểm tra kết nối bằng ping
            String response = productService.ping();
            if ("PONG".equals(response)) {
                connected = true;
                System.out.println("Connected successfully!");
            }
            
        } catch (java.rmi.ConnectException e) {
            System.err.println("Cannot connect to RMI Server: " + e.getMessage());
            System.err.println("Make sure server is running and RMI Registry is accessible.");
        } catch (java.rmi.NotBoundException e) {
            System.err.println("Service not found in registry: " + e.getMessage());
        } catch (java.net.MalformedURLException e) {
            System.err.println("Invalid service URL: " + e.getMessage());
        } catch (RemoteException e) {
            System.err.println("Remote error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }
    
    public void start() {
        if (!connected) {
            connect();
        }
        
        if (!connected) {
            System.err.println("Failed to connect to server. Exiting...");
            return;
        }
        
        System.out.println("\n========================================");
        System.out.println("Product Management RMI Client");
        System.out.println("========================================");
        System.out.println("Enter commands in format: COMMAND [params...]");
        System.out.println("Commands:");
        System.out.println("  ADD productID name price count");
        System.out.println("  BUY productID1 [productID2 ...]");
        System.out.println("  PRICE fromPrice toPrice");
        System.out.println("  NAME productName");
        System.out.println("  LIST - Show all products");
        System.out.println("  QUIT - Exit");
        System.out.println("========================================\n");
        
        while (connected) {
            System.out.print("> ");
            System.out.flush();
            
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            // Parse command
            String[] parts = input.split("\\s+");
            String command = parts[0].toUpperCase();
            
            try {
                switch (command) {
                    case "ADD":
                        handleAdd(parts);
                        break;
                    case "BUY":
                        handleBuy(parts);
                        break;
                    case "PRICE":
                        handlePrice(parts);
                        break;
                    case "NAME":
                        handleName(parts);
                        break;
                    case "LIST":
                        handleList();
                        break;
                    case "QUIT":
                        System.out.println("Disconnecting from server...");
                        connected = false;
                        break;
                    default:
                        System.out.println("ERROR\tUnknown command: " + command);
                        System.out.println("Valid commands: ADD, BUY, PRICE, NAME, LIST, QUIT");
                }
            } catch (RemoteException e) {
                System.err.println("Remote error: " + e.getMessage());
            }
        }
        
        System.out.println("Client terminated.");
    }
    
    // ADD productID name price count
    private void handleAdd(String[] parts) throws RemoteException {
        if (parts.length < 5) {
            System.out.println("ERROR\tInvalid syntax. Usage: ADD productID name price count");
            return;
        }
        
        try {
            String productID = parts[1];
            String name = parts[2];
            double price = Double.parseDouble(parts[3]);
            int count = Integer.parseInt(parts[4]);
            
            String result = productService.addProduct(productID, name, price, count);
            System.out.println(result);
            
        } catch (NumberFormatException e) {
            System.out.println("ERROR\tInvalid number format for price or count");
        }
    }
    
    // BUY productID1 [productID2 ...]
    private void handleBuy(String[] parts) throws RemoteException {
        if (parts.length < 2) {
            System.out.println("ERROR\tInvalid syntax. Usage: BUY productID1 [productID2 ...]");
            return;
        }
        
        // Lấy tất cả productIDs (bỏ qua command)
        String[] productIDs = new String[parts.length - 1];
        System.arraycopy(parts, 1, productIDs, 0, parts.length - 1);
        
        String result = productService.buyProducts(productIDs);
        System.out.println(result);
    }
    
    // PRICE fromPrice toPrice
    private void handlePrice(String[] parts) throws RemoteException {
        if (parts.length < 3) {
            System.out.println("ERROR\tInvalid syntax. Usage: PRICE fromPrice toPrice");
            return;
        }
        
        try {
            double fromPrice = Double.parseDouble(parts[1]);
            double toPrice = Double.parseDouble(parts[2]);
            
            List<Product> products = productService.findProductsByPriceRange(fromPrice, toPrice);
            
            if (products.isEmpty()) {
                System.out.println("EMPTY\tNo products found in price range [" + fromPrice + " - " + toPrice + "]");
            } else {
                System.out.println("RESULT\t" + products.size() + " products found:");
                for (Product p : products) {
                    System.out.println("  " + p.toString());
                }
            }
            
        } catch (NumberFormatException e) {
            System.out.println("ERROR\tInvalid number format for price");
        } catch (RemoteException e) {
            System.out.println("ERROR\t" + e.getMessage());
        }
    }
    
    // NAME productName
    private void handleName(String[] parts) throws RemoteException {
        if (parts.length < 2) {
            System.out.println("ERROR\tInvalid syntax. Usage: NAME productName");
            return;
        }
        
        String name = parts[1];
        
        List<Product> products = productService.findProductsByName(name);
        
        if (products.isEmpty()) {
            System.out.println("EMPTY\tNo products found with name containing: " + name);
        } else {
            System.out.println("RESULT\t" + products.size() + " products found:");
            for (Product p : products) {
                System.out.println("  " + p.toString());
            }
        }
    }
    
    // LIST - Show all products
    private void handleList() throws RemoteException {
        List<Product> products = productService.getAllProducts();
        
        if (products.isEmpty()) {
            System.out.println("EMPTY\tNo products in database");
        } else {
            System.out.println("RESULT\t" + products.size() + " products:");
            for (Product p : products) {
                System.out.println("  " + p.toString());
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Product Management RMI Client ===");
        
        Client client = new Client();
        client.connect();
        
        if (client.connected) {
            client.start();
        }
    }
}

