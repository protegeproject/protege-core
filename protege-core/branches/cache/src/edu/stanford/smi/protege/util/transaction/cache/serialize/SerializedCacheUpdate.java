package edu.stanford.smi.protege.util.transaction.cache.serialize;

import java.io.Serializable;

public class SerializedCacheUpdate<S, V, R> implements Serializable {
    private static final long serialVersionUID = 6402530119072810497L;
    private S session;
	
	public SerializedCacheUpdate(S session) {
	    this.session = session;
	}

	public S getSession() {
		return session;
	}
	
}
