package edu.stanford.smi.protege.util.transaction.cache.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.stanford.smi.protege.util.transaction.cache.Cache;
import edu.stanford.smi.protege.util.transaction.cache.CacheResult;

public class RepeatableReadCache<S, V, R> implements Cache<S, V, R> {
    private Map<S, Set<V>> readOrWrittenVarsMap = new  HashMap<S, Set<V>>();
    private Map<S, Map<V, CacheResult<R>>> transactedReads = new HashMap<S, Map<V, CacheResult<R>>>();
    private Cache<S, V, R> delegate;
    
    public RepeatableReadCache(Cache<S, V, R> delegate) {
        this.delegate = delegate;
    }

    public CacheResult<R> readCache(S session, V var) {
        Set<V> readOrWrittenVars = readOrWrittenVarsMap.get(session);
        Map<V, CacheResult<R>> reads = transactedReads.get(session);
        int transactionNesting = delegate.getTransactionNesting(session);
        
        if (transactionNesting > 0 && !readOrWrittenVars.contains(var)) {
            return new CacheResult<R>(null, false);
        }
        else if (transactionNesting > 0 && reads.containsKey(var)) {
            return reads.get(var);
        }
        else {
            return delegate.readCache(session, var);
        }
    }

    public void updateCache(S session, V var) {
        if (getTransactionNesting(session) == 0) {
            delegate.updateCache(session, var);
        }
    }

    public void updateCache(S session, V var, R value) {
        int transactionNesting = delegate.getTransactionNesting(session);
        if (transactionNesting > 0) {
            readOrWrittenVarsMap.get(session).add(var);
            transactedReads.get(session).put(var, new CacheResult<R>(value, true));
        }
        else {
            delegate.updateCache(session, var, value);
        }
    }

    public void modifyCache(S session, V var) {
        if (getTransactionNesting(session) > 0) {
            readOrWrittenVarsMap.get(session).add(var);
        }
        delegate.modifyCache(session, var);
    }

    public void modifyCache(S session, V var, R value) {
        if (getTransactionNesting(session) > 0) {
            readOrWrittenVarsMap.get(session).add(var);
        }
        delegate.modifyCache(session, var, value);
    }

    public void delete(S session) {
        delegate.delete(session);
    }
    
    public boolean isDeleted() {
        return delegate.isDeleted();
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
            readOrWrittenVarsMap.put(session, new HashSet<V>());
            transactedReads.put(session, new HashMap<V, CacheResult<R>>());
        }
        delegate.beginTransaction(session);
    }

    public void commitTransaction(S session) {
        if (getTransactionNesting(session) <= 0) {
            flush();
            return;
        }
        delegate.commitTransaction(session);
        decrementTransaction(session);
    }

    public void rollbackTransaction(S session) {
        if (getTransactionNesting(session) <= 0) {
            flush();
            return;
        }
        delegate.rollbackTransaction(session);
        decrementTransaction(session);
    }
    
    private void decrementTransaction(S session) {
        if (getTransactionNesting(session) == 0) {
            readOrWrittenVarsMap.remove(session);
            transactedReads.remove(session);
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
        readOrWrittenVarsMap.clear();
        transactedReads.clear();
    }
}
