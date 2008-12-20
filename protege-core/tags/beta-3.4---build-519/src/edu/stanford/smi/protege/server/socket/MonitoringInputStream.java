package edu.stanford.smi.protege.server.socket;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.util.Log;

public class MonitoringInputStream extends FilterInputStream {
    static Logger log = Log.getLogger(MonitoringInputStream.class);
    private static int KB = MonitoringAspect.KB;
    
    private static int counter = 0;
    
    private int id;
    private int bytesRead = 0;
    private boolean readingNotified = false;

    
    public MonitoringInputStream(InputStream is) throws IOException {
        super(is);
        id = counter++;
        if (log.isLoggable(Level.FINER)) {
            log.finer(logPrefix() + " opened.");
        }
    }
    
    @Override
    public int read() throws IOException {
        int ret = -1;
        try {
            ret = super.read();
            if (ret != -1) {
                countRead(1);
            }
        }
        catch (Throwable t) {
            rethrow(t);
        }
        return ret;
    }
    
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int ret = -1;
        try {
            ret = super.read(b, off, len);;
            if (ret > 0) {
                countRead(ret);
            }
        }
        catch (Throwable t) {
            rethrow(t);
        }
        return ret;
    }
    
    @Override
    public long skip(long n) throws IOException {
        long ret = -1;
        try {
            ret = super.skip(n);
            if (ret > 0) {
                countRead((int) ret);
            }
             }
        catch (Throwable t) {
            rethrow(t);
        }
       return ret;
    }
    
    @Override
    public void close() throws IOException {
        try {
            if (log.isLoggable(Level.FINER)) {
                log.finer(logPrefix() + "closing");
            }
            readingNotified = false;
            super.close();
        }
        catch (Throwable t) {
            rethrow(t);
        }
    }
    
    private synchronized void countRead(int n) {
        if (log.isLoggable(Level.FINER) && !readingNotified) {
            log.finer(logPrefix() + "reading");
            readingNotified = true;
        }
        int previousMB = bytesRead / (KB * KB);
        bytesRead += n;
        if (log.isLoggable(Level.FINE)) {
            int newMB = bytesRead / (KB * KB);
            if (newMB > previousMB) {
                log.fine(logPrefix() + newMB + " megabytes read");
            }
        }
    }
    
    private String logPrefix() {
        return "InputStream " + id + ": ";
    }

    private void rethrow(Throwable t) throws IOException {
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Exception caught", t);
        }
        if (t instanceof IOException) {
            throw (IOException) t;
        }
        else {
            IOException ioe = new IOException(t.getMessage());
            ioe.initCause(t);
            throw ioe;
        }
    }

}
