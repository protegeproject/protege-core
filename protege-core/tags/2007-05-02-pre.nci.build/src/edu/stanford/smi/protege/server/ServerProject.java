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
import edu.stanford.smi.protege.server.narrowframestore.RemoteServerNarrowFrameStore;
import edu.stanford.smi.protege.server.narrowframestore.ServerNarrowFrameStore;

public class ServerProject extends UnicastRemoteObject implements RemoteServerProject {
    private URI _uri;
    private Server _server;
    private Project _project;
    private ServerFrameStore _domainKbFrameStore;
    private ServerFrameStore _projectKbFrameStore;
    private ServerNarrowFrameStore _domainKbNarrowFrameStore;

    public URI getURI(RemoteSession session) {
        return _uri;
    }

    public String getDomainKbFactoryClassName() {
        return _project.getKnowledgeBase().getKnowledgeBaseFactory().getClass().getName();
    }

    public String getProjectKbFactoryClassName() {
        return _project.getInternalProjectKnowledgeBase().getKnowledgeBaseFactory().getClass().getName();
    }

    public ServerProject(Server server, URI uri, Project project) throws RemoteException {
        _server = server;
        _uri = uri;
        _project = project;
        _domainKbFrameStore = createServerFrameStore(_project.getKnowledgeBase());
        _projectKbFrameStore = createServerFrameStore(_project.getInternalProjectKnowledgeBase());
        _domainKbNarrowFrameStore = createServerNarrowFrameStore();
    }

    private static ServerFrameStore createServerFrameStore(KnowledgeBase kb) throws RemoteException {
        FrameStore fs = ((DefaultKnowledgeBase) kb).getHeadFrameStore();
        ServerFrameStore sfs = new ServerFrameStore(fs, kb);

        return sfs;
    }
    
    private ServerNarrowFrameStore createServerNarrowFrameStore() throws RemoteException {
      KnowledgeBase kb = _project.getKnowledgeBase();
      NarrowFrameStore nfs = MergingNarrowFrameStore.get(kb);
      return new ServerNarrowFrameStore(nfs, kb);
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

    public void register(RemoteSession session) {
        _domainKbFrameStore.register(session);
        _projectKbFrameStore.register(session);
    }

    public void close(RemoteSession session) {
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
}