package edu.stanford.smi.protege.util.transaction.cache.serialize;

import java.io.Serializable;

import edu.stanford.smi.protege.util.transaction.cache.Cache;

public abstract class SerializedCacheUpdate<S, V, R> implements Serializable {
    private static final long serialVersionUID = 6402530119072810497L;
    private S session;
	
	public SerializedCacheUpdate(S session) {
	    this.session = session;
	}

	public S getSession() {
		return session;
	}
	
	public abstract void performUpdate(Cache<S,V,R> cache);

	
}
