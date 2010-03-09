package edu.stanford.smi.protege.util.transaction.cache.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.transaction.cache.Cache;
import edu.stanford.smi.protege.util.transaction.cache.CacheResult;

/**
 * This is a basic cache that takes the behavior of its delegate.  But it
 * ignores the impact of transactions on the cache and assumes that its 
 * delegate does the same.  
 *  
 * It will only return 
 * valid results in the case that the transaction isolation level is NONE or where
 * the caller covers the in transaction case.
 * 
 * @author tredmond
 *
 */
public class CompleteableCache<S, V, R> implements Cache<S, V, R> {
    enum CompletionStatus {
        NORMAL, GETTING_COMPLETE_CACHE, CACHE_COMPLETE;
    };
    
    private Logger logger = Log.getLogger(CompleteableCache.class);
    
    private CompletionStatus status = CompletionStatus.NORMAL;
    private Set<V> invalidReads = new HashSet<V>();
    private Cache<S, V, R> delegate;
    
    public CompleteableCache(Cache<S, V, R> delegate) {
        this.delegate = delegate;
    }
    
    public CacheResult<R> readCache(S session, V var) {
        CacheResult<R> result = delegate.readCache(session, var);
        if (result.getResult() == null &&
                status == CompletionStatus.CACHE_COMPLETE && 
                !invalidReads.contains(var) && !result.isValid()) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Cache " + getCacheId() + " is complete - null is valid.");
            }
            return new CacheResult<R>(null, true);
        }
        return result;
    }

    public void updateCache(S session, V var) {
        if (status != CompletionStatus.NORMAL) {
            invalidReads.add(var);
        }
        delegate.updateCache(session, var);
    }

    public void updateCache(S session, V var, R value) {
        if (status != CompletionStatus.NORMAL) {
            invalidReads.remove(var);
        }
        delegate.updateCache(session, var, value);
    }

    public void modifyCache(S session, V var) {
        if (status != CompletionStatus.NORMAL) {
            invalidReads.add(var);
        }
        delegate.modifyCache(session, var);
    }

    public void modifyCache(S session, V var, R value) {
        if (status != CompletionStatus.NORMAL) {
            invalidReads.remove(var);
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
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Cache "  + getCacheId() + " starting to complete the cache");
        }
        status = CompletionStatus.GETTING_COMPLETE_CACHE;
        invalidReads = new HashSet<V>();
        delegate.startCompleteCache();
    }

    public void finishCompleteCache() {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Cache "  + getCacheId() + " cache completed");
        }
        if (status == CompletionStatus.GETTING_COMPLETE_CACHE) {
            status = CompletionStatus.CACHE_COMPLETE;
        }
        delegate.finishCompleteCache();
    }
    
    public void abortCompleteCache() {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Cache "  + getCacheId() + " caching aborted");
        }
        status = CompletionStatus.NORMAL;
        invalidReads.clear();
        delegate.abortCompleteCache();
    }
    
    public boolean isCacheComplete() {
        return status == CompletionStatus.CACHE_COMPLETE;
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

    public void flush() {
        localFlush();
        delegate.flush();
    }
    
    private void localFlush() {
        status = CompletionStatus.NORMAL;
        invalidReads.clear();
    }
    
    public int getCacheId() {
        return delegate.getCacheId();
    }


}
