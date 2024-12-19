package nodeoperations;

import java.nio.ByteBuffer;

public class Unchoke {

	public byte[] unchoke = new byte[5];	
    private static final byte[] LENGTH_HEADER = new byte[4];
    private static final byte MESSAGE_TYPE = 1;

	public Unchoke() {

        ByteBuffer.wrap(LENGTH_HEADER).putInt(0);
        System.arraycopy(LENGTH_HEADER, 0, unchoke, 0, LENGTH_HEADER.length);
        unchoke[4] = MESSAGE_TYPE;

	}
	
}
