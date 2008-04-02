package edu.stanford.smi.protege.server;

import java.net.*;
import java.rmi.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;
import edu.stanford.smi.protege.util.*;

public class RemoteClientProject extends Project {
    private RemoteServerProject _serverProject;
    private RemoteSession _session;
    private Thread shutdownHook;

    public static Project createProject(RemoteServerProject serverProject, RemoteSession session, boolean pollForEvents)
            throws RemoteException {
        return new RemoteClientProject(serverProject, session, pollForEvents);
    }

    public RemoteClientProject(RemoteServerProject serverProject, RemoteSession session, boolean pollForEvents)
            throws RemoteException {
        super(null, null, new ArrayList(), false);
        _serverProject = serverProject;
        _session = session;
        KnowledgeBase domainKb = createKnowledgeBase(serverProject.getDomainKbFrameStore(session), serverProject
                .getDomainKbFactoryClassName(), session, false);
        KnowledgeBase projectKb = createKnowledgeBase(serverProject.getProjectKbFrameStore(session), serverProject
                .getProjectKbFactoryClassName(), session, true);
        projectKb = copyKb(projectKb);
        setKnowledgeBases(domainKb, projectKb);
        if (pollForEvents) {
            domainKb.setPollForEvents(true);
        }
        installShutdownHook();
    }

    private static KnowledgeBase copyKb(KnowledgeBase remoteKb) {
        Collection errors = new ArrayList();
        KnowledgeBase localKb = loadProjectKB(null, null, errors);
        localKb.deleteInstance(getProjectInstance(localKb));
        Instance projectInstance = getProjectInstance(remoteKb);
        projectInstance.deepCopy(localKb, null);
        return localKb;
    }

    private static KnowledgeBase createKnowledgeBase(RemoteServerFrameStore serverFrameStore, String factoryClassName,
            RemoteSession session, boolean preloadAll) {
        Class factoryClass = SystemUtilities.forName(factoryClassName, true);
        KnowledgeBaseFactory factory = (KnowledgeBaseFactory) SystemUtilities.newInstance(factoryClass);
        DefaultKnowledgeBase kb = (DefaultKnowledgeBase) factory.createKnowledgeBase(new ArrayList());
        // Log.trace("created kb=" + kb, RemoteClientProject.class, "createKnowledgeBase");
        FrameStore clientFrameStore = new RemoteClientFrameStore(serverFrameStore, session, kb, preloadAll);
        kb.setTerminalFrameStore(clientFrameStore);
        kb.setGenerateEventsEnabled(false);
        kb.setCallCachingEnabled(true);
        return kb;
    }

    public URI getProjectURI() {
        URI uri = null;
        try {
            uri = _serverProject.getURI(_session);
        } catch (Exception e) {
            Log.getLogger().severe(Log.toString(e));
        }
        return uri;
    }

    public URL getProjectURL() {
        URL url = null;
        try {
            url = new URL(getProjectURI().toString());
        } catch (MalformedURLException e) {
            Log.getLogger().severe(Log.toString(e));
        }
        return url;
    }

    public Collection getCurrentUsers() {
        Collection users = new ArrayList();
        try {
            Collection sessions = _serverProject.getCurrentSessions(_session);
            Iterator i = sessions.iterator();
            while (i.hasNext()) {
                Session session = (Session) i.next();
                users.add(session.getUserName());
            }
        } catch (RemoteException e) {
            Log.getLogger().severe(Log.toString(e));
        }
        return users;
    }

    public String getLocalUser() {
        return _session.getUserName();
    }

    public void dispose() {
        Log.getLogger().info("remote project dispose");
        super.dispose();
        attemptClose();
        uninstallShutdownHook();
    }

    private void attemptClose() {
        try {
            _serverProject.close(_session);
        } catch (java.rmi.RemoteException e) {
            Log.getLogger().warning(e.toString());
        }
    }

    public KnowledgeBaseFactory getKnowledgeBaseFactory() {
        return null;
    }

    public boolean isMultiUserClient() {
        return true;
    }

    private void installShutdownHook() {
        shutdownHook = new Thread("Remote Project ShutdownHook") {
            public void run() {
                attemptClose();
            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    private void uninstallShutdownHook() {
        Runtime.getRuntime().removeShutdownHook(shutdownHook);
    }

    public boolean isDirty() {
        // changes are committed automatically so we are never dirty.
        return false;
    }

}
