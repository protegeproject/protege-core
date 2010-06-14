package edu.stanford.smi.protege.server.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface SocketAspect {
    
    SocketAspect getDelegate();
    
    InputStream getInputStream() throws IOException;
    
    OutputStream getOutputStream() throws IOException;
    
    void close() throws IOException;

}
