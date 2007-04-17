package edu.stanford.smi.protege.util.transaction;

import edu.stanford.smi.protege.util.CacheMap;

public class CacheImpl<V, R> implements Cache<V, R> {
  CacheMap<V, R> mainCache = new CacheMap<V,R>();
  
  public boolean isCached(V var) {
    return mainCache.get(var) != null;
  }
  
  public R readCache(V var) {
    return mainCache.get(var);
  }
  
  public void writeCache(V var, R result) {
    mainCache.put(var, result);
  }
  
  public void removeCacheEntry(V var) {
    mainCache.remove(var);
  }
}
