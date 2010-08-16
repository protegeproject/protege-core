package edu.stanford.smi.protege.server.util;

import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.framestore.SynchronizationFrameStore;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.util.ProtegeJob;

public class ExcludeFromSequentialTransactionsJob extends ProtegeJob {
	private static final long serialVersionUID = 414166886992083111L;
	
	private boolean exclude;
	
	public static void setExcluded(KnowledgeBase kb, boolean exclude) {
		new ExcludeFromSequentialTransactionsJob(kb, exclude).execute();
	}
	
	private ExcludeFromSequentialTransactionsJob(KnowledgeBase kb, boolean exclude) {
		super(kb);
		this.exclude = exclude;
	}

	@Override
	public Object run() throws ProtegeException {
		SynchronizationFrameStore sfs = getKnowledgeBase().getFrameStoreManager().getFrameStoreFromClass(SynchronizationFrameStore.class);
		sfs.setExcludedFromSequentialTransactions(ServerFrameStore.getCurrentSession(), exclude);
		return true;
	}

}
