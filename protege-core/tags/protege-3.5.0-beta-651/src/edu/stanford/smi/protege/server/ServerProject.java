package edu.stanford.smi.protege.server;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Localizable;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.framestore.RemoteServerFrameStore;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.server.framestore.ServerSessionLost;
import edu.stanford.smi.protege.server.metaproject.ProjectInstance;
import edu.stanford.smi.protege.server.socket.RmiSocketFactory;
import edu.stanford.smi.protege.server.socket.SSLFactory;

public class ServerProject extends UnicastRemoteObject implements RemoteServerProject {
    private static final long serialVersionUID = 7320382402535936929L;
    
    private URI _uri;
    private Server _server;
    private Project _project;
    private ProjectInstance metaProjectInstance;
    private ServerFrameStore _domainKbFrameStore;
    private ServerFrameStore _projectKbFrameStore;
    
    public enum ProjectStatus implements Localizable {
        READY, SHUTTING_DOWN, CLOSED_FOR_MAINTENANCE;
        
        public void localize(KnowledgeBase kb) {

        }
    }



    public ServerProject(Server server, URI uri, ProjectInstance projectInstance, Project project) throws RemoteException {
        super(SSLFactory.getServerPort(SSLFactory.Context.ALWAYS),
              new RmiSocketFactory(SSLFactory.Context.ALWAYS),
              new RmiSocketFactory(SSLFactory.Context.ALWAYS));
        _server = server;
        _uri = uri;
        _project = project;
        metaProjectInstance = projectInstance;
        _domainKbFrameStore = createServerFrameStore(_project.getKnowledgeBase());
        _domainKbFrameStore.setMetaProjectInstance(projectInstance);
        _projectKbFrameStore = createServerFrameStore(_project.getInternalProjectKnowledgeBase());
    }
    
    private static ServerFrameStore createServerFrameStore(KnowledgeBase kb) throws RemoteException {
        ServerFrameStore sfs = new ServerFrameStore(kb);
        return sfs;
    }

    public ProjectInstance getMetaProjectInstance() {
        return metaProjectInstance;
    }
    
    public URI getURI(RemoteSession session) {
        return _uri;
    }

    public String getDomainKbFactoryClassName() {
        return _project.getKnowledgeBase().getKnowledgeBaseFactory().getClass().getName();
    }


    public RemoteServerFrameStore getDomainKbFrameStore(RemoteSession session) {
        return _domainKbFrameStore;
    }
    
    public RemoteSimpleStream uploadProjectKb() throws RemoteException {
        URI uri = _project.getProjectURI();
        if (uri != null) {
            try {
                return new SimpleStream(uri.toURL());
            }
            catch (MalformedURLException malformed) {
                throw new RemoteException("Unexpected exception: project url is malformed " + uri, malformed);
            }
        }
        else {
            return null;
        }
    }

    public void register(RemoteSession session) throws ServerSessionLost {
        _domainKbFrameStore.register(session);
        _projectKbFrameStore.register(session);
    }
    
    public void deregister(RemoteSession session) throws ServerSessionLost {
      _domainKbFrameStore.deregister(session);
      _projectKbFrameStore.deregister(session);
  }

    public void close(RemoteSession session) throws ServerSessionLost{
        _server.disconnectFromProject(this, session);
    }

    public Collection getCurrentSessions(RemoteSession unusedSession) {
        return _server.getCurrentSessions(this);
    }

    public boolean isDirty() {
        return _domainKbFrameStore.isDirty();
    }

    public void setClean() {
        _domainKbFrameStore.markClean();
        _projectKbFrameStore.markClean();
    }
    
    public void setFrameCalculatorDisabled(boolean disabled) {
      _domainKbFrameStore.setFrameCalculatorDisabled(disabled);
    }
}