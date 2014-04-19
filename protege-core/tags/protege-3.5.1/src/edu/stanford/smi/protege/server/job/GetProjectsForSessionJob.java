package edu.stanford.smi.protege.server.job;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.stanford.smi.protege.exception.ProtegeException;
import edu.stanford.smi.protege.server.RemoteServer;
import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.server.Server;
import edu.stanford.smi.protege.server.ServerProject;
import edu.stanford.smi.protege.server.metaproject.ProjectInstance;
import edu.stanford.smi.protege.util.ServerJob;

public class GetProjectsForSessionJob extends ServerJob {
	   
	private static final long serialVersionUID = 1218145961291562140L;
	
	private RemoteSession sessionWithPrjs;

	public GetProjectsForSessionJob(RemoteServer server, RemoteSession session, RemoteSession sessionWithPrjs) {
        super(server, session);        
        this.sessionWithPrjs = sessionWithPrjs;
    }

    @Override
    public Object run() throws ProtegeException {
    	List<String> prjNames = new ArrayList<String>();
        Server server = Server.getInstance();
        Collection<ServerProject> prjs = server.getCurrentProjects(sessionWithPrjs);
        if (prjs == null) { //happens if this is an administrative session
        	return prjNames;
        }
        for (ServerProject prj : prjs) {
			ProjectInstance metaProjectInstance = prj.getMetaProjectInstance();
			if (metaProjectInstance != null) { //null, when temporary project, e.g., chat 
				String name = metaProjectInstance.getName();
				prjNames.add(name);			
			}
		}
        Collections.sort(prjNames);        
        return prjNames;
    }

}
