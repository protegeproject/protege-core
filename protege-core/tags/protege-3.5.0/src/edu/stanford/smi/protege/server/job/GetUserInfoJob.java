package edu.stanford.smi.protege.server.job;

import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.server.RemoteServer;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.server.ServerProject;
import edu.stanford.smi.protege.server.framestore.ServerFrameStore;
import edu.stanford.smi.protege.util.ServerJob;

public class GetUserInfoJob extends ServerJob {
	   
	private static final long serialVersionUID = 1218145961291562140L;

	private String projectName;
	
	public GetUserInfoJob(RemoteServer server, RemoteSession session, String projectName) {
        super(server, session);
        this.projectName = projectName;
    }

    @Override
    public Object run() throws ProtegeException {
        Server server = Server.getInstance();
        synchronized (server) {
            ServerProject serverProject = server.getServerProject(projectName);
            if (serverProject == null) {
            	return null;
            }
            ServerFrameStore serverFs = (ServerFrameStore) serverProject.getDomainKbFrameStore(getSession());
            return serverFs.getUserInfo();
		}
    }

}
