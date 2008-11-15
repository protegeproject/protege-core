package edu.stanford.smi.protege.server.socket;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class CompressingOutputStream extends OutputStream {
    private OutputStream original;
    private GZIPOutputStream compressed;
    
    public CompressingOutputStream(OutputStream original) {
        this.original = original;
    }

    @Override
    public void write(int b) throws IOException {
        if (compressed == null) {
            compressed = new GZIPOutputStream(original);
        }
        compressed.write(b);
        return;
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
    
    @Override
    public void flush() throws IOException {
        if (compressed != null) {
            compressed.finish();
        }
        else {
            original.flush();
        }
    }

}
