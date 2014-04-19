package edu.stanford.smi.protege.action;

import java.awt.event.*;

import javax.swing.*;

import edu.stanford.smi.protege.resource.*;
import edu.stanford.smi.protege.ui.*;

/**
 * 
 * @author Ray Fergerson <fergerson@smi.stanford.edu>
 */
public class AutosynchronizeTrees extends ProjectAction {

    private static final long serialVersionUID = -1371399799983255730L;

    public AutosynchronizeTrees() {
        super(ResourceKey.AUTOSYNCHRONIZE_CLASS_TREES);
    }

    public void actionPerformed(ActionEvent event) {
        ProjectView view = getProjectView();
        if (view != null) {
            JCheckBoxMenuItem item = (JCheckBoxMenuItem) event.getSource();
            view.setAutosynchronizeClsTrees(item.isSelected());
        }
    }

}
