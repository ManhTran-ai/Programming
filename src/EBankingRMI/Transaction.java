package EBankingRMI;

import java.io.Serializable;
import java.time.LocalDateTime;

/*
 * E-Banking System - Transaction Model
 * Lưu thông tin giao dịch
 */

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String accountNumber;
    private String operation;  // "DEPOSIT" or "WITHDRAW"
    private LocalDateTime transactionDate;
    private double amount;
    
    public Transaction() {
    }
    
    public Transaction(String accountNumber, String operation, LocalDateTime transactionDate, double amount) {
        this.accountNumber = accountNumber;
        this.operation = operation;
        this.transactionDate = transactionDate;
        this.amount = amount;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public void setOperation(String operation) {
        this.operation = operation;
    }
    
    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }
    
    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    // Format for display
    public String toDisplayFormat() {
        String op = operation.equals("DEPOSIT") ? "GUI" : "RUT";
        return String.format("%s\t|| %s\t|| %s\t|| %.0f", 
                           accountNumber, 
                           transactionDate.toLocalDate(),
                           op, 
                           amount);
    }
    
    // Format for database
    public String toDatabaseFormat() {
        return String.format("%s\t%s\t%s\t%.0f", 
                           accountNumber, 
                           operation, 
                           transactionDate.toString(), 
                           amount);
    }
    
    // Parse from database format
    public static Transaction fromDatabaseFormat(String line) {
        String[] parts = line.split("\t");
        if (parts.length >= 4) {
            return new Transaction(
                parts[0],
                parts[1],
                LocalDateTime.parse(parts[2]),
                Double.parseDouble(parts[3])
            );
        }
        return null;
    }
}

