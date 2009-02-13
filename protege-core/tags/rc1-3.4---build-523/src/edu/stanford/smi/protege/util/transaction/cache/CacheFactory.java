package edu.stanford.smi.protege.util.transaction.cache;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.transaction.TransactionIsolationLevel;
import edu.stanford.smi.protege.util.transaction.TransactionMonitor;
import edu.stanford.smi.protege.util.transaction.cache.impl.BasicCache;
import edu.stanford.smi.protege.util.transaction.cache.impl.CompleteableCache;
import edu.stanford.smi.protege.util.transaction.cache.impl.InvalidatableCache;
import edu.stanford.smi.protege.util.transaction.cache.impl.ReadCommittedCache;
import edu.stanford.smi.protege.util.transaction.cache.impl.ReadUncommittedCache;
import edu.stanford.smi.protege.util.transaction.cache.impl.RepeatableReadCache;

public class CacheFactory {
    
    public static <S, V, R> Cache<S, V, R> createEmptyCache(KnowledgeBase kb) {
        TransactionMonitor tsm = kb.getFrameStoreManager().getHeadFrameStore().getTransactionStatusMonitor();
        TransactionIsolationLevel level;
        if (tsm == null) {
            level = TransactionIsolationLevel.READ_UNCOMMITTED;
        }
        else {
            level = tsm.getTransationIsolationLevel();
        }
        return createEmptyCache(level);
    }
    
    public static <S, V, R> Cache<S, V, R> createEmptyCache(TransactionIsolationLevel level) {
        Cache<S, V, R> untransactedCache = new CompleteableCache<S, V, R>(new BasicCache<S, V, R>());
        if (level == TransactionIsolationLevel.NONE) {
            return new InvalidatableCache<S, V, R>(untransactedCache, true);
        }
        if (level.compareTo(TransactionIsolationLevel.READ_UNCOMMITTED) <= 0) {
            return new InvalidatableCache<S, V, R>(new ReadUncommittedCache<S, V, R>(untransactedCache), true);
        }
        Cache<S, V, R> readCommittedCache = new ReadCommittedCache<S, V, R>(untransactedCache);
        if (level == TransactionIsolationLevel.READ_COMMITTED) {
            return new InvalidatableCache<S, V, R>(readCommittedCache, false);
        }
        else {
            RepeatableReadCache<S, V, R> repeatableReadCache = new RepeatableReadCache<S, V, R>(readCommittedCache);
            return new InvalidatableCache<S, V, R>(repeatableReadCache, false);
        }
    }

}
