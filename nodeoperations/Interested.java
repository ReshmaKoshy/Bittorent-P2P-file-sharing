package nodeoperations;

import java.nio.ByteBuffer;

public class Interested {

    public byte[] interested = new byte[5];	
    private static final byte[] LENGTH_HEADER = new byte[4];
    private static final byte MESSAGE_TYPE = 2;

	public Interested() {

        ByteBuffer.wrap(LENGTH_HEADER).putInt(0);
        System.arraycopy(LENGTH_HEADER, 0, interested, 0, LENGTH_HEADER.length);
        interested[4] = MESSAGE_TYPE;

	}
	
}
