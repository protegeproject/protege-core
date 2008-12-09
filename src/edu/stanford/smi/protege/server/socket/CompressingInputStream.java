package edu.stanford.smi.protege.server.socket;

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
    boolean initialized = false;
    
    long totalData = 0;
    long compressedData = 0;
    long lastTotalsLogMsg = 0;
    
    public CompressingInputStream(InputStream is) {
        compressing = new ZipInputStream(is);
    }

    @Override
    public int read() throws IOException {
        if (!initialize() || entry == null) {
            return  -1;
        }
        int ret = -1;
        if (compressing.available() != 0) {
            ret = compressing.read();
        }
        if (ret < 0) {
            compressing.closeEntry();
            logZipEntry(entry);
            if ((entry = compressing.getNextEntry()) != null) {
                if (log.isLoggable(Level.FINER)) {
                    log.finer("InputStream: reading new segment " + entry.getName());
                }
                ret = compressing.read();
            }
            else {
                compressing.close();
            }
        }
        return ret;
    }
    
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (!initialize() || entry == null) {
            return  -1;
        }
        int bytesRead = -1;
        if (compressing.available() != 0) {
            bytesRead = compressing.read(b, off, len);
        }
        if (bytesRead < 0) {
            compressing.closeEntry();
            logZipEntry(entry);
            if  ((entry = compressing.getNextEntry()) != null) {
                if (log.isLoggable(Level.FINER)) {
                    log.finer("InputStream: reading new segment " + entry.getName());
                }
                bytesRead = compressing.read(b, off, len);
            }
        }
        return bytesRead;
    }
 
    @Override
    public void close() throws IOException {
        if (entry != null)  {
            compressing.closeEntry();
        }
        compressing.close();
    }
    
    private boolean initialize() throws IOException {
        if (!initialized) {
            initialized = true;
            entry = compressing.getNextEntry();
            return entry != null;
        }
        return true;
    }
    
    private void logZipEntry(ZipEntry entry) {
        totalData += entry.getSize();
        compressedData += entry.getCompressedSize();
        if (log.isLoggable(Level.FINE) && compressedData != 0 && (System.currentTimeMillis() - lastTotalsLogMsg >= 5000)) {
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
}
