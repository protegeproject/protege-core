package edu.stanford.smi.protege.util.transaction.cache.serialize;

import edu.stanford.smi.protege.util.transaction.cache.CacheResult;

public class CacheRead<S, V, R> extends SerializedCacheUpdate<S, V, R> {
	private V var;
	private CacheResult<R> value;
	
	public CacheRead(S session, V var, CacheResult<R> value) {
	    super(session);
	    this.var = var;
	    this.value = value;
	}
	
	public V getVar() {
		return var;
	}
	public CacheResult<R> getValue() {
		return value;
	}
	
	
}
