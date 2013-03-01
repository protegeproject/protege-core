package edu.stanford.smi.protege.util.transaction.cache.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.transaction.cache.Cache;
import edu.stanford.smi.protege.util.transaction.cache.CacheResult;
import edu.stanford.smi.protege.util.transaction.cache.serialize.CacheModify;

public class ReadCommittedCache<S, V, R> implements Cache<S, V, R> {
    public static final Logger LOGGER = Log.getLogger(ReadCommittedCache.class);
    private Map<S, Map<V, CacheResult<R>>> transactedWriteCache = new HashMap<S, Map<V, CacheResult<R>>>();
    private Map<S, List<CacheModify<S, V, R>>> transactedModifications = new HashMap<S, List<CacheModify<S, V, R>>>();
    private Cache<S, V, R> delegate;
    
    public ReadCommittedCache(Cache<S, V, R> delegate) {
        this.delegate = delegate;
    }

    public CacheResult<R> readCache(S session, V var) {
        Map<V, CacheResult<R>> valuesWrittenInTransaction = transactedWriteCache.get(session);
        if (valuesWrittenInTransaction != null && valuesWrittenInTransaction.containsKey(var)) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("Cache " + getCacheId() + " reading from change made in transaction for session " + session);
            }
            return valuesWrittenInTransaction.get(var);
        }
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("Cache " + getCacheId() + " reading from cache seen by everyone  for session " + session);
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
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Modifying cache " + getCacheId() + " (read-committed) with unknown value");
        }       
        if (delegate.getTransactionNesting(session) == 0) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Cache " + getCacheId() + " not in transaction for session " + session);
            }
            delegate.modifyCache(session, var);
            return;
        }
        addUpdateToTransaction(session, var, new CacheResult<R>(null, false));
    }

    public void modifyCache(S session, V var, R value) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Modifying cache " + getCacheId() + " (read-committed) with known value");
        }
        if (delegate.getTransactionNesting(session) == 0) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Cache " + getCacheId() + " not in transaction for session " + session);
            }
            delegate.modifyCache(session, var, value);
            return;
        }
        addUpdateToTransaction(session, var, new CacheResult<R>(value, true));
    }
    
    private void addUpdateToTransaction(S session, V var, CacheResult<R> result) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Cache " + getCacheId() + " is in transaction for session " + session);
        }
        transactedWriteCache.get(session).put(var, result);
        transactedModifications.get(session).add(new CacheModify<S, V, R>(session, var, result));
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
            transactedWriteCache.put(session, new HashMap<V, CacheResult<R>>());
            transactedModifications.put(session, new ArrayList<CacheModify<S,V,R>>());
        }
        delegate.beginTransaction(session);
    }

    public void commitTransaction(S session) {
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

    public void rollbackTransaction(S session) {
        delegate.rollbackTransaction(session);
        if (getTransactionNesting(session) == 0) {
            transactedWriteCache.remove(session);
            transactedModifications.remove(session);
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
    
    public int getCacheId() {
        return delegate.getCacheId();
    }

}
