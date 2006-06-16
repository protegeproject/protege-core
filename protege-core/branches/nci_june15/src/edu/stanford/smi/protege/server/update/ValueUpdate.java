package edu.stanford.smi.protege.server.update;

import java.io.Serializable;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Localizable;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.util.LocalizeUtils;

public abstract class ValueUpdate implements Localizable, Serializable {
  private Frame frame;
  private transient RemoteSession client;
  private boolean transactionScope = false;
  
  public ValueUpdate(Frame frame) {
    this.frame = frame;
  }
  
  public Frame getFrame() {
    return frame;
  }

  public RemoteSession getClient() {
    return client;
  }

  public void setClient(RemoteSession client) {
    this.client = client;
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
  
  public abstract ValueUpdate getInvalidatingVariant();
  
  public String toString() {
    return "ValueUpdate[" + frame.getFrameID() + ", Client = " + client + ", Transaction(" + transactionScope + ")]";
  }


}
