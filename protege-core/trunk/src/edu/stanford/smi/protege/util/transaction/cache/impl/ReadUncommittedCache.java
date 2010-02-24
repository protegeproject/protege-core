package edu.stanford.smi.protege.util.transaction.cache.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.stanford.smi.protege.util.transaction.cache.Cache;
import edu.stanford.smi.protege.util.transaction.cache.CacheResult;

public class ReadUncommittedCache<S, V, R> implements Cache<S, V, R> {
    private Map<S, Set<V>> transactedModifications = new HashMap<S, Set<V>>();
    private Cache<S, V, R> delegate;
    
    public ReadUncommittedCache(Cache<S, V, R> delegate) {
        this.delegate = delegate;
    }

    public CacheResult<R> readCache(S session, V var) {
        return delegate.readCache(session, var);
    }

    public void updateCache(S session, V var) {
        delegate.updateCache(session, var);
    }

    public void updateCache(S session, V var, R value) {
        delegate.updateCache(session, var, value);
    }

    public void modifyCache(S session, V var) {
        if (delegate.getTransactionNesting(session) > 0) {
            transactedModifications.get(session).add(var);
        }
        delegate.modifyCache(session, var);
    }

    public void modifyCache(S session, V var, R value) {
        if (delegate.getTransactionNesting(session) > 0) {
            transactedModifications.get(session).add(var);
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
        if (delegate.getTransactionNesting(session) == 0) {
            transactedModifications.put(session, new HashSet<V>());
        }
        delegate.beginTransaction(session);
    }

    public void commitTransaction(S session) {
        delegate.commitTransaction(session);
        if (delegate.getTransactionNesting(session) == 0) {
            transactedModifications.remove(session);
        }
    }

    public void rollbackTransaction(S session) {
        delegate.rollbackTransaction(session);
        for (V var : transactedModifications.get(session)) {
            delegate.modifyCache(session, var);
        }
        if (delegate.getTransactionNesting(session) == 0) {
            transactedModifications.remove(session);
        }
        else {
            transactedModifications.get(session).clear();
        }
    }

    public int getTransactionNesting(S session) {
        return delegate.getTransactionNesting(session);
    }
    
    public void flush() {
        myFlush();
        delegate.flush();
    }
    
    private void myFlush() {
        transactedModifications.clear();
    }
    
    public int getCacheId() {
        return delegate.getCacheId();
    }

}
