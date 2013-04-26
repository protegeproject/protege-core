package edu.stanford.smi.protege.server.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class TeeAspect implements SocketAspect {
    private SocketAspect delegate;
    private OutputStream os;

    public TeeAspect(SocketAspect delegate) {
        super();
        this.delegate = delegate;
    }
    
    public SocketAspect getDelegate() {
        return delegate;
    }


    public void close() throws IOException {
        if (os != null) {
            os.flush();
            os.close();
        }
        if (getInputStream() != null) {
            getInputStream().close();
        }
    }

    public InputStream getInputStream() throws IOException {
        return delegate.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        if (os == null) {
            os = new TeeOutputStream(delegate.getOutputStream());
        }
        return os;
    }

    public static boolean doTeeOuput() {
        return System.getProperty(TeeOutputStream.TEE_OUTPUT_OPTION) != null;
    }

}
