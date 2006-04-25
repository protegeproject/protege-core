package edu.stanford.smi.protege.server.framestore.background;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.framestore.FrameStore;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.framestore.Registration;
import edu.stanford.smi.protege.server.framestore.ServerEventWrapper;
import edu.stanford.smi.protege.server.util.FifoWriter;
import edu.stanford.smi.protege.util.Log;

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
  private Object kbLock;
  private FifoWriter<ServerEventWrapper> events;
  private Map<RemoteSession, Registration> sessionToRegistrationMap;
  
  FrameCalculatorThread innerThread;
  
  /*
   * The kb lock is never taken while the request Lock is held.
   */
  
  private Object requestLock = new Object();
  private List<Frame> requests = new ArrayList<Frame>();
  private Map<Frame, Set<RemoteSession>> requestMap = new HashMap<Frame, Set<RemoteSession>>();
  
  public FrameCalculator(FrameStore fs,
                         Object kbLock,
                         FifoWriter<ServerEventWrapper> events,
                         Map<RemoteSession, Registration> sessionToRegistrationMap) {
    this.fs = fs;
    this.kbLock = kbLock;
    this.events = events;
    this.sessionToRegistrationMap = sessionToRegistrationMap;
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
      List values;
      synchronized (kbLock) {
        slots = fs.getOwnSlots(frame);
      }
      for (Slot slot : slots) {
        synchronized (kbLock) {
          values = fs.getDirectOwnSlotValues(frame, slot);
          insertValueEvent(frame, slot, (Facet) null, false, values);
        }
      }
      if (frame instanceof Cls) {
        Cls cls = (Cls) frame;
        synchronized (kbLock) {
          slots = fs.getTemplateSlots(cls);
        }
        for (Slot slot : slots) {
          synchronized (kbLock) {
            values = fs.getDirectTemplateSlotValues(cls, slot);
          }
          insertValueEvent(cls, slot, (Facet) null, true, values);
          Set<Facet> facets;
          synchronized (kbLock) {
            facets = fs.getTemplateFacets(cls, slot);
          }
          for (Facet facet : facets) {
            synchronized (kbLock) {
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
  
  /**
   * This call preloads data for the frames on behalf of a given client.
   * It gives other clients several opporunities to take the kbLock
   *
   * @param frames The frames to be preloaded.
   # @param client The client on whose behalf the frames are being loaded. 
   */
  public void preLoadFrames(Collection<Frame> frames, RemoteSession client) {
    /*
     * An alternative to the following code is to simply send this to the 
     * frame calculator - it will have a head start - is this enough?
     */
    Set<RemoteSession> clients = Collections.singleton(client);
    List values;
    for (Frame frame : frames) {
      synchronized (kbLock) {
        insertEvent(new FrameEvaluationStarted(frame), clients);
      }
      
      Set<Slot> slots;
      synchronized (kbLock) {
        slots = fs.getOwnSlots(frame);
      }
      for (Slot slot : slots) {
        synchronized (kbLock) {
          values = fs.getDirectOwnSlotValues(frame, slot);
          insertEvent(
              new FrameEvaluationEvent(frame, slot, (Facet) null, false, values), 
              clients);
        }
        
      }
      if (frame instanceof Cls) {
        Cls cls = (Cls) frame;
          
        synchronized (kbLock)  {
          slots = fs.getTemplateSlots(cls);
        }
        for (Slot slot : slots) {
          synchronized (kbLock) {
            values = fs.getDirectTemplateSlotValues(cls, slot);
            insertEvent(
                new FrameEvaluationEvent(cls, slot, (Facet) null,  true, values), 
                clients);
          }
          Set<Facet> facets;
          synchronized (kbLock) {
            facets = fs.getTemplateFacets(cls, slot);
          }
          for (Facet facet : facets) {
            synchronized (kbLock) {
              values = fs.getDirectTemplateFacetValues(cls, slot, facet);
              insertEvent(
                  new FrameEvaluationEvent(cls, slot, facet, true, values),
                  clients);
            }
          }
        }
      }
      synchronized (kbLock) {
        insertEvent(new FrameEvaluationCompleted(frame), clients);
      }
    }
  }

  public void addRequest(Frame frame, RemoteSession session) {
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
      Set<RemoteSession> clients = requestMap.get(frame);
      if (clients == null) {
        clients = new HashSet<RemoteSession>();
        requestMap.put(frame, clients);
      }
      clients.add(session);
      if (innerThread == null || 
          innerThread.getStatus() == RunStatus.SHUTDOWN) {
        innerThread = new FrameCalculatorThread();
        innerThread.start();
      }
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
  private void insertEvent(ClientNotification event) {
    Frame frame = event.getFrame();
    Set<RemoteSession> clients = null;
    synchronized(requestLock) {
      clients= requestMap.get(frame);
    }
    insertEvent(event, clients);
  }
  
  private void insertEvent(ClientNotification event, Set<RemoteSession> clients) {
    event.setClients(clients);
    for (EventObject previousEvent : fs.getEvents()) {
      events.write(new ServerEventWrapper(previousEvent));
    }
    events.write(new ServerEventWrapper(event));
  }

  public boolean checkInterest(ClientNotification event,
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
  
}
