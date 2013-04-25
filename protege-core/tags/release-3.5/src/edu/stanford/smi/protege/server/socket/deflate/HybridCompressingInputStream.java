package edu.stanford.smi.protege.server.socket.deflate;

import java.io.IOException;
import java.io.InputStream;

/**
 * doesn't work - fix the two broken junits.
 */
public class HybridCompressingInputStream extends CompressingInputStream {
    
    public HybridCompressingInputStream(InputStream is) {
        super(is);
    }
    
    @Override
    protected void fillBuffer(PacketHeader header) throws IOException {
        if (header.getSize() > HybridCompressingOutputStream.TOO_SMALL_TO_COMPRESS) {
            super.fillBuffer(header);
        }
        else {
            readFully(buffer, header.getSize());
        }
    }

}
