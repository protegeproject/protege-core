package edu.stanford.smi.protege.server.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

public class MonitoringAspect implements SocketAspect {
    public final static int KB = 1024;
    private SocketAspect delegate;

    private InputStream is;
    private OutputStream os;
    
    public MonitoringAspect(SocketAspect aspect) {
        delegate = aspect;
    }
    
    public SocketAspect getDelegate() {
        return delegate;
    }

    public InputStream getInputStream() throws IOException {
        if (is == null) {
            is = new MonitoringInputStream(delegate.getInputStream());
        }
        return is;
    }

    public OutputStream getOutputStream() throws IOException {
        if (os == null) {
            os = new MonitoringOutputStream(delegate.getOutputStream());
        }
        return os;
    }
    
    public void close() throws IOException {
        delegate.close();
    }
    
    public static boolean useMonitoring() {
        return MonitoringInputStream.log.isLoggable(Level.FINE)
            || MonitoringOutputStream.log.isLoggable(Level.FINE);
    }

}
