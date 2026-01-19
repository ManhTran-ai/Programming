package EBankingTCP;

import java.io.Serializable;

/*
 * E-Banking System - Account Model
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
    
    public void deposit(double amount) {
        if (amount > 0) {
            this.balance += amount;
        }
    }
    
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
}

