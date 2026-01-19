package ProductManagementTCP;

/*
 * Product Management System - Client Handler
 * Xử lý từng kết nối client riêng biệt
 */

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private DatabaseManager dbManager;
    private BufferedReader in;
    private PrintWriter out;
    
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        this.dbManager = DatabaseManager.getInstance();
    }
    
    @Override
    public void run() {
        try {
            // Khởi tạo input/output streams
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            
            // Gửi welcome message
            out.println("Welcome to Product Management System ...");
            
            // Xử lý các lệnh từ client
            processCommands();
            
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }
    
    private void processCommands() {
        String commandLine;
        
        try {
            while ((commandLine = in.readLine()) != null) {
                if (commandLine.trim().isEmpty()) {
                    continue;
                }
                
                // Parse command
                String[] parts = commandLine.split("\t");
                String command = parts[0].toUpperCase().trim();
                
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
                    case "QUIT":
                        out.println("BYE");
                        return;
                    default:
                        out.println("ERROR\tUnknown command: " + command);
                }
            }
        } catch (IOException e) {
            System.err.println("Client disconnected: " + e.getMessage());
        }
    }
    
    // ADD\tmã sản phẩm\t tên sản phẩm\tgiá bán\tsố lượng
    private void handleAdd(String[] parts) {
        if (parts.length < 5) {
            out.println("ERROR\tInvalid syntax. Usage: ADD\tproductID\tname\tprice\tcount");
            return;
        }
        
        try {
            String productID = parts[1].trim();
            String name = parts[2].trim();
            double price = Double.parseDouble(parts[3].trim());
            int count = Integer.parseInt(parts[4].trim());
            
            // Validate
            if (productID.isEmpty() || name.isEmpty() || price < 0 || count < 0) {
                out.println("ERROR\tInvalid product data");
                return;
            }
            
            boolean success = dbManager.addProduct(productID, name, price, count);
            
            if (success) {
                out.println("OK");
            } else {
                out.println("ERROR\tProduct ID already exists");
            }
        } catch (NumberFormatException e) {
            out.println("ERROR\tInvalid number format for price or count");
        }
    }
    
    // BUY\tmã sản phẩm1\tmã sản phẩm2\t...
    private void handleBuy(String[] parts) {
        if (parts.length < 2) {
            out.println("ERROR\tInvalid syntax. Usage: BUY\tproductID1\tproductID2\t...");
            return;
        }
        
        StringBuilder result = new StringBuilder("OK");
        int successCount = 0;
        int failCount = 0;

        for (int i = 1; i < parts.length; i++) {
            String productID = parts[i].trim();

            if (productID.isEmpty()) {
                continue;
            }

            boolean success = dbManager.buyProduct(productID);

            if (success) {
                result.append("\t").append(productID).append(":OK");
                successCount++;
            } else {
                result.append("\t").append(productID).append(":FAIL");
                failCount++;
            }
        }

        // Thêm thống kê vào response
        result.append("\t[Success: ").append(successCount).append(", Failed: ").append(failCount).append("]");

        out.println(result.toString());
    }
    
    // PRICE\ttừ giá\tđến giá
    private void handlePrice(String[] parts) {
        if (parts.length < 3) {
            out.println("ERROR\tInvalid syntax. Usage: PRICE\tfromPrice\ttoPrice");
            return;
        }
        
        try {
            double fromPrice = Double.parseDouble(parts[1].trim());
            double toPrice = Double.parseDouble(parts[2].trim());
            
            List<Product> products = dbManager.findProductsByPriceRange(fromPrice, toPrice);
            
            if (products.isEmpty()) {
                out.println("EMPTY\tNo products found in price range [" + fromPrice + " - " + toPrice + "]");
            } else {
                StringBuilder response = new StringBuilder("RESULT\t" + products.size() + " products found");
                for (Product p : products) {
                    response.append("\n").append(p.toNetworkFormat());
                }
                out.println(response.toString());
            }
        } catch (NumberFormatException e) {
            out.println("ERROR\tInvalid number format for price");
        }
    }
    
    // NAME\ttên sản phẩm
    private void handleName(String[] parts) {
        if (parts.length < 2) {
            out.println("ERROR\tInvalid syntax. Usage: NAME\tproductName");
            return;
        }
        
        String name = parts[1].trim();
        
        if (name.isEmpty()) {
            out.println("ERROR\tProduct name cannot be empty");
            return;
        }
        
        List<Product> products = dbManager.findProductsByName(name);
        
        if (products.isEmpty()) {
            out.println("EMPTY\tNo products found with name containing: " + name);
        } else {
            StringBuilder response = new StringBuilder("RESULT\t" + products.size() + " products found");
            for (Product p : products) {
                response.append("\n").append(p.toNetworkFormat());
            }
            out.println(response.toString());
        }
    }
    
    private void closeConnection() {
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (clientSocket != null) {
                clientSocket.close();
            }
            System.out.println("Client disconnected: " + clientSocket.getRemoteSocketAddress());
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}

