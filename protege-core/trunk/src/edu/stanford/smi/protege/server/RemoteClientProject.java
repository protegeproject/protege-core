package edu.stanford.smi.protege.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore;
import edu.stanford.smi.protege.server.framestore.RemoteServerFrameStore;
import edu.stanford.smi.protege.server.framestore.ServerSessionLost;
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
                                                     serverProject.getDomainKbFactoryClassName(), 
                                                     session, false);
        KnowledgeBase projectKb = copyKb(serverProject);
        setKnowledgeBases(domainKb, projectKb);
        if (pollForEvents) {
            domainKb.setPollForEvents(true);
        }
        installShutdownHook();
    }

    private static KnowledgeBase copyKb(RemoteServerProject serverProject) throws RemoteException {
        RemoteSimpleStream rss = null;
        FileOutputStream tempFileStream = null;
        try {
            long startTime = System.currentTimeMillis();
            File tmpFile = File.createTempFile("RemoteProject", ".pprj");
            tempFileStream = new FileOutputStream(tmpFile);
            rss = serverProject.uploadProjectKb();
            byte[] input;
            do {
                input = rss.read();
                if (input == null) {
                    break;
                }
                tempFileStream.write(input);
            } while (true);
            tempFileStream.flush();
            tempFileStream.close();
            tempFileStream = null;
            log.info("Uploaded pprj file in " + (System.currentTimeMillis() - startTime) + " milliseconds.");
            
            startTime = System.currentTimeMillis();
            Collection errors = new ArrayList();
            KnowledgeBase localKb = loadProjectKB(tmpFile.toURI(), null, errors);
            log.info("Loaded pprj file into knowledge base in " + (System.currentTimeMillis() - startTime) + " milliseconds.");
            Log.handleErrors(log, Level.WARNING, errors);
            return localKb;
        }
        catch (IOException ioe) {
            if (ioe instanceof RemoteException) {
                throw (RemoteException) ioe;
            }
            else {
                throw new RemoteException("Exception caught uploading project knowledge base from the server", ioe);
            }
        }
        finally {
            if (rss != null) {
                rss.close();
            }
            if (tempFileStream != null) {
                try {
                    tempFileStream.close();
                }
                catch (IOException ioe) {
                    throw new RemoteException("Exception caught closing temp file", ioe);
                }
            }
        }
    }
    
     

    private static KnowledgeBase createKnowledgeBase(RemoteServer server,
                                                     RemoteServerFrameStore serverFrameStore, 
                                                     String factoryClassName,
                                                     RemoteSession session, 
                                                     boolean preloadAll) {
        Class factoryClass = SystemUtilities.forName(factoryClassName, true);
        KnowledgeBaseFactory factory = (KnowledgeBaseFactory) SystemUtilities.newInstance(factoryClass);
        List errors = new ArrayList();
        DefaultKnowledgeBase kb = (DefaultKnowledgeBase) factory.createKnowledgeBase(errors);
        Log.handleErrors(log, Level.WARNING, errors);

        if (log.isLoggable(Level.FINE)) {
          log.fine("created kb=" + kb);
        }
        
        kb.setGenerateEventsEnabled(false);
        kb.setCallCachingEnabled(false);
        
        FrameStore clientFrameStore
               = new RemoteClientFrameStore(server, serverFrameStore, session, kb, preloadAll);
        kb.setTerminalFrameStore(clientFrameStore);
        return kb;
    }
    
    public RemoteSession getSession() {
      return _session;
    }
    
    public RemoteServer getServer() {
      return _server;
    }

    @Override
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

    @Override
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

    @Override
    public String getLocalUser() {
        return _session.getUserName();
    }

    @Override
    public void dispose() {
    	if (log.isLoggable(Level.FINE)) {
    		Log.getLogger().fine("Remote project dispose " + this);
    	}
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

    @Override
    public KnowledgeBaseFactory getKnowledgeBaseFactory() {
        return null;
    }

    @Override
    public boolean isMultiUserClient() {
        return true;
    }

    private void installShutdownHook() {
        shutdownHook = new Thread("Remote Project ShutdownHook") {
            @Override
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

    @Override
    public boolean isDirty() {
        // changes are committed automatically so we are never dirty.
        return false;
    }

}
