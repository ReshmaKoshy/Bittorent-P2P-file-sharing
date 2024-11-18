package nodeoperations;

import java.nio.ByteBuffer;

public class BitField {

    private static int totalPieces;
    private static boolean isCompleteFile = false;
    private static byte[] dataPayload;
    private static final byte[] LENGTH_HEADER = new byte[4];
    private static final byte MESSAGE_TYPE = 5;
    public static byte[] bitfield;

    public static void initializeBitfield(boolean completeFile, int pieceCount) {
        isCompleteFile = completeFile;
        totalPieces = pieceCount;
        int payloadSize = (int) Math.ceil((double) totalPieces / 8);
        int extraBits = totalPieces % 8;
        
        // Allocate and fill the length header
        ByteBuffer.wrap(LENGTH_HEADER).putInt(payloadSize);
        
        // Initialize the bitfield array with the header and type
        bitfield = new byte[payloadSize + 5];
        System.arraycopy(LENGTH_HEADER, 0, bitfield, 0, LENGTH_HEADER.length);
        bitfield[4] = MESSAGE_TYPE;

        if (isCompleteFile) {
            // Set all bits to 1 for complete file
            for (int i = 5; i < bitfield.length; i++) {
                bitfield[i] = (byte) 0xFF;
            }
            // Adjust the last byte for any extra bits
            if (extraBits > 0) {
                bitfield[bitfield.length - 1] &= (byte) (0xFF << (8 - extraBits));
            }
        }
    }

    public static void modifyBitField(int pieceIndex) {
        int arrayIndex = (pieceIndex - 1) / 8 + 5; // Offset by header and type bytes
        int bitPosition = 7 - ((pieceIndex - 1) % 8);
        bitfield[arrayIndex] |= (1 << bitPosition);
    }
}