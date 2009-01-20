package edu.stanford.smi.protege.server.socket.original;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import edu.stanford.smi.protege.server.ServerProperties;
import edu.stanford.smi.protege.util.Log;

public class CompressingOutputStream extends OutputStream {
    private static final transient Logger log = Log.getLogger(CompressingOutputStream.class);
    
    private int smallSize = ServerProperties.tooSmallToCompress();
    private byte[] data = new byte[smallSize];
    int offset = 0;  // the next location in the buffer to write to
                     // also doubles as the size of the unflushed data
    private boolean inZipEntry = false;
    
    private OutputStream os;
    private ZipOutputStream compressing;
    private static int blockCounter = 0;
    private int currentSegmentCounter = 0;

    public CompressingOutputStream(OutputStream os) {
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
            currentSegmentCounter = blockCounter;
            ZipEntry entry = new ZipEntry("Segment" + blockCounter++);
            entry.setMethod(ZipEntry.STORED);
            CRC32 crc = new CRC32();
            crc.update(data, 0, offset);
            entry.setCrc(crc.getValue());
            entry.setSize(offset);
            compressing.putNextEntry(entry);
            compressing.write(data, 0, offset);
            offset = 0;
            closingZipEntry = true;
        }
        else if (inZipEntry) {
            closingZipEntry = true;
        }
        if (closingZipEntry) {
            inZipEntry = false;
            compressing.closeEntry();
            compressing.flush();
            if (log.isLoggable(Level.FINER)) {
                log.finer("OutputStream: Segment" + currentSegmentCounter + " written");
            }
        } else {
            os.flush();
        }
    }
    
    @Override
    public void close() throws IOException {
        if (inZipEntry) {
            compressing.closeEntry();
            inZipEntry = false;
        }
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
    
    private boolean stillBuffering(int moreToWrite) throws IOException {
        if (inZipEntry) {
            return false;
        }
        if (offset + moreToWrite < smallSize) {
            return true;
        }
        else {
            currentSegmentCounter = blockCounter;
            ZipEntry entry = new ZipEntry("Segment" + blockCounter++);
            entry.setMethod(ZipEntry.DEFLATED);
            compressing.putNextEntry(entry);
            inZipEntry = true;
            compressing.write(data, 0, offset);
            offset = 0;
            return false;
        }
    }

}
