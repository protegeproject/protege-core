package edu.stanford.smi.protege.server.job;

import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.server.ServerProject;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.server.framestore.background.FrameCalculator;
import edu.stanford.smi.protege.util.ProtegeJob;

public class CacheControlJob extends ProtegeJob {
    private static final long serialVersionUID = 8617813426801936579L;
    private final boolean disableFrameCalculator;
    private final boolean disableDatabaseCache;

    public CacheControlJob(KnowledgeBase kb, boolean disableFrameCalculator, boolean disableDatabaseCache) {
        super(kb);
        this.disableFrameCalculator = disableFrameCalculator;
        this.disableDatabaseCache = disableDatabaseCache;
    }

    public static void setCacheStatus(KnowledgeBase kb, boolean disableFrameCalculator, boolean disableDatabaseCache) {
        new CacheControlJob(kb, disableFrameCalculator, disableDatabaseCache).execute();
    }

    @Override
    public Object run() throws ProtegeException {
        RemoteSession session = ServerFrameStore.getCurrentSession();
        getKnowledgeBase().getFrameStoreManager().setCaching(session, !disableDatabaseCache);
        if (getKnowledgeBase().getProject().isMultiUserServer()) {
            ServerProject serverProject = Server.getInstance().getServerProject(getKnowledgeBase().getProject());
            FrameCalculator fc = ((ServerFrameStore) serverProject.getDomainKbFrameStore(session)).getFrameCalculator();
            fc.setDisabled(disableFrameCalculator, session);
        }
        return true;
    }

}
