package edu.stanford.smi.protege.util.transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.stanford.smi.protege.exception.TransactionException;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.util.Log;


/**
 * This is a simple class that keeps track of running transactions and their nesting.
 * @author tredmond
 *
 */
public abstract class TransactionMonitor {
  
  private Map<RemoteSession,Integer> transactionsInProgress = new HashMap<RemoteSession, Integer>();

  public synchronized void beginTransaction() {
    RemoteSession session = ServerFrameStore.getCurrentSession();
    int nesting = getNesting();
    transactionsInProgress.put(session, nesting + 1);
  }

  public synchronized void rollbackTransaction() {
    decrementTransaction();
  }
  
  public synchronized void commitTransaction() {
    decrementTransaction();
  }
  
  private void decrementTransaction() {
    RemoteSession session = ServerFrameStore.getCurrentSession();
    int nesting = getNesting();
    if (nesting <= 0) {
      Log.getLogger().warning("Exiting a transaction when no transaction is in progress");
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
    RemoteSession session = ServerFrameStore.getCurrentSession();
    return getNesting(session);
  }
  
  public synchronized int getNesting(RemoteSession session) {
    Integer nesting = transactionsInProgress.get(session);
    if (nesting == null) {
      return 0;
    } else {
      return nesting;
    }
  }
  
  public static boolean updatesSeenByUntransactedClients(TransactionMonitor tm) {
      return tm == null || tm.updatesSeenByUntransactedClients();
  }
  
  public static boolean updatesSeenByUntransactedClients(TransactionMonitor tm,
                                                         TransactionIsolationLevel level) {
      return tm == null || tm.updatesSeenByUntransactedClients(level);
  }
  
  public boolean updatesSeenByUntransactedClients() {
      TransactionIsolationLevel level = getTransationIsolationLevel();
      return updatesSeenByUntransactedClients(level);
  }
  
  public boolean updatesSeenByUntransactedClients(TransactionIsolationLevel level) {
    return !inTransaction() || 
      (level != null && level.compareTo(TransactionIsolationLevel.READ_UNCOMMITTED) <= 0);
  }
  


  public synchronized Set<RemoteSession> getSessions() {
    return transactionsInProgress.keySet();
  }

  public abstract TransactionIsolationLevel getTransationIsolationLevel() 
  throws TransactionException;

  /**
   * Sets the transaction isolation level for the current connection.
   * 
   * @param level The desired TransactionIsolationLevel
   * @throws TransactionException 
   */
  public abstract void setTransactionIsolationLevel(TransactionIsolationLevel level)
    throws TransactionException;
}
