package edu.stanford.smi.protege.server.framestore.background;

import java.util.EnumSet;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.server.RemoteSession;

public class WorkInfo implements Comparable<WorkInfo> {
  private static int counter = 0;
  
  private RemoteSession client;
  private Frame frame;
  private EnumSet<State> states = EnumSet.noneOf(State.class);
  private EnumSet<CacheRequestReason> reasons = EnumSet.noneOf(CacheRequestReason.class);
  private boolean skipDirectInstances = true;
  private boolean targetFullCache = true;
  
  private int sequence = counter++;
  
  public RemoteSession getClient() {
    return client;
  }
  
  public void setClient(RemoteSession client) {
    this.client = client;
  }
  
  public void addState(State state) {
    states.add(state);
  }
  
  public EnumSet<State> getStates() {
    return states;
  }
  
  public void addReason(CacheRequestReason reason) {
    reasons.add(reason);
  }
  
  public EnumSet<CacheRequestReason> getReasons() {
    return reasons;
  }
  
  public boolean skipDirectInstances() {
    return skipDirectInstances;
  }

  public void setSkipDirectInstances(boolean skipDirectInstances) {
    this.skipDirectInstances = skipDirectInstances;
  }
  
  public int getSequence() {
    return sequence;
  }
  
  public void setNewest() {
    sequence = counter++;
  }

  public int compareTo(WorkInfo other) {
    int myPriority = CacheRequestReason.priority(reasons);
    int otherPriority = CacheRequestReason.priority(other.reasons);
    if (myPriority != otherPriority) {
      return otherPriority - myPriority;
    }
    return other.sequence - sequence;
  }

  public Frame getFrame() {
    return frame;
  }

  public void setFrame(Frame frame) {
    this.frame = frame;
  }

  public boolean isTargetFullCache() {
    return targetFullCache;
  }

  public void setTargetFullCache(boolean targetFullCache) {
    this.targetFullCache = targetFullCache;
  }
  
  public ClientAndFrame getClientAndFrame() {
    return new ClientAndFrame(client, frame);
  }
}