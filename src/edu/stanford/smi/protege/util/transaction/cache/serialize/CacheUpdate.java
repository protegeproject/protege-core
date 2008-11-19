package edu.stanford.smi.protege.util.transaction.cache.serialize;

public class CacheUpdate<S, V, R> {
	private S session;
	
	public CacheUpdate(S session) {
	    this.session = session;
	}

	public S getSession() {
		return session;
	}
	
}
