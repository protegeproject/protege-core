package edu.stanford.smi.protege.server;

import java.net.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.*;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.framestore.*;

public class ServerProject extends UnicastRemoteObject implements RemoteServerProject {
    private URI _uri;
    private Server _server;
    private Project _project;
    private ServerFrameStore _domainKbFrameStore;
    private ServerFrameStore _projectKbFrameStore;

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
    }

    private static ServerFrameStore createServerFrameStore(KnowledgeBase kb) throws RemoteException {
        FrameStore fs = ((DefaultKnowledgeBase) kb).getHeadFrameStore();
        ServerFrameStore sfs = new ServerFrameStore(fs, kb);

        return sfs;
    }

    public RemoteServerFrameStore getDomainKbFrameStore(RemoteSession session) {
        return _domainKbFrameStore;
    }

    public RemoteServerFrameStore getProjectKbFrameStore(RemoteSession session) {
        return _projectKbFrameStore;
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