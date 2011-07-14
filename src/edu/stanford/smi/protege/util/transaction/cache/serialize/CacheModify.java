package edu.stanford.smi.protege.util.transaction.cache.serialize;

import edu.stanford.smi.protege.util.transaction.cache.Cache;
import edu.stanford.smi.protege.util.transaction.cache.CacheResult;

public class CacheModify<S, V, R> extends SerializedCacheUpdate<S, V, R> {
	private static final long serialVersionUID = -4073876220493559307L;

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
	
	@Override
	public void performUpdate(Cache<S, V, R> cache) {
	    if (newValue.isValid()) {
	        cache.modifyCache(getSession(), var, newValue.getResult());
	    }
	    else {
	        cache.modifyCache(getSession(), var);
	    }
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("<CacheOp: Session ");
		sb.append(getSession());
		sb.append(" modifies var ");
		sb.append(getVar());
		sb.append(" to ");
		sb.append(getNewValue());
		sb.append(">");
		return sb.toString();
	}
}
