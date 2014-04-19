package edu.stanford.smi.protege.util.transaction.cache.serialize;

import edu.stanford.smi.protege.util.transaction.cache.Cache;
import edu.stanford.smi.protege.util.transaction.cache.CacheResult;

public class CacheRead<S, V, R> extends SerializedCacheUpdate<S, V, R> {
	private static final long serialVersionUID = -1535032893706670178L;
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
	
	@Override
	public void performUpdate(Cache<S, V, R> cache) {
	    if (value.isValid()) {
	        cache.updateCache(getSession(), var, value.getResult());
	    }
	    else {
	    	cache.updateCache(getSession(), var);
	    }
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("<CacheOp: Session ");
		sb.append(getSession());
		sb.append(" reads var ");
		sb.append(getVar());
		sb.append(" retrieving ");
		sb.append(getValue());
		sb.append(">");
		return sb.toString();
	}
	
}
