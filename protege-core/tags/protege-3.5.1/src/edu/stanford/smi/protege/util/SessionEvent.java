package edu.stanford.smi.protege.util;

import java.util.EventObject;

import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;



public class SessionEvent extends EventObject {
    private static final long serialVersionUID = -6704290705069672257L;
    private RemoteSession session;
    private boolean isHiddenByTransaction = false;

    public SessionEvent(Object source) {
        super(source);
        session = ServerFrameStore.getCurrentSession();
    }

    public RemoteSession getSession() {
        return session;
    }

    public boolean isHiddenByTransaction() {
        return isHiddenByTransaction;
    }

    public void setHiddenByTransaction(boolean isHiddenByTransaction) {
        this.isHiddenByTransaction = isHiddenByTransaction;
    }
}
