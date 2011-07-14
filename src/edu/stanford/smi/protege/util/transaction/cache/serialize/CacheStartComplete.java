package edu.stanford.smi.protege.util.transaction.cache.serialize;

import edu.stanford.smi.protege.util.transaction.cache.Cache;

public class CacheStartComplete<S, V, R> extends SerializedCacheUpdate<S, V, R> {
    
    private static final long serialVersionUID = -4163523460425406009L;

    public CacheStartComplete() {
        super(null);
    }
    
    @Override
    public void performUpdate(Cache<S, V, R> cache) {
        cache.startCompleteCache();
    }

    public String toString() {
    	return "<Cache OP: Start Cache Complete>";
    }
}
