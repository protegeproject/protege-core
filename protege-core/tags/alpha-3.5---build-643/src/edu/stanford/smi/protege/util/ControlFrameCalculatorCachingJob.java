package edu.stanford.smi.protege.util;

import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.server.ServerProject;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;

public class ControlFrameCalculatorCachingJob extends ProtegeJob {
    private static final long serialVersionUID = 670423590593538409L;
    private boolean enable;
    
    public ControlFrameCalculatorCachingJob(KnowledgeBase kb, boolean enable) {
        super(kb);
        this.enable = enable;
    }
    

    @Override
    public Boolean run() throws ProtegeException {
        Project p = getKnowledgeBase().getProject();
        if (!p.isMultiUserServer()) {
            return false;
        }
        Server server = Server.getInstance();
        RemoteSession session = ServerFrameStore.getCurrentSession();
        ServerProject serverProject = server.getServerProject(getKnowledgeBase().getProject());
        ServerFrameStore serverFrameStore = (ServerFrameStore) serverProject.getDomainKbFrameStore(session);
        return !serverFrameStore.getFrameCalculator().setDisabled(!enable, session);
    }

}
