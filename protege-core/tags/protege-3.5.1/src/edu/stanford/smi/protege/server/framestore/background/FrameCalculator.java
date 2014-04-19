package edu.stanford.smi.protege.server.framestore.background;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
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
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Model;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.model.framestore.Sft;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.server.ServerProject;
import edu.stanford.smi.protege.server.framestore.Registration;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.server.framestore.ServerSessionLost;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.transaction.cache.CacheResult;
import edu.stanford.smi.protege.util.transaction.cache.serialize.CacheAbortComplete;
import edu.stanford.smi.protege.util.transaction.cache.serialize.CacheCompleted;
import edu.stanford.smi.protege.util.transaction.cache.serialize.CacheRead;
import edu.stanford.smi.protege.util.transaction.cache.serialize.CacheStartComplete;
import edu.stanford.smi.protege.util.transaction.cache.serialize.SerializedCacheUpdate;

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
  
  public static long WAIT_FOR_OVERLOADED_CLIENT = 300; // ms
  public static long MAX_WORKINFO_QUEUE = 15000;


  private FrameStore fs;
  private final Object kbLock;
  private ServerFrameStore server;
  private RemoteSession effectiveClient;

  FrameCalculatorThread innerThread;

  /*
   * The kb lock is never taken while the request Lock is held.
   */

  private Object requestLock = new Object();
  private SortedSet<WorkInfo> requests = new TreeSet<WorkInfo>();
  private Map<ClientAndFrame, WorkInfo> requestMap = new HashMap<ClientAndFrame, WorkInfo>();
  private ServerCacheStateMachine machine = null;
  private Map<RemoteSession, Registration> sessionMap;

  private static boolean disabled = false;
  private Set<RemoteSession> disabledSessions = new HashSet<RemoteSession>();

  private FrameCalculatorStatsImpl stats = new FrameCalculatorStatsImpl();
  
  private long startOfLockHeldTime;

  public FrameCalculator(FrameStore fs,
                         ServerCacheStateMachine machine,
                         Object kbLock,
                         ServerFrameStore server,
                         Map<RemoteSession, Registration> sessionMap) {
    this.fs = fs;
    this.machine = machine;
    this.kbLock = kbLock;
    this.server = server;
    this.sessionMap = sessionMap;
    innerThread = new FrameCalculatorThread();
    innerThread.start();
  }

  public void setStateMachine(ServerCacheStateMachine machine) {
      synchronized (requestLock) {
          this.machine = machine;
      }
  }


  @SuppressWarnings("unchecked")
