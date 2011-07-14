package edu.stanford.smi.protege.action;

import java.awt.event.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class CloseCurrentView extends ProjectAction {

    private static final long serialVersionUID = 48257985564189923L;

    public CloseCurrentView() {
        super(ResourceKey.CLOSE_VIEW);
    }

    public void actionPerformed(ActionEvent e) {
        ProjectView view = getProjectView();
        if (view != null) {
            view.closeCurrentView();
        }
    }

}
