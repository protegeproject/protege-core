package edu.stanford.smi.protege.action;

import java.awt.event.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class DetachCurrentView extends ProjectAction {

    private static final long serialVersionUID = -7883949818350799286L;

    public DetachCurrentView() {
        super(ResourceKey.DETACH_VIEW);
    }

    public void actionPerformed(ActionEvent e) {
        ProjectView view = getProjectView();
        if (view != null) {
            view.detachCurrentView();
        }
    }

}
