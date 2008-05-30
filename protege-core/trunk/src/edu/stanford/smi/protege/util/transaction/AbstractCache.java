package edu.stanford.smi.protege.util.transaction;

import java.util.HashMap;
import java.util.Map;

import edu.stanford.smi.protege.util.ArrayListMultiMap;
import edu.stanford.smi.protege.util.MultiMap;


public abstract class AbstractCache<S, V, R> implements Cache<S,V,R> {
	public enum CacheStatus  {
		NORMAL, COMPLETE_CACHE_OPERATION_IN_PROGRESS, COMPLETE_CACHE, DELETED;
	};
	
	private CacheStatus cacheStatus = CacheStatus.NORMAL;
	private Map<V, R> cache = new HashMap<V, R>();
	
	private Map<S, CacheStatus> transactedCacheStatus = new HashMap<S, CacheStatus>();
	private Map<S, Map<V, R>> transactedCache = new HashMap<S, Map<V, R>>();
	private MultiMap<S, CacheUpdate<S, V, R>> transactedUpdates = new ArrayListMultiMap<S, CacheUpdate<S, V,R>>();

	protected abstract boolean updatesHiddenByTransaction(S session);
	
	protected abstract boolean transactionCanReadFromUntransacted();
	
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

}
