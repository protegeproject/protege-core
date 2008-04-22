package edu.stanford.smi.protege.action;

import java.awt.event.*;

import edu.stanford.smi.protege.resource.*;

/**
 * Change the backend used to store a project
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ChangeProjectStorageFormat extends LocalProjectAction {

    public ChangeProjectStorageFormat() {
        super(ResourceKey.PROJECT_CONVERT);
    }

    public void actionPerformed(ActionEvent event) {
        getProjectManager().changeProjectStorageFormatRequest();
    }
}
