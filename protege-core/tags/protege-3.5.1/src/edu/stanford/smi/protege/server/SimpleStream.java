package edu.stanford.smi.protege.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import edu.stanford.smi.protege.server.socket.RmiSocketFactory;
import edu.stanford.smi.protege.server.socket.SSLFactory;
import edu.stanford.smi.protege.server.socket.SSLFactory.Context;

public class SimpleStream implements RemoteSimpleStream {
    private final int bufferSize = 1024 * 1024;
    private InputStream is;
    private URL url;
    
    public SimpleStream(URL url) throws RemoteException {
        this.url = url;
        try {
            is = url.openStream();
        }
        catch (IOException ioe) {
            throw new RemoteException("Could not open remote file " + url, ioe);
        }
        UnicastRemoteObject.exportObject(this, 
                                         SSLFactory.getServerPort(Context.ALWAYS),
                                         new RmiSocketFactory(SSLFactory.Context.ALWAYS),
                                         new RmiSocketFactory(SSLFactory.Context.ALWAYS));
    }
    
    public byte[] read() throws RemoteException {
        try {
            byte[] buffer = new byte[bufferSize];
            int len = is.read(buffer);
            if (len > 0) {
                byte[] output = new byte[len];
                System.arraycopy(buffer, 0, output, 0, len);
                return output;
            }
            else {
                return null;
            }
        }
        catch (IOException ioe) {
            throw new RemoteException("IO Exception reading remote file " + url, ioe);
        }
    }
    
    public void close() throws RemoteException {
        try {
            is.close();
            is = null;
        }
        catch (IOException ioe) {
            throw new RemoteException("IO Exception closing remote file for " + url, ioe);
        }
    }

}
