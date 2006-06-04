package edu.stanford.smi.protege.server.update;

import java.io.Serializable;
import java.util.Set;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Localizable;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.util.LocalizeUtils;

public class ValueUpdate implements Localizable, Serializable {
  private Frame frame;
  private transient Set<RemoteSession> clients;
  private boolean transactionScope = false;
  
  public ValueUpdate(Frame frame) {
    this.frame = frame;
  }
  
  public Frame getFrame() {
    return frame;
  }

  public Set<RemoteSession> getClients() {
    return clients;
  }

  public void setClients(Set<RemoteSession> clients) {
    this.clients = clients;
  }
  
  public void addClient(RemoteSession client) {
    clients.add(client);
  }

  public void localize(KnowledgeBase kb) {
    LocalizeUtils.localize(frame, kb);
  }
  
  public boolean isTransactionScope() {
    return transactionScope;
  }

  public void setTransactionScope(boolean transactionScope) {
    this.transactionScope = transactionScope;
  }


}
