package edu.stanford.smi.protege.server.socket;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.util.Log;

public class MonitoringOutputStream extends FilterOutputStream {
    static Logger log = Log.getLogger(MonitoringOutputStream.class);
    private static int KB = MonitoringAspect.KB;
    private static int counter = 0;
    
    private int id;
    private int bytesWritten = 0;
    private boolean writingNotified = false;

    public MonitoringOutputStream(OutputStream os) {
        super(os);
        id = counter++;
        if (log.isLoggable(Level.FINER)) {
            log.finer(logPrefix() + "opened.");
        }
    }
    
    @Override
    public void write(int b) throws IOException {
        try {
            super.write(b);
            countWritten(1);
        }
        catch (Throwable t) {
            rethrow(t);
        }
    }
    
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        try {
            super.write(b, off, len);
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
                log.finer(logPrefix() + "flushing");
            }
            writingNotified = false;
            super.flush();
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
            super.close();
        }
        catch (Throwable t) {
            rethrow(t);
        }
    }

    private synchronized void countWritten(int n) {
        if (log.isLoggable(Level.FINER) && !writingNotified) {
            log.finer(logPrefix() + "writing");
            writingNotified = true;
        }
        int previousMB = bytesWritten / (KB * KB);
        
        bytesWritten += n;

        if (log.isLoggable(Level.FINER)) {
            int newMB = bytesWritten / (KB * KB);
            if (newMB > previousMB) {
                log.finer(logPrefix() + newMB + " megabytes read");
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
