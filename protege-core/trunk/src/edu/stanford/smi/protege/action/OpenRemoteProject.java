package edu.stanford.smi.protege.action;

import java.awt.event.*;

import edu.stanford.smi.protege.resource.*;

/**
 *  Description of the class
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class OpenRemoteProject extends ProjectAction {

    private static final long serialVersionUID = -6134251339616787461L;

    public OpenRemoteProject() {
        super(ResourceKey.PROJECT_OPEN_REMOTE);
        setEnabled(true);
    }

    public void actionPerformed(ActionEvent event) {
        getProjectManager().openRemoteProjectRequest();
    }
}
