package edu.stanford.smi.protege.util.transaction.cache.serialize;

public class CacheAbortComplete<S,V,R> extends SerializedCacheUpdate<S,V,R> {
    public CacheAbortComplete() {
        super(null);
    }
}
