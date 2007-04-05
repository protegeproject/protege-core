package edu.stanford.smi.protege.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.util.Log;

public class ServerCacheManager {
  private static  transient Logger log = Log.getLogger(ServerCacheManager.class);
  
  private List<Frame> requests = new ArrayList<Frame>();
  
  public enum State {
    idle, preCalculatingFrameValues, userRequestWaiting, userRequestActive
  };
  public State cacheManagerState = State.idle;
  Object stateLock = new Object();
  Object requestLock = new Object();
  
  public void startUserRequest() {
    synchronized (stateLock) {
      switch (cacheManagerState) {
      case idle:
        cacheManagerState = State.userRequestActive;
        return;
      case preCalculatingFrameValues:
        cacheManagerState = State.userRequestWaiting;
        try {
          stateLock.wait();
        } catch (InterruptedException interrupt) {
          RuntimeException re = new RuntimeException("Server got interrupted");
          re.initCause(interrupt);
          throw re;
        }
        cacheManagerState = State.userRequestActive;
        break;
      case userRequestWaiting:
      case userRequestActive:
        throw new RuntimeException("Programming error in cache manager - two simultaneous active user requests");
      default:
        throw new RuntimeException("Unhandled cache manager state = " + cacheManagerState);
      }
    }
    if (log.isLoggable(Level.FINE)) {
      log.fine("waiting for cache thread to finish");
    }
      
  }
  
  public void finishUserRequest() {
    synchronized (stateLock) {
      assert(cacheManagerState == State.userRequestActive);
      if (requests.isEmpty()) {
        cacheManagerState = State.idle;
      } else {
        stateLock.notify();
      }  
    }
  }
  
  public boolean pendingRequests() {
    synchronized (requestLock) {
      return requests.isEmpty();
    }
  }
  
  public Frame getRequest() {
    synchronized (requestLock) {
      Frame request = requests.get(requests.size() - 1);
      requests.remove(request);
      return request;
    }
  }
  
 
  private class FrameValuePreFetch extends Thread {
    public void run() {
      while (!requests.isEmpty()) {
        synchronized(stateLock) {
          
        }
        ;
      }
      synchronized (stateLock) {
        switch (cacheManagerState) {
        case preCalculatingFrameValues:
          cacheManagerState = ServerCacheManager.State.idle;
        case userRequestWaiting:
          stateLock.notify();
          break;
        case userRequestActive:
        case idle:
          log.severe("Server Cache Manager in invalid state = " + cacheManagerState);
          log.severe("Frame Value pre-fetch should be active");
          break;
        default:
          log.severe("Unknown Server Cache Manager State: " + cacheManagerState);
        }
      }
      
    }
  }
  
}
