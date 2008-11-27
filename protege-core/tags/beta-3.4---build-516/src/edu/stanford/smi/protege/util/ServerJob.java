package edu.stanford.smi.protege.util;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.server.RemoteServer;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore;

public abstract class ServerJob extends RemoteJob implements Serializable {
    private static final long serialVersionUID = -7633748858975967471L;
    private static transient Logger log = Log.getLogger(ServerJob.class);
    
    private RemoteServer server;
    private RemoteSession session;

    public ServerJob(RemoteServer server, RemoteSession session) {
        this.server = server;
        this.session = session;
    }
    
    public ServerJob(KnowledgeBase kb) {
        DefaultKnowledgeBase dkb = (DefaultKnowledgeBase) kb;
        FrameStore terminalFrameStore = dkb.getTerminalFrameStore();
        if (terminalFrameStore instanceof RemoteClientFrameStore) {
          RemoteClientFrameStore clientFrameStore = (RemoteClientFrameStore) terminalFrameStore;
          
          server = clientFrameStore.getRemoteServer();
          session = clientFrameStore.getSession();
        }
    }
    
    public void fixLoader() {
  	  ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
  	  ClassLoader correctLoader = getClass().getClassLoader();
  	  if (currentLoader != correctLoader) {
  		  if (log.isLoggable(Level.FINEST)) {
  			  Log.getLogger().finest("Changing loader from " + currentLoader + " to " + correctLoader);
  		  }
  		  Thread.currentThread().setContextClassLoader(correctLoader);
  	  }
    }
    
    public RemoteSession getSession() {
        return session;
    }
    
    public Object execute() throws ProtegeException {
        try {
            return server.executeServerJob(this, session);
        }
        catch (RemoteException re) {
            throw new ProtegeException(re);
        }
    }
    
}
