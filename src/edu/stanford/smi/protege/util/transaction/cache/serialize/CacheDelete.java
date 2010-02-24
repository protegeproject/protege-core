package edu.stanford.smi.protege.util.transaction.cache.serialize;

import edu.stanford.smi.protege.util.transaction.cache.Cache;


public class CacheDelete<S, V, R> extends SerializedCacheUpdate<S, V, R> {
    
    public CacheDelete(S session) {
        super(session);
    }

    @Override
    public void performUpdate(Cache<S, V, R> cache) {
        cache.invalidate(getSession());
    }
    
    public String toString() {
    	StringBuffer sb = new StringBuffer("<CacheOp: ");
    	sb.append(getSession());
    	sb.append(" invalidates cache>");
    	return sb.toString();
    }
}
