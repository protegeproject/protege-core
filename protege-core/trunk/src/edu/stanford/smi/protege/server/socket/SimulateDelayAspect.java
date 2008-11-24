package edu.stanford.smi.protege.server.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import edu.stanford.smi.protege.server.ServerProperties;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;

public class SimulateDelayAspect implements SocketAspect {
    
    private static int nDelayedCalls = 0;
    
    private SocketAspect delegate;
    private InputStream is;
    private OutputStream os;

    public static void delayForLatency() {
        if (ServerProperties.delayInMilliseconds() != 0) {
            SystemUtilities.sleepMsec(ServerProperties.delayInMilliseconds());
            if (++nDelayedCalls % 10 == 0) {
                Log.getLogger().info(nDelayedCalls + " delayed calls");
            }
        }
    }
    
    public SimulateDelayAspect(SocketAspect delegate) {
        this.delegate = delegate;
    }

    public void close() throws IOException {
        delegate.close();
    }

    public SocketAspect getDelegate() {
        return delegate;
    }

    public InputStream getInputStream() throws IOException {
        if (is == null) {
            is = new SimulateDelayInputStream(delegate.getInputStream());
        }
        return is;
    }

    public OutputStream getOutputStream() throws IOException {
        if (os == null) {
            os = new SimulateDelayOutputStream(delegate.getOutputStream());
        }
        return os;
    }
    
    public static boolean useSimulatedDelay() {
        return ServerProperties.getKiloBytesPerSecondDownload() != 0 
            || ServerProperties.getKiloBytesPerSecondUpload() != 0 ;
    }

}
