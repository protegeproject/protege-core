package edu.stanford.smi.protege.server.socket;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.server.ServerProperties;
import edu.stanford.smi.protege.util.Log;

public class SimulateDelayOutputStream extends FilterOutputStream {
    static Logger log = Log.getLogger(SimulateDelayOutputStream.class);
    private static int KB = MonitoringAspect.KB;
    
    private OutputStream os;
    
    private int bandwidth = ServerProperties.getKiloBytesPerSecondUpload();
    
    private int bytesWritten = 0;

    public SimulateDelayOutputStream(OutputStream os) {
        super(os);
        this.os = os;
    }
    
    @Override
    public void write(int b) throws IOException {
        os.write(b);
        delayForDownLoad(1);
    }
    
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        os.write(b, off, len);
        delayForDownLoad(len);
    }

    
    private synchronized void delayForDownLoad(int n) {
        bytesWritten += n;
        
        if (bandwidth != 0) {
            int secondsDelay = bytesWritten / (bandwidth * KB);
            if (secondsDelay > 0) {
                bytesWritten -= secondsDelay * bandwidth * KB;
                try {
                    Thread.sleep(1000 * secondsDelay);
                    log.info("Simulated " + secondsDelay + " seconds of download delay");
                } catch (InterruptedException e) {
                    log.log(Level.WARNING, "Simulation of network delay failed", e);
                }
            }
        }
    }
}
