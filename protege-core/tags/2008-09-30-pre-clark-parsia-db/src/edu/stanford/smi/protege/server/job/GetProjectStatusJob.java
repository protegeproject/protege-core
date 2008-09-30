package edu.stanford.smi.protege.server.job;

import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.util.ProtegeJob;

public class GetProjectStatusJob extends ProtegeJob {
	private static final long serialVersionUID = -3858327318078369912L;

	private String projectName;

    public GetProjectStatusJob(KnowledgeBase kb, String projectName) {
        super(kb);
        this.projectName = projectName;
    }

    @Override
    public Object run() throws ProtegeException {
        Server server = Server.getInstance();
        return server.getProjectStatus(projectName);
    }

}
