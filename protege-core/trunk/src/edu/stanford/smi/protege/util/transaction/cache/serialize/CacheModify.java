package edu.stanford.smi.protege.util.transaction.cache.serialize;

import edu.stanford.smi.protege.util.transaction.cache.CacheResult;

public class CacheModify<S, V, R> extends SerializedCacheUpdate<S, V, R> {
	private V var;
	
	private CacheResult<R> newValue;
	
	public CacheModify(S session, V var, CacheResult<R> newValue) {
	    super(session);
        this.var = var;
        this.newValue = newValue;
    }
    public V getVar() {
		return var;
	}
	public CacheResult<R> getNewValue() {
		return newValue;
	}
	
	
}
