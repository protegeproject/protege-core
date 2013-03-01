package edu.stanford.smi.protege.action;

import java.awt.event.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.util.*;

/**
 * Close all of the top-level windows except the main window
 *
 * @author    Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class CloseAllWindows extends ProjectAction {

    private static final long serialVersionUID = -8224924500659516000L;

    public CloseAllWindows(boolean large) {
        super(ResourceKey.CLOSE_ALL_WINDOWS, large);
    }

    public void actionPerformed(ActionEvent event) {
        ComponentUtilities.closeAllWindows();
    }
}
