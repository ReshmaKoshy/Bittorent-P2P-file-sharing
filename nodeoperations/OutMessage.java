package nodeoperations;

import java.net.Socket;

public class OutMessage {
	
	private byte[] message;
    private Socket connectionSocket;

     public Socket getConnectionSocket() {
         return connectionSocket;
     }

     public void setConnectionSocket(Socket connectionSocket) {
         this.connectionSocket = connectionSocket;
     }
	
	public byte[] getMessage() {
		return message;
	}
	
	public void setMessage(byte[] msg) {
		this.message = msg;
	}
	
	
}
