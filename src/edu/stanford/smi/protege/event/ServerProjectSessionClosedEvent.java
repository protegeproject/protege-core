package edu.stanford.smi.protege.event;

import edu.stanford.smi.protege.server.RemoteSession;
import edu.stanford.smi.protege.util.AbstractEvent;

public class ServerProjectSessionClosedEvent extends AbstractEvent implements
		ServerProjectEvent {
	private static final long serialVersionUID = 2903887402234980133L;
	
	private String projectName;
	private RemoteSession sessionToKill;
	
	public ServerProjectSessionClosedEvent(String projectName, RemoteSession sessionToKill) {
		super(projectName, ServerEventTypes.PROJECT_NOTIFICATION_EVENT.getTypeAsInt());
		this.projectName = projectName;
		this.sessionToKill = sessionToKill;		
	}
	
	public String getProjectName() {
		return projectName;
	}	
	
	public RemoteSession getSessionToKill() {
		return sessionToKill;
	}
}
