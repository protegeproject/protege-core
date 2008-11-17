package edu.stanford.smi.protege.server.socket;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.server.ServerProperties;
import edu.stanford.smi.protege.util.Log;

public class MonitoringInputStream extends FilterInputStream {
    static Logger log = Log.getLogger(MonitoringInputStream.class);
    private static int KB = MonitoringAspect.KB;
    
    private static int counter = 0;
    
    private int bandwidth = ServerProperties.getKiloBytesPerSecondDownload();
    
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
        int ret = super.read();
        if (ret != -1) {
            countRead(1);
        }
        return ret;
    }
    
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int ret = super.read(b, off, len);;
        if (ret > 0) {
            countRead(ret);
        }
        return ret;
    }
    
    @Override
    public long skip(long n) throws IOException {
        long ret = super.skip(n);
        if (ret > 0) {
            countRead((int) ret);
        }
        return ret;
    }
    
    @Override
    public void close() throws IOException {
        if (log.isLoggable(Level.FINER)) {
            log.finer(logPrefix() + "closing");
        }
        readingNotified = false;
        super.close();
    }
    
    private synchronized void countRead(int n) {
        if (log.isLoggable(Level.FINER) && !readingNotified) {
            log.finer(logPrefix() + "reading");
            readingNotified = true;
        }
        int previousSecondsDelay = bandwidth != 0 ? bytesRead / (bandwidth * KB) : 0;
        int previousMB = bytesRead / (KB * KB);
        
        bytesRead += n;
        
        if (bandwidth != 0) {
            int newSecondsDelay = bytesRead / (bandwidth * KB);
            if (newSecondsDelay > previousSecondsDelay) {
                try {
                    Thread.sleep(1000 * (newSecondsDelay - previousSecondsDelay));
                } catch (InterruptedException e) {
                    log.log(Level.WARNING, "Simulation of network delay failed", e);
                }
            }
        }
        if (log.isLoggable(Level.FINER)) {
            int newMB = bytesRead / (KB * KB);
            if (newMB > previousMB) {
                log.finer(logPrefix() + newMB + " megabytes read");
            }
        }
    }
    
    private String logPrefix() {
        return "InputStream " + id + ": ";
    }
}
