package edu.stanford.smi.protege.server.socket.buffered;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import edu.stanford.smi.protege.server.ServerProperties;
import edu.stanford.smi.protege.server.socket.original.CompressingInputStream;
import edu.stanford.smi.protege.util.Log;

public class CompressingOutputStream extends OutputStream {
    private static final transient Logger log = Log.getLogger(CompressingOutputStream.class);
    public final static int BUFFER_SIZE = 16 * 4096;
    
    private int smallSize = ServerProperties.tooSmallToCompress();
    
    
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
    public void flush() throws IOException {
        if (offset > 0) {
            if (log.isLoggable(Level.FINER)) {
                log.finer("OutputStream: Flushing output by starting new segment " + (blockCounter + 1));
            }
            ZipEntry entry = new ZipEntry("Segment" + blockCounter++);
            if (offset < smallSize) {
                entry.setMethod(ZipEntry.STORED);
                CRC32 crc = new CRC32();
                crc.update(data, 0, offset);
                entry.setCrc(crc.getValue());
            }
            else {
                entry.setMethod(ZipEntry.DEFLATED);
            }
            entry.setSize(offset);
            compressing.putNextEntry(entry);
            compressing.write(data, 0, offset);
            compressing.closeEntry();
            compressing.flush();
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
        
        // mark the end of file so the reader can be warned and clean up
        ZipEntry entry = new ZipEntry(CompressingInputStream.EOS);
        entry.setMethod(ZipEntry.STORED);
        CRC32 crc = new CRC32();
        crc.update(data, 0, 0);
        entry.setCrc(crc.getValue());
        entry.setSize(offset);
        compressing.putNextEntry(entry);
        compressing.closeEntry();
        compressing.flush();

        compressing.close();
    }
    
    
    private void ensureNotFull() throws IOException {
        if (offset >= BUFFER_SIZE) {
            flush();
        }
    }

}
