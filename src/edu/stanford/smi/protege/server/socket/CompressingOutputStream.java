package edu.stanford.smi.protege.server.socket;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import edu.stanford.smi.protege.util.Log;

public class CompressingOutputStream extends OutputStream {
    private static final transient Logger log = Log.getLogger(CompressingOutputStream.class);
    public final static int BUFFER_SIZE = 4096;
    
    private byte[] data = new byte[BUFFER_SIZE];
    int offset = 0;  // the next location in the buffer to write to
                     // also doubles as the size of the unflushed data
    
    private OutputStream os;
    private ZipOutputStream compressing;
    private static int blockCounter = 0;

    public CompressingOutputStream(OutputStream os) {
        this.os = os;
        compressing = new ZipOutputStream(os);
    }
    

    @Override
    public void write(int b) throws IOException {
        ensureNotFull();
        data[offset++] = (byte) b;
        ensureNotFull();
    }
    
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        while (len > 0) {
            ensureNotFull();
            for (; len > 0 && offset < BUFFER_SIZE; offset++, off++, len--) {
                data[offset] = b[off];
            }
        }
        ensureNotFull();
    }
    
    @Override
    public void flush() throws IOException {
        if (offset > 0) {
            if (log.isLoggable(Level.FINER)) {
                log.finer("OutputStream: Flushing output by starting new segment " + (blockCounter + 1));
            }
            ZipEntry entry = new ZipEntry("Segment" + blockCounter++);
            entry.setMethod(ZipEntry.DEFLATED);
            entry.setSize(offset);
            compressing.putNextEntry(entry);
            compressing.write(data, 0, offset);
            compressing.closeEntry();
            if (log.isLoggable(Level.FINER)) {
                log.finer("OutputStream: segment " + blockCounter + " written (" + offset + " bytes)");
            }
        }
        offset = 0;
        os.flush();
    }
    
    @Override
    public void close() throws IOException {
        flush();
        compressing.close();
    }
    
    
    private void ensureNotFull() throws IOException {
        if (offset >= BUFFER_SIZE) {
            flush();
        }
    }

}
