package edu.stanford.smi.protege.server.socket;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class CompressingInputStream extends InputStream {
    private InputStream original;
    private GZIPInputStream compressed;
    
    public CompressingInputStream(InputStream original) {
        this.original = original;
    }
    

    @Override
    public int read() throws IOException {
        if (compressed == null) {
            compressed = new GZIPInputStream(original);
        }
        return compressed.read();
    }
    
    @Override
    public int available() throws IOException {
        return compressed.available();
    }
    
    @Override
    public void close() throws IOException {
        if (compressed != null) {
            compressed.close();
        }
        else {
            original.close();
        }
    }

}
