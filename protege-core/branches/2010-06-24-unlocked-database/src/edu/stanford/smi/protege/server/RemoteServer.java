package edu.stanford.smi.protege.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

import edu.stanford.smi.protege.model.KnowledgeBaseFactory;
import edu.stanford.smi.protege.server.ServerProject.ProjectStatus;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.server.util.ProjectInfo;
import edu.stanford.smi.protege.util.ServerJob;

public interface RemoteServer extends Remote {

	/*
     * Server management
     */

    void reinitialize() throws RemoteException;

    boolean createUser(String userName, String password) throws RemoteException;


    /*
     * Session management
     */
    RemoteSession openSession(String username, String userMachine, String password) throws RemoteException;

    RemoteSession cloneSession(RemoteSession session) throws RemoteException;

    void closeSession(RemoteSession session) throws RemoteException;

    Collection<RemoteSession> getCurrentSessions(String projectName, RemoteSession session) throws RemoteException;

    void killOtherUserSession(RemoteSession sessionToKill, RemoteSession session) throws RemoteException;

    void killOtherUserSession(RemoteSession sessionToKill, RemoteSession session, int finalGracePeriod) throws RemoteException;


    /*
     * Project management
     */

    Collection<String> getAvailableProjectNames(RemoteSession session) throws RemoteException;

    Collection<ProjectInfo> getAvailableProjectInfo(RemoteSession session) throws RemoteException;

    RemoteServerProject openProject(String projectName, RemoteSession session) throws RemoteException;

    RemoteServerProject openMetaProject(RemoteSession session) throws RemoteException;

    boolean saveMetaProject(RemoteSession session) throws RemoteException;

    RemoteServerProject createProject(String projectName, RemoteSession session, KnowledgeBaseFactory kbfactory, boolean saveToMetaProject) throws RemoteException;

    ProjectStatus getProjectStatus(String projectName) throws RemoteException;

    void setProjectStatus(String projectName, ProjectStatus status, RemoteSession session) throws RemoteException;

    void notifyProject(String projectName, String message, RemoteSession session) throws RemoteException;

    void shutdown() throws RemoteException;

    void shutdown(String projectName, RemoteSession session) throws RemoteException;

    /**
	 * Shuts down the remote project with the name projectName and sends shutdown notifications
	 * at time periods (in seconds) from the warningTimesInSeconds argument.
	 * A {@link SecurityException} is thrown if the user of the session does not
	 * have the right to shutdown the project.
	 * @param session - the session that tries to shutdown the project
	 * @param projectName - the remote project name
	 * @param warningTimesInSeconds - a list of {@link Integer} with the notify periods of time in seconds
	 * @param finalGracePeriodInSeconds - the time the server will wait until shutting down the project after the last notification message
	 */
    void shutdownProject(RemoteSession session, String projectName,
				Integer[] warningTimesInSeconds, int finalGracePeriodInSeconds) throws RemoteException;

    /**
     * Shuts down the remote project with the name projectName and sends shutdown notifications
	 * at certain time periods: it starts with the warningTimeInSeconds argument and divides it by two.
	 * A {@link SecurityException} is thrown if the user of the session does not
	 * have the right to shutdown the project.
	 * @param session - the session that tries to shutdown the project
	 * @param projectName - the remote project name
     * @param warningTimeInSeconds - the time of the first notification in seconds
     */
    void shutdownProject(RemoteSession session, String projectName, float warningTimeInSeconds) throws RemoteException;

    boolean cancelShutdownProject(RemoteSession session, String projectName) throws RemoteException;


    /*
     * Policy
     */

    boolean allowsCreateUsers() throws RemoteException;

    boolean isOperationAllowed(RemoteSession session, Operation op, String projectName) throws RemoteException;

    boolean isGroupOperationAllowed(RemoteSession session, Operation op, String groupName) throws RemoteException;

    boolean isServerOperationAllowed(RemoteSession session, Operation op) throws RemoteException;

    boolean isServerOperationAllowed(RemoteSession session, Operation op, String serverName) throws RemoteException;

    boolean hasValidCredentials(String userName, String password) throws RemoteException;

    Collection<Operation> getAllowedOperations(RemoteSession session, String projectName, String userName) throws RemoteException;

    /*
     * Misc
     */
    Object executeServerJob(ServerJob job, RemoteSession session) throws RemoteException;
}