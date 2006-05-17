package edu.stanford.smi.protege.server.framestore.background;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import edu.stanford.smi.protege.server.update.FrameEvaluationCompleted;
import edu.stanford.smi.protege.server.update.FrameEvaluationEvent;
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
  
  private boolean shuttingDown = false;
  private FrameStore fs;
  private final Object kbLock;
  private FifoWriter<ValueUpdate> updates;
  private ServerFrameStore server;
  private Map<RemoteSession, Registration> sessionToRegistrationMap;
  
  FrameCalculatorThread innerThread;
  
  /*
   * The kb lock is never taken while the request Lock is held.
   */
  
  private Object requestLock = new Object();
  private List<Frame> requests = new ArrayList<Frame>();
  private Map<Frame, WorkInfo> requestMap = new HashMap<Frame, WorkInfo>();
  private StateMachine machine = null;
  
  public FrameCalculator(FrameStore fs,
                         Object kbLock,
                         FifoWriter<ValueUpdate> updates,
                         ServerFrameStore server,
                         Map<RemoteSession, Registration> sessionMap) {
    this.fs = fs;
    this.kbLock = kbLock;
    this.updates = updates;
    this.server = server;
    sessionToRegistrationMap = sessionMap;
  }
  

  private void doWork(Frame frame, WorkInfo wi) {
    if (log.isLoggable(Level.FINE)) {
      log.fine("Precalculating " + fs.getFrameName(frame) + "/" + frame.getFrameID());
    }
    try {
      synchronized(kbLock) {
        insertEvent(new FrameEvaluationStarted(frame));
      }
      Set<Slot> slots = null;
      List values = null;
      synchronized (kbLock) {
        server.waitForTransactionsToComplete();
        slots = fs.getOwnSlots(frame);
      }
      for (Slot slot : slots) {
        synchronized (kbLock) {
          if (slot.getFrameID().equals(Model.SlotID.DIRECT_INSTANCES) &&
              wi.skipDirectInstances()) {
            insertEvent(
                new InvalidateCacheUpdate(frame, slot, (Facet) null, false));
          } else {
            server.waitForTransactionsToComplete();
            values = fs.getDirectOwnSlotValues(frame, slot);
            if (values != null && !values.isEmpty()) {
              insertValueEvent(frame, slot, (Facet) null, false, values);
            }
          }
          addFollowedExprs(frame, slot, values);
        }  
      }
      if (frame instanceof Cls) {
        Cls cls = (Cls) frame;
        synchronized (kbLock) {
          server.waitForTransactionsToComplete();
          slots = fs.getTemplateSlots(cls);
        }
        for (Slot slot : slots) {
          synchronized (kbLock) {
            server.waitForTransactionsToComplete();
            values = fs.getDirectTemplateSlotValues(cls, slot);
            if (values != null && !values.isEmpty()) {
              insertValueEvent(cls, slot, (Facet) null, true, values);
            }
          }
          Set<Facet> facets;
          synchronized (kbLock) {
            server.waitForTransactionsToComplete();
            facets = fs.getTemplateFacets(cls, slot);
          }
          for (Facet facet : facets) {
            synchronized (kbLock) {
              server.waitForTransactionsToComplete();
              values = fs.getDirectTemplateFacetValues(cls, slot,facet);
              if (values != null && !values.isEmpty()) {
                insertValueEvent(cls, slot,  facet, true, values);
              }
            }
          }
        }
      }
      synchronized(kbLock) {
        insertEvent(new FrameEvaluationCompleted(frame));
      }
    } catch (Throwable t) {
      Log.getLogger().log(Level.SEVERE, 
                          "Exception caught caching frame values", 
                          t);
    }
  }
  
  public void addFollowedExprs(Frame frame, Slot slot, List values) {
    synchronized (requestLock) {
      if (machine == null) {
        machine = new StateMachine(fs, kbLock);
      }
      WorkInfo wi = requestMap.get(frame);
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
            iwi.getClients().addAll(wi.getClients());
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
      WorkInfo wi = addRequest(frame, State.Start, reason);
      wi.getClients().add(session);
      return wi;
    }
  }
  
  public WorkInfo addRequest(Frame frame , State state, CacheRequestReason reason) {
    if (log.isLoggable(Level.FINE)) {
      log.fine("Added " + fs.getFrameName(frame) + " in state " + state + 
               " to head of frames to precalculate");
    }
    if (frame.getKnowledgeBase() == null) {
      log.log(Level.WARNING, "Non-localized frame being added to the FrameCalculator", new Exception());
    }
    synchronized (requestLock) {
      WorkInfo wi = requestMap.get(frame);
      if (wi == null) {
        wi = new WorkInfo();
        requestMap.put(frame, wi);
      } else {
        requests.remove(frame);
      }
      wi.addState(state);
      wi.addReason(reason);
      wi.setNewest();
      requests.add(frame);
      // A tree set would have been cheaper here but it is tricky and
      // I decided not to debug it.
      Collections.sort(requests, new Comparator<Frame>() {
        
        public int compare(Frame f1, Frame f2) {
          return requestMap.get(f1).compareTo(requestMap.get(f2));
        }
        
      });
      if (innerThread == null || 
          innerThread.getStatus() == RunStatus.SHUTDOWN) {
        innerThread = new FrameCalculatorThread();
        innerThread.start();
      }
      return wi;
    }
  }

  public void dispose() {
    shuttingDown = true;
    synchronized (requestLock) {
      requestLock.notifyAll();
    }
  }


  /*
   * This call assumes that the kbLock is held on entry.
   */
  private void insertValueEvent(Frame frame, 
                                Slot slot, 
                                Facet facet, 
                                boolean isTemplate, 
                                List values) {

    insertEvent( 
        new FrameEvaluationEvent(frame, slot, facet, isTemplate, values));
    if (log.isLoggable(Level.FINER)) {
      log.finer("Added frame eval event for frame " + fs.getFrameName(frame)
          + "/" + frame.getFrameID() + " and " + (isTemplate ? "template" : " own ")
          + "slot " + fs.getFrameName(slot) 
          + "/" + slot.getFrameID());
    }
  }
  
  
  /*
   * This call assumes that the kbLock is held on entry.
   */
  private void insertEvent(ValueUpdate event) {
    Frame frame = event.getFrame();
    Set<RemoteSession> clients = null;
    synchronized(requestLock) {
      clients= requestMap.get(frame).getClients();
    }
    insertEvent(event, clients);
  }
  
  private void insertEvent(ValueUpdate update, Set<RemoteSession> clients) {
    update.setClients(clients);
    server.updateEvents();
    updates.write(update);
  }

  public boolean checkInterest(ValueUpdate event,
                               RemoteSession session) {
    synchronized (requestLock) {
      return event.getClients().contains(session);
    }
  }
  
  public void logRequests() {
    if (log.isLoggable(Level.FINE)) {
      synchronized (requestLock) {
        log.fine("Request queue has length " + requests.size());
        for (Frame frame : requests) {
          WorkInfo wi = requestMap.get(frame);
          log.fine("Request for frame" + frame);
          for (RemoteSession session : wi.getClients()) {
            log.fine("\tClient " + session);
          }
          log.fine("\tStates = " + wi.getStates());
          log.fine("\tReasons = " + wi.getReasons());
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
      super("Frame Pre-Calculation Thread");
    }
    
    public void run() {
      Frame work;
      WorkInfo workInfo;
      synchronized(requestLock) {
        status = RunStatus.RUNNING;
      }
      try {
        while (true) {
          synchronized (requestLock) {
            if (requests.isEmpty() || shuttingDown) {
              status = RunStatus.SHUTDOWN;
              return;
            }
            work = requests.get(0);
            workInfo = requestMap.get(work);
          }
          doWork(work, workInfo);
          synchronized (requestLock) {
            requests.remove(work);
            requestMap.remove(work);
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
  
}
