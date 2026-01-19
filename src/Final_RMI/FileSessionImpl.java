package Final_RMI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Implementation của FileSession - xử lý các thao tác file sau đăng nhập
 */
public class FileSessionImpl extends UnicastRemoteObject implements FileSession {
    private String username;
    private FileManager fileManager;
    private String currentServerPath;
    private boolean isLoggedIn;

    public FileSessionImpl(String username, FileManager fileManager) throws RemoteException {
        super();
        this.username = username;
        this.fileManager = fileManager;
        this.currentServerPath = fileManager.getServerBaseDir();
        this.isLoggedIn = true;
    }

    @Override
    public String changeServerDirectory(String folderName) throws RemoteException {
        if (!isLoggedIn) {
            throw new RemoteException("ERR Session expired");
        }

        if (fileManager.changeServerDirectory(currentServerPath, folderName)) {
            currentServerPath = java.nio.file.Paths.get(currentServerPath, folderName).normalize().toString();
            return "OK Dir changed";
        } else {
            return "ERR Directory not found";
        }
    }

    @Override
    public String uploadFile(String localFileName, String serverFileName, byte[] fileData) throws RemoteException {
        if (!isLoggedIn) {
            throw new RemoteException("ERR Session expired");
        }

        if (fileData == null || fileData.length == 0) {
            return "ERR Empty file data";
        }

        try {
            if (fileManager.uploadFile(currentServerPath, serverFileName, fileData)) {
                return "OK Upload success";
            } else {
                return "ERR Upload failed - invalid path or permission denied";
            }
        } catch (Exception e) {
            return "ERR Upload failed: " + e.getMessage();
        }
    }

    @Override
    public byte[] downloadFile(String serverFileName) throws RemoteException {
        if (!isLoggedIn) {
            throw new RemoteException("ERR Session expired");
        }

        try {
            byte[] fileData = fileManager.downloadFile(currentServerPath, serverFileName);
            if (fileData == null) {
                throw new RemoteException("ERR File not found");
            }
            return fileData;
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException("ERR Download failed: " + e.getMessage());
        }
    }

    @Override
    public String logout() throws RemoteException {
        isLoggedIn = false;
        return "OK Logout success";
    }

    @Override
    public String getUsername() throws RemoteException {
        return username;
    }

    /**
     * Lấy đường dẫn server hiện tại
     */
    public String getCurrentServerPath() {
        return currentServerPath;
    }
}
