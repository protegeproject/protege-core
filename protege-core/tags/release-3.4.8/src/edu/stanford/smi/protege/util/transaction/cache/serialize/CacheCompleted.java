package edu.stanford.smi.protege.util.transaction.cache.serialize;

import edu.stanford.smi.protege.util.transaction.cache.Cache;

public class CacheCompleted<S,V, R> extends SerializedCacheUpdate<S,V,R> {
    
    private static final long serialVersionUID = 4597109170674264151L;

    public CacheCompleted() {
        super(null);
    }

    @Override
    public void performUpdate(Cache<S, V, R> cache) {
        cache.finishCompleteCache();
    }
    
    public String toString() {
    	return "<CacheOp: Completed Cache>";
    }
}
