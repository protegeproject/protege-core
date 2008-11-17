package edu.stanford.smi.protege.server.socket;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.server.ServerProperties;
import edu.stanford.smi.protege.util.Log;

public class MonitoringOutputStream extends FilterOutputStream {
    static Logger log = Log.getLogger(MonitoringOutputStream.class);
    private static int KB = MonitoringAspect.KB;
    private static int counter = 0;
    
    private int bandwidth = ServerProperties.getKiloBytesPerSecondUpload();
    
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
        super.write(b);
        countWritten(1);
    }
    
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
        countWritten(len);
    }

    @Override
    public void flush() throws IOException {
        if (log.isLoggable(Level.FINER)) {
            log.finer(logPrefix() + "flushing");
        }
        writingNotified = false;
        super.flush();
    }
    
    @Override
    public void close() throws IOException {
        if (log.isLoggable(Level.FINER)) {
            log.finer(logPrefix() + "closing");
        }
        writingNotified = false;
        super.close();
    }
    
    private synchronized void countWritten(int n) {
        if (log.isLoggable(Level.FINER) && !writingNotified) {
            log.finer(logPrefix() + "writing");
            writingNotified = true;
        }
        int previousSecondsDelay = bandwidth != 0 ? bytesWritten / (bandwidth * KB) : 0;
        int previousMB = bytesWritten / (KB * KB);
        
        bytesWritten += n;
        
        if (bandwidth != 0) {
            int newSecondsDelay = bytesWritten / (bandwidth * KB);
            if (newSecondsDelay > previousSecondsDelay) {
                try {
                    Thread.sleep(1000 * (newSecondsDelay - previousSecondsDelay));
                } catch (InterruptedException e) {
                    log.log(Level.WARNING, "Simulation of network delay failed", e);
                }
            }
        }
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
}
