package edu.stanford.smi.protege.util;

import java.util.HashMap;
import java.util.Map;

import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;


/**
 * This is a simple class that keeps track of running transactions and their nesting.
 * @author tredmond
 *
 */
public class TransactionMonitor {
  
  private Map<RemoteSession,Integer> transactionsInProgress = new HashMap<RemoteSession, Integer>();
  /*
   * Using an internal lock like this is simpler but using the knowledge base lock might be more efficient?
   */
  private Object lock;
  
  public TransactionMonitor() {
    lock = new Object();
  }
  
  public TransactionMonitor(Object lock) {
    this.lock = lock;
  }

  public void beginTransaction(String name) {
    synchronized (lock) {
      RemoteSession session = ServerFrameStore.getCurrentSession();
      Integer nesting = transactionsInProgress.get(session);
      if (nesting == null) {
        transactionsInProgress.put(session, 1);
      } else {
        transactionsInProgress.put(session, nesting + 1);
      }
    }
  }

  public void rollbackTransaction() {
    decrementTransaction();
  }
  
  public void commitTransaction() {
    decrementTransaction();
  }
  
  private void decrementTransaction() {
    synchronized (lock) {
      RemoteSession session = ServerFrameStore.getCurrentSession();
      Integer nesting = transactionsInProgress.get(session);
      if (nesting <= 0) {
        throw new RuntimeException("Programming error...");
      } else if (nesting == 1) {
        transactionsInProgress.remove(session);
        if (!existsTransaction()) {
          lock.notifyAll();
        }
      } else {
        transactionsInProgress.put(session, nesting - 1);
      }   
    }
  }
  
  public boolean inTransaction() {
    synchronized (lock) {
      RemoteSession session = ServerFrameStore.getCurrentSession();
      Integer nesting = transactionsInProgress.get(session);
      return nesting != null;
    }
  }
  
  public boolean existsTransaction() {
    synchronized (lock) {
      return !transactionsInProgress.isEmpty();
    }
  }
  
  public void waitForTransactionsToComplete() {
    synchronized (lock) {
      while (existsTransaction()) {
        try {
          lock.wait();
        } catch (InterruptedException e) {
          Log.getLogger().severe("Error waiting for kbLock");
        }
      }
    }
  }
}
