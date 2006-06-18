package edu.stanford.smi.protege.server.framestore.background;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.framestore.Registration;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.server.framestore.ServerSessionLost;
import edu.stanford.smi.protege.server.update.FrameEvaluationCompleted;
import edu.stanford.smi.protege.server.update.FrameEvaluationPartial;
import edu.stanford.smi.protege.server.update.FrameEvaluationStarted;
import edu.stanford.smi.protege.server.update.InvalidateCacheUpdate;
import edu.stanford.smi.protege.server.update.ValueUpdate;
import edu.stanford.smi.protege.server.util.FifoWriter;
import edu.stanford.smi.protege.util.Log;

/*
 * This frame calculator has to be aware of transactions.  A good
 * illustrative case is as follows:
 * 
 *   client X starts transaction
 *   client X modifies slot s for frame f
 *   the frame calculator evaluates a value for slot s for frame f
 *   client X rolls back the transaction
 *
 * At this point the evaluation of the value for the frame f is
 * invalid.  We will take a simple approach to this situation; we wait
 * for all transactions to be complete before calculating frame
 * values.  
 * 
 */


/**
 * This class is a thread that precalculates frames needed by a client.
 * @author tredmond
 *
 */

public class FrameCalculator {
  private static transient Logger log = Log.getLogger(FrameCalculator.class);
  
  private enum RunStatus {
    IDLE, RUNNING, SHUTDOWN
  }
  
  private FrameStore fs;
  private final Object kbLock;
  private FifoWriter<ValueUpdate> updates;
  private ServerFrameStore server;
  private RemoteSession effectiveClient;
  
  FrameCalculatorThread innerThread;
  
  /*
   * The kb lock is never taken while the request Lock is held.
   */
  
  private Object requestLock = new Object();
  private SortedSet<WorkInfo> requests = new TreeSet<WorkInfo>();
  private Map<ClientAndFrame, WorkInfo> requestMap = new HashMap<ClientAndFrame, WorkInfo>();
  private StateMachine machine = null;
  private static boolean disabled = false;
  
  FrameCalculatorStatsImpl stats = new FrameCalculatorStatsImpl();
  
  public FrameCalculator(FrameStore fs,
                         Object kbLock,
                         FifoWriter<ValueUpdate> updates,
                         ServerFrameStore server,
                         Map<RemoteSession, Registration> sessionMap) {
    this.fs = fs;
    this.kbLock = kbLock;
    this.updates = updates;
    this.server = server;
  }
  

  private void doWork(WorkInfo wi) throws ServerSessionLost {
    Frame frame = wi.getFrame();
    effectiveClient = wi.getClient();
    ServerFrameStore.setCurrentSession(effectiveClient);
    if (log.isLoggable(Level.FINE)) {
      log.fine("Precalculating " + fs.getFrameName(frame) + "/" + frame.getFrameID());
    }
    try {
      stats.startWork();
      synchronized(kbLock) {
        if (server.inTransaction()) {
          if (log.isLoggable(Level.FINE)) {
            log.fine("\tbut transaction in progress");
          }
          wi.setTargetFullCache(false);
        } else {
          insertValueUpdate(new FrameEvaluationStarted(frame));
        }
      }
      Set<Slot> slots = null;
      List values = null;
      synchronized (kbLock) {
        checkAbilityToGenerateFullCache(wi);
        slots = fs.getOwnSlots(frame);
      }
      for (Slot slot : slots) {
        synchronized (kbLock) {
          checkAbilityToGenerateFullCache(wi);
          if (slot.getFrameID().equals(Model.SlotID.DIRECT_INSTANCES) &&
              wi.skipDirectInstances()) {
            server.invalidateCacheForWriteToStore(frame, slot, null, false);
          } else {
            values = fs.getDirectOwnSlotValues(frame, slot);
            if (values != null && !values.isEmpty()) {
              server.cacheValuesReadFromStore(effectiveClient, frame, slot, null, false, values);
            }
          }
          addFollowedExprs(frame, slot, values);
        }  
      }
      if (frame instanceof Cls) {
        Cls cls = (Cls) frame;
        synchronized (kbLock) {
          checkAbilityToGenerateFullCache(wi);
          slots = fs.getTemplateSlots(cls);
        }
        for (Slot slot : slots) {
          synchronized (kbLock) {
            checkAbilityToGenerateFullCache(wi);
            values = fs.getDirectTemplateSlotValues(cls, slot);
            if (values != null && !values.isEmpty()) {
              server.cacheValuesReadFromStore(effectiveClient, cls, slot, null, true, values);
            }
          }
          Set<Facet> facets;
          synchronized (kbLock) {
            checkAbilityToGenerateFullCache(wi);
            facets = fs.getTemplateFacets(cls, slot);
          }
          for (Facet facet : facets) {
            synchronized (kbLock) {
              checkAbilityToGenerateFullCache(wi);
              values = fs.getDirectTemplateFacetValues(cls, slot,facet);
              if (values != null && !values.isEmpty()) {
                server.cacheValuesReadFromStore(effectiveClient, cls, slot, facet, true, values);
              }
            }
          }
        }
      }
      synchronized(kbLock) {
        if (wi.isTargetFullCache() && !server.inTransaction()) {
          insertValueUpdate(new FrameEvaluationCompleted(frame));
        } else if (wi.isTargetFullCache()) {
          insertValueUpdate(new FrameEvaluationPartial(wi.getFrame()));
        }
      }
    } catch (Throwable t) {
      Log.getLogger().log(Level.SEVERE, 
                          "Exception caught caching frame values", 
                          t);
      wi.setTargetFullCache(false);
      insertValueUpdate(new FrameEvaluationPartial(wi.getFrame()));
    } finally {
      stats.completeWork();
    }
  }
  
  
  private void checkAbilityToGenerateFullCache(WorkInfo wi) {
    if (server.inTransaction() && wi.isTargetFullCache()) {
      if (log.isLoggable(Level.FINE)) {
        log.fine("Found transaction in progress - can't complete cache for frame " + wi.getFrame());
      }
      wi.setTargetFullCache(false);
      insertValueUpdate(new FrameEvaluationPartial(wi.getFrame()));
    }
  }
  
