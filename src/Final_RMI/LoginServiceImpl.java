package Final_RMI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Implementation của LoginService - Factory Pattern
 * Xử lý việc đăng nhập và tạo FileSession
 */
public class LoginServiceImpl extends UnicastRemoteObject implements LoginService {
    private DatabaseManager dbManager;
    private FileManager fileManager;
    private String pendingUsername;

    public LoginServiceImpl() throws RemoteException {
        super();
        this.dbManager = new DatabaseManager();
        this.fileManager = new FileManager();
        this.pendingUsername = null;
    }

    @Override
    public String sendUsername(String username) throws RemoteException {
        if (dbManager.userExists(username)) {
            this.pendingUsername = username;
            return "OK User accepted";
        } else {
            this.pendingUsername = null;
            return "ERR User not found";
        }
    }

    @Override
    public FileSession login(String password) throws RemoteException {
        if (pendingUsername == null) {
            throw new RemoteException("ERR No username provided");
        }

        if (dbManager.authenticateUser(pendingUsername, password)) {
            try {
                // Tạo FileSession mới cho user đã đăng nhập thành công
                FileSessionImpl session = new FileSessionImpl(pendingUsername, fileManager);
                pendingUsername = null; // Reset pending username
                return session;
            } catch (RemoteException e) {
                throw new RemoteException("ERR Failed to create session: " + e.getMessage());
            }
        } else {
            pendingUsername = null; // Reset pending username
            throw new RemoteException("ERR Wrong password");
        }
    }
}
