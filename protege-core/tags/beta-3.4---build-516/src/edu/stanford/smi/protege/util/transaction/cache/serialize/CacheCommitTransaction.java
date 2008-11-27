package edu.stanford.smi.protege.util.transaction.cache.serialize;

import edu.stanford.smi.protege.util.transaction.cache.Cache;

public class CacheCommitTransaction<S, V, R> extends SerializedCacheUpdate<S, V, R> {

    public CacheCommitTransaction(S session) {
        super(session);
    }
    
    @Override
    public void performUpdate(Cache<S, V, R> cache) {
        cache.commitTransaction(getSession());
    }
}
