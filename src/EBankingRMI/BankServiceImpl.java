package EBankingRMI;

/*
 * E-Banking System - RMI Service Implementation
 * Triển khai các phương thức remote cho BankService
 */

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class BankServiceImpl extends UnicastRemoteObject implements BankService {
    
    private static final long serialVersionUID = 1L;
    private DatabaseManager dbManager;
    
    public BankServiceImpl() throws RemoteException {
        super();
        this.dbManager = new DatabaseManager();
    }
    
    @Override
    public String getBanner() throws RemoteException {
        return "Welcome to NLU e-Bank...";
    }
    
    @Override
    public String authenticateUser(String username) throws RemoteException {
        if (username == null || username.trim().isEmpty()) {
            return "-ERR Username cannot be empty";
        }
        return dbManager.checkUsername(username.trim());
    }
    
    @Override
    public String authenticatePassword(String password) throws RemoteException {
        if (password == null || password.isEmpty()) {
            return "-ERR Password cannot be empty";
        }
        return dbManager.authenticatePassword(password);
    }
    
    @Override
    public String deposit(double amount) throws RemoteException {
        if (amount <= 0) {
            return "-ERR Invalid amount. Amount must be greater than 0";
        }
        return dbManager.deposit(amount);
    }
    
    @Override
    public String withdraw(double amount) throws RemoteException {
        if (amount <= 0) {
            return "-ERR Invalid amount. Amount must be greater than 0";
        }
        return dbManager.withdraw(amount);
    }
    
    @Override
    public double getBalance() throws RemoteException {
        return dbManager.getBalance();
    }
    
    @Override
    public List<Transaction> getTransactionLog() throws RemoteException {
        return dbManager.getTransactionLog();
    }
    
    @Override
    public boolean isLoggedIn() throws RemoteException {
        return dbManager.isLoggedIn();
    }
    
    @Override
    public String logout() throws RemoteException {
        dbManager.logout();
        return "+OK Logged out successfully";
    }
    
    @Override
    public String ping() throws RemoteException {
        return "PONG";
    }
}

