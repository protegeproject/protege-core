package edu.stanford.smi.protege.util.transaction.cache.impl;

import java.lang.ref.SoftReference;

import edu.stanford.smi.protege.util.transaction.cache.Cache;
import edu.stanford.smi.protege.util.transaction.cache.CacheResult;

public class SoftCache<S, V, R> implements Cache<S, V, R> {
    private Cache<S, V, SoftReference<R>> delegate;
    
    public SoftCache(Cache<S, V, SoftReference<R>> delegate) {
        this.delegate = delegate;
    }
    
    private SoftReference<R> encode(R result) {
        if (result == null) {
            return null;
        }
        else {
            return new SoftReference<R>(result);
        }
    }

    public void abortCompleteCache() {
        delegate.abortCompleteCache();
    }

    public void beginTransaction(S session) {
        delegate.beginTransaction(session);
    }

    public void commitTransaction(S session) {
        delegate.commitTransaction(session);
    }

    public void delete(S session) {
        delegate.delete(session);
    }

    public void finishCompleteCache() {
        delegate.finishCompleteCache();
    }

    public void flush() {
        delegate.flush();
    }

    public int getCacheId() {
        return delegate.getCacheId();
    }

    public int getTransactionNesting(S session) {
        return  delegate.getTransactionNesting(session);
    }

    public boolean isCacheComplete() {
        return delegate.isCacheComplete();
    }

    public boolean isDeleted() {
        return delegate.isDeleted();
    }

    public void modifyCache(S session, V var) {
        delegate.modifyCache(session, var);
    }

    public void modifyCache(S session, V var, R value) {
        delegate.modifyCache(session, var, encode(value));
    }

    public CacheResult<R> readCache(S session, V var) {
        CacheResult<SoftReference<R>> result = delegate.readCache(session, var);
        if (!result.isValid()) {
            return CacheResult.getInvalid();
        }
        else if (result.getResult() == null) {
            return new CacheResult<R>(null, true);
        }
        else if (result.getResult().get() == null) {
            return CacheResult.getInvalid();
        }
        else {
            return new CacheResult<R>(result.getResult().get(), true);
        }
    }

    public void rollbackTransaction(S session) {
        delegate.rollbackTransaction(session);
    }

    public void startCompleteCache() {
        delegate.startCompleteCache();
    }

    public void updateCache(S session, V var) {
        delegate.updateCache(session, var);
    }

    public void updateCache(S session, V var, R value) {
        delegate.updateCache(session, var, encode(value));
    }


}
