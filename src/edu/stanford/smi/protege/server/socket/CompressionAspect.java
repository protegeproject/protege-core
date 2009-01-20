package edu.stanford.smi.protege.server.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import edu.stanford.smi.protege.server.ServerProperties;
import edu.stanford.smi.protege.server.socket.deflate.CompressingInputStream;
import edu.stanford.smi.protege.server.socket.deflate.CompressingOutputStream;

public class CompressionAspect implements SocketAspect {
    
    private SocketAspect delegate;
    private InputStream is;
    private OutputStream os;
    
    public CompressionAspect(SocketAspect delegate) {
        this.delegate = delegate;
    }

    public SocketAspect getDelegate() {
        return delegate;
    }

    public InputStream getInputStream() throws IOException {
        if (is == null) {
            is = new CompressingInputStream(delegate.getInputStream());
        }
        return is;
    }

    public OutputStream getOutputStream() throws IOException {
        if (os == null) {
            os = new CompressingOutputStream(delegate.getOutputStream());
        }
        return os;
    }
    
    public void close() throws IOException {
        if (os != null) {
            os.flush();
            os.close();
        }
        if (is != null) {
            is.close();
        }
    }
    
    public static boolean useCompression() {
        return ServerProperties.useCompression();
    }

}
