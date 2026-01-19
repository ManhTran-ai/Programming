package EBankingRMI;

import java.io.Serializable;

/*
 * E-Banking System - Account Model
 * Lưu thông tin tài khoản ngân hàng
 */

public class Account implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String username;
    private String password;
    private String accountNumber;
    private double balance;
    
    public Account() {
    }
    
    public Account(String username, String password, String accountNumber, double balance) {
        this.username = username;
        this.password = password;
        this.accountNumber = accountNumber;
        this.balance = balance;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public double getBalance() {
        return balance;
    }
    
    public void setBalance(double balance) {
        this.balance = balance;
    }
    
    // Deposit money
    public void deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
        }
    }
    
    // Withdraw money (check if sufficient balance)
    public boolean withdraw(double amount) {
        if (amount > 0 && balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return String.format("Username: %s | Account: %s | Balance: %.0f", 
                           username, accountNumber, balance);
    }
    
    // Format for display
    public String toDisplayFormat() {
        return String.format("So tai khoan: %s\nSo du tai khoan: %.0f", 
                           accountNumber, balance);
    }
    
    // Format for network transfer
    public String toNetworkFormat() {
        return String.format("%s\t%s\t%s\t%.0f", username, password, accountNumber, balance);
    }
    
    // Parse from network format
    public static Account fromNetworkFormat(String line) {
        String[] parts = line.split("\t");
        if (parts.length >= 4) {
            return new Account(
                parts[0],
                parts[1],
                parts[2],
                Double.parseDouble(parts[3])
            );
        }
        return null;
    }
}

