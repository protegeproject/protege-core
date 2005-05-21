package edu.stanford.smi.protege.server;

import java.net.*;
import java.rmi.*;
import java.util.*;

public interface RemoteServerProject extends Remote {
    URI getURI(RemoteSession session) throws RemoteException;

    RemoteServerFrameStore getDomainKbFrameStore(RemoteSession session) throws RemoteException;
    String getDomainKbFactoryClassName() throws RemoteException;

    RemoteServerFrameStore getProjectKbFrameStore(RemoteSession session) throws RemoteException;
    String getProjectKbFactoryClassName() throws RemoteException;

    Collection getCurrentSessions(RemoteSession session) throws RemoteException;

    void close(RemoteSession session) throws RemoteException;
    void register(RemoteSession session) throws RemoteException;
}