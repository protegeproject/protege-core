package edu.stanford.smi.protege.util.transaction.cache;

public class CacheRead<S, V, R> extends CacheUpdate<S, V, R> {
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
