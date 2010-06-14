package edu.stanford.smi.protege.event;

import java.util.EventListener;

/**
 * Listener interface for projects.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface ServerProjectListener extends EventListener {

    void projectNotificationReceived(ServerProjectNotificationEvent event);

    void projectStatusChanged(ServerProjectStatusChangeEvent event);
    
    void beforeProjectSessionClosed(ServerProjectSessionClosedEvent event);
}