  public void addFollowedExprs(Frame frame, Slot slot, List values) {
    synchronized (requestLock) {
      if (machine == null) {
        machine = new StateMachine(fs, kbLock);
      }
      WorkInfo wi = requestMap.get(new ClientAndFrame(effectiveClient, frame));
      if (wi == null) {
        return;
      }
      for (State state : wi.getStates()) {
        if (log.isLoggable(Level.FINER)) {
          log.finer("Following expr " + frame + " with slot " + slot + " in state " + state);
        }

        for (Object o : values) {
          if (o instanceof Frame) {
            Frame inner = (Frame) o;
            State newState = machine.nextState(state, slot, inner);
            if (newState == null) {
              continue;
            }
            WorkInfo iwi = addRequest(inner, newState, CacheRequestReason.STATE_MACHINE);
            if (iwi != null) {
              iwi.setClient(wi.getClient());
            }
            if (log.isLoggable(Level.FINE)) {
              log.fine("Added cache request frame for state transition " + 
                  frame + " x " + state + " -> " + inner + " x " + newState);
            }
          }
        }
      }
    }
  }

  public WorkInfo addRequest(Frame frame, RemoteSession session, CacheRequestReason reason) {
    synchronized (requestLock) {
      return addRequest(frame, session, State.Start, reason);
    }
  }
  
  public WorkInfo addRequest(Frame frame, State state, CacheRequestReason  reason) {
    synchronized (requestLock) {
      return addRequest(frame, effectiveClient, state, reason);
    }
  }
  
  public WorkInfo addRequest(Frame frame, 
                             RemoteSession session, 
                             State state, 
                             CacheRequestReason reason) {
    if (disabled) {
      return null;
    }
    if (frame.getKnowledgeBase() == null) {
      log.log(Level.WARNING, "Non-localized frame being added to the FrameCalculator", new Exception());
    }
    synchronized (requestLock) {
      ClientAndFrame cwf = new ClientAndFrame(session, frame);
      WorkInfo wi = requestMap.get(cwf);
      if (wi == null) {
        if (log.isLoggable(Level.FINE)) {
          log.fine("Added " + fs.getFrameName(frame) + " in state " + state + 
                   " with reason " + reason + " to head of frames to precalculate");
        }
        wi = new WorkInfo();
        wi.setClient(session);
        wi.setFrame(frame);
        requestMap.put(cwf, wi);
      } else {
        if (log.isLoggable(Level.FINE)) {
          log.fine("Updating state for " + fs.getFrameName(frame) + " to include " + state);
        }
        requests.remove(wi);
      }
      wi.addState(state);
      wi.addReason(reason);
      wi.setNewest();
      requests.add(wi);
      if (innerThread == null || 
          innerThread.getStatus() == RunStatus.SHUTDOWN) {
        innerThread = new FrameCalculatorThread();
        innerThread.start();
      }
      return wi;
    }
  }

  
  
  /*
   * This call assumes that the kbLock is held on entry.
   */
  private void insertValueUpdate(ValueUpdate vu) {
    vu.setClient(effectiveClient);
    server.updateEvents(effectiveClient);
    updates.write(vu);
  }
  
  
  public void deregister(RemoteSession session) {
    synchronized (requestLock) {
      List<WorkInfo> remove = new ArrayList<WorkInfo>();
      for (WorkInfo wi : requests) {
        if (wi.getClient().equals(session)) {
          remove.add(wi);
        }
      }
      for (WorkInfo wi : remove) {
        requests.remove(wi);
        requestMap.remove(wi.getClientAndFrame());
      }
    }
  }
  
