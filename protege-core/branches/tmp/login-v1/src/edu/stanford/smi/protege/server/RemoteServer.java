package edu.stanford.smi.protege.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

import edu.stanford.smi.protege.server.auth.LoginToken;

public interface RemoteServer extends Remote {
    void reinitialize() throws RemoteException;
    
    LoginToken openSession(String username, String userMachine, String password) throws RemoteException;

    void closeSession(RemoteSession session) throws RemoteException;

    Collection getAvailableProjectNames(LoginToken session) throws RemoteException;

    Collection getCurrentSessions(String projectName, LoginToken session) throws RemoteException;

    RemoteServerProject openProject(String projectName, LoginToken token) throws RemoteException;

    void shutdown() throws RemoteException;
}