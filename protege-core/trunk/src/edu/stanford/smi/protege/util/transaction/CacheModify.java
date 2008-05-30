package edu.stanford.smi.protege.util.transaction;

public class CacheModify<S, V, R> extends CacheUpdate<S, V, R> {
	private V var;
	
	private CacheResult<R> newValue;
	
	
	public V getVar() {
		return var;
	}
	public CacheResult<R> getNewValue() {
		return newValue;
	}
	
	
}
