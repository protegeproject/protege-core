package edu.stanford.smi.protege.server.job;

import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.server.RemoteServer;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.util.ServerJob;

public class GetProjectsStatusMapJob extends ServerJob {
	private static final long serialVersionUID = -3858327318078369912L;

    public GetProjectsStatusMapJob(RemoteServer server, RemoteSession session) {
        super(server, session);        
    }

    @Override
    public Object run() throws ProtegeException {
        Server server = Server.getInstance();
        return server.getProjectsStatusMap();
    }

}
