package edu.stanford.smi.protege.util.transaction.cache.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.stanford.smi.protege.util.transaction.cache.Cache;
import edu.stanford.smi.protege.util.transaction.cache.CacheResult;

public class RepeatableReadCache<S, V, R> implements Cache<S, V, R> {
    private Map<S, Set<V>> readOrWrittenVarsMap = new  HashMap<S, Set<V>>();
    private Cache<S, V, R> delegate;
    
    public RepeatableReadCache(Cache<S, V, R> delegate) {
        this.delegate = delegate;
    }

    public CacheResult<R> readCache(S session, V var) {
        Set<V> readOrWrittenVars = readOrWrittenVarsMap.get(session);       
        if (readOrWrittenVars != null && !readOrWrittenVars.contains(var)) {
            return new CacheResult<R>(null, false);
        }
        else {
            return delegate.readCache(session, var);
        }
    }

    public void updateCache(S session, V var) {
        markReadOrWritten(session, var);
        delegate.updateCache(session, var);
    }

    public void updateCache(S session, V var, R value) {
        markReadOrWritten(session, var);
        delegate.updateCache(session, var, value);
    }

    public void modifyCache(S session, V var) {
        markReadOrWritten(session, var);
        delegate.modifyCache(session, var);
    }

    public void modifyCache(S session, V var, R value) {
        markReadOrWritten(session, var);
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
        readOrWrittenVarsMap.clear();
        delegate.flush();
    }
    
    private void markReadOrWritten(S session, V var) {
        Set<V> readOrWrittenVars = readOrWrittenVarsMap.get(session);
        if (readOrWrittenVars != null) {
            readOrWrittenVars.add(var);
        }
    }
    
    private void decrementTransaction(S session) {
        if (getTransactionNesting(session) == 0) {
            readOrWrittenVarsMap.remove(session);
        }
    }
}
