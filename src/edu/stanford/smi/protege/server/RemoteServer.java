package edu.stanford.smi.protege.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

import edu.stanford.smi.protege.model.KnowledgeBaseFactory;
import edu.stanford.smi.protege.server.ServerProject.ProjectStatus;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.util.ServerJob;

public interface RemoteServer extends Remote {
    
    void reinitialize() throws RemoteException;
    
    /*
     * Session Management
     */
    RemoteSession openSession(String username, String userMachine, String password) throws RemoteException;
    
    RemoteSession cloneSession(RemoteSession session) throws RemoteException;
    
    void closeSession(RemoteSession session) throws RemoteException;
    
    Collection<RemoteSession> getCurrentSessions(String projectName, RemoteSession session) throws RemoteException;

    /*
     * Project Access
     */
    
    Collection<String> getAvailableProjectNames(RemoteSession session) throws RemoteException;
    
    RemoteServerProject openProject(String projectName, RemoteSession session) throws RemoteException;
    
    /*
     * Server admin
     */
    
    RemoteServerProject createProject(String projectName, RemoteSession session, KnowledgeBaseFactory kbfactory, boolean saveToMetaProject) throws RemoteException;
    
    void setProjectStatus(String projectName, ProjectStatus status, RemoteSession session) throws RemoteException;
    
    boolean createUser(String userName, String password) throws RemoteException;

    void shutdown() throws RemoteException;
    
    void shutdown(String projectName, RemoteSession session) throws RemoteException;
    
    /*
     * Policy
     */
    
    boolean allowsCreateUsers() throws RemoteException;
    
    boolean isOperationAllowed(RemoteSession session, Operation op, String projectName) throws RemoteException;
    
    /* 
     * Misc
     */
    Object executeServerJob(ServerJob job, RemoteSession session) throws RemoteException;
}