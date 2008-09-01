package edu.stanford.smi.protege.util.transaction;

import static edu.stanford.smi.protege.util.transaction.TransactionIsolationLevel.READ_COMMITTED;
import static edu.stanford.smi.protege.util.transaction.TransactionIsolationLevel.READ_UNCOMMITTED;
import static edu.stanford.smi.protege.util.transaction.TransactionIsolationLevel.REPEATABLE_READ;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import edu.stanford.smi.protege.util.ArrayListMultiMap;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.MultiMap;


public abstract class AbstractCache<S, V, R> implements Cache<S,V,R> {
    private static transient final Logger log = Log.getLogger(AbstractCache.class);
    
	public enum CacheStatus  {
		NORMAL, COMPLETE_CACHE_OPERATION_IN_PROGRESS, COMPLETE_CACHE;
	};
	
	private boolean warnedAboutSynchronizationProblem = false;
	
	private CacheStatus cacheStatus = CacheStatus.NORMAL;
	
	private CacheResult<R> invalidResult = new CacheResult<R>(null, false);
	
	/**
	 * This map defines the value of a variable for any session that is not in a transaction.
	 */
	private Map<V, CacheResult<R>> cache = new HashMap<V, CacheResult<R>>();
	
	/**
     * This variable is unused when the transaction isolation level is at or
     * below READ_UNCOMMITTED. Otherwise when a session, s, enters a
     * transaction, the <code>transactedCachedMap.get(s)</code> value will be
     * initialized to a map containing the cached values seen during the
     * transaction. If the transaction isolation level is READ_COMMMITTED then
     * this map contains only those values modified during the transaction. If
     * the transaction isolation level If the transaction isolation level is
     * READ_COMMMITTED, then even if the <code>trasactedCacheMap.get(s)</code>
     * does not have an entry for a variable, v, we can still fall back to the
     * cache outside of transactions. At REPEATABLE_READ and above we need to
     * keep going back to the data-source. So at REPEATABLE_READ, this cache
     * contains the values read during the transaction as well as values written
     * during the transaction. We cannot use the untransacted cache even when we
     * know that the value given there is the value that will be returned by the
     * data source. The data source needs to be aware of all read operations.
     */
	private Map<S, Map<V, CacheResult<R>>> transactedCacheMap = new HashMap<S, Map<V, CacheResult<R>>>();
	
	/**
	 * This  multimap tracks  the operations that occur in a transaction. If the transaction isolation
	 * level is READ_UNCOMMITTED or below then these operations are needed during a final rollback operation.
	 * If  the transaction isolation level is READ_COMMITTED or higher then these  operations are 
	 * needed during the commit.
	 */
	private MultiMap<S, CacheUpdate<S, V, R>> transactedUpdates = new ArrayListMultiMap<S, CacheUpdate<S, V,R>>();

	/**
	 * This variable keeps track of how nested a transaction is.
	 */
	private Map<S, Integer> transactionDepth = new HashMap<S, Integer>();
	
	protected abstract TransactionIsolationLevel getTransactionIsolationLevel();
	
	private boolean transactionIsolationLevelAtOrBelow(TransactionIsolationLevel level) {
	    return getTransactionIsolationLevel().compareTo(level) <= 0;
	}
	
	private boolean transactionIsolationLevelAtOrAbove(TransactionIsolationLevel level) {
	    return getTransactionIsolationLevel().compareTo(level) >= 0;
	}
	
	protected void assertCacheValuesConsistent(CacheResult<R> existingCachedResult,
	                                           CacheResult<R> recentlyReadResult) {
	    if (existingCachedResult == null || recentlyReadResult == null) {
	        return;
	    }
	    if (!existingCachedResult.isValid() || !recentlyReadResult.isValid()) {
	        return;
	    }
	    if (existingCachedResult.equals(recentlyReadResult)) {
	        return;
	    }
	    if (!warnedAboutSynchronizationProblem) {
	        warnedAboutSynchronizationProblem = true;
	        log.warning("Cache out of sync");
	        log.warning("\tExisting cached value = " + existingCachedResult.getResult());
	        log.warning("\tNewly read value = " + recentlyReadResult.getResult());
	    }
	}

	/*
	 * This method is applied to 
	 */
	private CacheResult<R> getCacheOutsideTransaction(V var) {
	    CacheResult<R> result = cache.get(var);
	    if (result != null) {
	        return result;
	    }
	    return new CacheResult<R>(null, cacheStatus == CacheStatus.COMPLETE_CACHE);
	}
	
	private boolean inTransaction(S session) {
	    Integer nesting = transactionDepth.get(session);
	    if (nesting == null) {
	        return false;
	    }
	    else  {
	        return nesting != 0;
	    }
	}
	
