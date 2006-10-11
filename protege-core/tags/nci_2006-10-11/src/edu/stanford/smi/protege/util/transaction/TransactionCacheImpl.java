package edu.stanford.smi.protege.util.transaction;

import java.util.logging.Level;

import edu.stanford.smi.protege.exception.TransactionException;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.util.Log;

public class TransactionCacheImpl<V, R> extends TransactionCache<V, R> {
  private TransactionMonitor transactionMonitor;
  private TransactionIsolationLevel level;
  private boolean disabled = false;     
  
  public TransactionCacheImpl(TransactionMonitor transactionMonitor) {
    super(new CacheImpl<V,R>());
    this.transactionMonitor = transactionMonitor;
  }
  
  public boolean inTransaction() {
    return transactionMonitor != null && transactionMonitor.inTransaction();
  }
  
  public TransactionIsolationLevel getTransactionIsolationLevel() {
    if (level == null) {
      if (transactionMonitor == null) {
        level = TransactionIsolationLevel.NONE;
      }
      try {
        level = transactionMonitor.getTransationIsolationLevel();
      } catch (TransactionException e) {
        disabled = true;
        Log.getLogger().log(Level.WARNING, "Could not determine trahsaction isolation level", e);
        Log.getLogger().warning("Caching disabled");
      }
    }
    return level;
  }
  
  public void resetTransactionIsolationLevel() {
    level = null;
  }
  
  public boolean isDisabled() {
    return disabled;
  }

  @Override
  public LosslessCache createSessionCache(RemoteSession session) {
    return new LosslessCacheImpl();
  }
  
}

