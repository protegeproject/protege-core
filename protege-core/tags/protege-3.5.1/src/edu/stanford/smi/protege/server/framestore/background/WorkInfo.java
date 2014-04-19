package edu.stanford.smi.protege.server.framestore.background;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.util.ApplicationProperties;

public class WorkInfo implements Comparable<WorkInfo> {
  public static final String WORK_INFO_TIMEOUT_PROPERTY = "edu.stanford.smi.protege.server.framecalculator.work.info.expiration";
  public static final int WORK_INFO_TIMEOUT = ApplicationProperties.getIntegerProperty(WORK_INFO_TIMEOUT_PROPERTY, 60000);
  private static int counter = 0;

  private RemoteSession client;
  private Frame frame;
  private final Set<ServerCachedState> states = new HashSet<ServerCachedState>();
  private final EnumSet<CacheRequestReason> reasons = EnumSet.noneOf(CacheRequestReason.class);
  private boolean skipDirectInstances = true;
  private boolean targetFullCache = true;

  private int sequence = counter++;

  private final long timeWorkInfoAdded = System.currentTimeMillis();

  public boolean expired() {
      return System.currentTimeMillis() - timeWorkInfoAdded > WORK_INFO_TIMEOUT;
  }

  public RemoteSession getClient() {
    return client;
  }

  public void setClient(RemoteSession client) {
    this.client = client;
  }

  public void addState(ServerCachedState state) {
    states.add(state);
  }

  public Set<ServerCachedState> getStates() {
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