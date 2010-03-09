package edu.stanford.smi.protege.event;

import edu.stanford.smi.protege.util.AbstractEvent;

public class ServerProjectNotificationEvent extends AbstractEvent implements ServerProjectEvent {
    private static final long serialVersionUID = 7117669971447806365L;
    
    private String projectName;
    private String message;
    
    public ServerProjectNotificationEvent(String projectName, String message) {
        super(projectName, ServerEventTypes.PROJECT_NOTIFICATION_EVENT.getTypeAsInt(), message);
        this.projectName = projectName;
        this.message = message;
    }
    
    public String getProjectName() {
        return projectName;
    }
    
    public String getMessage() {
        return message;
    }
}
