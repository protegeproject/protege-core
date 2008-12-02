package edu.stanford.smi.protege.server.socket;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import edu.stanford.smi.protege.util.Log;

public class CompressingOutputStream2 extends OutputStream {
    private static final transient Logger log = Log.getLogger(CompressingOutputStream2.class);

    public final static int SMALL_DATA = 1024;
    
    private byte[] data = new byte[SMALL_DATA];
    int offset = 0;  // the next location in the buffer to write to
                     // also doubles as the size of the unflushed data
    private boolean inZipEntry = false;
    
    private OutputStream os;
    private ZipOutputStream compressing;
    private static int blockCounter = 0;

    public CompressingOutputStream2(OutputStream os) {
        this.os = os;
        compressing = new ZipOutputStream(os);
    }
    

    @Override
    public void write(int b) throws IOException {
        if (stillBuffering(1)) {
            data[offset++] = (byte) b;
        }
        else {
            compressing.write(b);
        }
    }
    
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (stillBuffering(len)) {
            for (; len > 0; offset++, off++, len--) {
                data[offset] = b[off];
            }
        }
        else {
            compressing.write(b, off, len);
        }
    }
    
    @Override
    public void flush() throws IOException {
        boolean closingZipEntry = false;
        if (!inZipEntry && offset > 0) {
            if (log.isLoggable(Level.FINER)) {
                log.finer("OutputStream: Flushing small output by starting new segment " + (blockCounter + 1));
            }
            ZipEntry entry = new ZipEntry("Segment" + blockCounter++);
            entry.setMethod(ZipEntry.STORED);
            CRC32 crc = new CRC32();
            crc.update(data, 0, offset);
            entry.setCrc(crc.getValue());
            entry.setSize(offset);
            compressing.putNextEntry(entry);
            compressing.write(data, 0, offset);
            closingZipEntry = true;
        }
        else if (inZipEntry) {
            closingZipEntry = true;
        }
        if (closingZipEntry) {
            inZipEntry = false;
            compressing.closeEntry();
            compressing.flush();
            offset = 0;
            if (log.isLoggable(Level.FINER)) {
                log.finer("OutputStream: segment " + blockCounter + " written (" + offset + " bytes)");
            }
        } else {
            os.flush();
        }
    }
    
    @Override
    public void close() throws IOException {
        flush();
        compressing.close();
    }
    
    private boolean stillBuffering(int moreToWrite) throws IOException {
        if (inZipEntry) {
            return false;
        }
        if (offset + moreToWrite < SMALL_DATA) {
            return true;
        }
        else {
            ZipEntry entry = new ZipEntry("Segment" + blockCounter++);
            entry.setMethod(ZipEntry.DEFLATED);
            compressing.putNextEntry(entry);
            compressing.write(data, 0, offset);
            offset = 0;
            inZipEntry = true;
            return false;
        }
    }

}
