package edu.stanford.smi.protege.util.transaction;

public class CacheRead<S, V, R> extends CacheUpdate<S, V, R> {
	private V var;
	private CacheResult<R> value;
	
	
	public V getVar() {
		return var;
	}
	public CacheResult<R> getValue() {
		return value;
	}
	
	
}
