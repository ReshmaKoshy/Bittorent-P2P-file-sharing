package nodeoperations;

import java.nio.ByteBuffer;

public class NotInterested {

    public byte[] not_interested = new byte[5];	
    private static final byte[] LENGTH_HEADER = new byte[4];
    private static final byte MESSAGE_TYPE = 3;

	public void Notinterested() {

        ByteBuffer.wrap(LENGTH_HEADER).putInt(0);
        System.arraycopy(LENGTH_HEADER, 0, not_interested, 0, LENGTH_HEADER.length);
        not_interested[4] = MESSAGE_TYPE;

	}
	
}
