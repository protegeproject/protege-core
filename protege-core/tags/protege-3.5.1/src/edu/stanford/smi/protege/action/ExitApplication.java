package edu.stanford.smi.protege.action;

import java.awt.event.*;

import edu.stanford.smi.protege.resource.*;

/**
 * Request that the application exit.  This may result in a dialog popping up to ask if you want to save.
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class ExitApplication extends ProjectAction {

    private static final long serialVersionUID = -6593695038344336228L;

    public ExitApplication() {
        super(ResourceKey.APPLICATION_EXIT);
        substituteIntoName(Text.getProgramName());
        setEnabled(true);
    }

    public void actionPerformed(ActionEvent event) {
        getProjectManager().exitApplicationRequest();
    }
}
