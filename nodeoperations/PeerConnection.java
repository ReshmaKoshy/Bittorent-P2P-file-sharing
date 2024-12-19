package nodeoperations;

import java.net.Socket;

public class PeerConnection {

    private int ownPeerId;         
    private int remotePeerId;        
    private Socket connectionSocket;  
    private byte[] remotePeerBitfield;      
    private boolean isRemotePeerInteresting;
    private boolean isRemotePeerInterested;
    private boolean isChoked;

    // Constructor to initialize peer connection
    public PeerConnection(int ownPeerId, int remotePeerId, Socket connectionSocket) {
        this.ownPeerId = ownPeerId;
        this.remotePeerId = remotePeerId;
        this.connectionSocket = connectionSocket;
        this.remotePeerBitfield = new byte[0]; // Initialize bitfield as an empty byte array
        this.isRemotePeerInteresting = false;    // Default interest status
        this.isRemotePeerInterested = false;
        this.isChoked = true;
    }

    // Getter for the remote peer ID
    public int getRemotePeerId() {
        return remotePeerId;
    }


    // Getter for the connection connectionSocket
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
    public boolean isRemotePeerInteresting() {
        return isRemotePeerInteresting;
    }

    // Setter for the interest status of the peer
    public void setPeerInterested(boolean isRemotePeerInteresting) {
        this.isRemotePeerInteresting = isRemotePeerInteresting;
    }

    // Getter for the local peer ID
    public int getLocalPeerId() {
        return ownPeerId;
    }

    public boolean isRemotePeerInterested() {
        return isRemotePeerInterested;
    }

    public void setRemotePeerInterested(boolean remotePeerInterested) {
        this.isRemotePeerInterested = remotePeerInterested;
    }

    public boolean isChoked() {
        return isChoked;
    }

    public void setChoked(boolean choked) {
        this.isChoked = choked;
    }

}
