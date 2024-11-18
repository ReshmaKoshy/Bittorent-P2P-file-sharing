package nodeoperations;

public class Handshake {

    public byte[] handshake = new byte[32];
    private String HANDSHAKE_HEADER = "P2PFILESHARINGPROJ0000000000";

    public Handshake(int peer_ID) {
        String array = HANDSHAKE_HEADER  + Integer.toString(peer_ID); 
        handshake = array.getBytes();
    }

}