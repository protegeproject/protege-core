package edu.stanford.smi.protege.util.transaction.cache.serialize;

public class CacheBeginTransaction<S, V, R> extends SerializedCacheUpdate<S, V, R> {

    public CacheBeginTransaction(S session) {
        super(session);
    }
}
