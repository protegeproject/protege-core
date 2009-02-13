package edu.stanford.smi.protege.event;

/**
 * Adapter for listeners to project events.  Subclass this class if you only want to catch some project events.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public abstract class ProjectAdapter implements ProjectListener {

    public void formChanged(ProjectEvent event) {
    }

    public void runtimeClsWidgetCreated(ProjectEvent event) {
    }

    public void projectClosed(ProjectEvent event) {
    }

    public void projectSaved(ProjectEvent event) {
    }
    
    public void serverSessionLost(ProjectEvent event) {    	
    }
}
