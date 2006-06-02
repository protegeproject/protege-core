package edu.stanford.smi.protege.util.transaction;

import java.util.HashMap;
import java.util.Map;

import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.util.CacheMap;

public class BasicCacheStorage<V, R> implements TransactionCacheStorage<V, R> {
  CacheMap<V, R> mainCache = new CacheMap<V,R>();
  Map<RemoteSession, Map<V,R>> sessionCache = new HashMap<RemoteSession, Map<V, R>>();
  
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
  
  public boolean isSessionCached(RemoteSession session, V var) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }
  
  public R readSessionCache(RemoteSession session, V var) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }
  
  public void clearSessionCache(RemoteSession session) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }
  
  public void removeSessionCacheEntry(RemoteSession session, V var) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }
  
  public void writeSessionCache(RemoteSession session, V var, R result) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Not implemented yet");
  }
  
}
