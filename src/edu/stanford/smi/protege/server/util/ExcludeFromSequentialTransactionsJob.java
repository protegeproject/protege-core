package edu.stanford.smi.protege.server.util;

import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.framestore.SynchronizationFrameStore;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.util.ProtegeJob;

public class ExcludeFromSequentialTransactionsJob extends ProtegeJob {
	private static final long serialVersionUID = -9145545436217058954L;
	private boolean exclude;
	
	public static void setExcludeFromSequentialTransaction(KnowledgeBase kb, boolean exclude) {
		ExcludeFromSequentialTransactionsJob job = new ExcludeFromSequentialTransactionsJob(kb, exclude);
		job.execute();
	}
	
	private ExcludeFromSequentialTransactionsJob(KnowledgeBase kb, boolean exclude) {
		super(kb);
		this.exclude = exclude;
	}

	@Override
	public Object run() throws ProtegeException {
		SynchronizationFrameStore sfs = getKnowledgeBase().getFrameStoreManager().getFrameStoreFromClass(SynchronizationFrameStore.class);
		sfs.setSessionExcludedFromSequentialTransactions(ServerFrameStore.getCurrentSession(), exclude);
		return true;
	}

}
