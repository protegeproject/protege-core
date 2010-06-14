package edu.stanford.smi.protege.util.transaction.cache.impl;

import java.util.HashMap;
import java.util.Map;

import edu.stanford.smi.protege.util.transaction.cache.Cache;
import edu.stanford.smi.protege.util.transaction.cache.CacheResult;

/**
 * This is a basic cache that 
 *  - ignores the impact of transactions on the cache.  
 *  - ignores deletion functionality
 *  - ignore the cache completion mechanism (at least this one is correct...) 
 *  
 * It will only return 
 * valid results in the case that the transaction isolation level is NONE or where
 * the caller covers the in transaction case.
 * 
 * @author tredmond
 *
 */
public class BasicCache<S, V, R> implements Cache<S, V, R> {
    private Map<V, R> cache = new HashMap<V, R>();
    private Map<S, Integer> transactionNestingMap = new HashMap<S, Integer>();
    
    private static int idCounter = 0;
    private int id;
    
    public BasicCache() {
        synchronized (BasicCache.class) {
            id = (idCounter++);
        }
    }
    
    
    public CacheResult<R> readCache(S session, V var) {
        return new CacheResult<R>(cache.get(var), cache.containsKey(var));
    }

    public void updateCache(S session, V var) {
        ;
    }

    public void updateCache(S session, V var, R value) {
        cache.put(var, value);
    }

    public void modifyCache(S session, V var) {
        cache.remove(var);
    }

    public void modifyCache(S session, V var, R value) {
        cache.put(var, value);
    }
    
    public void invalidate(S session) {
        
    }
    
    public boolean isInvalid() {
        return false;
    }

    public void startCompleteCache() {
        ;
    }

    public void finishCompleteCache() {
        
    }
    
    public void abortCompleteCache() {
        ;
    }
    
    public boolean isCacheComplete() {
        return false;
    }

    public void beginTransaction(S session) {
        Integer nesting = transactionNestingMap.get(session);
        if (nesting == null) {
            transactionNestingMap.put(session, 1);
            return;
        }
        transactionNestingMap.put(session, nesting + 1);
    }

    public void commitTransaction(S session) {
        decrementTransaction(session);
    }
    
    public void rollbackTransaction(S session) {
        decrementTransaction(session);
    }
    
    private void decrementTransaction(S session) {
        Integer nesting = transactionNestingMap.get(session);
        if (nesting == null)  {
            transactionNestingMap.put(session, -1);
        }
        else if (nesting.intValue() == 1) {
            transactionNestingMap.remove(session);
        }
        else {
            transactionNestingMap.put(session, nesting - 1);
        }
    }
    
    public int getTransactionNesting(S session) {
        Integer nesting = transactionNestingMap.get(session);
        if (nesting == null) {
            return 0;
        }
        return nesting;
    }

    public void flush() {
        transactionNestingMap.clear();
        cache.clear();
    }
    
    public int getCacheId() {
        return id;
    }


}
