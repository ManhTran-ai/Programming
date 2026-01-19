package EBankingTCP;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.*;

/*
 * E-Banking System - Server
 * Phục vụ đồng thời nhiều Client độc lập
 */

public class Server {
    private static final int PORT = 1099;
    private static final String BANNER = "Welcome to NLU e-Bank...";
    
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    
    public Server() {
        try {
            serverSocket = new ServerSocket(PORT);
            threadPool = Executors.newCachedThreadPool();
            System.out.println("E-Banking Server started on port " + PORT);
            System.out.println("Waiting for clients...");
        } catch (IOException e) {
            System.err.println("Server startup error: " + e.getMessage());
        }
    }
    
    public void start() {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getRemoteSocketAddress());
                
                // Mỗi client có DatabaseManager riêng
                DatabaseManager dbManager = new DatabaseManager();
                threadPool.execute(new ClientHandler(clientSocket, dbManager));
            }
        } catch (IOException e) {
            System.err.println("Error accepting client: " + e.getMessage());
        } finally {
            shutdown();
        }
    }
    
    private void shutdown() {
        if (threadPool != null) {
            threadPool.shutdown();
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        }
    }
    
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private DatabaseManager dbManager;
        private BufferedReader in;
        private PrintWriter out;
        private boolean isLoggedIn = false;
        private boolean isInLoginPhase = false;
        
        public ClientHandler(Socket socket, DatabaseManager dbManager) {
            this.clientSocket = socket;
            this.dbManager = dbManager;
        }
        
        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                
                // Gửi banner chào mừng
                out.println(BANNER);
                
                String command;
                while ((command = in.readLine()) != null) {
                    String response = processCommand(command.trim());
                    out.println(response);
                    
                    // Nếu là QUIT thì kết thúc kết nối
                    if (command.equalsIgnoreCase("QUIT")) {
                        break;
                    }
                }
            } catch (IOException e) {
                System.err.println("Client error: " + e.getMessage());
            } finally {
                closeConnection();
            }
        }
        
        private String processCommand(String command) {
            String[] parts = command.split("\\s+");
            String cmd = parts[0].toUpperCase();
            
            // === GIAI ĐOẠN ĐĂNG NHẬP (POP3-like) ===
            if (!isLoggedIn) {
                switch (cmd) {
                    case "USER":
                        if (parts.length < 2) {
                            return "-ERR Missing username";
                        }
                        isInLoginPhase = true;
                        return dbManager.checkUsername(parts[1]);
                    
                    case "PASSWORD":
                        if (!isInLoginPhase) {
                            return "-ERR Not in login phase (use USER first)";
                        }
                        if (parts.length < 2) {
                            return "-ERR Missing password";
                        }
                        String authResult = dbManager.authenticatePassword(parts[1]);
                        if (authResult.startsWith("+OK")) {
                            isLoggedIn = true;
                            isInLoginPhase = false;
                        }
                        return authResult;
                    
                    case "QUIT":
                        return "+OK Goodbye";
                    
                    default:
                        return "-ERR Command not available. Please login first (USER, PASSWORD, QUIT)";
                }
            }
            
            // === GIAI ĐOẠN GIAO DỊCH (Sau khi đăng nhập thành công) ===
            switch (cmd) {
                case "DEPOSIT":
                    if (parts.length < 2) {
                        return "-ERR Missing amount (usage: DEPOSIT <amount>)";
                    }
                    try {
                        double amount = Double.parseDouble(parts[1]);
                        String result = dbManager.deposit(amount);
                        if (result.startsWith("+OK")) {
                            return result + " | So du hien tai: " + dbManager.getBalance();
                        }
                        return result;
                    } catch (NumberFormatException e) {
                        return "-ERR Invalid amount";
                    }
                
                case "WITHDRAW":
                    if (parts.length < 2) {
                        return "-ERR Missing amount (usage: WITHDRAW <amount>)";
                    }
                    try {
                        double amount = Double.parseDouble(parts[1]);
                        String result = dbManager.withdraw(amount);
                        if (result.startsWith("+OK")) {
                            return result + " | So du hien tai: " + dbManager.getBalance();
                        }
                        return result;
                    } catch (NumberFormatException e) {
                        return "-ERR Invalid amount";
                    }
                
                case "BALANCE":
                    double balance = dbManager.getBalance();
                    return "+OK So du tai khoan: " + balance;
                
                case "REPORT":
                    return getTransactionReport();
                
                case "QUIT":
                    dbManager.logout();
                    isLoggedIn = false;
                    return "+OK Goodbye";
                
                default:
                    return "-ERR Unknown command. Available: DEPOSIT, WITHDRAW, BALANCE, REPORT, QUIT";
            }
        }
        
        private String getTransactionReport() {
            List<Transaction> transactions = dbManager.getTransactionLog();
            
            if (transactions.isEmpty()) {
                return "+OK No transactions found";
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("+OK \n");
            sb.append("So tai khoan\t|| Ngay thang\t|| Thao tac\t|| Gia tri\n");
            sb.append("------------------------------------------------------------\n");
            
            for (Transaction t : transactions) {
                sb.append(t.toDisplayFormat()).append("\n");
            }
            
            sb.append("------------------------------------------------------------\n");
            sb.append("So du tai khoan: ").append(dbManager.getBalance());
            
            return sb.toString();
        }
        
        private void closeConnection() {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (clientSocket != null) clientSocket.close();
                if (dbManager != null) dbManager.closeConnection();
                System.out.println("Client disconnected: " + clientSocket.getRemoteSocketAddress());
            } catch (IOException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
    
    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}