private void doWork(WorkInfo wi) throws ServerSessionLost {
    Frame frame = wi.getFrame();
    effectiveClient = wi.getClient();
    ServerFrameStore.setCurrentSession(effectiveClient);
    if (log.isLoggable(Level.FINE)) {
        letOtherThreadsRun();
        synchronized (kbLock) {
            log.fine("Precalculating " + fs.getFrameName(frame) + "/" + frame.getFrameID());
        }
        afterKbLockHeld();
    }
    try {
      stats.startWork();
      letOtherThreadsRun();
      synchronized(kbLock) {
        if (server.inTransaction()) {
          if (log.isLoggable(Level.FINE)) {
            log.fine("\tbut transaction in progress");
          }
          wi.setTargetFullCache(false);
        } else {
          insertValueUpdate(frame, new CacheStartComplete<RemoteSession, Sft, List>());
        }
      }
      afterKbLockHeld();
      Set<Slot> slots = null;
      List values = null;
      Sft sft;
      letOtherThreadsRun();
      synchronized (kbLock) {
        checkAbilityToGenerateFullCache(wi);
        slots = fs.getOwnSlots(frame);
      }
      afterKbLockHeld();
      for (Slot slot : slots) {
        letOtherThreadsRun();
        synchronized (kbLock) {
          checkAbilityToGenerateFullCache(wi);
    	  sft = new Sft(slot, null, false);
          if (slot.getFrameID().equals(Model.SlotID.DIRECT_INSTANCES) &&
              wi.skipDirectInstances()) {
        	  CacheResult<List> invalid = CacheResult.getInvalid();
        	  insertValueUpdate(frame, new CacheRead<RemoteSession, Sft, List>(effectiveClient, sft, invalid));
          } else {
            values = fs.getDirectOwnSlotValues(frame, slot);
            if (values != null && !values.isEmpty()) {
            	CacheResult<List> result = new CacheResult<List>(values, true);
            	insertValueUpdate(frame, new CacheRead<RemoteSession, Sft, List>(effectiveClient, sft, result));
            }
          }
          addFollowedExprs(frame, slot, values);
        }
        afterKbLockHeld();
      }
      if (frame instanceof Cls) {
        Cls cls = (Cls) frame;
        letOtherThreadsRun();
        synchronized (kbLock) {
          checkAbilityToGenerateFullCache(wi);
          slots = fs.getTemplateSlots(cls);
        }
        afterKbLockHeld();
        for (Slot slot : slots) {
          letOtherThreadsRun();
          synchronized (kbLock) {
            checkAbilityToGenerateFullCache(wi);
            sft = new Sft(slot, null, true);
            values = fs.getDirectTemplateSlotValues(cls, slot);
            if (values != null && !values.isEmpty()) {
            	CacheResult<List> result = new CacheResult<List>(values, true);
            	insertValueUpdate(frame, new CacheRead<RemoteSession, Sft, List>(effectiveClient, sft, result));
            }
          }
          afterKbLockHeld();
          Set<Facet> facets;
          letOtherThreadsRun();
          synchronized (kbLock) {
            checkAbilityToGenerateFullCache(wi);
            facets = fs.getTemplateFacets(cls, slot);
          }
          afterKbLockHeld();
          for (Facet facet : facets) {
            letOtherThreadsRun();
            synchronized (kbLock) {
              checkAbilityToGenerateFullCache(wi);
              sft = new Sft(slot, facet, true);
              values = fs.getDirectTemplateFacetValues(cls, slot,facet);
              if (values != null && !values.isEmpty()) {
              	CacheResult<List> result = new CacheResult<List>(values, true);
            	insertValueUpdate(frame, new CacheRead<RemoteSession, Sft, List>(effectiveClient, sft, result));
              }
            }
            afterKbLockHeld();
          }
        }
      }
      letOtherThreadsRun();
      synchronized(kbLock) {
        if (wi.isTargetFullCache() && !server.inTransaction()) {
          insertValueUpdate(frame, new CacheCompleted<RemoteSession, Sft, List>());
        } else if (wi.isTargetFullCache()) {
            insertValueUpdate(frame, new CacheAbortComplete<RemoteSession, Sft, List>());
        }
      }
      afterKbLockHeld();
    } catch (Throwable t) {
      Log.getLogger().log(Level.SEVERE,
                          "Exception caught caching frame values",
                          t);
      wi.setTargetFullCache(false);
      insertValueUpdate(frame, new CacheAbortComplete<RemoteSession, Sft, List>());
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
      insertValueUpdate(wi.getFrame(), new CacheAbortComplete<RemoteSession, Sft, List>());
    }
  }

  @SuppressWarnings("unchecked")
  public void addFollowedExprs(Frame frame, Slot slot, List values) {
    synchronized (requestLock) {
      if (machine == null) {
          return;
      }
      WorkInfo wi = requestMap.get(new ClientAndFrame(effectiveClient, frame));
      if (wi == null) {
        return;
      }
      for (ServerCachedState state : wi.getStates()) {
        if (log.isLoggable(Level.FINER)) {
          log.finer("Following expr " + frame + " with slot " + slot + " in state " + state);
        }

        for (Object o : values) {
          if (o instanceof Frame) {
            Frame inner = (Frame) o;
            ServerCachedState newState = machine.nextState(state, frame, slot, inner);
            if (newState == null) {
              continue;
            }
            WorkInfo iwi = addRequest(inner, effectiveClient, newState, CacheRequestReason.STATE_MACHINE, true);
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
      return addRequest(frame, session, machine == null ? null : machine.getInitialState(), reason, false);
    }
  }

  public WorkInfo addRequest(Frame frame,
                             RemoteSession session,
                             ServerCachedState state,
                             CacheRequestReason reason,
                             boolean forceUpdate) {
    if (inFrameCalculatorThread() && !forceUpdate) {
        return null;
    }
    if (isDisabled(session)) {
      return null;
    }
    if (reason != CacheRequestReason.PRELOAD &&
    		reason != CacheRequestReason.IMMEDIATE_PRELOAD &&
    		sessionMap.get(session).getBandWidthPolicy().stopSending()) {
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
              log.fine("Added " + frame.getFrameID() + " in state " + state +
                       " with reason " + reason + " to head of frames to precalculate");
          }
        wi = new WorkInfo();
        wi.setClient(session);
        wi.setFrame(frame);
        requestMap.put(cwf, wi);
      } else {
        if (log.isLoggable(Level.FINE)) {
                log.fine("Updating state for " + frame.getFrameID() + " to include " + state);
        }
        requests.remove(wi);
      }
      wi.addState(state);
      wi.addReason(reason);
      wi.setNewest();
      requests.add(wi);
      requestLock.notify();
      return wi;
    }
  }
  
  
  public void removeRequest(WorkInfo wi) {
      synchronized (requestLock) {
          requests.remove(wi);
          requestMap.remove(new ClientAndFrame(wi.getClient(),
                                               wi.getFrame()));
      }
  }



  /*
   * This call assumes that the kbLock is held on entry.
   */
  private void insertValueUpdate(Frame frame, SerializedCacheUpdate<RemoteSession, Sft, List> update) {
    if (log.isLoggable(Level.FINEST)) {
        log.finest("For frame " + frame.getName() + " inserted value update " + update);
    }
    Registration registration = sessionMap.get(effectiveClient);
    if (registration == null) {
        return;
    }
    registration.getBandWidthPolicy().addItemToWaitList();
    server.updateEvents(effectiveClient);
    server.addReadUpdate(effectiveClient, frame, update);
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

  public boolean isDisabled(RemoteSession session) {
      synchronized (requestLock) {
          return disabled
                    || session == null
                    || sessionMap.get(session) == null //a Protege Job accessed a different kb
                    || disabledSessions.contains(session);
      }
  }

  public static void setDisabled(boolean disabled) {
    FrameCalculator.disabled = disabled;
  }

  public boolean setDisabled(boolean disabled, RemoteSession session) {
      synchronized (requestLock) {
          boolean previousValue = disabledSessions.contains(session);
          if (disabled) {
              disabledSessions.add(session);
          }
          else {
              disabledSessions.remove(session);
          }
          return previousValue;
      }
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
          try {
              if (log.isLoggable(Level.FINER)) {
                  SortedSet<WorkInfo> requestsCopy;
                  synchronized (requestLock) {
                      requestsCopy = new TreeSet<WorkInfo>(requests);
                  }
                  log.fine("Request queue has size " + requestsCopy.size());
                  for (WorkInfo wi : requestsCopy) {
                      log.finer("Request for frame" + wi.getFrame());
                      log.finer("\tClient = " + wi.getClient());
                      log.finer("\tStates = " + wi.getStates());
                      log.finer("\tReasons = " + wi.getReasons());
                  }
              } else {
                  Map<CacheRequestReason, Integer> reasonCounts = new EnumMap<CacheRequestReason, Integer>(CacheRequestReason.class);
                  Map<ServerCachedState, Integer> stateCounts = new HashMap<ServerCachedState, Integer>();
                  for (CacheRequestReason reason : CacheRequestReason.values()) {
                      reasonCounts.put(reason, 0);
                  }
                  synchronized (requestLock) {
                      log.fine("Request queue has size " + requests.size());
                      for (WorkInfo wi : requests) {
                          for (ServerCachedState state : wi.getStates()) {
                              Integer counts = stateCounts.get(state);
                              if (counts == null) {
                                  counts = 0;
                              }
                              stateCounts.put(state, counts + 1);
                          }
                      }
                      for (WorkInfo wi : requests) {
                          for (CacheRequestReason reason : wi.getReasons()) {
                              reasonCounts.put(reason, reasonCounts.get(reason) + 1);
                          }
                      }
                  }
                  for (ServerCachedState state : stateCounts.keySet()) {
                      if (stateCounts.get(state) != null) {
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
          catch (Throwable t) {
              log.log(Level.FINE, "Could not log requests", t);
          }
      }
  }

  public Object getRequestLock() {
    return requestLock;
  }

  public boolean inFrameCalculatorThread() {
      synchronized (requestLock) {
          if (innerThread == null) {
              return false;
          }
          else {
              return Thread.currentThread().equals(innerThread);
          }
      }
  }
  
  /*
   * This method assumes that the caller is holding the kbLock.  The idea is to
   * encourage other thread that are waiting on the kbLock to continue running at
   * the expense of the current thread.  It has recently been discovered that the thread
   * Manager implementation is very expensive to invoke so by default this logic is turned off.
   */
  private void letOtherThreadsRun() {
	  if (log.isLoggable(Level.FINE)) {
		  startOfLockHeldTime = System.currentTimeMillis();
	  }
	  server.letOtherThreadsRun();
  }
  
  private void afterKbLockHeld() {
	  if (log.isLoggable(Level.FINE)) {
		  log.fine("Knowledge Base Lock held for " + (System.currentTimeMillis() - startOfLockHeldTime));
	  }
  }

  private class FrameCalculatorThread extends Thread {

    public FrameCalculatorThread() {
      super("Frame Pre-Calculation Thread");
    }

    @Override
    public void run() {
        WorkInfo workInfo;

        while (true) {
            try {
                workInfo = getWorkInfo();
                long startTime = System.currentTimeMillis();
                doWork(workInfo);
                synchronized (requestLock) {
                    removeRequest(workInfo);
                }
                if (log.isLoggable(Level.FINE)) {
                    log.fine("work on frame " + workInfo.getFrame() + " took " + (System.currentTimeMillis() - startTime));
                    logRequests();
                }
            } catch (Throwable  t) {
                Log.getLogger().log(Level.SEVERE, "Exception caught in background frame value evaluator", t);
            }
        }
    }
  }
  
  private WorkInfo getWorkInfo() {
      synchronized (requestLock) {
          while  (true) {
              List<WorkInfo> toRemove = new  ArrayList<WorkInfo>();
              for (WorkInfo wi : requests) {
                  if (wi.expired())  {
                      toRemove.add(wi);
                  }
                  else if (wi.getReasons().contains(CacheRequestReason.PRELOAD)
                          || wi.getReasons().contains(CacheRequestReason.IMMEDIATE_PRELOAD)
                          || !sessionMap.get(wi.getClient()).getBandWidthPolicy().stopSending()) {
                      return wi;
                  }
              }
              for (WorkInfo removeMe : toRemove) {
                  removeRequest(removeMe);
              }
              try {
                  if (requests.isEmpty()) {
                      requestLock.wait();
                  }
                  else {
                      requestLock.wait(WAIT_FOR_OVERLOADED_CLIENT);
                  }
              }
              catch (InterruptedException ie) {
                  log.log(Level.WARNING, "Unexpected interrupt", ie);
              }
          }
      }
  }

  public static FrameCalculator getFrameCalculator(KnowledgeBase kb) {
    ServerProject sp = Server.getInstance().getServerProject(kb.getProject());
    if (sp == null) {
        return null;
    }
    ServerFrameStore sfs = (ServerFrameStore) sp.getDomainKbFrameStore(null);
    return sfs.getFrameCalculator();
}

public static class FrameCalculatorStatsImpl implements FrameCalculatorStats, Serializable {
    private static final long serialVersionUID = -573113660316027300L;

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
      return workUnits == 0 ? 0l : totalWorkTime / workUnits;
    }

  }

}
