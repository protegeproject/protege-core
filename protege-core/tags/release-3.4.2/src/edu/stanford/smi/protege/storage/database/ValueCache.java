package edu.stanford.smi.protege.storage.database;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.smi.protege.model.framestore.Sft;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.transaction.TransactionIsolationLevel;
import edu.stanford.smi.protege.util.transaction.cache.Cache;
import edu.stanford.smi.protege.util.transaction.cache.CacheFactory;
import edu.stanford.smi.protege.util.transaction.cache.CacheResult;
import edu.stanford.smi.protege.util.transaction.cache.serialize.SerializedCacheUpdate;

public class ValueCache implements Cache<RemoteSession, Sft, List> {
    private final Logger log = Log.getLogger(getClass());
	
	private  Cache<RemoteSession, Sft, List> delegate;
	private int point = 0;
	private List<SerializedCacheUpdate<RemoteSession, Sft, List>> transactions;
	
	@SuppressWarnings("unchecked")
    public ValueCache(TransactionIsolationLevel level, 
	                  List<SerializedCacheUpdate<RemoteSession, Sft, List>> transactions) { 
		delegate = CacheFactory.createEmptyCache(level);
		this.transactions = transactions;
	}
	
	@SuppressWarnings("unchecked")
    private void catchUp() {
	    boolean needsLog = log.isLoggable(Level.FINE) && point < transactions.size();
	    if (needsLog) {
	        log.fine("Belatedly catching up with transactions for cache " + getCacheId());
	    }
		while (point < transactions.size()) {
		    SerializedCacheUpdate<RemoteSession, Sft, List> update = transactions.get(point++);
			update.performUpdate(delegate);
			if (log.isLoggable(Level.FINE)) {
			    log.fine("Update " + update + " received by cache " + getCacheId());
			}
		}
		if (needsLog){
		    log.fine("Cache " + getCacheId() + " caught up with transactions...");
		}
	}
	
	private void logEntry(String method, Sft sft) {
	    if (log.isLoggable(Level.FINE)) {
	        log.fine("Cache " + getCacheId() + " entering method " + method);
	        if (sft != null) {
	            log.fine("Cache " + getCacheId() + " Sft  = " + sft.getSlot().getFrameID().getName() + ", " 
	                        + (sft.getFacet() != null ? sft.getFacet().getFrameID().getName() : ""));
	        }
	    }
	}
	

	public void abortCompleteCache() {
	    catchUp();
	    logEntry("abort complete cache",  null);
	    delegate.abortCompleteCache();
	}

	public void beginTransaction(RemoteSession session) {
        catchUp();
        logEntry("begin transaction",  null);
        delegate.beginTransaction(session);
	}

	public void commitTransaction(RemoteSession session) {
        catchUp();
        logEntry("commitTransaction", null);
        delegate.commitTransaction(session);
	}

	public void delete(RemoteSession session) {
        catchUp();
        logEntry("delete", null);
        delegate.delete(session);
	}

	public void finishCompleteCache() {
        catchUp();
        logEntry("finish complete cache", null);
        delegate.finishCompleteCache();
	}

	public void flush() {
        catchUp();
        logEntry("flush", null);
        delegate.flush();
	}

	public int getCacheId() {
        return delegate.getCacheId();
	}

	public int getTransactionNesting(RemoteSession session) {
        catchUp();
        return delegate.getTransactionNesting(session);
	}

	public boolean isCacheComplete() {
        catchUp();
        return delegate.isCacheComplete();
	}

	public boolean isDeleted() {
        catchUp();
        return delegate.isDeleted();
	}

	public void modifyCache(RemoteSession session, Sft var) {
        catchUp();
        logEntry("modify cache with unknown", var);
        delegate.modifyCache(session, var);
	}

	@SuppressWarnings("unchecked")
    public void modifyCache(RemoteSession session, Sft var, List value) {
        catchUp();
        logEntry("modify cache with known", var);
        delegate.modifyCache(session, var,  value);
	}

	@SuppressWarnings("unchecked")
    public CacheResult<List> readCache(RemoteSession session, Sft var) {
        catchUp();
        logEntry("read cache", var);
        return delegate.readCache(session, var);
	}

	public void rollbackTransaction(RemoteSession session) {
        catchUp();
        logEntry("rollback transaction", null);
        delegate.rollbackTransaction(session);
	}

	public void startCompleteCache() {
        catchUp();
        logEntry("start complete cache", null);
        delegate.startCompleteCache();
	}

	public void updateCache(RemoteSession session, Sft var) {
        catchUp();
        logEntry("update cache with unknown", var);
        delegate.updateCache(session, var);
	}

	@SuppressWarnings("unchecked")
    public void updateCache(RemoteSession session, Sft var, List value) {
        catchUp();
        logEntry("update cache with known", var);
        delegate.updateCache(session, var, value);
	}
	
}
