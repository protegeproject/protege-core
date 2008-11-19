package edu.stanford.smi.protege.util.transaction.cache.serialize;

public class CacheRollbackTransaction<S, V, R> extends CacheUpdate<S, V, R> {
    
    public CacheRollbackTransaction(S session) {
        super(session);
    }
}
