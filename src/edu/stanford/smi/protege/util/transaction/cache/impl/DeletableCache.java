package edu.stanford.smi.protege.util.transaction.cache.impl;

import java.util.HashSet;
import java.util.Set;

import edu.stanford.smi.protege.util.transaction.cache.Cache;
import edu.stanford.smi.protege.util.transaction.cache.CacheResult;

public class DeletableCache<S, V, R> implements Cache<S, V, R> {
    private boolean ignoreTransactions;
    private Set<S> sessionsWithCacheDeleted = new HashSet<S>();
    private boolean invalid = false;
    private Cache<S, V, R> delegate;
    
    public DeletableCache(Cache<S, V, R> delegate, boolean ignoreTransactions) {
        this.delegate = delegate;
        this.ignoreTransactions = ignoreTransactions;
    }
    
    private boolean isInvalid(S session) {
        return invalid || sessionsWithCacheDeleted.contains(session);
    }
    
    public CacheResult<R> readCache(S session, V var) {
        if (isInvalid(session)) {
            return new CacheResult<R>(null, false);
        }
        return delegate.readCache(session, var);
    }
    
    public void updateCache(S session, V var) {
        if (isInvalid(session)) {
            return;
        }
        delegate.updateCache(session, var);
    }

    public void updateCache(S session, V var, R value) {
        if (isInvalid(session)) {
            return;
        }
        delegate.updateCache(session, var, value);
    }

    public void modifyCache(S session, V var) {
        if (isInvalid(session)) {
            return;
        }
        delegate.modifyCache(session, var);
    }

    public void modifyCache(S session, V var, R value) {
        if (isInvalid(session)) {
            return;
        }
        delegate.modifyCache(session, var, value);
    }
    
    public void delete(S session) {
        if (invalid) {
            return;
        }
        else if (!ignoreTransactions && delegate.getTransactionNesting(session) > 0) {
            sessionsWithCacheDeleted.add(session);
        }
        else {
            invalid = true;
            delegate = null;
        }
    }
    
    public boolean isDeleted() {
        return invalid;
    }

    public void startCompleteCache() {
        if (invalid) {
           return; 
        }
        delegate.startCompleteCache();
    }

    public void finishCompleteCache() {
        if (invalid) {
            return;
        }
        delegate.finishCompleteCache();
    }
    
    public void abortCompleteCache() {
        if (invalid) {
            return;
        }
        delegate.abortCompleteCache();
    }
    
    public boolean isCacheComplete() {
        if (invalid) {
            return false;
        }
        return delegate.isCacheComplete();
    }

    public void beginTransaction(S session) {
        if (invalid) {
            return;
        }
        delegate.beginTransaction(session);
    }

    public void commitTransaction(S session) {
        if (invalid) {
            return;
        }
        if (getTransactionNesting(session) < 0) {
            localFlush();
        }
        delegate.commitTransaction(session);
        if (!ignoreTransactions 
                && delegate.getTransactionNesting(session) == 0 
                && sessionsWithCacheDeleted.contains(session)) {
            invalid = true;
            delegate = null;
            sessionsWithCacheDeleted.clear();
        }
    }

    public void rollbackTransaction(S session) {
        if (invalid) {
            return;
        }
        if (getTransactionNesting(session) < 0) {
            localFlush();
        }
        delegate.rollbackTransaction(session);
        if (!ignoreTransactions && delegate.getTransactionNesting(session) == 0) {
            sessionsWithCacheDeleted.remove(session);
        }
    }

    public int getTransactionNesting(S session) {
        if (isInvalid(session)) {
            return 0;
        }
        return delegate.getTransactionNesting(session);
    }


    public void flush() {
        if (invalid) {
            return;
        }
        localFlush();
        delegate.flush();
    }
    
    private void localFlush() {
        sessionsWithCacheDeleted.clear();
        invalid = false;
    }
    
}
