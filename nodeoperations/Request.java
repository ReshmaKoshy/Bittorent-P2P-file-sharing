package nodeoperations;

import java.nio.ByteBuffer;

public class Request {
	
	private static final byte[] LENGTH_HEADER = new byte[4];
    private static final byte MESSAGE_TYPE = 6;
    private static final int PAYLOAD_SIZE = 4;
    private static byte[] dataPayload;
    public static byte[] request = new byte[9];

	
	public Request(int pieceIndex) {

		// Initialize the length header with constant size 4
        ByteBuffer.wrap(LENGTH_HEADER).putInt(PAYLOAD_SIZE);
        
        dataPayload = new byte[PAYLOAD_SIZE];
        ByteBuffer.wrap(dataPayload).putInt(pieceIndex);
        
        // // Create and populate the complete message array
        // statusMessage = new byte[PAYLOAD_SIZE + 5];

        // Copy the length header
        System.arraycopy(LENGTH_HEADER, 0, request, 0, LENGTH_HEADER.length);
        
        // Set message type
        request[4] = MESSAGE_TYPE;
        
        // Copy the piece index payload
        System.arraycopy(dataPayload, 0, request, 5, dataPayload.length);
		
	}
	
}
