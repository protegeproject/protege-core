package edu.stanford.smi.protege.util.transaction.cache.serialize;

import edu.stanford.smi.protege.util.transaction.cache.Cache;

public class CacheCommitTransaction<S, V, R> extends SerializedCacheUpdate<S, V, R> {

    private static final long serialVersionUID = 4338588783148800848L;

    public CacheCommitTransaction(S session) {
        super(session);
    }
    
    @Override
    public void performUpdate(Cache<S, V, R> cache) {
        cache.commitTransaction(getSession());
    }
    
    public String toString() {
    	StringBuffer sb = new StringBuffer("<CacheOp: Commit Transaction for ");
    	sb.append(getSession());
    	sb.append(">");
    	return sb.toString();
    }
}
