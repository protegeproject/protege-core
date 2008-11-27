package edu.stanford.smi.protege.util.transaction.cache.serialize;

import edu.stanford.smi.protege.util.transaction.cache.Cache;

public class CacheRollbackTransaction<S, V, R> extends SerializedCacheUpdate<S, V, R> {
    
    public CacheRollbackTransaction(S session) {
        super(session);
    }
    
    @Override
    public void performUpdate(Cache<S, V, R> cache) {
        cache.rollbackTransaction(getSession());
    }
}
