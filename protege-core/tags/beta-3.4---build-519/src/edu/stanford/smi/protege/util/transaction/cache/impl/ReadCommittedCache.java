package edu.stanford.smi.protege.util.transaction.cache.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.smi.protege.util.transaction.cache.Cache;
import edu.stanford.smi.protege.util.transaction.cache.CacheResult;
import edu.stanford.smi.protege.util.transaction.cache.serialize.CacheModify;

public class ReadCommittedCache<S, V, R> implements Cache<S, V, R> {
    private Map<S, Map<V, CacheResult<R>>> transactedWriteCache = new HashMap<S, Map<V, CacheResult<R>>>();
    private Map<S, List<CacheModify<S, V, R>>> transactedModifications = new HashMap<S, List<CacheModify<S, V, R>>>();
    private Cache<S, V, R> delegate;
    
    public ReadCommittedCache(Cache<S, V, R> delegate) {
        this.delegate = delegate;
    }

    public CacheResult<R> readCache(S session, V var) {
        Map<V, CacheResult<R>> valuesWrittenInTransaction = transactedWriteCache.get(session);
        if (valuesWrittenInTransaction != null && valuesWrittenInTransaction.containsKey(var)) {
            return valuesWrittenInTransaction.get(var);
        }
        return delegate.readCache(session, var);
    }

    public void updateCache(S session, V var) {
        Map<V, CacheResult<R>> valuesWrittenInTransaction = transactedWriteCache.get(session);
        if (valuesWrittenInTransaction != null && valuesWrittenInTransaction.containsKey(var)) {
            return;
        }
        delegate.updateCache(session, var);
    }

    public void updateCache(S session, V var, R value) {
        Map<V, CacheResult<R>> valuesWrittenInTransaction = transactedWriteCache.get(session);
        if (valuesWrittenInTransaction != null && valuesWrittenInTransaction.containsKey(var)) {
            return;
        }
        delegate.updateCache(session, var, value);
    }

    public void modifyCache(S session, V var) {
        if (delegate.getTransactionNesting(session) == 0) {
            delegate.modifyCache(session, var);
            return;
        }
        addUpdateToTransaction(session, var, new CacheResult<R>(null, false));
    }

    public void modifyCache(S session, V var, R value) {
        if (delegate.getTransactionNesting(session) == 0) {
            delegate.modifyCache(session, var, value);
            return;
        }
        addUpdateToTransaction(session, var, new CacheResult<R>(value, true));
    }
    
    private void addUpdateToTransaction(S session, V var, CacheResult<R> result) {
        transactedWriteCache.get(session).put(var, result);
        transactedModifications.get(session).add(new CacheModify<S, V, R>(session, var, result));
    }

    public void delete(S session) {
        delegate.delete(session);
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
            transactedWriteCache.put(session, new HashMap<V, CacheResult<R>>());
            transactedModifications.put(session, new ArrayList<CacheModify<S,V,R>>());
        }
        delegate.beginTransaction(session);
    }

    public void commitTransaction(S session) {
        if (getTransactionNesting(session) < 0) {
            flush(); // draconian
        }
        else {
            delegate.commitTransaction(session);
            if (getTransactionNesting(session) == 0) {
                transactedWriteCache.remove(session);
                for (CacheModify<S, V, R> modification : transactedModifications.remove(session)) {
                    if (modification.getNewValue().isValid()) {
                        delegate.modifyCache(session, modification.getVar(), modification.getNewValue().getResult());
                    }
                    else {
                        delegate.modifyCache(session, modification.getVar());
                    }
                }
            }
        }
    }

    public void rollbackTransaction(S session) {
        if (getTransactionNesting(session) < 0) {
            flush(); // draconian
        }
        else {
            delegate.rollbackTransaction(session);
            if (getTransactionNesting(session) == 0) {
                transactedWriteCache.remove(session);
                transactedModifications.remove(session);
            }
        }
    }

    public int getTransactionNesting(S session) {
        return delegate.getTransactionNesting(session);
    }
    
    public void flush() {
        transactedModifications.clear();
        transactedWriteCache.clear();
        delegate.flush();
    }

}
