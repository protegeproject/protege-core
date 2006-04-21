package edu.stanford.smi.protege.server.framestore.background;

import java.util.EventObject;
import java.util.Set;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Localizable;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.util.LocalizeUtils;

public class ClientNotification extends EventObject implements Localizable {
  private Frame frame;
  private transient Set<RemoteSession> clients;
  
  public ClientNotification(Frame frame) {
    super(frame);
    this.frame = frame;
  }
  
  public Frame getFrame() {
    return frame;
  }

  protected Set<RemoteSession> getClients() {
    return clients;
  }

  protected void setClients(Set<RemoteSession> clients) {
    this.clients = clients;
  }
  
  public void addClient(RemoteSession client) {
    clients.add(client);
  }

  public void localize(KnowledgeBase kb) {
    LocalizeUtils.localize(frame, kb);
  }

}
