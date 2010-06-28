package edu.stanford.smi.protege.server.job;

import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.util.ProtegeJob;

public class GetServerProjectName extends ProtegeJob {
    private static final long serialVersionUID = 6865959553790815517L;

    public GetServerProjectName(KnowledgeBase kb) {
        super(kb);
    }

    @Override
    public Object run() throws ProtegeException {
        return getMetaProjectInstance().getName();
    }

}
