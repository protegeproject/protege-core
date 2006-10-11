package edu.stanford.smi.protege.action;

import java.awt.event.*;

import edu.stanford.smi.protege.resource.*;

/**
 * Prompt the user to open a new project. Closes the existing open project, if there is one.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class OpenProject extends ProjectAction {

    public OpenProject(boolean large) {
        super(ResourceKey.PROJECT_OPEN, large);
        setEnabled(true);
    }

    public void actionPerformed(ActionEvent event) {
        getProjectManager().openProjectRequest();
    }
}