package edu.stanford.smi.protege.server.util;

import java.util.List;

import edu.stanford.smi.protege.model.framestore.Sft;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.util.transaction.cache.Cache;
import edu.stanford.smi.protege.util.transaction.cache.CacheResult;
import edu.stanford.smi.protege.util.transaction.cache.serialize.SerializedCacheUpdate;

public class DeferredTransactionsCache implements
		Cache<RemoteSession, Sft, List> {
	private FifoReader<SerializedCacheUpdate<RemoteSession, Sft, List>> transactionUpdates;
	private int location = 0;
	private Cache<RemoteSession, Sft, List> delegate;
	
	public DeferredTransactionsCache(Cache<RemoteSession, Sft, List> delegate, 
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
	
	public void delete(RemoteSession session) {
		catchUp();
		delegate.delete(session);
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

}
