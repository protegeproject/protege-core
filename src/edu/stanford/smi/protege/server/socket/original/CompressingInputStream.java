package edu.stanford.smi.protege.server.socket.original;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import edu.stanford.smi.protege.util.Log;

public class CompressingInputStream extends InputStream {
    private static transient final Logger  log  = Log.getLogger(CompressingInputStream.class);
    private ZipInputStream compressing;
    private ZipEntry entry;
    private boolean initialized = false;
    
    private long totalData = 0;
    private long compressedData = 0;
    private long lastTotalsLogMsg = 0;
    private String lastSegmentRead;
    public final static String EOS = "End of Stream";
    
    public CompressingInputStream(InputStream is) {
        compressing = new ZipInputStream(is);
    }

    @Override
    public int read() throws IOException {
        if (!checkStream()) {
            return  -1;
        }
        int ret = -1;
        ret = compressing.read();
        if (ret < 0 && gotoNextEntry()) {
            ret = compressing.read();
        }
        return ret;
    }
    
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (!checkStream()) {
            return  -1;
        }
        int bytesRead = -1;
        bytesRead = compressing.read(b, off, len);
        if (bytesRead < 0 && gotoNextEntry()) {
            bytesRead = compressing.read(b, off, len);
        }
        return bytesRead;
    }
 
    @Override
    public void close() throws IOException {
        if (entry != null)  {
            compressing.closeEntry();
            logZipEntry(entry);
            entry = null;
        }
        compressing.close();
    }
    
    private boolean checkStream() throws IOException {
        if (!initialized) {
            initialized = true;
            entry = compressing.getNextEntry();
            if (entry != null && entry.getName().equals(EOS)) {
                compressing.closeEntry();
                logZipEntry(entry);
                entry = null;
            }
        }
        return entry != null;
    }
    
    private boolean gotoNextEntry() throws IOException {
        if (entry == null) {
            return false;
        }
        if (log.isLoggable(Level.FINER)) {
            log.finer("last segment read was " + lastSegmentRead + " getting new segment");
        }
        compressing.closeEntry();
        logZipEntry(entry);
        entry = compressing.getNextEntry();
        if (entry == null) {
            if (log.isLoggable(Level.FINER)) {
                log.finer("Came to end of stream with no end of stream marker");
            }
        }
        else if (entry.getName().equals(EOS)) {
            if (log.isLoggable(Level.FINER)) {
                log.finer("Output stream left an end of stream marker");
            }
            compressing.closeEntry();
            logZipEntry(entry);
            entry = null;
        } else {
            if (log.isLoggable(Level.FINER)) {
                lastSegmentRead = entry.getName();
                log.finer("InputStream: reading new segment " + entry.getName());
            }
        }
        return entry != null;
    }
    
    private void logZipEntry(ZipEntry entry) {
        try {
            if (!log.isLoggable(Level.FINE)) {
                return;
            }
            if (entry.getName().equals(EOS)) {
                log.fine("EOS found");
                return;
            }
            totalData += entry.getSize();
            compressedData += entry.getCompressedSize();
            if (compressedData != 0 && (System.currentTimeMillis() - lastTotalsLogMsg >= 5000)) {
                log.fine(String.format("Average Compression Ratio = %.3f to 1, Compressed = %.2f MB, Uncompressed = %.2f MB (Cumulative) ", 
                                       (((double) totalData) / ((double) compressedData)),
                                       ((double) compressedData)/(1024.0 * 1024.0),
                                       ((double) totalData)/(1024.0 * 1024.0)));
                lastTotalsLogMsg = System.currentTimeMillis();
            }
            if (!log.isLoggable(Level.FINER)) {
                return;
            }
            log.finer("" + entry.getName() 
                      + " storage method " + (entry.getMethod() == ZipEntry.STORED ? "Uncompressed" : "Compressed")
                      + " read - original size " + entry.getSize() 
                      + " compressed size " + entry.getCompressedSize());
        }
        catch (Throwable t) {
            log.fine("Exception caught trying to log zip entry " + t);
        }
    }
}
