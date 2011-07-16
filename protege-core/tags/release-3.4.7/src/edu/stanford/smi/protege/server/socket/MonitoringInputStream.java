package edu.stanford.smi.protege.server.socket;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.util.Log;

public class MonitoringInputStream extends InputStream {
    static Logger log = Log.getLogger(MonitoringInputStream.class);
    private static int KB = MonitoringAspect.KB;
    
    private static int counter = 0;
    
    private InputStream is;
    
    private int id;
    private int bytesRead = 0;
    private boolean readingNotified = false;

    
    public MonitoringInputStream(InputStream is) throws IOException {
        this.is = is;
        id = counter++;
        if (log.isLoggable(Level.FINER)) {
            log.finer(logPrefix() + " opened.");
        }
    }
    
    @Override
    public int read() throws IOException {
        int ret = -1;
        try {
            if (log.isLoggable(Level.FINER)) {
                log.finer(logPrefix() + "requesting byte");
            }
            ret = is.read();
            showRead(ret);
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
            if (log.isLoggable(Level.FINER)) {
                log.finer(logPrefix() + "requesting " + len + " bytes");
            }
            ret = is.read(b, off, len);
            showRead(b, off, len, ret);
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
            ret = is.skip(n);
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
            is.close();
        }
        catch (Throwable t) {
            rethrow(t);
        }
    }
    
    private void showRead(byte[] b, int off, int len, int ret) {
        if (log.isLoggable(Level.FINER)) {
            if (len != ret) {
                log.finer(logPrefix() + "asked for " + len + " bytes and received " + ret);
            }
        }
        if (log.isLoggable(Level.FINEST)) {
            StringBuffer sb = new StringBuffer(logPrefix());
            sb.append("bytes read: ");
            for (int i = off; i < off + ret; i++) {
                sb.append(b[i]);
                sb.append(" ");
            }
            log.finest(sb.toString());
        }
    }
    
    private void showRead(int b) {
        if (log.isLoggable(Level.FINE)) {
            if (b < 0) {
                log.fine(logPrefix() + "End of Stream read");
            }
        }
        if (log.isLoggable(Level.FINEST)) {
            log.finest(logPrefix() + " byte read " + (byte) b + " : " + b);
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
