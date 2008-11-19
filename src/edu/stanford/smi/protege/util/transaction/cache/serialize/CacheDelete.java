package edu.stanford.smi.protege.util.transaction.cache.serialize;


public class CacheDelete<S, V, R> extends SerializedCacheUpdate<S, V, R> {
    
    public CacheDelete(S session) {
        super(session);
    }

}
