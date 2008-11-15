package edu.stanford.smi.protege.server.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import edu.stanford.smi.protege.server.ServerProperties;


public class SocketWithAspects extends Socket {
    private SocketAspect myAspects;

    public SocketWithAspects() {
        myAspects = new SocketAspect() {

            public SocketAspect getDelegate() {
                return null;
            }

            public InputStream getInputStream() throws IOException {
                return defaultGetInputStream();
            }

            public OutputStream getOutputStream()  throws IOException {
                return defaultGetOutputStream();
            }
            
            public void close() throws IOException {
                defaultClose();
            }
            
        };
        if (ServerProperties.useCompression()) {
            myAspects = new CompressionAspect(myAspects);
        }
    }
    
    public InputStream getInputStream() throws IOException {
        return myAspects.getInputStream();
    }
    
    private InputStream defaultGetInputStream() throws IOException {
        return super.getInputStream();
    }
    
    public OutputStream getOutputStream() throws IOException {
        return myAspects.getOutputStream();
    }
    
    private OutputStream defaultGetOutputStream() throws IOException {
        return super.getOutputStream();
    }
    
    public void close() throws IOException {
        myAspects.close();
    }
    
    private void defaultClose() throws IOException {
        super.close();
    }
}
