package edu.stanford.smi.protege.server.job;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;

import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.server.ServerProject;
import edu.stanford.smi.protege.server.framestore.RemoteServerFrameStore;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.server.framestore.ServerSessionLost;
import edu.stanford.smi.protege.server.metaproject.MetaProjectConstants;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.ProtegeJob;

public class KillUserSessionJob extends ProtegeJob {

    private RemoteSession prototypeSessionToKill;

    public KillUserSessionJob(RemoteSession session, KnowledgeBase kb) {
        super(kb);
        this.prototypeSessionToKill = session;
    }


    @Override
	public Object run() throws ProtegeException {
        if (!isAllowed()) {
            return Boolean.FALSE;
        }
        boolean allSucceeded = true;
        Server server = Server.getInstance();
        for (RemoteSession sessionToKill : new ArrayList<RemoteSession>(server.getCurrentSessions())) {
            if (prototypeSessionToKill.getUserName().equals(sessionToKill.getUserName()) &&
                    prototypeSessionToKill.getSessionGroup() == sessionToKill.getSessionGroup()) {
                allSucceeded =  allSucceeded && killSession(server, sessionToKill);
            }
        }
        return Boolean.valueOf(allSucceeded);
    }

    private boolean killSession(Server server, RemoteSession session) {
        Collection<ServerProject> projects = server.getCurrentProjects(session);
        if (projects == null) {
            return true;
        }
        boolean allSucceeded  = true;
        projects = new ArrayList<ServerProject>(projects);
        for (ServerProject project : projects) {
            try {
                project.close(session);
            } catch (ServerSessionLost e) {
                Log.getLogger().log(Level.WARNING, "Could not close session", e);
                allSucceeded = false;
            }
        }
        return allSucceeded;
    }

    private boolean isAllowed() {
        Project project = getKnowledgeBase().getProject();
        ServerProject serverProject = Server.getInstance().getServerProject(project);
        RemoteSession mySession = ServerFrameStore.getCurrentSession();
        RemoteServerFrameStore fs = serverProject.getDomainKbFrameStore(mySession);

        try {
            return prototypeSessionToKill.getUserName().equals(mySession.getUserName()) ||
                fs.getAllowedOperations(mySession).contains(MetaProjectConstants.OPERATION_KILL_OTHER_USER_SESSION);
        } catch (RemoteException e) {
            Log.getLogger().log(Level.WARNING, "Caught Exception trying to check permissions", e);
            return false;
        }
    }
}
