package edu.stanford.smi.protege.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface is used to transfer the project (.pprj) file from the server
 * to the client.  It is very primitive but for this purpose nothing more is
 * really needed.  The idea here is that the server gets to choose the buffer size.
 * 
 * @author tredmond
 *
 */
public interface RemoteSimpleStream extends Remote {
    /**
     * Read some buffer size count of bytes until it reaches the end of the file 
     * at which point it returns null.
     * @return
     */
    byte[] read() throws RemoteException;
    
    void close() throws RemoteException;
}
