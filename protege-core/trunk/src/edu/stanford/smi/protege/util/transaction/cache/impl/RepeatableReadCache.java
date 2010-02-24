package edu.stanford.smi.protege.util.transaction.cache.impl;

import java.util.HashMap;
import java.util.Map;

import edu.stanford.smi.protege.util.transaction.cache.Cache;
import edu.stanford.smi.protege.util.transaction.cache.CacheResult;

public class RepeatableReadCache<S, V, R> implements Cache<S, V, R> {
    private Map<S, Map<V, CacheResult<R>>> repeatableReadCacheMap = new HashMap<S, Map<V, CacheResult<R>>>();
    private Cache<S, V, R> delegate;
    
    public RepeatableReadCache(Cache<S, V, R> delegate) {
        this.delegate = delegate;
    }

    public CacheResult<R> readCache(S session, V var) {
        Map<V, CacheResult<R>> repeatableReadCache = repeatableReadCacheMap.get(session); 
        if (repeatableReadCache == null) {
            return delegate.readCache(session, var); 
        }
        else if (!repeatableReadCache.containsKey(var)) {
            return new CacheResult<R>(null, false);
        }
        else {
            return repeatableReadCache.get(var);
        }
    }

    public void updateCache(S session, V var) { 
        if (getTransactionNesting(session) == 0) {
            delegate.updateCache(session, var);
        }
    }

    public void updateCache(S session, V var, R value) {
        Map<V, CacheResult<R>> repeatableReadCache = repeatableReadCacheMap.get(session);  
        if (repeatableReadCache != null) {
            repeatableReadCache.put(var, new CacheResult<R>(value, true));
        }
        else {
            delegate.updateCache(session, var, value);
        }
    }

    public void modifyCache(S session, V var) {
        Map<V, CacheResult<R>> repeatableReadCache = repeatableReadCacheMap.get(session);  
        if (repeatableReadCache != null) {
            repeatableReadCache.remove(var);
        }
        delegate.modifyCache(session, var);
    }

    public void modifyCache(S session, V var, R value) {
        Map<V, CacheResult<R>> repeatableReadCache = repeatableReadCacheMap.get(session);  
        if (repeatableReadCache != null) {
            repeatableReadCache.put(var, new CacheResult<R>(value, true));
        }
        delegate.modifyCache(session, var, value);
    }

    public void invalidate(S session) {
        delegate.invalidate(session);
    }
    
    public boolean isInvalid() {
        return delegate.isInvalid();
    }

    public void startCompleteCache() {
        delegate.startCompleteCache();
    }

    public void finishCompleteCache() {
        delegate.finishCompleteCache();
    }
    
    public void abortCompleteCache() {
        delegate.abortCompleteCache();
    }
    
    public boolean isCacheComplete() {
        return delegate.isCacheComplete();
    }

    public void beginTransaction(S session) {
        if (getTransactionNesting(session) == 0) {
            repeatableReadCacheMap.put(session, new HashMap<V, CacheResult<R>>());
        }
        delegate.beginTransaction(session);
    }

    public void commitTransaction(S session) {
        delegate.commitTransaction(session);
        decrementTransaction(session);
    }

    public void rollbackTransaction(S session) {
        delegate.rollbackTransaction(session);
        decrementTransaction(session);
    }
   

    public int getTransactionNesting(S session) {
        return delegate.getTransactionNesting(session);
    }

    public void flush() {
        repeatableReadCacheMap.clear();
        delegate.flush();
    }
    
    private void decrementTransaction(S session) {
        if (getTransactionNesting(session) == 0) {
            repeatableReadCacheMap.remove(session);
        }
    }
    
    public int getCacheId() {
        return delegate.getCacheId();
    }
}
