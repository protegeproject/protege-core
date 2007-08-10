package edu.stanford.smi.protege.server;

import java.net.URI;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;

import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.model.framestore.MergingNarrowFrameStore;
import edu.stanford.smi.protege.model.framestore.NarrowFrameStore;
import edu.stanford.smi.protege.server.framestore.RemoteServerFrameStore;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.server.framestore.ServerSessionLost;
import edu.stanford.smi.protege.server.metaproject.MetaProjectInstance;
import edu.stanford.smi.protege.server.narrowframestore.RemoteServerNarrowFrameStore;
import edu.stanford.smi.protege.server.narrowframestore.ServerNarrowFrameStore;

public class ServerProject extends UnicastRemoteObject implements RemoteServerProject {
    private URI _uri;
    private Server _server;
    private Project _project;
    private ServerFrameStore _domainKbFrameStore;
    private ServerFrameStore _projectKbFrameStore;
    private ServerNarrowFrameStore _domainKbNarrowFrameStore;
    private ServerNarrowFrameStore _systemNarrowFrameStore;
    private final Object _kbLock;

    public URI getURI(RemoteSession session) {
        return _uri;
    }

    public String getDomainKbFactoryClassName() {
        return _project.getKnowledgeBase().getKnowledgeBaseFactory().getClass().getName();
    }

    public String getProjectKbFactoryClassName() {
        return _project.getInternalProjectKnowledgeBase().getKnowledgeBaseFactory().getClass().getName();
    }

    public ServerProject(Server server, URI uri, MetaProjectInstance projectInstance, Project project) throws RemoteException {
        _server = server;
        _uri = uri;
        _project = project;
        _kbLock = _project.getKnowledgeBase();
        _domainKbFrameStore = createServerFrameStore(_project.getKnowledgeBase(), _kbLock);
        _domainKbFrameStore.setMetaProjectInstance(projectInstance);
        _projectKbFrameStore = createServerFrameStore(_project.getInternalProjectKnowledgeBase(), _kbLock);
        _domainKbNarrowFrameStore = createServerNarrowFrameStore();
        _systemNarrowFrameStore = createServerSystemNarrowFrameStore();
    }


    private static ServerFrameStore createServerFrameStore(KnowledgeBase kb, Object kbLock) throws RemoteException {
        ServerFrameStore sfs = new ServerFrameStore(kb, kbLock);
        return sfs;
    }
    
    private ServerNarrowFrameStore createServerNarrowFrameStore() throws RemoteException {
      KnowledgeBase kb = _project.getKnowledgeBase();
      MergingNarrowFrameStore merging = MergingNarrowFrameStore.get(kb);
      NarrowFrameStore nfs = merging.getActiveFrameStore();
      return new ServerNarrowFrameStore(nfs, kb, _kbLock);
    }
    
    private ServerNarrowFrameStore createServerSystemNarrowFrameStore() throws RemoteException {
      KnowledgeBase kb = _project.getKnowledgeBase();
      MergingNarrowFrameStore merging = MergingNarrowFrameStore.get(kb);
      NarrowFrameStore nfs = merging.getSystemFrameStore();
      return new ServerNarrowFrameStore(nfs, kb, _kbLock); 
    }

    public RemoteServerFrameStore getDomainKbFrameStore(RemoteSession session) {
        return _domainKbFrameStore;
    }
    

    public RemoteServerFrameStore getProjectKbFrameStore(RemoteSession session) {
        return _projectKbFrameStore;
    }
    
    public RemoteServerNarrowFrameStore getDomainKbNarrowFrameStore() {
        return _domainKbNarrowFrameStore;
    }
    
    public RemoteServerNarrowFrameStore getSystemNarrowFrameStore()  {
      return _systemNarrowFrameStore;
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