package nodeoperations;

import java.nio.ByteBuffer;

public class Choke {
	
	public byte[] choke = new byte[5];
    private static final byte[] LENGTH_HEADER = new byte[4];
    private static final byte MESSAGE_TYPE = 0;
	
	public Choke() {
        ByteBuffer.wrap(LENGTH_HEADER).putInt(0);
        System.arraycopy(LENGTH_HEADER, 0, choke, 0, LENGTH_HEADER.length);
        choke[4] = MESSAGE_TYPE;

	
	}
		
}