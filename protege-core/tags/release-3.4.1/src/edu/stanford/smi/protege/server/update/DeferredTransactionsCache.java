package edu.stanford.smi.protege.server.update;

import java.util.List;

import edu.stanford.smi.protege.model.framestore.Sft;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.util.FifoReader;
import edu.stanford.smi.protege.util.transaction.cache.Cache;
import edu.stanford.smi.protege.util.transaction.cache.CacheResult;
import edu.stanford.smi.protege.util.transaction.cache.serialize.CacheCommitTransaction;
import edu.stanford.smi.protege.util.transaction.cache.serialize.CacheRollbackTransaction;
import edu.stanford.smi.protege.util.transaction.cache.serialize.SerializedCacheUpdate;

/**
 * This is a hacky cache designed to solve two problems seen by the remote client frame store.
 * First, it is sometimes necessary to start a cache when the invoker is already in a transaction.
 * This is bad.  It can lead to incorrect data in the untransacted cache and missing rollbacks.  However, 
 * since this cache will only be used by the invoker, I believe that all this badness is countered by 
 * flushing the cache when the invokers transaction completes.  I also believe that all transactions will
 * be closed when this flush is called making it a clean flush.  This is true because the server sends
 * the begin and end of transactions made by other clients (not the invoker) in a group.
 * 
 * Second, the begin/commit/rollback transaction operations apply to all the frame caches 
 * in the RemoteClientFrameStore.  It is clear that updating all of these caches on such an operation
 * is expensive.  So we defer telling the frame caches about transaction operations until that cache is 
 * needed.  The RemoteClientFrameStore uses a Fifo writer to track the transactions that have not 
 * been seen by its various caches.  When this 
 * cache is needed it can read through all the transaction operations that it has not seen and it will be
 * ready to perform the needed operation.
 * 
 * @author tredmond
 *
 */
public class DeferredTransactionsCache implements
		Cache<RemoteSession, Sft, List> {
	private FifoReader<SerializedCacheUpdate<RemoteSession, Sft, List>> transactionUpdates;
	private Cache<RemoteSession, Sft, List> delegate;
	
	private RemoteSession invoker;
	private boolean needsEndOfTransactionFlush = false;
	
	@SuppressWarnings("unchecked")
    public DeferredTransactionsCache(Cache<RemoteSession, Sft, List> delegate, 
	                                 RemoteSession invoker,
	                                 int invokersTransactionNesting,
			                         FifoReader<SerializedCacheUpdate<RemoteSession, Sft, List>> transactionUpdates) {
		this.delegate = delegate;
		this.transactionUpdates = transactionUpdates;
		this.invoker = invoker;
		if (invokersTransactionNesting > 0) {
		    needsEndOfTransactionFlush = true;
		    while (invokersTransactionNesting-- > 0) {
		        delegate.beginTransaction(invoker);
		    }
		}
	}
	
	public CacheResult<List> readCache(RemoteSession session, Sft var) {
		catchUp();
		return delegate.readCache(session, var);
	}
	
	public void updateCache(RemoteSession session, Sft var) {
		catchUp();
		delegate.updateCache(session, var);
	}
	
	public void updateCache(RemoteSession session, Sft var, List value) {
		catchUp();
		delegate.updateCache(session, var, value);
	}
	
	public void modifyCache(RemoteSession session, Sft var) {
		catchUp();
		delegate.modifyCache(session, var);
	}
	
	public void modifyCache(RemoteSession session, Sft var, List value) {
		catchUp();
		delegate.modifyCache(session, var, value);
	}
	
	public void delete(RemoteSession session) {
		catchUp();
		delegate.delete(session);
	}
	
    public boolean isDeleted() {
        catchUp();
        return delegate.isDeleted();
    }
	
	public void flush() {
		while (transactionUpdates.read() != null) {
			;
		}
		delegate.flush();
	}
	
	public void startCompleteCache() {
		catchUp();
		delegate.startCompleteCache();
	}
	
	public void finishCompleteCache() {
		catchUp();
		delegate.finishCompleteCache();
	}
	
	public void abortCompleteCache() {
		catchUp();
		delegate.abortCompleteCache();
	}
	
	public boolean isCacheComplete() {
		catchUp();
		return delegate.isCacheComplete();
	}
	
	public void beginTransaction(RemoteSession session) {
		throw new UnsupportedOperationException("transactions are deferred");
	}
	
	public void commitTransaction(RemoteSession session) {
		throw new UnsupportedOperationException("transactions are deferred");
	}
	
	public void rollbackTransaction(RemoteSession session) {
		throw new UnsupportedOperationException("transactions are deferred");
	}
	
	public int getTransactionNesting(RemoteSession session) {
		catchUp();
		return delegate.getTransactionNesting(session);
	}
	
	@SuppressWarnings("unchecked")
	private void catchUp() {
		SerializedCacheUpdate<RemoteSession, Sft, List> update;
		while ((update = transactionUpdates.read()) != null) {
			update.performUpdate(delegate);
			if (needsEndOfTransactionFlush &&
			        (update instanceof CacheCommitTransaction
			            || update instanceof CacheRollbackTransaction) &&
		            invoker.equals(update.getSession()) &&
		            getTransactionNesting(invoker) == 0) {
			    delegate.flush();
			    needsEndOfTransactionFlush = false;
			}
		}
	}

    public int getCacheId() {
        return delegate.getCacheId();
    }
}
