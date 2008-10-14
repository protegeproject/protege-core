package edu.stanford.smi.protege.server;

import java.net.URI;
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

    public String getProjectKbFactoryClassName() {
        return _project.getInternalProjectKnowledgeBase().getKnowledgeBaseFactory().getClass().getName();
    }


    public RemoteServerFrameStore getDomainKbFrameStore(RemoteSession session) {
        return _domainKbFrameStore;
    }
    

    public RemoteServerFrameStore getProjectKbFrameStore(RemoteSession session) {
        return _projectKbFrameStore;
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