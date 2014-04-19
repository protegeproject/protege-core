package edu.stanford.smi.protege.util.transaction.cache.serialize;

import edu.stanford.smi.protege.util.transaction.cache.Cache;

public class CacheAbortComplete<S,V,R> extends SerializedCacheUpdate<S,V,R> {
    private static final long serialVersionUID = -3081897187225022558L;

    public CacheAbortComplete() {
        super(null);
    }
    
    @Override
    public void performUpdate(Cache<S, V, R> cache) {
        cache.abortCompleteCache();
    }
    
    public String toString() {
    	return "<CacheOp: Abort Complete Cache>";
    }
}
