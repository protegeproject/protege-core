package edu.stanford.smi.protege.util.transaction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;

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
  private Cache<V, R> storage;
  private Map<RemoteSession, Cache<V, R>> sessionCacheMap
    = new HashMap<RemoteSession, Cache<V, R>>();
  private Map<RemoteSession, Map<V, R>> deferredWrites = new HashMap<RemoteSession, Map<V, R>>();
  private Map<RemoteSession, Set<V>> deferredInvalids = new HashMap<RemoteSession, Set<V>>();
  
  public TransactionCache(Cache<V, R> storage) {
    this.storage = storage;
  }
  
  public boolean isCached(V var) {
    TransactionIsolationLevel level = getTransactionIsolationLevel();
    RemoteSession session = getCurrentSession();
    if (isDisabled()) {
      return false;
    } else if (!inTransaction() || 
               level.compareTo(TransactionIsolationLevel.READ_UNCOMMITTED) <= 0) {
      return storage.isCached(var);
    } else if (level == TransactionIsolationLevel.READ_COMMITTED) {
      Set<V> invalids = deferredInvalids.get(session);
      boolean invalid = false;
      if (invalids != null && invalids.contains(var)) {
        invalid = true;
      }
      return (!invalid && storage.isCached(var)) || getSessionCache().isCached(var);
    } else {
      return getSessionCache().isCached(var);
    }
  }
  
  public R read(V var) {
    TransactionIsolationLevel level = getTransactionIsolationLevel();
    Cache<V,R> sessionCache = getSessionCache();
    if (!inTransaction() || 
        level.compareTo(TransactionIsolationLevel.READ_UNCOMMITTED) <= 0) {
      return storage.readCache(var);
    } else if (level == TransactionIsolationLevel.READ_COMMITTED) {
      if (sessionCache.isCached(var)) {
        return sessionCache.readCache(var);
      } else {
        return storage.readCache(var);
      }
    } else {
      return sessionCache.readCache(var);
    }
  }
  
  public void valueFromStore(V var, R result) {
    TransactionIsolationLevel level = getTransactionIsolationLevel();
    RemoteSession session = getCurrentSession();
    Cache<V,R> sessionCache = getSessionCache();
    if (isDisabled()) {
      return;
    } else if (!inTransaction() || level.compareTo(TransactionIsolationLevel.READ_UNCOMMITTED) <= 0) {
      storage.writeCache(var, result);
    } else if (level == TransactionIsolationLevel.READ_COMMITTED) {
      Set<V> invalids = deferredInvalids.get(session);
      if (!sessionCache.isCached(var) && (invalids == null || !invalids.contains(var))) {
        storage.writeCache(var, result);
      }
    } else {
      sessionCache.writeCache(var, result);
    }
  }
  
  /**
   * This method is called when the caller does an operation that changes
   * the value of var to result.  There is a completeness requirement on the 
   * caller - any time  a var is changed the caller must call either write or
   * invalidate.
   * 
   * @param var the variable that was modified.
   * @param result the value that the variable was changed to.
   */
  public void write(V var, R result) {
    TransactionIsolationLevel level = getTransactionIsolationLevel();
    Cache<V,R> sessionCache = getSessionCache();
    RemoteSession session = getCurrentSession();
    if (!inTransaction() || level.compareTo(TransactionIsolationLevel.READ_UNCOMMITTED) <= 0) {
      storage.writeCache(var, result);
    } else {
      Map<V, R> deferred = deferredWrites.get(session);
      if (deferred == null) {
        deferred = new HashMap<V, R>();
        deferredWrites.put(session, deferred);
      }
      deferred.put(var, result);
      sessionCache.writeCache(var, result);
      Set<V> invalids = deferredInvalids.get(session);
      if (invalids != null) {
        invalids.remove(var);
      }
    }
  }
  
  /**
   * This call is made when the caller changes the variable but the caller is not 
   * sure what  value the caller set.  There is a completeness requirement on the 
   * caller - any time  a var is changed the caller must call either write or
   * invalidate.
   * 
   * @param var the variable being updated.
   */
  public void invalidate(V var) {
    TransactionIsolationLevel level = getTransactionIsolationLevel();
    RemoteSession session = getCurrentSession();
    Cache<V,R> sessionCache = getSessionCache();
    if (inTransaction() && level.compareTo(TransactionIsolationLevel.READ_COMMITTED) >= 0) {
      sessionCache.removeCacheEntry(var);
      Set<V> invalids = deferredInvalids.get(session);
      if (invalids == null) {
        invalids = new  HashSet<V>();
        deferredInvalids.put(session, invalids);
      }
      invalids.add(var);
    } else {
      storage.removeCacheEntry(var);
    }
  }
  
  public void commitTransaction() {
    TransactionIsolationLevel level = getTransactionIsolationLevel();
    if (!inTransaction() && level.compareTo(TransactionIsolationLevel.READ_COMMITTED) >= 0) {
      RemoteSession session = getCurrentSession();
      
      Map<V,R> deferred = deferredWrites.get(getCurrentSession());
      deferredWrites.put(session, null);
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
      sessionCacheMap.remove(session);
    }
  }
  
  public void rollbackTransaction() {
    TransactionIsolationLevel level = getTransactionIsolationLevel();
    if (!inTransaction() && level.compareTo(TransactionIsolationLevel.READ_COMMITTED) >= 0) {
      RemoteSession session = getCurrentSession();
      deferredWrites.remove(session);
      sessionCacheMap.remove(session);
    }
  }
  

  
  private RemoteSession getCurrentSession() {
    return ServerFrameStore.getCurrentSession();
  }
  
  private Cache<V, R> getSessionCache() {
    if (!inTransaction()) {
      throw new UnsupportedOperationException("Session cache only valid during a transaction");
    }
    RemoteSession session = getCurrentSession();
    Cache<V,R> cache = sessionCacheMap.get(session);
    if (cache == null) {
      cache = createSessionCache(session);
      sessionCacheMap.put(session, cache);
    }
    return cache;
  }
  
  public abstract boolean inTransaction();
  
  public abstract TransactionIsolationLevel getTransactionIsolationLevel();
  
  public abstract boolean isDisabled();
  
  public abstract LosslessCache<V,R> createSessionCache(RemoteSession session);
}
