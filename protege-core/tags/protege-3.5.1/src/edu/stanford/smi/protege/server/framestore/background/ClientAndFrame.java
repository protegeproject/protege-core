package edu.stanford.smi.protege.server.framestore.background;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.server.RemoteSession;

public class ClientAndFrame {
  private RemoteSession session;
  private Frame frame;
  
  public ClientAndFrame(RemoteSession session, Frame frame) {
    this.session = session;
    this.frame = frame;
  }

  public Frame getFrame() {
    return frame;
  }

  public void setFrame(Frame frame) {
    this.frame = frame;
  }

  public RemoteSession getSession() {
    return session;
  }

  public void setSession(RemoteSession session) {
    this.session = session;
  }
  
  public int hashCode() {
    return (session == null ? 10 : session.hashCode()) + 3 * frame.hashCode();
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof ClientAndFrame)) {
      return false;
    }
    ClientAndFrame other = (ClientAndFrame) o;
    return frame.equals(other.frame) && session.equals(other.session);
  } 
}
