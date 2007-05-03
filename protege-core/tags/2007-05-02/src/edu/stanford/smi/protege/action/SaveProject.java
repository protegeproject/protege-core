package edu.stanford.smi.protege.action;

import java.awt.event.*;

import edu.stanford.smi.protege.resource.*;

/**
 * Save the current project into the existing project name. Prompts for a new name if necessary.
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SaveProject extends LocalProjectAction {

    public SaveProject(boolean large) {
        super(ResourceKey.PROJECT_SAVE, large);
    }

    public void actionPerformed(ActionEvent event) {
        getProjectManager().saveProjectRequest();
    }
}