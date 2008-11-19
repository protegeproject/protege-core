package edu.stanford.smi.protege.util.transaction.cache.serialize;

public class CacheCommitTransaction<S, V, R> extends CacheUpdate<S, V, R> {

    public CacheCommitTransaction(S session) {
        super(session);
    }
}
