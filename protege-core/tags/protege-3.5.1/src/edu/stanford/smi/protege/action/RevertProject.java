package edu.stanford.smi.protege.action;

import java.awt.event.*;

import edu.stanford.smi.protege.resource.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class RevertProject extends LocalProjectAction {

    private static final long serialVersionUID = -6653736460672035227L;

    public RevertProject(boolean large) {
        super(ResourceKey.PROJECT_REVERT, large);
    }

    public void actionPerformed(ActionEvent e) {
        getProjectManager().requestRevertProject();
    }
}
