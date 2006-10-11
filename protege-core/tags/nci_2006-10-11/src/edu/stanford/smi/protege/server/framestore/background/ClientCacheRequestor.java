package edu.stanford.smi.protege.server.framestore.background;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.framestore.RemoteServerFrameStore;
import edu.stanford.smi.protege.util.Log;

public class ClientCacheRequestor {
  Logger log = Log.getLogger(ClientCacheRequestor.class);
  
  public enum ThreadStatus {
    IDLE, RUNNING, SHUTDOWN
  }
  private Set<Frame> frames  = new HashSet<Frame>();
  private Set<Frame> framesWithDirectInstances = new HashSet<Frame>();
  private Object lock = new Object();
  private RequestorThread th = null;
  private RemoteServerFrameStore delegate;
  private RemoteSession session;
  
  public ClientCacheRequestor(RemoteServerFrameStore delegate, RemoteSession session) {
    this.delegate = delegate;
    this.session = session;
  }
  
  public void requestFrameValues(Set<Frame> frames, boolean skipDirectInstances) {
    synchronized (lock) {
      if (skipDirectInstances)  {
        this.frames.addAll(frames);
      } else {
        framesWithDirectInstances.addAll(frames);
      }
      if (th == null || th.getStatus() == ThreadStatus.SHUTDOWN) {
        th = new RequestorThread();
        th.start();
      }
    }
  }
  
  public class RequestorThread extends Thread {
    private ThreadStatus status = ThreadStatus.IDLE;
    
    public void run() {
      synchronized (lock) {
        status = ThreadStatus.RUNNING;
      }
      while (true) {
        Set<Frame> workingFrames = null;
        Set<Frame> workingFramesWithDirectInstances = null;
        synchronized(lock) {
          if (frames.isEmpty() && framesWithDirectInstances.isEmpty()) {
            status = ThreadStatus.SHUTDOWN;
            return;
          } 
          if (!frames.isEmpty()) {
            workingFrames = frames;
            frames = new HashSet<Frame>();
          }
          if (!framesWithDirectInstances.isEmpty()) {
            workingFramesWithDirectInstances = framesWithDirectInstances;
            framesWithDirectInstances = new HashSet<Frame>();
          }
        }
        try {
          if (log.isLoggable(Level.FINE)) {
            log.fine("Sending frames " + workingFrames + " / " + workingFramesWithDirectInstances);
          }
          if (workingFrames != null) {
            delegate.requestValueCache(workingFrames, true, session);
          }
          if (workingFramesWithDirectInstances != null) {
            delegate.requestValueCache(workingFramesWithDirectInstances, false, session);
          }
          
        } catch (Exception e) {
          Log.emptyCatchBlock(e);
        }
      }   
    }
    
    public ThreadStatus getStatus() {
      synchronized (lock) {
        return status;
      }
    }
  }

}
