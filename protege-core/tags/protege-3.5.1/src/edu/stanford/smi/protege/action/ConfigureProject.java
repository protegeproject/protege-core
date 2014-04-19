package edu.stanford.smi.protege.action;

import java.awt.event.*;

import edu.stanford.smi.protege.resource.*;

/**
 * Bring up the configuration panel for the current project.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ConfigureProject extends ProjectAction {

    private static final long serialVersionUID = 1144385115917044225L;

    public ConfigureProject() {
        super(ResourceKey.PROJECT_CONFIGURE);
    }

    public void actionPerformed(ActionEvent event) {
        getProjectManager().configureProjectRequest();
    }
}