	public void apply(CacheUpdate<S, V, R> update) {
		S session = update.getSession();
		if (update instanceof CacheRead) {
			CacheRead<S, V, R> read = (CacheRead<S, V, R>) update;
			if (read.getValue().isValid()) {
				updateCache(session, read.getVar(), read.getValue().getResult());
			}
			else {
				updateCache(session, read.getVar());
			}
		}
		else if (update instanceof CacheModify) {
			CacheModify<S, V, R> modification = (CacheModify<S, V, R>) update;
			if (modification.getNewValue().isValid()) {
				modifyCache(session, modification.getVar(), modification.getNewValue().getResult());
			}
			else {
				modifyCache(session, modification.getVar());
			}
		}
		else if (update instanceof  CacheDelete) {
			CacheDelete<S, V, R> delete = (CacheDelete<S,V,R>) update;
			delete(delete.getSession());
		}
	}
	
	/* *******************************************************************
	 * Implementation of Cache interfaces.
	 */
	
    public CacheResult<R> readCache(S session, V var) {
        if (!inTransaction(session)) {
            return getCacheOutsideTransaction(var);
        }
        if (transactionIsolationLevelAtOrBelow(READ_UNCOMMITTED)) {
            return getCacheOutsideTransaction(var);
        }
        else {
            CacheResult<R> result = transactedCacheMap.get(session).get(var);
            if (result != null) {
                return result;
            }
            /*
             * if the transaction isolation level is repeatable read or above then the 
             * caller must not use a cache even if a useful entry exists.  The underlying
             * storage mechanism needs to know that a read was executed so that it will be
             * repeated for other queries.
             */
            if (transactionIsolationLevelAtOrAbove(REPEATABLE_READ)) {
                return invalidResult;
            }
            return getCacheOutsideTransaction(var);
        }
    }
    
    public void updateCache(S session, V var) {
        /*
         * if there is no value cached  outside of transactions and we are building a 
         * complete  cache then mark it as possibly having a value. 
         */
        if (cacheStatus == CacheStatus.COMPLETE_CACHE_OPERATION_IN_PROGRESS) {
            if (cache.get(var) == null) {
                cache.put(var, invalidResult);
            }
        }
        /*
         * If we are in a transaction nothing needs to be done.  If the session has 
         * a cache entry then it is still valid.  If not then when the session can fall
         * back to the untransacted cache.
         */
    }
    
    public void updateCache(S session, V var, R value) {
        CacheResult<R> result = new CacheResult<R>(value, true);
        if (!inTransaction(session) || 
                transactionIsolationLevelAtOrBelow(READ_UNCOMMITTED)) {
            assertCacheValuesConsistent(cache.get(var), result);
            cache.put(var, result);
            return;
        }
        if (transactedCacheMap.get(session).get(var) == null) {
            assertCacheValuesConsistent(cache.get(var), result);
            cache.put(var, result);
            /*
             * At repeatable read or above it is useful to know that a read operation
             * with a known value has already occurred.
             */
            if (transactionIsolationLevelAtOrAbove(REPEATABLE_READ)) {
                transactedCacheMap.get(session).put(var, result);
            }
            return;
        }
        /*
         * if there is no value cached  outside of transactions and we are building a 
         * complete  cache then mark it as possibly having a value.
         */
        if (cacheStatus == CacheStatus.COMPLETE_CACHE_OPERATION_IN_PROGRESS) {
            if (cache.get(var) == null) {
                cache.put(var, invalidResult);
            }
        }
        /*
         * if things are going correctly then the following should  be unnecessary (the value
         * being placed in the map should  be the same as the value there).  But bugs are always
         * possible so this will at least make one correction if the cache is somehow out of sync.
         */
        assertCacheValuesConsistent(transactedCacheMap.get(session).get(var), result);
        transactedCacheMap.get(session).put(var, result);
    }
    
    public void modifyCache(S session, V var) {
        if (!inTransaction(session) 
                || transactionIsolationLevelAtOrBelow(READ_UNCOMMITTED)) {
            if (cacheStatus != CacheStatus.NORMAL) {
                cache.put(var, invalidResult);
            }
            else {
                cache.remove(var);
            }
        }
        if (!inTransaction(session)) {
            return;
        }
        transactedUpdates.addValue(session, new CacheModify<S, V, R>(var, invalidResult));
        if (transactionIsolationLevelAtOrAbove(READ_COMMITTED)) {
            transactedCacheMap.get(session).put(var, invalidResult);
        }
    }
    
    public void modifyCache(S session, V var, R value) {
        CacheResult<R> result = new  CacheResult<R>(value, true);
        if (!inTransaction(session) 
                || transactionIsolationLevelAtOrBelow(READ_UNCOMMITTED)) {
            cache.put(var, result);
        }
        if (!inTransaction(session)) {
            return;
        }
        transactedUpdates.addValue(session, new CacheModify<S, V, R>(var, result));
        if (transactionIsolationLevelAtOrAbove(READ_COMMITTED)) {
            transactedCacheMap.get(session).put(var, result);
        }
    }
    
    // remember to ignore transactions for READ_UNCOMITTED and below
    // beginTransaction and endTransaction are the ones.

}
