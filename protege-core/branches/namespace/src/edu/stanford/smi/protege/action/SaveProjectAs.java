package edu.stanford.smi.protege.action;

import java.awt.event.*;

import edu.stanford.smi.protege.resource.*;

/**
 * Save the current project with a different name.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SaveProjectAs extends LocalProjectAction {

    public SaveProjectAs() {
        super(ResourceKey.PROJECT_SAVE_AS);
    }

    public void actionPerformed(ActionEvent event) {
        getProjectManager().saveProjectAsRequest();
    }
}
