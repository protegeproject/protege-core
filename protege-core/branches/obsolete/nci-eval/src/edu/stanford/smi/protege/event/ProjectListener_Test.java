package edu.stanford.smi.protege.event;

import edu.stanford.smi.protege.test.*;

/**
 * Unit tests for ProjectListener inteface.
 *
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ProjectListener_Test extends APITestCase {

    public void testProjectCloseEvent() {
        clearEvents();
        ProjectListener listener = new ProjectAdapter() {
            public void projectClosed(ProjectEvent event) {
                recordEventFired(event);
            }
        };
        getProject().addProjectListener(listener);
        saveAndReload();
        assertEventFired(ProjectEvent.PROJECT_CLOSED);
    }

    public void testProjectSaveEvent() {
        clearEvents();
        ProjectListener listener = new ProjectAdapter() {
            public void projectSaved(ProjectEvent event) {
                recordEventFired(event);
            }
        };
        getProject().addProjectListener(listener);
        saveAndReload();
        assertEventFired(ProjectEvent.PROJECT_SAVED);
    }
}
