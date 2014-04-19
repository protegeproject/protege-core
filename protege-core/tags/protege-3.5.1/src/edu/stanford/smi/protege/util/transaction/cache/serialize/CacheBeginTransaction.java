package edu.stanford.smi.protege.util.transaction.cache.serialize;

import edu.stanford.smi.protege.util.transaction.cache.Cache;

public class CacheBeginTransaction<S, V, R> extends SerializedCacheUpdate<S, V, R> {

    private static final long serialVersionUID = -7393471152354884717L;

    public CacheBeginTransaction(S session) {
        super(session);
    }
    
    @Override
    public void performUpdate(Cache<S, V, R> cache) {
        cache.beginTransaction(getSession());
    }
    
    public String toString() {
    	StringBuffer sb = new StringBuffer("<CacheOp: Begin Transaction for ");
    	sb.append(getSession());
    	sb.append(">");
    	return sb.toString();
    }
}
