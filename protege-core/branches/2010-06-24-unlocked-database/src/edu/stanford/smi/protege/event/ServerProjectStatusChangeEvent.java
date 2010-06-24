package edu.stanford.smi.protege.event;

import edu.stanford.smi.protege.server.ServerProject.ProjectStatus;
import edu.stanford.smi.protege.util.AbstractEvent;

public class ServerProjectStatusChangeEvent extends AbstractEvent implements ServerProjectEvent {
    private static final long serialVersionUID = 7117669971447806365L;

    private String projectName;
    private ProjectStatus oldStatus;
    private ProjectStatus newStatus;
    
    public ServerProjectStatusChangeEvent(String projectName, ProjectStatus oldStatus, ProjectStatus newStatus) {
        super(projectName, ServerEventTypes.PROJECT_STATUS_CHANGE_EVENT.getTypeAsInt(), oldStatus, newStatus);
        this.projectName = projectName;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }
    
    public String getProjectName() {
        return projectName;
    }
    
    public ProjectStatus getOldStatus() {
        return oldStatus;
    }

    public ProjectStatus getNewStatus() {
        return newStatus;
    }
    


}
