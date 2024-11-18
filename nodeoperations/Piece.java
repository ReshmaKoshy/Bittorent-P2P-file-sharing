package nodeoperations;

import java.nio.ByteBuffer;

public class Piece {

    public byte[] piece;
    private static final byte MESSAGE_LENGTH = 4;
    private static final byte MESSAGE_TYPE = 7;
    private static final byte PIECE_INDEX_LENGTH = 4;
    private byte[] lengthHeader = new byte[4];
    private byte[] indexBytes = new byte[4];
    private byte[] payload;

    public Piece(int index, byte[] data) {
        this.payload = data;
        int payloadLength = payload.length;
        
        // Prepare the index and length headers
        indexBytes = ByteBuffer.allocate(PIECE_INDEX_LENGTH).putInt(index).array();
        lengthHeader = ByteBuffer.allocate(MESSAGE_LENGTH).putInt( PIECE_INDEX_LENGTH + payloadLength).array();
        
        // Initialize the piece array with the correct size
        piece = new byte[MESSAGE_LENGTH + 1 + PIECE_INDEX_LENGTH + payloadLength];

        int position = 0;
        
        // Copy the length header into the piece array
        System.arraycopy(lengthHeader, 0, piece, position, lengthHeader.length);
        position += MESSAGE_LENGTH;
        
        // Set the message type
        piece[position] = MESSAGE_TYPE;
        position++;
        
        // Copy the index bytes into the piece array
        System.arraycopy(indexBytes, 0, piece, position, indexBytes.length);
        position += indexBytes.length;
        
        // Copy the payload into the piece array
        System.arraycopy(payload, 0, piece, position, payload.length);
    }
}