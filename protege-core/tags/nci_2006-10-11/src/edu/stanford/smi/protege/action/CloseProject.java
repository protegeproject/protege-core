package edu.stanford.smi.protege.action;

import java.awt.event.*;

import edu.stanford.smi.protege.resource.*;

/**
 * Request a close of the project.  This will prompt the user to save if the project is "dirty".
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class CloseProject extends ProjectAction {

    public CloseProject() {
        super(ResourceKey.PROJECT_CLOSE);
    }

    public void actionPerformed(ActionEvent event) {
        getProjectManager().closeProjectRequest();
    }
}
