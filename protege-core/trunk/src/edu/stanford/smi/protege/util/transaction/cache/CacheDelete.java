package edu.stanford.smi.protege.util.transaction.cache;

public class CacheDelete<S, V, R> extends CacheUpdate<S, V, R> {
    
    public CacheDelete(S session) {
        super(session);
    }

}
