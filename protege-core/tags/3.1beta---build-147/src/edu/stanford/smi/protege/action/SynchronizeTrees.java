package edu.stanford.smi.protege.action;

import java.awt.event.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class SynchronizeTrees extends ProjectAction {

    public SynchronizeTrees() {
        super(ResourceKey.SYNCHRONIZE_CLASS_TREE);
    }
    public void actionPerformed(ActionEvent e) {
        ProjectView view = getProjectView();
        if (view != null) {
            view.synchronizeClsTree();
        }
    }

}
