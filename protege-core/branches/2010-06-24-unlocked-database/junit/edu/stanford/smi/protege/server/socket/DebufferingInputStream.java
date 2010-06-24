package edu.stanford.smi.protege.server.socket;

import java.io.IOException;
import java.io.InputStream;

public class DebufferingInputStream extends InputStream {
    private InputStream  is;
    
    public DebufferingInputStream(InputStream is) {
        this.is = is;
    }

    @Override
    public int read() throws IOException {
        return is.read();
    }
    
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return is.read(b, off, 1);
    }

}
