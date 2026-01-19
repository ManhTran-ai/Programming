package ProductManagementTCP;

/*
 * Product Management System - Client
 * Kết nối với server và gửi nhận lệnh
 */

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 1080;
    
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Scanner scanner;
    private boolean connected;
    
    public Client() {
        this.scanner = new Scanner(System.in);
        this.connected = false;
    }
    
    public void connect() {
        try {
            socket = new Socket(HOST, PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            connected = true;
            
            // Đọc welcome message
            String welcome = in.readLine();
            System.out.println(welcome);
            
        } catch (IOException e) {
            System.err.println("Cannot connect to server: " + e.getMessage());
            System.err.println("Make sure server is running at " + HOST + ":" + PORT);
        }
    }
    
    public void start() {
        if (!connected) {
            connect();
        }
        
        if (!connected) {
            return;
        }
        
        System.out.println("\n=== Product Management Client ===");
        System.out.println("Enter commands in format: COMMAND\\tparam1\\tparam2...");
        System.out.println("Commands: ADD, BUY, PRICE, NAME, QUIT");
        System.out.println("==================================\n");
        
        while (connected) {
            System.out.print("> ");
            System.out.flush();
            
            String input = scanner.nextLine();
            
            if (input.trim().isEmpty()) {
                continue;
            }
            
            // Gửi lệnh đến server
            out.println(input);
            
            // Đọc và hiển thị phản hồi
            try {
                String response;
                
                // Xử lý response có nhiều dòng (kết quả tìm kiếm)
                boolean multiLine = false;
                StringBuilder fullResponse = new StringBuilder();
                
                while ((response = in.readLine()) != null) {
                    if (response.startsWith("RESULT\t") || response.startsWith("EMPTY\t")) {
                        // Response có nhiều dòng
                        fullResponse.append(response).append("\n");
                        multiLine = true;
                        break;
                    } else if (multiLine) {
                        // Đọc tiếp các dòng tiếp theo
                        fullResponse.append(response).append("\n");
                        if (response.isEmpty()) {
                            break;
                        }
                    } else {
                        // Response đơn giòng
                        System.out.println(response);
                        break;
                    }
                }
                
                if (multiLine && fullResponse.length() > 0) {
                    System.out.println(fullResponse.toString());
                }
                
                // Kiểm tra nếu server gửi BYE
                if ("BYE".equals(response)) {
                    System.out.println("Disconnected from server.");
                    break;
                }
                
            } catch (IOException e) {
                System.err.println("Error reading response: " + e.getMessage());
                break;
            }
        }
        
        disconnect();
    }
    
    public void disconnect() {
        connected = false;
        
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null) {
                socket.close();
            }
            scanner.close();
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Product Management System Client ===");
        System.out.println("Connecting to " + HOST + ":" + PORT + "...");
        
        Client client = new Client();
        client.connect();
        
        if (client.connected) {
            client.start();
        }
        
        System.out.println("Client terminated.");
    }
}

