package EBankingTCP;

import java.io.*;
import java.net.*;
import java.util.Scanner;

/*
 * E-Banking System - Client
 * Tương tác với Server thông qua kết nối TCP
 */

public class Client {
    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 1099;
    
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Scanner scanner;
    private boolean connected = false;
    
    public Client() {
        scanner = new Scanner(System.in);
    }
    
    public void connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            connected = true;
            System.out.println("Connected to E-Banking Server at " + SERVER_HOST + ":" + SERVER_PORT);
            
            // Nhận và hiển thị banner từ Server
            String banner = in.readLine();
            System.out.println("\n" + banner);
            System.out.println("==========================================");
            
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + SERVER_HOST);
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
            System.err.println("Make sure the server is running on port " + SERVER_PORT);
        }
    }
    
    public void run() {
        if (!connected) {
            System.err.println("Not connected to server.");
            return;
        }

        String input;
        while (true) {
            System.out.print("\nNHAP LENH> ");
            input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            // Gửi lệnh lên server
            out.println(input);

            // Nhận và hiển thị phản hồi
            String response;
            try {
                response = in.readLine();

                if (response == null) {
                    System.out.println("Server disconnected.");
                    break;
                }

                // Hiển thị phản hồi
                System.out.println(response);

                // Xử lý phản hồi đặc biệt (multi-line response)
                while (in.ready()) {
                    String extraLine = in.readLine();
                    if (extraLine == null || extraLine.startsWith("+OK ") || extraLine.startsWith("-ERR")) {
                        break;
                    }
                    System.out.println(extraLine);
                }
            } catch (IOException e) {
                System.err.println("Error reading from server: " + e.getMessage());
                break;
            }

            // Kiểm tra nếu client yêu cầu kết thúc
            String[] parts = input.split("\\s+");
            if (parts[0].equalsIgnoreCase("QUIT")) {
                System.out.println("Disconnected from server.");
                break;
            }
        }

        close();
    }
    
    public void close() {
        try {
            if (scanner != null) scanner.close();
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            connected = false;
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("       NLU E-BANKING CLIENT SYSTEM        ");
        System.out.println("==========================================");
        System.out.println("\nHuong dan su dung:");
        System.out.println("  - Dang nhap: USER <username>");
        System.out.println("              PASSWORD <password>");
        System.out.println("              QUIT (huy dang nhap)");
        System.out.println("  - Giao dich: DEPOSIT <so tien>  (gui tien)");
        System.out.println("               WITHDRAW <so tien> (rut tien)");
        System.out.println("               BALANCE            (kiem tra so du)");
        System.out.println("               REPORT             (xem nhat ky)");
        System.out.println("               QUIT               (ket thuc)");
        System.out.println("==========================================\n");
        
        Client client = new Client();
        client.connect();
        client.run();
    }
}

