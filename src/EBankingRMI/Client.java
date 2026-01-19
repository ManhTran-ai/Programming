package EBankingRMI;

/*
 * E-Banking System - RMI Client
 * Kết nối với RMI Server và gọi các phương thức từ xa
 */

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Scanner;

public class Client {
    private static final String SERVICE_URL = "rmi://localhost:1099/BankService";
    
    private BankService bankService;
    private Scanner scanner;
    private boolean connected;
    private boolean loggedIn;
    
    public Client() {
        this.scanner = new Scanner(System.in);
        this.connected = false;
        this.loggedIn = false;
    }
    
    public void connect() {
        try {
            System.out.println("Connecting to RMI Server: " + SERVICE_URL);
            
            // Lookup service từ RMI Registry
            bankService = (BankService) Naming.lookup(SERVICE_URL);
            
            // Kiểm tra kết nối bằng ping
            String response = bankService.ping();
            if ("PONG".equals(response)) {
                connected = true;
                System.out.println("Connected successfully!");
                
                // Hiển thị banner
                String banner = bankService.getBanner();
                System.out.println(banner);
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
        System.out.println("NLU e-Bank Client");
        System.out.println("========================================");
        System.out.println("Phase 1: Login");
        System.out.println("Commands: USER username, PASSWORD password, QUIT");
        System.out.println("========================================\n");
        
        // Phase 1: Login
        handleLoginPhase();
        
        if (loggedIn) {
            // Phase 2: Transactions
            System.out.println("\n========================================");
            System.out.println("Phase 2: Transactions");
            System.out.println("Commands:");
            System.out.println("  DEPOSIT amount   - Gửi tiền");
            System.out.println("  WITHDRAW amount  - Rút tiền");
            System.out.println("  BALANCE          - Kiểm tra số dư");
            System.out.println("  REPORT           - Xem nhật ký giao dịch");
            System.out.println("  QUIT             - Đăng xuất và thoát");
            System.out.println("========================================\n");
            
            handleTransactionPhase();
        }
        
        System.out.println("Client terminated.");
    }
    
    // === Phase 1: Login ===
    private void handleLoginPhase() {
        while (!loggedIn && connected) {
            System.out.print("> ");
            System.out.flush();
            
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            String[] parts = input.split("\\s+", 2);
            String command = parts[0].toUpperCase();
            
            try {
                switch (command) {
                    case "USER":
                        if (parts.length < 2) {
                            System.out.println("-ERR Missing username");
                        } else {
                            String username = parts[1].trim();
                            String response = bankService.authenticateUser(username);
                            System.out.println(response);
                        }
                        break;
                        
                    case "PASSWORD":
                        if (parts.length < 2) {
                            System.out.println("-ERR Missing password");
                        } else {
                            String password = parts[1].trim();
                            String response = bankService.authenticatePassword(password);
                            System.out.println(response);
                            
                            // Kiểm tra nếu đăng nhập thành công
                            if (response.startsWith("+OK")) {
                                loggedIn = true;
                            }
                        }
                        break;
                        
                    case "QUIT":
                        System.out.println("+OK Goodbye!");
                        connected = false;
                        return;
                        
                    default:
                        System.out.println("-ERR Unknown command: " + command);
                        System.out.println("Valid commands: USER, PASSWORD, QUIT");
                }
            } catch (RemoteException e) {
                System.err.println("Remote error: " + e.getMessage());
                connected = false;
            }
        }
    }
    
    // === Phase 2: Transactions ===
    private void handleTransactionPhase() {
        while (loggedIn && connected) {
            System.out.print("> ");
            System.out.flush();
            
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            String[] parts = input.split("\\s+", 2);
            String command = parts[0].toUpperCase();
            
            try {
                switch (command) {
                    case "DEPOSIT":
                        if (parts.length < 2) {
                            System.out.println("-ERR Missing amount. Usage: DEPOSIT amount");
                        } else {
                            try {
                                double amount = Double.parseDouble(parts[1].trim());
                                String response = bankService.deposit(amount);
                                System.out.println(response);
                                
                                // Hiển thị số dư sau giao dịch thành công
                                if (response.startsWith("+OK")) {
                                    System.out.println("So du tai khoan: " + bankService.getBalance());
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("-ERR Invalid amount");
                            }
                        }
                        break;
                        
                    case "WITHDRAW":
                        if (parts.length < 2) {
                            System.out.println("-ERR Missing amount. Usage: WITHDRAW amount");
                        } else {
                            try {
                                double amount = Double.parseDouble(parts[1].trim());
                                String response = bankService.withdraw(amount);
                                System.out.println(response);
                                
                                // Hiển thị số dư sau giao dịch thành công
                                if (response.startsWith("+OK")) {
                                    System.out.println("So du tai khoan: " + bankService.getBalance());
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("-ERR Invalid amount");
                            }
                        }
                        break;
                        
                    case "BALANCE":
                        double balance = bankService.getBalance();
                        System.out.println("So du tai khoan: " + balance);
                        break;
                        
                    case "REPORT":
                        displayTransactionLog();
                        break;
                        
                    case "QUIT":
                        bankService.logout();
                        System.out.println("+OK Logged out successfully");
                        loggedIn = false;
                        break;
                        
                    default:
                        System.out.println("-ERR Unknown command: " + command);
                        System.out.println("Valid commands: DEPOSIT, WITHDRAW, BALANCE, REPORT, QUIT");
                }
            } catch (RemoteException e) {
                System.err.println("Remote error: " + e.getMessage());
                connected = false;
            }
        }
    }
    
    // Hiển thị nhật ký giao dịch theo định dạng yêu cầu
    private void displayTransactionLog() throws RemoteException {
        List<Transaction> transactions = bankService.getTransactionLog();
        
        if (transactions.isEmpty()) {
            System.out.println("Chua co giao dich nao!");
            return;
        }
        
        System.out.println("================ Nhat ky giao dich ================");
        System.out.println("So tai khoan || Ngay thang || Thao tac || Gia tri");
        System.out.println("-------------------------------------------------");
        
        for (Transaction t : transactions) {
            System.out.println(t.toDisplayFormat());
        }
        
        System.out.println("-------------------------------------------------");
        System.out.println("So du tai khoan: " + bankService.getBalance());
        System.out.println("=================================================");
    }
    
    public static void main(String[] args) {
        System.out.println("=== NLU e-Bank Client ===");
        
        Client client = new Client();
        client.connect();
        
        if (client.connected) {
            client.start();
        }
    }
}

