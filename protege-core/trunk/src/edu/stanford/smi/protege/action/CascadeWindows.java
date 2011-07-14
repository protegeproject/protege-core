package edu.stanford.smi.protege.action;

import java.awt.event.*;

import edu.stanford.smi.protege.resource.*;

/**
 * Cascade all of the top-level windows
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class CascadeWindows extends ProjectAction {

    private static final long serialVersionUID = 6854368222246681508L;

    public CascadeWindows(boolean large) {
        super(ResourceKey.CASCADE_WINDOWS, large);
    }

    public void actionPerformed(ActionEvent event) {
        getProjectManager().cascadeWindowsRequest();
    }
}
