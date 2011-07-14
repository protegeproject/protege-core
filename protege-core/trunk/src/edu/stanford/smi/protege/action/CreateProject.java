package edu.stanford.smi.protege.action;

import java.awt.event.*;

import edu.stanford.smi.protege.resource.*;

/**
 *  Create a new project.  This will result in the current project being closed.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class CreateProject extends ProjectAction {

    private static final long serialVersionUID = 6574861732826168229L;

    public CreateProject(boolean large) {
        super(ResourceKey.PROJECT_NEW, large);
        setEnabled(true);
    }

    public void actionPerformed(ActionEvent event) {
        getProjectManager().createNewProjectRequest();
    }
}
