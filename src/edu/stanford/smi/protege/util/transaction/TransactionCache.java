package edu.stanford.smi.protege.util.transaction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.exceptions.TransactionException;

/**
 * This class manages caches in the presence of transactions.  Transactions significantly 
 * complicate cache processing and it is very difficult to impossible to predict exactly what
 * scheduling decisions the database will make when transactions are on.
 * 
 * @author tredmond
 *
 * @param <V>
 * @param <R>
 */

public abstract class TransactionCache<V, R> {
  private TransactionCacheStorage<V, R> storage;
  private Map<RemoteSession, Map<V, R>> deferredWrites = new HashMap<RemoteSession, Map<V, R>>();
  private Map<RemoteSession, Set<V>> deferredInvalids = new HashMap<RemoteSession, Set<V>>();
  
  public TransactionCache(TransactionCacheStorage<V, R> storage) {
    this.storage = storage;
  }
  
  public boolean isCached(V var) {
    TransactionIsolationLevel level = getTransactionIsolationLevel();
    if (isDisabled()) {
      return false;
    } else if (!inTransaction() || 
               level.compareTo(TransactionIsolationLevel.READ_UNCOMMITTED) <= 0) {
      return storage.isCached(var);
    } else if (level.compareTo(TransactionIsolationLevel.REPEATABLE_READ) <= 0) {
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
      } else if (level == TransactionIsolationLevel.REPEATABLE_READ) {
        if (storage.isSessionCached(getCurrentSession(),  var)) {
          return storage.readSessionCache(getCurrentSession(), var);
        } else {
          /* 
           * A tricky optimization - I am out of synch with the database
           * but this is ok because I am implementing repeatable read.
           */
          R result = storage.readCache(var);
          storage.writeSessionCache(getCurrentSession(), var, result);
          return result;
        }
      } else {
        return storage.readCache(var);
      }
    } else {
      return storage.readSessionCache(getCurrentSession(), var);
    }
  }
  
  public void valueFromStore(V var, R result) {
    TransactionIsolationLevel level = getTransactionIsolationLevel();
    if (isDisabled()) {
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
    if (inTransaction() && level.compareTo(TransactionIsolationLevel.READ_COMMITTED) >= 0) {
      storage.removeSessionCacheEntry(getCurrentSession(), var);
      Set<V> invalids = deferredInvalids.get(getCurrentSession());
      if (invalids == null) {
        invalids = new  HashSet<V>();
        deferredInvalids.put(getCurrentSession(), invalids);
      }
      invalids.add(var);
    } else {
      storage.removeCacheEntry(var);
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
      Set<V> invalids = deferredInvalids.get(getCurrentSession());
      if (invalids != null) {
        for (V var : invalids) {
          storage.removeCacheEntry(var);
        }
      }
      storage.clearSessionCache(getCurrentSession());
    }
  }
  
  public void rollbackTransaction() {
    TransactionIsolationLevel level = getTransactionIsolationLevel();
    if (!inTransaction() && level.compareTo(TransactionIsolationLevel.READ_COMMITTED) >= 0) {
      deferredWrites.put(getCurrentSession(), null);
      storage.clearSessionCache(getCurrentSession());
    }
  }
  

  
  private RemoteSession getCurrentSession() {
    return ServerFrameStore.getCurrentSession();
  }
  
  public abstract boolean inTransaction();
  
  public abstract TransactionIsolationLevel getTransactionIsolationLevel();
  
  public abstract boolean isDisabled();
}
