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
    boolean endOfStream = false;
    
    long totalData = 0;
    long compressedData = 0;
    long lastTotalsLogMsg = 0;
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
        }
        compressing.close();
    }
    
    private boolean checkStream() throws IOException {
        if (!initialized) {
            initialized = true;
            entry = compressing.getNextEntry();
            if  (entry == null) {
                endOfStream = true;
            }
            if (entry != null && entry.getName().equals(EOS)) {
                compressing.closeEntry();
                endOfStream = true;
                entry = null;
            }
        }
        return !endOfStream;
    }
    
    private boolean gotoNextEntry() throws IOException {
        compressing.closeEntry();
        logZipEntry(entry);
        entry = compressing.getNextEntry();
        if (entry == null) {
            if (log.isLoggable(Level.FINER)) {
                log.finer("Came to end of stream with no end of stream marker");
            }
            endOfStream = true;
        }
        else if (entry.getName().equals(EOS)) {
            if (log.isLoggable(Level.FINER)) {
                log.finer("Output stream left and end of stream marker");
            }
            compressing.closeEntry();
            endOfStream = true;
        } else {
            if (log.isLoggable(Level.FINER)) {
                log.finer("InputStream: reading new segment " + entry.getName());
            }
        }
        return endOfStream != true;
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
