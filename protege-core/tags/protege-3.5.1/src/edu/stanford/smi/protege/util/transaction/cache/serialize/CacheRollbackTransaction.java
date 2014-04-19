package edu.stanford.smi.protege.util.transaction.cache.serialize;

import edu.stanford.smi.protege.util.transaction.cache.Cache;

public class CacheRollbackTransaction<S, V, R> extends SerializedCacheUpdate<S, V, R> {
    
    private static final long serialVersionUID = -6969778991362847105L;

    public CacheRollbackTransaction(S session) {
        super(session);
    }
    
    @Override
    public void performUpdate(Cache<S, V, R> cache) {
        cache.rollbackTransaction(getSession());
    }
    
    public String toString() {
    	StringBuffer sb = new StringBuffer("<CacheOp: Rollback Transaction for ");
    	sb.append(getSession());
    	sb.append(">");
    	return sb.toString();
    }
}
