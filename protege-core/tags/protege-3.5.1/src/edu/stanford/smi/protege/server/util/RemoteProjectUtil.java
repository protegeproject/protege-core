package edu.stanford.smi.protege.server.util;

import java.awt.BorderLayout;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.framestore.FrameStoreManager;
import edu.stanford.smi.protege.server.RemoteClientProject;
import edu.stanford.smi.protege.server.RemoteServer;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.ui.ProjectView;
import edu.stanford.smi.protege.ui.StatusBar;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.StringUtilities;

/**
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class RemoteProjectUtil {
	private static transient Logger log = Log.getLogger(RemoteProjectUtil.class);

    private static Thread statusBarUpdateThread;    
    //ESCA-JAVA0077
    private static final int DELAY_MSEC = 2000;

    public static void configure(ProjectView view) {
        StatusBar statusBar = new StatusBar();
        view.add(statusBar, BorderLayout.SOUTH);
        createUpdateThread((RemoteClientProject) view.getProject(), statusBar);
    }

    public static void dispose(ProjectView view) {
        statusBarUpdateThread = null;
    }

    private static void createUpdateThread(final RemoteClientProject project, final StatusBar bar) {
        statusBarUpdateThread = new Thread("Status Bar Updater") {
            @Override
			public void run() {
              try {
                while (statusBarUpdateThread == this) {
                    try {
                        sleep(DELAY_MSEC);
                        updateStatus(project, bar);
                    } catch (InterruptedException e) {
                      log.log(Level.INFO, "Exception caught", e);
                    }
                }
              } catch (Throwable t) {
                log.log(Level.INFO, "Exception caught",t);
              }
            }
        };
        statusBarUpdateThread.setDaemon(true);
        statusBarUpdateThread.start();
    }

    private static void updateStatus(RemoteClientProject project, StatusBar bar) {
        Collection users = new ArrayList(project.getCurrentUsers());
        users.remove(project.getLocalUser());
        String userText = StringUtilities.commaSeparatedList(users);
        String text;
        if (userText.length() == 0) {
            text = "No other users";
        } else {
            text = "Other users: " + userText;
        }
        bar.setText(text);
    }

    public static RemoteServer getRemoteServer(KnowledgeBase kb) {
    	if (!kb.getProject().isMultiUserClient()) { return null; }
		FrameStoreManager framestore_manager = ((DefaultKnowledgeBase) kb).getFrameStoreManager();
		RemoteClientFrameStore remote_frame_store = framestore_manager.getFrameStoreFromClass(RemoteClientFrameStore.class);
		RemoteServer server = remote_frame_store.getRemoteServer();
		return server;
    }
    
    /**
     * Checks whether the operation is allowed for a session on a remote project. 
     * If the remote call fails, it will return true (operation allowed)
     * @param server - the remote server
     * @param session - the session
     * @param projectName - the remote project name
     * @param op - the operation to check
     * @return - true, if operation allowed (or call to server failed); false, otherwise
     */
    public static boolean isOperationAllowed(RemoteServer server, RemoteSession session, String projectName, Operation op) {
    	try {
			return server.isOperationAllowed(session, op, projectName);			
		} catch (RemoteException e) {
			Log.getLogger().log(Level.WARNING, "Could not figure out from server whether session: "
					+ session + " is allowed to: " + op + " on remote project: " + projectName + 
					". Allowing the operation");
			return true;
		}
    }
    
    public static boolean isServerOperationAllowed(RemoteServer server, RemoteSession session, Operation op) {
    	try {
			return server.isServerOperationAllowed(session, op);			
		} catch (RemoteException e) {
			Log.getLogger().log(Level.WARNING, "Could not figure out from server whether session: "
					+ session + " is allowed to: " + op);
			return true;
		}
    }

 
}
