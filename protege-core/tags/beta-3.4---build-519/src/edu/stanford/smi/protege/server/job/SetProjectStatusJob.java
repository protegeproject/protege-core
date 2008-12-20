package edu.stanford.smi.protege.server.job;

import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.server.ServerProject.ProjectStatus;
import edu.stanford.smi.protege.server.metaproject.MetaProjectConstants;
import edu.stanford.smi.protege.util.ProtegeJob;

public class SetProjectStatusJob extends ProtegeJob {
    private static final long serialVersionUID = -1351964537749437538L;
    
    private ProjectStatus status;
    
    public SetProjectStatusJob(KnowledgeBase kb, ProjectStatus status) {
        super(kb);
        this.status = status;
    }

    @Override
    public Object run() throws ProtegeException {
        if (!serverSideCheckOperationAllowed(MetaProjectConstants.OPERATION_CONFIGURE_SERVER)) {
            return null;
        }
        Server server = Server.getInstance();
        String projectName = getMetaProjectInstance().getName();
        server.setProjectStatus(projectName, status);
        return null;
    }

}
