package edu.stanford.smi.protege.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

import edu.stanford.smi.protege.model.KnowledgeBaseFactory;

public interface RemoteServer extends Remote {
    
    void reinitialize() throws RemoteException;
    
    RemoteSession openSession(String username, String userMachine, String password) throws RemoteException;
    
    RemoteSession cloneSession(RemoteSession session) throws RemoteException;
    
    void closeSession(RemoteSession session) throws RemoteException;

    Collection getAvailableProjectNames(RemoteSession session) throws RemoteException;

    Collection getCurrentSessions(String projectName, RemoteSession session) throws RemoteException;

    RemoteServerProject openProject(String projectName, RemoteSession session) throws RemoteException;
    
    RemoteServerProject createProject(String projectName, RemoteSession session, KnowledgeBaseFactory kbfactory, boolean saveToMetaProject) throws RemoteException;

    //TT: To be added later
    //boolean createUser(String userName, String password) throws RemoteException;

    void shutdown() throws RemoteException;
}