package edu.stanford.smi.protege.server;

import java.rmi.*;
import java.util.*;

public interface RemoteServer extends Remote {
    
    void reinitialize() throws RemoteException;
    
    RemoteSession openSession(String username, String userMachine, String password) throws RemoteException;
    
    RemoteSession cloneSession(RemoteSession session) throws RemoteException;
    
    void closeSession(RemoteSession session) throws RemoteException;

    Collection getAvailableProjectNames(RemoteSession session) throws RemoteException;

    Collection getCurrentSessions(String projectName, RemoteSession session) throws RemoteException;

    RemoteServerProject openProject(String projectName, RemoteSession session) throws RemoteException;

    void shutdown() throws RemoteException;
}