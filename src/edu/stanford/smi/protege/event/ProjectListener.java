package edu.stanford.smi.protege.event;

import java.util.EventListener;

/**
 * Listener interface for projects.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public interface ProjectListener extends EventListener {

    void formChanged(ProjectEvent event);
    void runtimeClsWidgetCreated(ProjectEvent event);

    void projectClosed(ProjectEvent event);

    void projectSaved(ProjectEvent event);
    
    void serverSessionLost(ProjectEvent event);
}
