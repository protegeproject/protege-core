package edu.stanford.smi.protege.action;

import java.awt.event.*;

import edu.stanford.smi.protege.resource.*;

/**
 * Save the current project with a different name.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SaveProjectToFormat extends LocalProjectAction {

    public SaveProjectToFormat() {
        super(ResourceKey.PROJECT_SAVE_TO_FORMAT);
    }

    public void actionPerformed(ActionEvent event) {
        getProjectManager().saveToFormatRequest();
    }
}
