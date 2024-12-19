package nodeoperations;

import java.nio.ByteBuffer;

public class Have {
    
    private static final byte[] LENGTH_HEADER = new byte[4];
    private static final byte MESSAGE_TYPE = 4;
    private static final int PAYLOAD_SIZE = 4;
    private static byte[] dataPayload;
    public static byte[] have = new byte[9];

    public Have(int pieceIndex) {
        // Initialize the length header with constant size 4
        ByteBuffer.wrap(LENGTH_HEADER).putInt(PAYLOAD_SIZE);
        
        dataPayload = new byte[PAYLOAD_SIZE];
        ByteBuffer.wrap(dataPayload).putInt(pieceIndex);
        
        // // Create and populate the complete message array
        // statusMessage = new byte[PAYLOAD_SIZE + 5];

        // Copy the length header
        System.arraycopy(LENGTH_HEADER, 0, have, 0, LENGTH_HEADER.length);
        
        // Set message type
        have[4] = MESSAGE_TYPE;
        
        // Copy the piece index payload
        System.arraycopy(dataPayload, 0, have, 5, dataPayload.length);
    }
}