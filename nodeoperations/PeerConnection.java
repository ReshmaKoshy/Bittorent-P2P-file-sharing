package nodeoperations;

import java.net.Socket;

public class PeerConnection {

    private int ownPeerId;         
    private int remotePeerId;        
    private Socket connectionSocket;  
    private byte[] remotePeerBitfield;      
    private boolean isRemotePeerInterested;  

    // Constructor to initialize peer connection
    public PeerConnection(int ownPeerId, int remotePeerId, Socket connectionSocket) {
        this.ownPeerId = ownPeerId;
        this.remotePeerId = remotePeerId;
        this.connectionSocket = connectionSocket;
        this.remotePeerBitfield = new byte[0]; // Initialize bitfield as an empty byte array
        this.isRemotePeerInterested = false;    // Default interest status
    }

    // Getter for the remote peer ID
    public int getRemotePeerId() {
        return remotePeerId;
    }


    // Getter for the connection socket
    public Socket getConnectionSocket() {
        return connectionSocket;
    }

    // Getter for the peer's bitfield
    public byte[] getPeerBitfield() {
        return remotePeerBitfield;
    }

    // Setter for the peer's bitfield
    public void setPeerBitfield(byte[] remotePeerBitfield) {
        this.remotePeerBitfield = remotePeerBitfield;
    }

    // Getter for the interest status of the peer
    public boolean isRemotePeerInterested() {
        return isRemotePeerInterested;
    }

    // Setter for the interest status of the peer
    public void setPeerInterested(boolean isRemotePeerInterested) {
        this.isRemotePeerInterested = isRemotePeerInterested;
    }

    // Getter for the local peer ID
    public int getLocalPeerId() {
        return ownPeerId;
    }

}
