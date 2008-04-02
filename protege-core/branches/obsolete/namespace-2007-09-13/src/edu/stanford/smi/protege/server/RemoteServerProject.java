package edu.stanford.smi.protege.server;

import java.net.URI;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

import edu.stanford.smi.protege.server.framestore.RemoteServerFrameStore;
import edu.stanford.smi.protege.server.narrowframestore.RemoteServerNarrowFrameStore;

public interface RemoteServerProject extends Remote {
    URI getURI(RemoteSession session) throws RemoteException;

    RemoteServerFrameStore getDomainKbFrameStore(RemoteSession session) throws RemoteException;
    
    String getDomainKbFactoryClassName() throws RemoteException;

    RemoteServerFrameStore getProjectKbFrameStore(RemoteSession session) throws RemoteException;
    
    String getProjectKbFactoryClassName() throws RemoteException;
    
    RemoteServerNarrowFrameStore getDomainKbNarrowFrameStore() throws RemoteException;

    Collection getCurrentSessions(RemoteSession session) throws RemoteException;

    void close(RemoteSession session) throws RemoteException;
    
    void register(RemoteSession session) throws RemoteException;
}