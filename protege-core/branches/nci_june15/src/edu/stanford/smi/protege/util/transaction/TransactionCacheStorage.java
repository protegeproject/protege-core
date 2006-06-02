package edu.stanford.smi.protege.util.transaction;

import edu.stanford.smi.protege.server.RemoteSession;

/**
 * This interface represents a simple cache mechanism for storing cached  
 * values either globally or on a per session basis.  This cache mechanism is 
 * caching the values (of type R) of a variable (of type V).
 * 
 * The global cache is to be interpreted as the latest data from the database.
 * The session cache is a cache of data as found in the a transactionn for a session.
 * 
 * @author tredmond
 *
 * @param <V> the variable type
 * @param <R> the result set type
 */
public interface TransactionCacheStorage<V, R> {
 
  /**
   * determines if the variable var is cached in the global cache.
   * isCached(var) should always be checked before calling read(var).
   * 
   * @param var a variable
   * @return whether the variable is cached.
   */
  boolean isCached(V var);
  
  /**
   * If  the variable var is cached then this returns the cached
   * value.  It can be null.
   * 
   * @param var a variable
   * @return the value in the cache for the variable var.
   */
  R readCache(V var);
  
  /**
   * Writes the value result to the global cache for the variable 
   * var.
   * 
   * @param var a variable
   * @param result the cached value for the variable.
   */
  void writeCache(V var, R result);
  
  /**
   * Invalidates the global cache  for the variable  var.
   * 
   * @param var a variable
   */
  void removeCacheEntry(V var);
  
  
  boolean isSessionCached(RemoteSession session, V var);
  
  R readSessionCache(RemoteSession session, V var);
  
  void clearSessionCache(RemoteSession session);
  
  void removeSessionCacheEntry(RemoteSession session, V var);
  
  void writeSessionCache(RemoteSession session, V var, R result);

}