  public static void setDisabled(boolean disabled) {
    FrameCalculator.disabled = disabled;
  }
  
  public FrameCalculatorStats getStats() {
    Map<RemoteSession, Integer> backlogs = new HashMap<RemoteSession, Integer>();
    synchronized (requestLock) {
      for (WorkInfo wi : requests) {
        RemoteSession session = wi.getClient();
        Integer count = backlogs.get(session);
        if (count == null) {
          backlogs.put(session, 1);
        } else {
          backlogs.put(session, count+1);
        }
      }
    }
    stats.setPreCacheBackLog(backlogs);
    return stats;
  }

  
  public void logRequests() {
    if (log.isLoggable(Level.FINE)) {
      synchronized (requestLock) {
        log.fine("Request queue has length " + requests.size());
        if (log.isLoggable(Level.FINER)) {
          for (WorkInfo wi : requests) {
            log.fine("Request for frame" + wi.getFrame());
            log.fine("\tClient = " + wi.getClient());
            log.fine("\tStates = " + wi.getStates());
            log.fine("\tReasons = " + wi.getReasons());
          }
        } else {
          EnumMap<CacheRequestReason, Integer> reasonCounts
              = new EnumMap<CacheRequestReason, Integer>(CacheRequestReason.class);
          EnumMap<State, Integer> stateCounts
              = new EnumMap<State, Integer>(State.class);
          for (State state : State.values()) {
            stateCounts.put(state, 0);
          }
          for (CacheRequestReason reason : CacheRequestReason.values()) {
            reasonCounts.put(reason, 0);
          }
          for (WorkInfo wi : requests) {
            for (State state : wi.getStates()) {
              stateCounts.put(state, stateCounts.get(state) + 1);
            }
          }
          for (WorkInfo wi : requests) {
            for (CacheRequestReason reason : wi.getReasons()) {
              reasonCounts.put(reason, reasonCounts.get(reason) + 1);
            }
          }
          for (State state : State.values()) {
            if (stateCounts.get(state) != 0) {
              log.fine("\tCount for state " + state + " = " + stateCounts.get(state));
            }
          }
          for (CacheRequestReason reason : CacheRequestReason.values()) {
            if (reasonCounts.get(reason) != 0) {
              log.fine("\tCount for reason " + reason + " = " + reasonCounts.get(reason));
            }
          }
        }
        
      }
    }
  }
  
  public Object getRequestLock() {
    return requestLock;
  }
  
  private class FrameCalculatorThread extends Thread {
    private RunStatus status = RunStatus.IDLE;
    
    public FrameCalculatorThread() {
      super("Frame Pre-Calculation Thread [" + server + "]");
    }
    
    public void run() {
      WorkInfo workInfo;
      synchronized(requestLock) {
        status = RunStatus.RUNNING;
      }
      try {
        while (true) {
          synchronized (requestLock) {
            if (requests.isEmpty()) {
              status = RunStatus.SHUTDOWN;
              return;
            }
            workInfo = requests.first();
          }
          long startTime = System.currentTimeMillis();
          doWork(workInfo);
          synchronized (requestLock) {
            requests.remove(workInfo);
            requestMap.remove(new ClientAndFrame(workInfo.getClient(), 
                                                 workInfo.getFrame()));
          }
          if (log.isLoggable(Level.FINE)) {
            log.fine("work on frame " + workInfo.getFrame() + " took " + (System.currentTimeMillis() - startTime));
            logRequests();
          }
        }
      } catch (Throwable  t) {
        Log.getLogger().log(Level.SEVERE, "Exception caught in background frame value evaluator", t);
        Log.getLogger().severe("Pre-caching of frames will fail.");
      }
    }
    
    public RunStatus getStatus() {
      return status;
    }
  }
  
  public static class FrameCalculatorStatsImpl implements FrameCalculatorStats, Serializable {
    private long startWorkTime;
    private long workUnits = 0;
    private long totalWorkTime  = 0;
    private Map<RemoteSession, Integer> preCacheBacklog;
    
    private void startWork() {
      startWorkTime = System.currentTimeMillis();
    }
    
    private void completeWork() {
      totalWorkTime = totalWorkTime + System.currentTimeMillis() - startWorkTime;
      workUnits++;
    }

    public Map<RemoteSession, Integer> getPreCacheBacklog() {
      return preCacheBacklog;
    }
    
    public void setPreCacheBackLog(Map<RemoteSession, Integer> backlog) {
      preCacheBacklog = backlog;
    }

    public long getPrecalculateTime() {
      return totalWorkTime / workUnits;
    }
    
  }
  
}
