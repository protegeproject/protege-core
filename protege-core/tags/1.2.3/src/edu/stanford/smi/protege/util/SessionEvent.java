package edu.stanford.smi.protege.util;

import java.util.EventObject;

import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;



public class SessionEvent extends EventObject {
  private transient RemoteSession session;

  public SessionEvent(Object source) {
    super(source);
    session = ServerFrameStore.getCurrentSession();
  }
  
  public RemoteSession getSession() {
    return session;
  }
}
