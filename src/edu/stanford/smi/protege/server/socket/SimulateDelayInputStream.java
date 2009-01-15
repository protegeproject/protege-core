package edu.stanford.smi.protege.server.socket;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.server.ServerProperties;
import edu.stanford.smi.protege.util.Log;

public class SimulateDelayInputStream extends FilterInputStream {
    static Logger log = Log.getLogger(SimulateDelayInputStream.class);
    private static int KB = MonitoringAspect.KB;
    
    private InputStream is;
    
    private static int bandwidth = ServerProperties.getKiloBytesPerSecondDownload();

    private int bytesRead = 0;

    
    public SimulateDelayInputStream(InputStream is) throws IOException {
        super(is);
        this.is = is;
    }
    
    @Override
    public int read() throws IOException {
        int ret = is.read();
        if (ret != -1) {
            delayForUpload(1);
        }
        return ret;
    }
    
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int ret = is.read(b, off, len);;
        if (ret > 0) {
            delayForUpload(ret);
        }
        return ret;
    }
    
    @Override
    public long skip(long n) throws IOException {
        long ret = super.skip(n);
        if (ret > 0) {
            delayForUpload((int) ret);
        }
        return ret;
    }
       
    private synchronized void delayForUpload(int n) {        
        bytesRead += n;
        
        if (bandwidth != 0) {
            int secondsDelay = bytesRead / (bandwidth * KB);
            if (secondsDelay > 0) {
                bytesRead -= bandwidth * KB * secondsDelay;
                try {
                    Thread.sleep(1000 * secondsDelay);
                    log.info("Simulated " + secondsDelay + " seconds of upload delay");
                } catch (InterruptedException e) {
                    log.log(Level.WARNING, "Simulation of network delay failed", e);
                }
            }
        }
    }

}
