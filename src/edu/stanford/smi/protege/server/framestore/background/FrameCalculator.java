package edu.stanford.smi.protege.server.framestore.background;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
                         ServerFrameStore server) {
    this.fs = fs;
    this.kbLock = kbLock;
    this.updates = updates;
    this.server = server;
  }
  

  private void doWork(Frame frame) {
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
          if (slot.getFrameID().equals(Model.SlotID.DIRECT_INSTANCES)) {
            insertEvent(
                new InvalidateCacheUpdate(frame, slot, (Facet) null, false));
          } else {
            server.waitForTransactionsToComplete();
            values = fs.getDirectOwnSlotValues(frame, slot);
            insertValueEvent(frame, slot, (Facet) null, false, values);
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
          }
          insertValueEvent(cls, slot, (Facet) null, true, values);
          Set<Facet> facets;
          synchronized (kbLock) {
            server.waitForTransactionsToComplete();
            facets = fs.getTemplateFacets(cls, slot);
          }
          for (Facet facet : facets) {
            synchronized (kbLock) {
              server.waitForTransactionsToComplete();
              values = fs.getDirectTemplateFacetValues(cls, slot,facet);
              insertValueEvent(cls, slot,  facet, true, values);
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
              break;
            }
            WorkInfo iwi = addRequest(inner, newState);
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

  public void addRequest(Frame frame, RemoteSession session) {
    WorkInfo wi = addRequest(frame, State.Start);
    wi.getClients().add(session);
  }
  
  public WorkInfo addRequest(Frame frame , State state) {
    if (log.isLoggable(Level.FINE)) {
      log.fine("Added " + fs.getFrameName(frame) + 
               " to head of frames to precalculate");
    }
    synchronized (requestLock) {
      // put the request at the front of the queue.
      // the status of the frame calculator thread will not change
      // for the duration of this lock
      if (requests.contains(frame)) {
        requests.remove(frame);
      }
      requests.add(0, frame);
      WorkInfo wi = requestMap.get(frame);
      if (wi == null) {
        wi = new WorkInfo();
        requestMap.put(frame, wi);
      }
      wi.addState(state);
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
  

  private class FrameCalculatorThread extends Thread {
    private RunStatus status = RunStatus.IDLE;
    
    public FrameCalculatorThread() {
      super("Frame Pre-Calculation Thread");
    }
    
    public void run() {
      Frame work;
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
          }
          doWork(work);
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
  
  private class WorkInfo {
    private Set<RemoteSession> clients = new HashSet<RemoteSession>();
    private EnumSet<State> states = EnumSet.noneOf(State.class);

    public Set<RemoteSession> getClients() {
      return clients;
    }
    
    public void addState(State state) {
      states.add(state);
    }
    
    public EnumSet<State> getStates() {
      return states;
    }

  }
  
}
