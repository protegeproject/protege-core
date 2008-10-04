package edu.stanford.smi.protege.util.transaction.cache.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import edu.stanford.smi.protege.util.transaction.cache.Cache;
import edu.stanford.smi.protege.util.transaction.cache.CacheResult;

/**
 * This is a basic cache that takes the behavior of its delegate.  But it
 * ignores the impact of transactions on the cache and assumes that its 
 * delegate does the same.  
 *  
 * It will only return 
 * valid results in the case that the transaction isolation level is READ_UNCOMMITTED.
 * 
 * @author tredmond
 *
 */
public class CompleteableCache<S, V, R> implements Cache<S, V, R> {
    enum CompletionStatus {
        NORMAL, GETTING_COMPLETE_CACHE, CACHE_COMPLETE;
    };
    
    private CompletionStatus status = CompletionStatus.NORMAL;
    private Set<V> invalidReads = new HashSet<V>();
    private Cache<S, V, R> delegate;
    
    public CompleteableCache(Cache<S, V, R> delegate) {
        this.delegate = delegate;
    }
    
    public CacheResult<R> readCache(S session, V var) {
        CacheResult<R> result = delegate.readCache(session, var);
        if (status == CompletionStatus.CACHE_COMPLETE && !result.isValid() && !invalidReads.contains(var)) {
            return new CacheResult<R>(null, true);
        }
        return result;
    }

    public void updateCache(S session, V var) {
        invalidReads.add(var);
        delegate.updateCache(session, var);
    }

    public void updateCache(S session, V var, R value) {
        invalidReads.remove(var);
        delegate.updateCache(session, var, value);
    }

    public void modifyCache(S session, V var) {
        invalidReads.add(var);
        delegate.modifyCache(session, var);
    }

    public void modifyCache(S session, V var, R value) {
        invalidReads.remove(var);
        delegate.modifyCache(session, var, value);
    }
    
    public void delete(S session) {
        delegate.delete(session);
    }

    public void startCompleteCache() {
        status = CompletionStatus.GETTING_COMPLETE_CACHE;
    }

    public void finishCompleteCache() {
        status = CompletionStatus.CACHE_COMPLETE;
    }

    public void beginTransaction(S session) {
        delegate.beginTransaction(session);
    }

    public void commitTransaction(S session) {
        delegate.commitTransaction(session);
    }
    
    public void rollbackTransaction(S session) {
        delegate.rollbackTransaction(session);
    }
    
    public int getTransactionNesting(S session) {
        return delegate.getTransactionNesting(session);
    }
}
