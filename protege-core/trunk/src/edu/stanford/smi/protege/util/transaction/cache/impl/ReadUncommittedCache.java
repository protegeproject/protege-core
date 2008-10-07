package edu.stanford.smi.protege.util.transaction.cache.impl;

import edu.stanford.smi.protege.util.transaction.cache.Cache;
import edu.stanford.smi.protege.util.transaction.cache.CacheResult;

public class ReadUncommittedCache<S, V, R> implements Cache<S, V, R> {

    public void abortCompleteCache() {
        // TODO Auto-generated method stub

    }

    public void beginTransaction(S session) {
        // TODO Auto-generated method stub

    }

    public void commitTransaction(S session) {
        // TODO Auto-generated method stub

    }

    public void delete(S session) {
        // TODO Auto-generated method stub

    }

    public void finishCompleteCache() {
        // TODO Auto-generated method stub

    }

    public int getTransactionNesting(S session) {
        // TODO Auto-generated method stub
        return 0;
    }

    public void modifyCache(S session, V var) {
        // TODO Auto-generated method stub

    }

    public void modifyCache(S session, V var, R value) {
        // TODO Auto-generated method stub

    }

    public CacheResult<R> readCache(S session, V var) {
        // TODO Auto-generated method stub
        return null;
    }

    public void rollbackTransaction(S session) {
        // TODO Auto-generated method stub

    }

    public void startCompleteCache() {
        // TODO Auto-generated method stub

    }

    public void updateCache(S session, V var) {
        // TODO Auto-generated method stub

    }

    public void updateCache(S session, V var, R value) {
        // TODO Auto-generated method stub

    }

}
