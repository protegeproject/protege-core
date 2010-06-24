package edu.stanford.smi.protege.server.socket.deflate;

import java.io.IOException;
import java.io.OutputStream;

/**
 * doesn't work - fix the two broken junits.
 */

public class HybridCompressingOutputStream extends CompressingOutputStream {
    public static int TOO_SMALL_TO_COMPRESS = 768;
    
    public HybridCompressingOutputStream(OutputStream os) {
        super(os);
    }
    
    
    @Override
    public void flush() throws IOException {
        if (offset > TOO_SMALL_TO_COMPRESS) {
            super.flush();
        }
        else {
            try {
                new PacketHeader(offset, offset).write(os);
                os.write(buffer, 0, offset);
            }
            finally {
                offset = 0;
            }
        }
    }
    
}
