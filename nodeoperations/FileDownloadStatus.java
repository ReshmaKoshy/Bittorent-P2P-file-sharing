package nodeoperations;

import java.net.Socket;

public class FileDownloadStatus {

    
    private boolean isFullFileDownloadComplete;
    private Socket connectionSocket;

     public Socket getConnectionSocket() {
         return connectionSocket;
     }

     public void setConnectionSocket(Socket connectionSocket) {
         this.connectionSocket = connectionSocket;
     }

    public boolean isFullFileDownloadComplete() {
        return isFullFileDownloadComplete;
    }
    
    public void setFullFileDownloadComplete(boolean isFullFileDownloadComplete) {
        this.isFullFileDownloadComplete = isFullFileDownloadComplete;
    }
}