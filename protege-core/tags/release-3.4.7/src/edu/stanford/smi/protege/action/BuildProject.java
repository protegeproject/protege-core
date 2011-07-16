package edu.stanford.smi.protege.action;

import java.awt.event.*;

import edu.stanford.smi.protege.resource.*;

/**
 * Action to build a project from component pieces (files)
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class BuildProject extends ProjectAction {

    private static final long serialVersionUID = 2846633391888909898L;

    public BuildProject() {
        super(ResourceKey.PROJECT_BUILD);
        setEnabled(true);
    }

    public void actionPerformed(ActionEvent event) {
        getProjectManager().buildProjectRequest();
    }
}
