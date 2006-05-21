package edu.stanford.smi.protege.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.util.exceptions.TransactionException;


/**
 * This is a simple class that keeps track of running transactions and their nesting.
 * @author tredmond
 *
 */
public abstract class TransactionMonitor {
  
  private Map<RemoteSession,Integer> transactionsInProgress = new HashMap<RemoteSession, Integer>();

  public synchronized void beginTransaction() {
    RemoteSession session = ServerFrameStore.getCurrentSession();
    Integer nesting = transactionsInProgress.get(session);
    if (nesting == null) {
      transactionsInProgress.put(session, 1);
    } else {
      transactionsInProgress.put(session, nesting + 1);
    }
  }

  public synchronized void rollbackTransaction() {
    decrementTransaction();
  }
  
  public synchronized void commitTransaction() {
    decrementTransaction();
  }
  
  private void decrementTransaction() {
    RemoteSession session = ServerFrameStore.getCurrentSession();
    Integer nesting = transactionsInProgress.get(session);
    if (nesting <= 0) {
      throw new RuntimeException("Programming error...");
    } else if (nesting == 1) {
      transactionsInProgress.remove(session);
    } else {
      transactionsInProgress.put(session, nesting - 1);
    }   
  }
  
  public synchronized boolean inTransaction() {
    RemoteSession session = ServerFrameStore.getCurrentSession();
    Integer nesting = transactionsInProgress.get(session);
    return nesting != null;
  }
  
  public synchronized boolean existsTransaction() {
    return !transactionsInProgress.isEmpty();
  }
  
  public synchronized boolean exclusiveTransaction() {
    RemoteSession mySession = ServerFrameStore.getCurrentSession();
    for (RemoteSession session : transactionsInProgress.keySet()) {
      int nesting = transactionsInProgress.get(session);
      if (session.equals(mySession) && nesting <= 0) {
        return false;
      }
      if (!session.equals(mySession) && nesting > 0) {
        return false;
      }
    }
    return true;
  }

  public synchronized int getNesting() {
    return transactionsInProgress.get(ServerFrameStore.getCurrentSession());
  }


  public abstract TransactionIsolationLevel getTransationIsolationLevel() 
  throws TransactionException;
  public abstract void setTransactionIsolationLevel(TransactionIsolationLevel level)
  throws TransactionException;
}
