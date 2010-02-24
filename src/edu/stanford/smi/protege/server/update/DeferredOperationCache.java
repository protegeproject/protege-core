package edu.stanford.smi.protege.server.update;

import java.util.List;

import edu.stanford.smi.protege.model.framestore.Sft;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.util.FifoReader;
import edu.stanford.smi.protege.util.transaction.cache.Cache;
import edu.stanford.smi.protege.util.transaction.cache.CacheResult;
import edu.stanford.smi.protege.util.transaction.cache.serialize.SerializedCacheUpdate;

/**
 * Certain operations, most noteably begin/commit/rollback transaction apply to all the cache's in the RemoteClientFrameStore.
 * It is clear that updating all of these caches on such an operation
 * is expensive.  So we defer telling the frame caches about transaction operations until that cache is 
 * needed.  The RemoteClientFrameStore uses a Fifo writer to track the transactions that have not 
 * been seen by its various caches.  When this 
 * cache is needed it can read through all the transaction operations that it has not seen and it will be
 * ready to perform the needed operation.
 * <p/>
 * Also, it is sometimes necessary to start a cache when the invoker is already in a transaction.
 * This is bad.  It can lead to incorrect data in the untransacted cache and missing rollbacks.  This situation
 * should be taken care of by the InvalidatableCache which commits suicide when it noties a transaction 
 * with nesting less than 0.
 * 
 * @author tredmond
 *
 */
public class DeferredOperationCache implements Cache<RemoteSession, Sft, List> {
	private FifoReader<SerializedCacheUpdate<RemoteSession, Sft, List>> transactionUpdates;
	private Cache<RemoteSession, Sft, List> delegate;
	
	@SuppressWarnings("unchecked")
    public DeferredOperationCache(Cache<RemoteSession, Sft, List> delegate, 
	                                 FifoReader<SerializedCacheUpdate<RemoteSession, Sft, List>> transactionUpdates) {
		this.delegate = delegate;
		this.transactionUpdates = transactionUpdates;
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
	
	public void invalidate(RemoteSession session) {
		catchUp();
		delegate.invalidate(session);
	}
	
    public boolean isInvalid() {
        catchUp();
        return delegate.isInvalid();
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
		}
	}

    public int getCacheId() {
        return delegate.getCacheId();
    }
}
