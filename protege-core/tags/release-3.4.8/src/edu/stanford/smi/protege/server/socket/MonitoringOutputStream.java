package edu.stanford.smi.protege.server.socket;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.util.Log;

public class MonitoringOutputStream extends OutputStream {
    static Logger log = Log.getLogger(MonitoringOutputStream.class);
    private static int KB = MonitoringAspect.KB;
    private static int counter = 0;
    
    private OutputStream os;
    
    private int id;
    private int bytesWritten = 0;
    private int bytesToBeFlushed = 0;
    private boolean writingNotified = false;

    public MonitoringOutputStream(OutputStream os) {
        this.os = os;
        id = counter++;
        if (log.isLoggable(Level.FINER)) {
            log.finer(logPrefix() + "opened.");
        }
    }
    
    @Override
    public void write(int b) throws IOException {
        try {
            os.write(b);
            showByte(b);
            countWritten(1);
        }
        catch (Throwable t) {
            rethrow(t);
        }
    }
    
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        try {
            os.write(b, off, len);
            showBytes(b, off, len);
            countWritten(len);
        }
        catch (Throwable t) {
            rethrow(t);
        }
    }

    @Override
    public void flush() throws IOException {
        try {
            if (log.isLoggable(Level.FINER)) {
                log.finer(logPrefix() + "flushing " + bytesToBeFlushed + " bytes");
            }
            bytesToBeFlushed = 0;
            writingNotified = false;
            os.flush();
        }
        catch (Throwable t) {
            rethrow(t);
        }
    }
    
    @Override
    public void close() throws IOException {
        try {
            if (log.isLoggable(Level.FINER)) {
                log.finer(logPrefix() + "closing");
            }
            writingNotified = false;
            os.close();
        }
        catch (Throwable t) {
            rethrow(t);
        }
    }
    
    private void showBytes(byte[] b, int off, int len) {
        if (log.isLoggable(Level.FINE)) {
            if (len == 0) {
                log.fine(logPrefix() + "writing zero bytes");
            }
        }
        if (log.isLoggable(Level.FINEST)) {
            if (log.isLoggable(Level.FINEST)) {
                StringBuffer sb = new StringBuffer(logPrefix());
                sb.append("bytes written: ");
                for (int i = off; i < off + len; i++) {
                    sb.append(b[i]);
                    sb.append(" ");
                }
                log.finest(sb.toString());
            }
        }
    }
    
    private void showByte(int b) {
        if (log.isLoggable(Level.FINEST)) {
            log.finest(logPrefix() + "writing " + b);
        }
    }

    private synchronized void countWritten(int n) {
        if (log.isLoggable(Level.FINER) && !writingNotified) {
            log.finer(logPrefix() + "writing");
            writingNotified = true;
        }
        int previousMB = bytesWritten / (KB * KB);
        
        bytesWritten += n;
        bytesToBeFlushed += n;

        if (log.isLoggable(Level.FINE)) {
            int newMB = bytesWritten / (KB * KB);
            if (newMB > previousMB) {
                log.fine(logPrefix() + newMB + " megabytes written");
            }
        }
    }

    private String logPrefix() {
        return "OutputStream " + id + ": ";
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
