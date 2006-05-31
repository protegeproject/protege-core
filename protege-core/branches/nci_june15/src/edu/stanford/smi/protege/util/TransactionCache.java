package edu.stanford.smi.protege.util;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.util.exceptions.TransactionException;

public abstract class TransactionCache<V, R> {
  private boolean disabled = false;
  private TransactionMonitor transactionMonitor;
  private TransactionCacheStorage<V, R> storage;
  private TransactionIsolationLevel level;
  private Map<RemoteSession, Map<V, R>> deferredWrites = new HashMap<RemoteSession, Map<V, R>>();
  
  public TransactionCache(TransactionMonitor transactionMonitor,
                          TransactionCacheStorage<V, R> storage) {
    this.transactionMonitor = transactionMonitor;
    this.storage = storage;
  }
  
  public boolean isCached(V var) {
    TransactionIsolationLevel level = getTransactionIsolationLevel();
    if (disabled) {
      return false;
    } else if (!inTransaction() || 
               level.compareTo(TransactionIsolationLevel.READ_UNCOMMITTED) <= 0) {
      return storage.isCached(var);
    } else if (level == TransactionIsolationLevel.READ_COMMITTED) {
      return storage.isCached(var) || storage.isSessionCached(getCurrentSession(), var);
    } else {
      return storage.isSessionCached(getCurrentSession(), var);
    }
  }
  
  public R read(V var) {
    TransactionIsolationLevel level = getTransactionIsolationLevel();
    if (!inTransaction() || 
        level.compareTo(TransactionIsolationLevel.READ_UNCOMMITTED) <= 0) {
      return storage.readCache(var);
    } else if (level == TransactionIsolationLevel.READ_COMMITTED) {
      if (storage.isSessionCached(getCurrentSession(), var)) {
        return storage.readSessionCache(getCurrentSession(), var);
      } else {
        return storage.readCache(var);
      }
    } else {
      return storage.readSessionCache(getCurrentSession(), var);
    }
  }
  
  public void valueFromStore(V var, R result) {
    TransactionIsolationLevel level = getTransactionIsolationLevel();
    if (disabled) {
      return;
    } else if (!inTransaction() || level.compareTo(TransactionIsolationLevel.READ_UNCOMMITTED) <= 0) {
      storage.writeCache(var, result);
    } else if (level == TransactionIsolationLevel.READ_COMMITTED) {
      if (!storage.isSessionCached(getCurrentSession(), var)) {
        storage.writeCache(var, result);
      }
    } else {
      storage.writeSessionCache(getCurrentSession(), var, result);
    }
  }
  
  public void write(V var, R result) {
    TransactionIsolationLevel level = getTransactionIsolationLevel();
    if (!inTransaction() || level.compareTo(TransactionIsolationLevel.READ_UNCOMMITTED) <= 0) {
      storage.writeCache(var, result);
    } else {
      Map<V, R> deferred = deferredWrites.get(getCurrentSession());
      if (deferred == null) {
        deferred = new HashMap<V, R>();
        deferredWrites.put(getCurrentSession(), deferred);
      }
      deferred.put(var, result);
      storage.writeSessionCache(getCurrentSession(), var, result);
    }
  }
  
  public void invalidate(V var) {
    TransactionIsolationLevel level = getTransactionIsolationLevel();
    storage.invalidateCache(var);
    if (inTransaction() && level.compareTo(TransactionIsolationLevel.READ_COMMITTED) >= 0) {
      Map<V, R> deferred = deferredWrites.get(getCurrentSession());
      if (deferred != null) {
        deferred.remove(var);
      }
      storage.invalidateSessionCache(getCurrentSession(), var);
    }
  }
  
  public void commitTransaction() {
    TransactionIsolationLevel level = getTransactionIsolationLevel();
    if (!inTransaction() && level.compareTo(TransactionIsolationLevel.READ_COMMITTED) >= 0) {
      Map<V,R> deferred = deferredWrites.get(getCurrentSession());
      deferredWrites.put(getCurrentSession(), null);
      if (deferred != null) {
        for (Map.Entry<V,R> entry :  deferred.entrySet()) {
          V var = entry.getKey();
          R result = entry.getValue();
          storage.writeCache(var, result);
        }
      }
      storage.clearSessionCache(getCurrentSession());
    }
  }
  
  public void rollbackTransaction() {
    if (!inTransaction() && level.compareTo(TransactionIsolationLevel.READ_COMMITTED) >= 0) {
      deferredWrites.put(getCurrentSession(), null);
      storage.clearSessionCache(getCurrentSession());
    }
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
  
  private RemoteSession getCurrentSession() {
    return ServerFrameStore.getCurrentSession();
  }
  
  private boolean inTransaction() {
    return transactionMonitor != null && transactionMonitor.inTransaction();
  }
  

}
