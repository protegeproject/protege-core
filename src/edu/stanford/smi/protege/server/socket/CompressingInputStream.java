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
    
    public CompressingInputStream(InputStream is) {
        compressing = new ZipInputStream(is);
    }

    @Override
    public int read() throws IOException {
        if (!initialize() || entry == null) {
            return  -1;
        }
        int ret = compressing.read();
        if (ret < 0) {
            compressing.closeEntry();
            if ((entry = compressing.getNextEntry()) != null) {
                if (log.isLoggable(Level.FINER)) {
                    log.finer("InputStream: reading new segment " + entry.getName());
                }
                ret = compressing.read();
            }
        }
        return ret;
    }
    
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (!initialize() || entry == null) {
            return  -1;
        }
        int bytesRead = compressing.read(b, off, len);
        if (bytesRead < 0) {
            compressing.closeEntry();
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
}
