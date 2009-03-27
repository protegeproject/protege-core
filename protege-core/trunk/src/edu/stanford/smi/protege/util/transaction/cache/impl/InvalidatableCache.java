package edu.stanford.smi.protege.util.transaction.cache.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.transaction.cache.Cache;
import edu.stanford.smi.protege.util.transaction.cache.CacheResult;

/**
 * This cache can take a delegate that either does not understand transactions (e.g. below
 * READ_COMMITTED) or a delegate that does.  This cache is responsible for invalidating the cache if 
 * on deletion or when the transaction nesting goes negative.  Once the cache is invalid, it 
 * cannot recover and should be tossed out by the caller who holds this cache.
 *
 * @author tredmond
 *
 * @param <S>
 * @param <V>
 * @param <R>
 */
public class InvalidatableCache<S, V, R> implements Cache<S, V, R> {
    private Logger logger = Log.getLogger(InvalidatableCache.class);
    
    private boolean ignoreTransactions;
    private Set<S> sessionsWithCacheDeleted = new HashSet<S>();
    private boolean invalid = false;
    private Cache<S, V, R> delegate;
    private int id;
    
    public InvalidatableCache(Cache<S, V, R> delegate, boolean ignoreTransactions) {
        this.delegate = delegate;
        this.ignoreTransactions = ignoreTransactions;
        id = delegate.getCacheId();
    }
    
    private boolean isInvalid(S session) {
        return invalid || sessionsWithCacheDeleted.contains(session);
    }
    
    public CacheResult<R> readCache(S session, V var) {
        if (isInvalid(session)) {
            return CacheResult.getInvalid();
        }
        return delegate.readCache(session, var);
    }
    
    public void updateCache(S session, V var) {
        if (isInvalid(session)) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Cache " + getCacheId() + " is invalid for session " + session);
            }
            return;
        }
        delegate.updateCache(session, var);
    }

    public void updateCache(S session, V var, R value) {
        if (isInvalid(session)) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Read ignored, Cache " + getCacheId() + " is invalid for session " + session);
            }
            return;
        }
        delegate.updateCache(session, var, value);
    }

    public void modifyCache(S session, V var) {
        if (isInvalid(session)) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Read ignored, Cache " + getCacheId() + " is invalid for session " + session);
            }
            return;
        }
        delegate.modifyCache(session, var);
    }

    public void modifyCache(S session, V var, R value) {
        if (isInvalid(session)) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Write ignored, Cache " + getCacheId() + " is invalid for session " + session);
            }
            return;
        }
        delegate.modifyCache(session, var, value);
    }
    
    public void delete(S session) {
        if (invalid) {
            return;
        }
        else if (!ignoreTransactions && delegate.getTransactionNesting(session) > 0) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Cache " + getCacheId() + " invalidated for " + session);
            }
            sessionsWithCacheDeleted.add(session);
        }
        else {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Cache " + getCacheId() + " invalidated");
            }
            invalid = true;
            delegate.flush();
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
            invalid = true;
            flush();
            return;
        }
        delegate.commitTransaction(session);
        if (!ignoreTransactions 
                && delegate.getTransactionNesting(session) == 0 
                && sessionsWithCacheDeleted.contains(session)) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Commmited invalidation of cache " + getCacheId());
            }
            invalid = true;
            delegate.flush();
            delegate = null;
            sessionsWithCacheDeleted.clear();
        }
    }

    public void rollbackTransaction(S session) {
        if (invalid) {
            return;
        }
        if (getTransactionNesting(session) < 0) {
            invalid = true;
            flush();
            return;
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
        if (!sessionsWithCacheDeleted.isEmpty()) {
            sessionsWithCacheDeleted.clear();
            invalid = true;
        }
        delegate.flush();
    }
    
    public int getCacheId() {
        return id;
    }
    
}
