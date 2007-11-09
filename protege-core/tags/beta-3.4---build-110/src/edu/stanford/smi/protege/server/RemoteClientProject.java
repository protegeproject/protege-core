package edu.stanford.smi.protege.server;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.KnowledgeBaseFactory;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.model.framestore.NarrowFrameStore;
import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore;
import edu.stanford.smi.protege.server.framestore.RemoteServerFrameStore;
import edu.stanford.smi.protege.server.framestore.ServerSessionLost;
import edu.stanford.smi.protege.server.narrowframestore.RemoteClientInvocationHandler;
import edu.stanford.smi.protege.server.narrowframestore.RemoteServerNarrowFrameStore;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.SystemUtilities;

public class RemoteClientProject extends Project {
    private static Logger log = Log.getLogger(RemoteClientProject.class);
  
    private RemoteServer _server;
    private RemoteServerProject _serverProject;
    private RemoteSession _session;
    private Thread shutdownHook;

    public static Project createProject(RemoteServer server, 
                                        RemoteServerProject serverProject, 
                                        RemoteSession session, boolean pollForEvents)
            throws RemoteException {
        return new RemoteClientProject(server, serverProject, session, pollForEvents);
    }

    public RemoteClientProject(RemoteServer server,
                               RemoteServerProject serverProject, 
                               RemoteSession session, 
                               boolean pollForEvents)
            throws RemoteException {
        super(null, null, new ArrayList(), false);
        _server = server;
        _serverProject = serverProject;
        _session = session;
        serverProject.getDomainKbFrameStore(session);
        KnowledgeBase domainKb = createKnowledgeBase(server,
                                                     serverProject.getDomainKbFrameStore(session), 
                                                     serverProject.getSystemNarrowFrameStore(),
                                                     serverProject.getDomainKbNarrowFrameStore(),
                                                     serverProject.getDomainKbFactoryClassName(), 
                                                     session, false);
        KnowledgeBase projectKb = createKnowledgeBase(server,
                                                      serverProject.getProjectKbFrameStore(session),
                                                      serverProject.getSystemNarrowFrameStore(),
                                                      serverProject.getDomainKbNarrowFrameStore(),
                                                      serverProject.getProjectKbFactoryClassName(), 
                                                      session, true);
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

    private static KnowledgeBase createKnowledgeBase(RemoteServer server,
                                                     RemoteServerFrameStore serverFrameStore, 
                                                     RemoteServerNarrowFrameStore systemNfs,
                                                     RemoteServerNarrowFrameStore userNfs,
                                                     String factoryClassName,
                                                     RemoteSession session, 
                                                     boolean preloadAll) {
        Class factoryClass = SystemUtilities.forName(factoryClassName, true);
        KnowledgeBaseFactory factory = (KnowledgeBaseFactory) SystemUtilities.newInstance(factoryClass);
        List errors = new ArrayList();
        DefaultKnowledgeBase kb = (DefaultKnowledgeBase) factory.createKnowledgeBase(errors);
        for (Object o : errors) {
          if (o instanceof Throwable) {
            log.log(Level.WARNING, "Exception caught", (Throwable) o);
          } else {
            log.warning("Error  found" + o);
          }
        }
        if (log.isLoggable(Level.FINE)) {
          log.fine("created kb=" + kb);
        }
        FrameStore clientFrameStore
               = new RemoteClientFrameStore(server, serverFrameStore, session, kb, preloadAll);
        RemoteClientInvocationHandler rcif
             = new RemoteClientInvocationHandler(kb, systemNfs, session);
        NarrowFrameStore systemNarrowFrameStore = rcif.getNarrowFrameStore();
        rcif = new RemoteClientInvocationHandler(kb, userNfs, session);
        NarrowFrameStore userNarrowFrameStore = rcif.getNarrowFrameStore();

        kb.setTerminalFrameStore(clientFrameStore);
        if (factory instanceof ClientInitializerKnowledgeBaseFactory) {
          ClientInitializerKnowledgeBaseFactory clientInit;
          clientInit = (ClientInitializerKnowledgeBaseFactory) factory;
          clientInit.initializeClientKnowledgeBase(clientFrameStore, 
                                                   systemNarrowFrameStore,
                                                   userNarrowFrameStore, 
                                                   kb);
        }
        kb.setGenerateEventsEnabled(false);
        kb.setCallCachingEnabled(false);

        return kb;
    }
    
    public RemoteSession getSession() {
      return _session;
    }
    
    public RemoteServer getServer() {
      return _server;
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
        } catch (ServerSessionLost ssl) {
          Log.getLogger().info("Session disconnected");
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
              try {
                attemptClose();
              } catch (Throwable t) {
                Log.getLogger().log(Level.INFO, "Exception caught", t);
              }
            }
        };
        try {
        	Runtime.getRuntime().addShutdownHook(shutdownHook);
		} catch (Exception e) {
			//this happens in applets
			Log.getLogger().log(Level.WARNING, "Unable to install shutdown hook", e);			
		}        
    }

    private void uninstallShutdownHook() {
        try {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
		} catch (Exception e) {
			//this happens in applets
			Log.getLogger().log(Level.WARNING, "Unable to remove shutdown hook", e);			
		}
    }

    public boolean isDirty() {
        // changes are committed automatically so we are never dirty.
        return false;
    }

}
