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
  private Set<Frame> frames = new HashSet<Frame>();
  private Object lock = new Object();
  private RequestorThread th = new RequestorThread();
  private RemoteServerFrameStore delegate;
  private RemoteSession session;
  
  public ClientCacheRequestor(RemoteServerFrameStore delegate, RemoteSession session) {
    this.delegate = delegate;
  }
  
  public void requestFrameValues(Set<Frame> frames) {
    synchronized (lock) {
      this.frames.addAll(frames);
      switch (th.getStatus()) {
      case IDLE:
        th.start();
        break;
      case RUNNING:
        break;
      case SHUTDOWN:
        th = new RequestorThread();
        th.start();
        break;
      default:
        Log.getLogger().severe("Programmer error in ClientCacheRequestor - behavior is still correct but degraded");
      }
    }
  }
  
  public class RequestorThread extends Thread {
    private ThreadStatus status = ThreadStatus.IDLE;
    
    public void run() {
      status = ThreadStatus.RUNNING;
      while (true) {
        Set<Frame> workingFrames = null;
        synchronized(lock) {
          if (frames.isEmpty()) {
            status = ThreadStatus.SHUTDOWN;
            return;
          } else {
            workingFrames = frames;
            frames = new HashSet();
          }
        }
        try {
          if (log.isLoggable(Level.FINE)) {
            log.fine("Sending frames " + workingFrames);
          }
          delegate.requestValueCache(workingFrames, session);
        } catch (Exception e) {
          Log.emptyCatchBlock(e);
        }
      }   
    }
    
    public ThreadStatus getStatus() {
      return status;
    }
  }

}
